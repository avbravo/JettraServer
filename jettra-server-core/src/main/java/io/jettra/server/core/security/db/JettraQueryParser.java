package io.jettra.server.core.security.db;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Parser de consultas SQL/JQL nativo para filtrar entidades en JettraSecurityDB.
 */
public class JettraQueryParser {

    public static <T> Predicate<T> parse(String queryStr, Class<T> clazz) {
        if (queryStr == null || queryStr.trim().isEmpty()) {
            return x -> true;
        }

        List<String> tokens = tokenize(queryStr);
        if (tokens.isEmpty()) {
            return x -> true;
        }

        Parser<T> parser = new Parser<>(tokens, clazz);
        return parser.parseExpression();
    }

    private static List<String> tokenize(String queryStr) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;

        for (int i = 0; i < queryStr.length(); i++) {
            char c = queryStr.charAt(i);
            if (inQuotes) {
                sb.append(c);
                if (c == quoteChar) {
                    inQuotes = false;
                    tokens.add(sb.toString());
                    sb.setLength(0);
                }
            } else {
                if (c == '\'' || c == '"') {
                    if (sb.length() > 0) {
                        tokens.add(sb.toString());
                        sb.setLength(0);
                    }
                    inQuotes = true;
                    quoteChar = c;
                    sb.append(c);
                } else if (c == '(' || c == ')') {
                    if (sb.length() > 0) {
                        tokens.add(sb.toString());
                        sb.setLength(0);
                    }
                    tokens.add(String.valueOf(c));
                } else if (Character.isWhitespace(c)) {
                    if (sb.length() > 0) {
                        tokens.add(sb.toString());
                        sb.setLength(0);
                    }
                } else if (c == '=' || c == '!' || c == '<' || c == '>') {
                    if (sb.length() > 0) {
                        tokens.add(sb.toString());
                        sb.setLength(0);
                    }
                    if (i + 1 < queryStr.length()) {
                        char next = queryStr.charAt(i + 1);
                        if ((c == '!' || c == '<' || c == '>') && next == '=') {
                            tokens.add("" + c + next);
                            i++;
                            continue;
                        }
                    }
                    tokens.add(String.valueOf(c));
                } else {
                    sb.append(c);
                }
            }
        }
        if (sb.length() > 0) {
            tokens.add(sb.toString());
        }
        return tokens;
    }

    private static class Parser<T> {
        private final List<String> tokens;
        private final Class<T> clazz;
        private int pos = 0;

        public Parser(List<String> tokens, Class<T> clazz) {
            this.tokens = tokens;
            this.clazz = clazz;
        }

        public Predicate<T> parseExpression() {
            Predicate<T> left = parseTerm();
            while (pos < tokens.size()) {
                String token = tokens.get(pos);
                if (token.equalsIgnoreCase("OR")) {
                    pos++;
                    Predicate<T> right = parseTerm();
                    Predicate<T> prev = left;
                    left = x -> prev.test(x) || right.test(x);
                } else {
                    break;
                }
            }
            return left;
        }

        private Predicate<T> parseTerm() {
            Predicate<T> left = parseFactor();
            while (pos < tokens.size()) {
                String token = tokens.get(pos);
                if (token.equalsIgnoreCase("AND")) {
                    pos++;
                    Predicate<T> right = parseFactor();
                    Predicate<T> prev = left;
                    left = x -> prev.test(x) && right.test(x);
                } else {
                    break;
                }
            }
            return left;
        }

        private Predicate<T> parseFactor() {
            if (pos >= tokens.size()) {
                return x -> true;
            }
            String token = tokens.get(pos);
            if (token.equals("(")) {
                pos++;
                Predicate<T> expr = parseExpression();
                if (pos < tokens.size() && tokens.get(pos).equals(")")) {
                    pos++;
                }
                return expr;
            } else if (token.equalsIgnoreCase("NOT")) {
                pos++;
                Predicate<T> factor = parseFactor();
                return x -> !factor.test(x);
            } else {
                return parseCondition();
            }
        }

        private Predicate<T> parseCondition() {
            if (pos >= tokens.size()) return x -> true;
            String field = tokens.get(pos++);
            if (pos >= tokens.size()) return x -> true;
            String op = tokens.get(pos++);
            if (pos >= tokens.size()) return x -> true;
            String value = tokens.get(pos++);

            if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
                value = value.substring(1, value.length() - 1);
            } else if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                value = value.substring(1, value.length() - 1);
            }

            final String fField = field;
            final String fOp = op;
            final String fVal = value;

            return entity -> {
                try {
                    Object actual = getFieldValue(entity, fField);
                    return evaluate(actual, fOp, fVal);
                } catch (Exception e) {
                    return false;
                }
            };
        }

        private Object getFieldValue(Object entity, String fieldPath) throws Exception {
            if (entity == null) return null;
            String[] parts = fieldPath.split("\\.");
            Object current = entity;
            for (String part : parts) {
                if (current == null) return null;
                current = invokeAccessor(current, part);
            }
            return current;
        }

        private Object invokeAccessor(Object obj, String property) throws Exception {
            Class<?> c = obj.getClass();
            if (c.isRecord()) {
                for (RecordComponent rc : c.getRecordComponents()) {
                    if (rc.getName().equalsIgnoreCase(property)) {
                        return rc.getAccessor().invoke(obj);
                    }
                }
            }
            for (Method m : c.getMethods()) {
                if (m.getParameterCount() == 0) {
                    if (m.getName().equalsIgnoreCase(property) ||
                        m.getName().equalsIgnoreCase("get" + property) ||
                        m.getName().equalsIgnoreCase("is" + property)) {
                        return m.invoke(obj);
                    }
                }
            }
            return null;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private boolean evaluate(Object actual, String op, String expectedStr) {
            if (actual == null) {
                if (expectedStr.equalsIgnoreCase("null")) {
                    return op.equals("=") || op.equals("==");
                }
                return op.equals("!=") || op.equals("<>");
            }

            if (actual instanceof Boolean b) {
                boolean expectedBool = Boolean.parseBoolean(expectedStr);
                return op.equals("=") || op.equals("==") ? b == expectedBool : b != expectedBool;
            }

            if (actual instanceof Number n) {
                double actualNum = n.doubleValue();
                double expectedNum = Double.parseDouble(expectedStr);
                return switch (op) {
                    case "=", "==" -> Double.compare(actualNum, expectedNum) == 0;
                    case "!=", "<>" -> Double.compare(actualNum, expectedNum) != 0;
                    case "<" -> actualNum < expectedNum;
                    case "<=" -> actualNum <= expectedNum;
                    case ">" -> actualNum > expectedNum;
                    case ">=" -> actualNum >= expectedNum;
                    default -> false;
                };
            }

            if (actual instanceof Collection<?> col) {
                if (op.equalsIgnoreCase("CONTAINS") || op.equals("=")) {
                    for (Object item : col) {
                        if (item != null && item.toString().equalsIgnoreCase(expectedStr)) {
                            return true;
                        }
                    }
                    return false;
                }
            }

            String actualStr = actual.toString();
            return switch (op.toUpperCase()) {
                case "=", "==" -> actualStr.equalsIgnoreCase(expectedStr);
                case "!=", "<>" -> !actualStr.equalsIgnoreCase(expectedStr);
                case "LIKE" -> actualStr.toLowerCase().contains(expectedStr.toLowerCase());
                case "STARTSWITH" -> actualStr.toLowerCase().startsWith(expectedStr.toLowerCase());
                case "ENDSWITH" -> actualStr.toLowerCase().endsWith(expectedStr.toLowerCase());
                default -> false;
            };
        }
    }
}


package io.jettra.jwt;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Minimal JSON utility for JettraJWT.
 */
public class JettraJson {

    public static String toJson(Map<String, Object> map) {
        StringJoiner sj = new StringJoiner(",", "{", "}");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sj.add("\"" + escape(entry.getKey()) + "\":" + formatValue(entry.getValue()));
        }
        return sj.toString();
    }

    private static String formatValue(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + escape((String) value) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof java.util.Collection<?> col) {
            StringJoiner sj = new StringJoiner(",", "[", "]");
            for (Object item : col) {
                sj.add(formatValue(item));
            }
            return sj.toString();
        }
        if (value instanceof Object[] arr) {
            StringJoiner sj = new StringJoiner(",", "[", "]");
            for (Object item : arr) {
                sj.add(formatValue(item));
            }
            return sj.toString();
        }
        if (value instanceof Map<?, ?> m) {
            StringJoiner sj = new StringJoiner(",", "{", "}");
            for (Map.Entry<?, ?> e : m.entrySet()) {
                sj.add("\"" + escape(String.valueOf(e.getKey())) + "\":" + formatValue(e.getValue()));
            }
            return sj.toString();
        }
        return "\"" + escape(value.toString()) + "\"";
    }

    private static String escape(String s) {
        return s.replace("\"", "\\\"");
    }

    public static Map<String, Object> parse(String json) {
        Map<String, Object> map = new HashMap<>();
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
            String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String pair : pairs) {
                String[] kv = pair.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (kv.length == 2) {
                    String key = kv[0].trim().replace("\"", "");
                    String val = kv[1].trim();
                    if (val.startsWith("\"") && val.endsWith("\"")) {
                        map.put(key, val.substring(1, val.length() - 1));
                    } else if (val.equals("true") || val.equals("false")) {
                        map.put(key, Boolean.parseBoolean(val));
                    } else if (val.matches("-?\\d+")) {
                        map.put(key, Long.parseLong(val));
                    } else if (val.startsWith("[") && val.endsWith("]")) {
                        String inner = val.substring(1, val.length() - 1).trim();
                        java.util.List<Object> list = new java.util.ArrayList<>();
                        if (!inner.isEmpty()) {
                            for (String item : inner.split(",")) {
                                item = item.trim();
                                if (item.startsWith("\"") && item.endsWith("\"")) {
                                    list.add(item.substring(1, item.length() - 1));
                                } else if (!item.isEmpty()) {
                                    list.add(item);
                                }
                            }
                        }
                        map.put(key, list);
                    } else {
                        map.put(key, val);
                    }
                }
            }
        }
        return map;
    }
}

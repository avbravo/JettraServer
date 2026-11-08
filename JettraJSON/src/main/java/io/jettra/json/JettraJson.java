package io.jettra.json;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.*;

public class JettraJson {

    private final JettraJsonBuilder builder;

    public JettraJson() {
        this.builder = new JettraJsonBuilder();
    }

    public JettraJson(JettraJsonBuilder builder) {
        this.builder = builder;
    }

    public String toJson(Object src) {
        if (src == null) {
            return "null";
        }
        if (src instanceof String) {
            return "\"" + escapeString((String) src) + "\"";
        }
        if (src instanceof Enum<?> e) {
            return "\"" + escapeString(e.name()) + "\"";
        }
        if (src instanceof Number || src instanceof Boolean) {
            return src.toString();
        }
        if (src instanceof JsonObject) {
            return serializeJsonObject((JsonObject) src);
        }
        if (src instanceof JsonArray) {
            return serializeJsonArray((JsonArray) src);
        }
        if (src instanceof Map) {
            return serializeMap((Map<?, ?>) src);
        }
        if (src instanceof Collection) {
            return serializeCollection((Collection<?>) src);
        }
        if (src.getClass().isArray()) {
            return serializeArray(src);
        }

        return serializeObject(src);
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJson(String json, Class<T> classOfT) {
        if (json == null || json.trim().isEmpty() || json.equals("null")) {
            return null;
        }
        try {
            Object parsed = parse(json);
            if (classOfT == JsonObject.class) {
                if (parsed instanceof JsonObject) return (T) parsed;
                if (parsed instanceof Map) {
                    JsonObject jo = new JsonObject();
                    for (Map.Entry<?, ?> e : ((Map<?, ?>) parsed).entrySet()) {
                        Object val = e.getValue();
                        if (val instanceof JsonObject) jo.add(String.valueOf(e.getKey()), (JsonObject) val);
                        else if (val instanceof JsonArray) jo.add(String.valueOf(e.getKey()), (JsonArray) val);
                        else if (val instanceof Number) jo.addProperty(String.valueOf(e.getKey()), (Number) val);
                        else if (val instanceof Boolean) jo.addProperty(String.valueOf(e.getKey()), (Boolean) val);
                        else jo.addProperty(String.valueOf(e.getKey()), val != null ? val.toString() : null);
                    }
                    return (T) jo;
                }
                JsonObject jo = new JsonObject();
                jo.addProperty("value", parsed != null ? parsed.toString() : null);
                return (T) jo;
            }
            if (classOfT == JsonArray.class) {
                if (parsed instanceof JsonArray) return (T) parsed;
                if (parsed instanceof List) {
                    JsonArray ja = new JsonArray();
                    for (Object item : (List<?>) parsed) {
                        if (item instanceof JsonObject) ja.add((JsonObject) item);
                        else if (item instanceof JsonArray) ja.add((JsonArray) item);
                        else if (item instanceof Number) ja.add((Number) item);
                        else if (item instanceof Boolean) ja.add((Boolean) item);
                        else ja.add(item != null ? item.toString() : null);
                    }
                    return (T) ja;
                }
                return null;
            }
            if (classOfT == String.class) {
                return (T) (parsed != null ? parsed.toString() : null);
            }
            if (classOfT == Map.class) {
                if (parsed instanceof JsonObject) return (T) ((JsonObject) parsed).getMap();
                if (parsed instanceof Map) return (T) parsed;
                return null;
            }
            if (classOfT == List.class) {
                if (parsed instanceof JsonArray) return (T) ((JsonArray) parsed).getList();
                if (parsed instanceof List) return (T) parsed;
                return null;
            }
            return convertParsed(parsed, classOfT);
        } catch (Exception e) {
            try {
                return classOfT.getDeclaredConstructor().newInstance();
            } catch (Exception ignored) {
                return null;
            }
        }
    }
    
    public <T> T fromJson(String json, Type typeOfT) {
        if (typeOfT instanceof Class<?>) {
            return fromJson(json, (Class<T>) typeOfT);
        }
        return null;
    }

    public Object parse(String json) {
        if (json == null || json.trim().isEmpty()) return null;
        JsonParser parser = new JsonParser(json.trim());
        return parser.parseValue();
    }

    @SuppressWarnings("unchecked")
    private <T> T convertParsed(Object parsed, Class<T> clazz) {
        if (parsed == null) return null;
        try {
            if (clazz.isRecord()) {
                RecordComponent[] components = clazz.getRecordComponents();
                Object[] args = new Object[components.length];
                Class<?>[] argTypes = new Class<?>[components.length];
                JsonObject obj = (parsed instanceof JsonObject) ? (JsonObject) parsed : new JsonObject();

                for (int i = 0; i < components.length; i++) {
                    RecordComponent comp = components[i];
                    argTypes[i] = comp.getType();
                    Object val = obj.get(comp.getName());
                    args[i] = coerceValue(val, comp.getType());
                }

                Constructor<T> ctor = clazz.getDeclaredConstructor(argTypes);
                ctor.setAccessible(true);
                return ctor.newInstance(args);
            }

            Constructor<T> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            T instance = ctor.newInstance();
            if (parsed instanceof JsonObject) {
                JsonObject obj = (JsonObject) parsed;
                for (Field f : clazz.getDeclaredFields()) {
                    f.setAccessible(true);
                    if (obj.has(f.getName())) {
                        Object v = obj.get(f.getName());
                        f.set(instance, coerceValue(v, f.getType()));
                    }
                }
            }
            return instance;
        } catch (Exception e) {
            return null;
        }
    }

    private Object coerceValue(Object val, Class<?> targetType) {
        if (val == null) return null;
        if (targetType.isInstance(val)) return val;
        if (targetType == String.class) return val.toString();
        if (targetType == int.class || targetType == Integer.class) {
            if (val instanceof Number) return ((Number) val).intValue();
            return Integer.parseInt(val.toString());
        }
        if (targetType == long.class || targetType == Long.class) {
            if (val instanceof Number) return ((Number) val).longValue();
            return Long.parseLong(val.toString());
        }
        if (targetType == double.class || targetType == Double.class) {
            if (val instanceof Number) return ((Number) val).doubleValue();
            return Double.parseDouble(val.toString());
        }
        if (targetType == float.class || targetType == Float.class) {
            if (val instanceof Number) return ((Number) val).floatValue();
            return Float.parseFloat(val.toString());
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            if (val instanceof Boolean) return val;
            return Boolean.parseBoolean(val.toString());
        }
        return val;
    }

    private String serializeJsonObject(JsonObject obj) {
        return serializeMap(obj.getMap());
    }

    private String serializeJsonArray(JsonArray arr) {
        return serializeCollection(arr.getList());
    }

    private String serializeMap(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(",");
            String keyStr = entry.getKey() != null ? escapeString(String.valueOf(entry.getKey())) : "null";
            sb.append("\"").append(keyStr).append("\":").append(toJson(entry.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String serializeCollection(Collection<?> col) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : col) {
            if (!first) sb.append(",");
            sb.append(toJson(item));
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    private String serializeArray(Object array) {
        StringBuilder sb = new StringBuilder("[");
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(Array.get(array, i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String serializeObject(Object obj) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (!first) sb.append(",");
                String fieldName = escapeString(field.getName());
                sb.append("\"").append(fieldName).append("\":").append(toJson(value));
                first = false;
            } catch (IllegalAccessException e) {
                // Ignore
            }
        }
        sb.append("}");
        return sb.toString();
    }

    public static String escapeString(String str) {
        if (str == null) return "";
        StringBuilder sb = new StringBuilder(str.length() + 16);
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    // =========================================================================
    // High-Performance Recursive-Descent JSON Parser
    // =========================================================================
    public static class JsonParser {
        private final String src;
        private int pos = 0;
        private final int len;

        public JsonParser(String src) {
            this.src = src;
            this.len = src.length();
        }

        public Object parseValue() {
            skipWhitespace();
            if (pos >= len) return null;
            char c = src.charAt(pos);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"' || c == '\'') return parseString();
            if (c == 't' || c == 'T' || c == 'f' || c == 'F') return parseBoolean();
            if (c == 'n' || c == 'N') return parseNull();
            if (c == '-' || (c >= '0' && c <= '9')) return parseNumber();
            return parseUnquotedString();
        }

        private JsonObject parseObject() {
            JsonObject obj = new JsonObject();
            pos++; // skip '{'
            skipWhitespace();
            if (pos < len && src.charAt(pos) == '}') {
                pos++;
                return obj;
            }

            while (pos < len) {
                skipWhitespace();
                String key = parseKey();
                skipWhitespace();
                if (pos < len && src.charAt(pos) == ':') {
                    pos++;
                }
                skipWhitespace();
                Object val = parseValue();

                if (val instanceof JsonObject) {
                    obj.add(key, (JsonObject) val);
                } else if (val instanceof JsonArray) {
                    obj.add(key, (JsonArray) val);
                } else if (val instanceof Number) {
                    obj.addProperty(key, (Number) val);
                } else if (val instanceof Boolean) {
                    obj.addProperty(key, (Boolean) val);
                } else {
                    obj.addProperty(key, val != null ? val.toString() : null);
                }

                skipWhitespace();
                if (pos < len && src.charAt(pos) == ',') {
                    pos++;
                } else if (pos < len && src.charAt(pos) == '}') {
                    pos++;
                    break;
                } else {
                    pos++;
                }
            }
            return obj;
        }

        private JsonArray parseArray() {
            JsonArray arr = new JsonArray();
            pos++; // skip '['
            skipWhitespace();
            if (pos < len && src.charAt(pos) == ']') {
                pos++;
                return arr;
            }

            while (pos < len) {
                skipWhitespace();
                Object val = parseValue();
                if (val instanceof JsonObject) {
                    arr.add((JsonObject) val);
                } else if (val instanceof JsonArray) {
                    arr.add((JsonArray) val);
                } else if (val instanceof Number) {
                    arr.add((Number) val);
                } else if (val instanceof Boolean) {
                    arr.add((Boolean) val);
                } else {
                    arr.add(val != null ? val.toString() : null);
                }

                skipWhitespace();
                if (pos < len && src.charAt(pos) == ',') {
                    pos++;
                } else if (pos < len && src.charAt(pos) == ']') {
                    pos++;
                    break;
                } else {
                    pos++;
                }
            }
            return arr;
        }

        private String parseKey() {
            skipWhitespace();
            if (pos >= len) return "";
            char c = src.charAt(pos);
            if (c == '"' || c == '\'') return parseString();
            int start = pos;
            while (pos < len && src.charAt(pos) != ':' && !Character.isWhitespace(src.charAt(pos))) {
                pos++;
            }
            return src.substring(start, pos).trim();
        }

        private String parseString() {
            char quote = src.charAt(pos);
            pos++; // skip opening quote
            StringBuilder sb = new StringBuilder();
            while (pos < len) {
                char c = src.charAt(pos++);
                if (c == quote) {
                    return sb.toString();
                }
                if (c == '\\' && pos < len) {
                    char esc = src.charAt(pos++);
                    switch (esc) {
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'u' -> {
                            if (pos + 4 <= len) {
                                String hex = src.substring(pos, pos + 4);
                                sb.append((char) Integer.parseInt(hex, 16));
                                pos += 4;
                            }
                        }
                        default -> sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Number parseNumber() {
            int start = pos;
            boolean isFloating = false;
            while (pos < len) {
                char c = src.charAt(pos);
                if (c == '.' || c == 'e' || c == 'E') {
                    isFloating = true;
                    pos++;
                } else if ((c >= '0' && c <= '9') || c == '-' || c == '+') {
                    pos++;
                } else {
                    break;
                }
            }
            String numStr = src.substring(start, pos);
            try {
                if (isFloating) return Double.parseDouble(numStr);
                long l = Long.parseLong(numStr);
                if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) return (int) l;
                return l;
            } catch (Exception e) {
                return 0;
            }
        }

        private Boolean parseBoolean() {
            if (src.regionMatches(true, pos, "true", 0, 4)) {
                pos += 4;
                return true;
            }
            if (src.regionMatches(true, pos, "false", 0, 5)) {
                pos += 5;
                return false;
            }
            return false;
        }

        private Object parseNull() {
            if (src.regionMatches(true, pos, "null", 0, 4)) {
                pos += 4;
            }
            return null;
        }

        private String parseUnquotedString() {
            int start = pos;
            while (pos < len && src.charAt(pos) != ',' && src.charAt(pos) != '}' && src.charAt(pos) != ']') {
                pos++;
            }
            return src.substring(start, pos).trim();
        }

        private void skipWhitespace() {
            while (pos < len && Character.isWhitespace(src.charAt(pos))) {
                pos++;
            }
        }
    }
}

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
                    } else {
                        map.put(key, val);
                    }
                }
            }
        }
        return map;
    }
}

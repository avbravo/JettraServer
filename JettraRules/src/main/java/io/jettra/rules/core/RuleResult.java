package io.jettra.rules.core;

public class RuleResult {
    private final boolean valid;
    private final String message;
    private final String field;

    public RuleResult(boolean valid, String message, String field) {
        this.valid = valid;
        this.message = message;
        this.field = field;
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }

    public String getField() {
        return field;
    }
}

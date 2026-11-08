package io.jettra.json;

public class JettraJsonBuilder {
    private boolean prettyPrinting = false;
    private String dateFormatPattern = null;

    public JettraJsonBuilder() {
    }

    public JettraJsonBuilder setPrettyPrinting() {
        this.prettyPrinting = true;
        return this;
    }

    public JettraJsonBuilder setDateFormat(String pattern) {
        this.dateFormatPattern = pattern;
        return this;
    }

    public JettraJson create() {
        return new JettraJson(this);
    }

    public boolean isPrettyPrinting() {
        return prettyPrinting;
    }

    public String getDateFormatPattern() {
        return dateFormatPattern;
    }
}

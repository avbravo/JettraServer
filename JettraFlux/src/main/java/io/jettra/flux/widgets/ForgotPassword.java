package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class ForgotPassword extends Widget {
    private String action = "/forgot-password";
    private String title = "Forgot Password";
    private String logoUrl = null;

    private ForgotPassword() {}

    public static ForgotPassword create() {
        return new ForgotPassword();
    }
    
    public ForgotPassword action(String action) {
        this.action = action;
        return this;
    }
    
    public ForgotPassword title(String title) {
        this.title = title;
        return this;
    }
    
    public ForgotPassword logo(String logoUrl) {
        this.logoUrl = logoUrl;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        Widget[] children;
        if (logoUrl != null && !logoUrl.isEmpty()) {
            children = new Widget[]{
                Image.of(logoUrl).modifier(new io.jettra.flux.core.Modifier().style("max-width: 150px; margin-bottom: 20px; align-self: center;")),
                Header.of(2, title).modifier(new io.jettra.flux.core.Modifier().style("text-align: center;")),
                Label.of("Email").forId("email"),
                TextField.of("email", "Enter your email").modifier(new io.jettra.flux.core.Modifier().attribute("type", "email")),
                ElevatedButton.of("Send Reset Link").modifier(new io.jettra.flux.core.Modifier().style("margin-top: 15px; width: 100%; background-color: var(--primary-color, #3b82f6); color: white; border: none;")),
                Link.of("/login", "Back to Login").modifier(new io.jettra.flux.core.Modifier().style("margin-top: 15px; text-align: center; display: block; font-size: 0.875rem; color: var(--primary-color, #3b82f6);"))
            };
        } else {
            children = new Widget[]{
                Header.of(2, title).modifier(new io.jettra.flux.core.Modifier().style("text-align: center;")),
                Label.of("Email").forId("email"),
                TextField.of("email", "Enter your email").modifier(new io.jettra.flux.core.Modifier().attribute("type", "email")),
                ElevatedButton.of("Send Reset Link").modifier(new io.jettra.flux.core.Modifier().style("margin-top: 15px; width: 100%; background-color: var(--primary-color, #3b82f6); color: white; border: none;")),
                Link.of("/login", "Back to Login").modifier(new io.jettra.flux.core.Modifier().style("margin-top: 15px; text-align: center; display: block; font-size: 0.875rem; color: var(--primary-color, #3b82f6);"))
            };
        }
        
        Widget formContent = Card.of(Column.of(children)).modifier(new io.jettra.flux.core.Modifier().style("width: 100%; max-width: 400px; box-sizing: border-box; padding: 2rem; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); border: 1px solid var(--surface-border, #ccc); background-color: var(--surface-color);"));

        Widget form = Form.of(formContent).action(action).method("POST");

        return form.render(theme);
    }
}

package io.jettra.flux.complex;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import io.jettra.flux.theme.Themes;
import io.jettra.flux.widgets.Div;
import io.jettra.flux.widgets.RawHtml;
import io.jettra.flux.widgets.Scaffold;
import io.jettra.flux.widgets.Span;
import io.jettra.flux.core.FluxConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SwaggerUIPage implements HttpHandler {

    public static String openApiUrl = "/openapi.json";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String resolvedUrl = FluxConfig.resolvePath(openApiUrl);
        ThemeData theme = Themes.AstTheme();

        Widget themeSwitch = Div.of(
                Span.of("Dark Theme").modifier(new Modifier().style("margin-right: 10px; font-weight: bold;")),
                RawHtml.of("<label class=\"theme-switch\">\n" +
                        "      <input type=\"checkbox\" id=\"checkbox\" />\n" +
                        "      <div class=\"slider\"></div>\n" +
                        "    </label>")
        ).modifier(new Modifier().cssClass("theme-switch-wrapper"));

        Widget swaggerUiContainer = Div.of().attribute("id", "swagger-ui");

        String customStyles = "<style>\n" +
                "    .theme-switch-wrapper {\n" +
                "      position: fixed; top: 15px; right: 25px; z-index: 9999;\n" +
                "      display: flex; align-items: center; background: #fff;\n" +
                "      padding: 5px 12px; border-radius: 20px; box-shadow: 0 2px 5px rgba(0,0,0,0.2);\n" +
                "      font-family: sans-serif; font-size: 14px;\n" +
                "      color: #333;\n" +
                "    }\n" +
                "    body.dark-mode .theme-switch-wrapper {\n" +
                "      background: #333; color: #fff;\n" +
                "    }\n" +
                "    .theme-switch { display: inline-block; position: relative; width: 44px; height: 22px; }\n" +
                "    .theme-switch input { opacity: 0; width: 0; height: 0; }\n" +
                "    .slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #ccc; transition: .4s; border-radius: 22px; }\n" +
                "    .slider:before { position: absolute; content: \"\"; height: 18px; width: 18px; left: 2px; bottom: 2px; background-color: white; transition: .4s; border-radius: 50%; }\n" +
                "    input:checked + .slider { background-color: #4caf50; }\n" +
                "    input:checked + .slider:before { transform: translateX(22px); }\n" +
                "    body.dark-mode { background-color: #222; }\n" +
                "  </style>";

        String scripts = "<script src=\"https://unpkg.com/swagger-ui-dist@5.9.0/swagger-ui-bundle.js\" crossorigin></script>\n" +
                "<script>\n" +
                "  window.onload = () => {\n" +
                "    window.ui = SwaggerUIBundle({\n" +
                "      url: '" + resolvedUrl + "',\n" +
                "      dom_id: '#swagger-ui',\n" +
                "      operationsSorter: 'alpha',\n" +
                "      tagsSorter: 'alpha',\n" +
                "    });\n" +
                "  };\n" +
                "  const toggleSwitch = document.querySelector('.theme-switch input[type=\"checkbox\"]');\n" +
                "  const themeLink = document.getElementById('swagger-theme');\n" +
                "  const lightThemeUrl = 'https://unpkg.com/swagger-ui-dist@5.9.0/swagger-ui.css';\n" +
                "  const darkThemeUrl = 'https://unpkg.com/swagger-ui-themes@3.0.1/themes/3.x/theme-muted.css';\n" +
                "  const currentTheme = localStorage.getItem('theme');\n" +
                "  if (currentTheme) {\n" +
                "      if (currentTheme === 'dark-theme') {\n" +
                "          toggleSwitch.checked = true;\n" +
                "          themeLink.href = darkThemeUrl;\n" +
                "          document.body.classList.add('dark-mode');\n" +
                "      }\n" +
                "  }\n" +
                "  toggleSwitch.addEventListener('change', function(e) {\n" +
                "      if (e.target.checked) {\n" +
                "          themeLink.href = darkThemeUrl;\n" +
                "          document.body.classList.add('dark-mode');\n" +
                "          localStorage.setItem('theme', 'dark-theme');\n" +
                "      } else {\n" +
                "          themeLink.href = lightThemeUrl;\n" +
                "          document.body.classList.remove('dark-mode');\n" +
                "          localStorage.setItem('theme', 'light');\n" +
                "      }\n" +
                "  });\n" +
                "</script>";

        Widget bodyContainer = Div.of(
                RawHtml.of("<link id=\"swagger-theme\" rel=\"stylesheet\" href=\"https://unpkg.com/swagger-ui-dist@5.9.0/swagger-ui.css\" />"),
                RawHtml.of(customStyles),
                themeSwitch,
                swaggerUiContainer,
                RawHtml.of(scripts)
        );

        Widget ui = Scaffold.of().body(bodyContainer);

        String html = "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n" +
                "<meta charset=\"utf-8\" />\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n" +
                "<meta name=\"description\" content=\"SwaggerUI\" />\n" +
                "<title>SwaggerUI - JettraFlux</title>\n" +
                theme.generateGlobalCss() + "\n" +
                "</head>\n<body style=\"margin: 0; padding: 0; box-sizing: border-box;\">\n" +
                ui.render(theme) + "\n" +
                "</body>\n</html>";

        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }
}


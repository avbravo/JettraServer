package io.jettra.flux.theme;

import io.jettra.flux.widgets.*;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class MatrixThemeTest {

    @Test
    public void testMatrixThemeData() {
        ThemeData theme = MatrixTheme.create();
        assertNotNull(theme);
        assertEquals("#00ff41", theme.primaryColor);
        assertEquals("#020b02", theme.backgroundColor);
        assertEquals("#000000", theme.onPrimaryColor);
        assertEquals("#00ff66", theme.onSurfaceColor);
        assertNotNull(theme.customCss);
        assertNotNull(theme.customJs);
        assertTrue(theme.generateGlobalCss().contains("--primary-color: #00ff41;"));
        assertTrue(theme.generateGlobalCss().contains("matrix-rain-canvas"));
    }

    @Test
    public void testThemesFactoryMethods() {
        ThemeData theme1 = Themes.MatrixTheme();
        ThemeData theme2 = Themes.Matrix();
        assertNotNull(theme1);
        assertNotNull(theme2);
        assertEquals("#00ff41", theme1.primaryColor);
        assertEquals("#00ff41", theme2.primaryColor);
    }

    @Test
    public void testThemeRegistryLookup() {
        ThemeData t1 = ThemeRegistry.getTheme("MatrixTheme");
        ThemeData t2 = ThemeRegistry.getTheme("Matrix");
        ThemeData t3 = ThemeRegistry.getTheme("matrix");
        ThemeData t4 = ThemeRegistry.getTheme("matrixtheme");
        
        assertNotNull(t1, "MatrixTheme should be resolved by ThemeRegistry");
        assertNotNull(t2, "Matrix should be resolved by ThemeRegistry");
        assertNotNull(t3, "matrix (case-insensitive) should be resolved by ThemeRegistry");
        assertNotNull(t4, "matrixtheme (case-insensitive) should be resolved by ThemeRegistry");
        assertEquals("#00ff41", t1.primaryColor);
    }

    @Test
    public void testThemeChangedRenderingWithMatrix() {
        ThemeChanged widget = ThemeChanged.of().current("Matrix");
        String html = widget.render(Themes.MatrixTheme());
        assertNotNull(html);
        assertTrue(html.contains("MatrixTheme") || html.contains("Matrix"));
        assertTrue(html.contains("changeJettraTheme"));
    }

    @Test
    public void testWidgetsRenderingWithMatrixTheme() {
        ThemeData matrix = Themes.MatrixTheme();

        // Button
        Button btn = Button.of("Execute");
        String btnHtml = btn.render(matrix);
        assertTrue(btnHtml.contains("<button"));
        assertTrue(btnHtml.contains("espresso-button"));

        // Card
        Card card = Card.of(Text.of("Terminal Card"));
        String cardHtml = card.render(matrix);
        assertTrue(cardHtml.contains("espresso-card"));

        // TextField
        TextField tf = TextField.of("code", "Enter command");
        String tfHtml = tf.render(matrix);
        assertTrue(tfHtml.contains("espresso-textfield") || tfHtml.contains("<input"));

        // Datatable
        Datatable table = Datatable.of(java.util.List.of("Column"), java.util.List.of(java.util.List.of("Row1")));
        String tableHtml = table.render(matrix);
        assertTrue(tableHtml.contains("espresso-datatable") || tableHtml.contains("<table"));

        // Scaffold
        Scaffold scaffold = Scaffold.of().body(card);
        String scaffoldHtml = scaffold.render(matrix);
        assertTrue(scaffoldHtml.contains("jettra-scaffold-layout"));
        assertTrue(scaffoldHtml.contains("--primary-color: #00ff41;"));
    }
}

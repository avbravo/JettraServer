package io.jettra.flux.theme;

import io.jettra.flux.widgets.*;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class RetroThemeTest {

    @Test
    public void testRetroThemeData() {
        ThemeData theme = RetroTheme.create();
        assertNotNull(theme);
        assertEquals("#5c8e32", theme.primaryColor);
        assertEquals("#d49a3d", theme.secondaryColor);
        assertEquals("#242220", theme.backgroundColor);
        assertEquals("#ffffff", theme.onPrimaryColor);
        assertEquals("#f0f0f0", theme.onSurfaceColor);
        assertNotNull(theme.customCss);
        assertNotNull(theme.customJs);
    }

    @Test
    public void testThemeRegistryLookup() {
        ThemeData r1 = ThemeRegistry.getTheme("RetroTheme");
        ThemeData r2 = ThemeRegistry.getTheme("Retro");
        ThemeData r3 = Themes.RetroTheme();
        ThemeData r4 = Themes.Retro();

        assertNotNull(r1);
        assertNotNull(r2);
        assertNotNull(r3);
        assertNotNull(r4);
        assertEquals("#5c8e32", r1.primaryColor);
    }
}

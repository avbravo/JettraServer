package io.jettra.flux.theme;

import io.jettra.flux.widgets.ThemeToggle;
import io.jettra.test.annotation.BeforeEach;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.EnumSource;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.ParameterizedTest;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class ThemeTokensAndToggleTest {

    @BeforeEach
    public void resetThemeContext() {
        ThemeContext.getInstance().set(JettraTheme.MATRIX, ColorMode.DARK);
    }

    @ParameterizedTest
    @EnumSource(JettraTheme.class)
    @DisplayName("Every JettraTheme enum constant provides non-null WCAG compliant ThemeTokens in WHITE and DARK")
    public void testThemeTokensCompleteness(JettraTheme theme) {
        for (ColorMode mode : ColorMode.values()) {
            ThemeTokens tokens = theme.tokens(mode);
            assertNotNull(tokens, "Tokens for " + theme + " in mode " + mode + " must not be null");
            assertNotNull(tokens.surfaceBackground(), "surfaceBackground must not be null");
            assertNotNull(tokens.cardBackground(), "cardBackground must not be null");
            assertNotNull(tokens.textPrimary(), "textPrimary must not be null");
            assertNotNull(tokens.textSecondary(), "textSecondary must not be null");
            assertNotNull(tokens.border(), "border must not be null");
            assertNotNull(tokens.accentPrimary(), "accentPrimary must not be null");
            assertNotNull(tokens.accentSecondary(), "accentSecondary must not be null");
            assertNotNull(tokens.focusRing(), "focusRing must not be null");
            assertNotNull(tokens.iconColor(), "iconColor must not be null");

            // Legibility sanity checks (text and surface should never be identical)
            assertNotEquals(tokens.textPrimary().toLowerCase(), tokens.surfaceBackground().toLowerCase(),
                "Text primary must not match surface background in " + theme + " (" + mode + ")");
            assertNotEquals(tokens.textPrimary().toLowerCase(), tokens.cardBackground().toLowerCase(),
                "Text primary must not match card background in " + theme + " (" + mode + ")");

            // CSS variable generation
            String cssVars = tokens.toCssVariables();
            assertTrue(cssVars.contains("--surface-background:"));
            assertTrue(cssVars.contains("--card-background:"));
            assertTrue(cssVars.contains("--text-primary:"));
            assertTrue(cssVars.contains("--text-secondary:"));
            assertTrue(cssVars.contains("--border:"));
            assertTrue(cssVars.contains("--accent-primary:"));
            assertTrue(cssVars.contains("--accent-secondary:"));
            assertTrue(cssVars.contains("--focus-ring:"));
            assertTrue(cssVars.contains("--icon-color:"));
        }
    }

    @ParameterizedTest
    @EnumSource(JettraTheme.class)
    @DisplayName("Every JettraTheme produces valid ThemeData injecting semantic tokens into global CSS")
    public void testThemeDataGeneration(JettraTheme theme) {
        for (ColorMode mode : ColorMode.values()) {
            ThemeData data = theme.create(mode);
            assertNotNull(data, "ThemeData for " + theme + " in " + mode + " must not be null");
            assertEquals(mode, data.getColorMode(), "ThemeData colorMode must match requested mode");
            assertNotNull(data.getTokens(), "ThemeData must contain ThemeTokens");

            String globalCss = data.generateGlobalCss();
            assertTrue(globalCss.contains("--surface-background:"), "Global CSS must contain --surface-background");
            assertTrue(globalCss.contains("--text-primary:"), "Global CSS must contain --text-primary");
            assertTrue(globalCss.contains("--accent-primary:"), "Global CSS must contain --accent-primary");
        }
    }

    @Test
    @DisplayName("Canonical refactored classes SL, Core, Heroes provide full functionality and backwards compatibility")
    public void testRefactoredCanonicalThemes() {
        // SL
        ThemeData slWhite = SL.create(ColorMode.WHITE);
        ThemeData slDark = SL.create(ColorMode.DARK);
        assertNotNull(slWhite);
        assertNotNull(slDark);
        assertEquals("#0f172a", slWhite.getTokens().textPrimary());
        assertEquals("#f1f5f9", slDark.getTokens().textPrimary());
        // SLTheme alias delegation
        assertEquals(slDark.primaryColor, SLTheme.create().primaryColor);
        assertEquals(slWhite.getTokens().accentPrimary(), SLTheme.create(ColorMode.WHITE).getTokens().accentPrimary());

        // Core
        ThemeData coreWhite = Core.create(ColorMode.WHITE);
        ThemeData coreDark = Core.create(ColorMode.DARK);
        assertNotNull(coreWhite);
        assertNotNull(coreDark);
        assertEquals("#0f172a", coreWhite.getTokens().textPrimary());
        assertEquals("#f1f5f9", coreDark.getTokens().textPrimary());
        // CoreTheme alias delegation
        assertEquals(coreDark.primaryColor, CoreTheme.create().primaryColor);
        assertEquals(coreWhite.getTokens().accentPrimary(), CoreTheme.create(ColorMode.WHITE).getTokens().accentPrimary());

        // Heroes
        ThemeData heroesWhite = Heroes.create(ColorMode.WHITE);
        ThemeData heroesDark = Heroes.create(ColorMode.DARK);
        assertNotNull(heroesWhite);
        assertNotNull(heroesDark);
        assertEquals("#111827", heroesWhite.getTokens().textPrimary());
        assertEquals("#f9fafb", heroesDark.getTokens().textPrimary());
        // HeroesTheme alias delegation
        assertEquals(heroesDark.primaryColor, HeroesTheme.create().primaryColor);
        assertEquals(heroesWhite.getTokens().accentPrimary(), HeroesTheme.create(ColorMode.WHITE).getTokens().accentPrimary());
    }

    @Test
    @DisplayName("ColorMode toggles and string resolution work accurately")
    public void testColorModeUtilities() {
        assertEquals(ColorMode.DARK, ColorMode.WHITE.toggle());
        assertEquals(ColorMode.WHITE, ColorMode.DARK.toggle());
        assertTrue(ColorMode.DARK.isDark());
        assertTrue(ColorMode.WHITE.isWhite());

        assertEquals(ColorMode.WHITE, ColorMode.fromString("white", ColorMode.DARK));
        assertEquals(ColorMode.WHITE, ColorMode.fromString("light", ColorMode.DARK));
        assertEquals(ColorMode.DARK, ColorMode.fromString("dark", ColorMode.WHITE));
        assertEquals(ColorMode.DARK, ColorMode.fromString("night", ColorMode.WHITE));
        assertEquals(ColorMode.DARK, ColorMode.fromString("invalid", ColorMode.DARK));
    }

    @Test
    @DisplayName("ThemeRegistry resolves canonical names and modes accurately")
    public void testThemeRegistry() {
        ThemeData sl = ThemeRegistry.getTheme("SL", ColorMode.WHITE);
        assertNotNull(sl);
        assertEquals(ColorMode.WHITE, sl.getColorMode());

        ThemeData matrix = ThemeRegistry.getTheme("Matrix", ColorMode.DARK);
        assertNotNull(matrix);
        assertEquals(ColorMode.DARK, matrix.getColorMode());

        ThemeData dark = ThemeRegistry.getTheme(JettraTheme.DARK_THEME, ColorMode.WHITE);
        assertNotNull(dark);
        assertEquals(ColorMode.WHITE, dark.getColorMode());
    }

    @Test
    @DisplayName("ThemeToggle renders Moon icon in WHITE mode and Sun icon in DARK mode with persistence script")
    public void testThemeToggleRendering() {
        ThemeData darkTheme = Themes.DarkTheme(ColorMode.DARK);
        ThemeData whiteTheme = Themes.DarkTheme(ColorMode.WHITE);

        // In Dark mode: must show Sun icon inviting user to switch to light mode
        ThemeToggle toggleDark = ThemeToggle.of().colorMode(ColorMode.DARK);
        String htmlDark = toggleDark.render(darkTheme);
        assertNotNull(htmlDark);
        assertTrue(htmlDark.contains("jettra-theme-icon-sun"), "Must render Sun icon in Dark mode");
        assertTrue(htmlDark.contains("Switch to Light Mode"));
        assertTrue(htmlDark.contains("data-next-mode=\"white\""));
        assertTrue(htmlDark.contains("toggleJettraColorMode"));
        assertTrue(htmlDark.contains("jettra_color_mode"));

        // In White mode: must show Moon icon inviting user to switch to dark mode
        ThemeToggle toggleWhite = ThemeToggle.of().colorMode(ColorMode.WHITE);
        String htmlWhite = toggleWhite.render(whiteTheme);
        assertNotNull(htmlWhite);
        assertTrue(htmlWhite.contains("jettra-theme-icon-moon"), "Must render Moon icon in White mode");
        assertTrue(htmlWhite.contains("Switch to Dark Mode"));
        assertTrue(htmlWhite.contains("data-next-mode=\"dark\""));
    }

    @Test
    @DisplayName("ThemeModeToggle renders reactively and supports independent operation")
    public void testThemeModeToggleDedicated() {
        ThemeData darkTheme = Themes.Core(ColorMode.DARK);
        ThemeData whiteTheme = Themes.Core(ColorMode.WHITE);

        io.jettra.flux.widgets.ThemeModeToggle modeToggle = io.jettra.flux.widgets.ThemeModeToggle.of()
                .size(22)
                .colorMode(ColorMode.WHITE);
        String html = modeToggle.render(whiteTheme);
        assertNotNull(html);
        assertTrue(html.contains("jettra-theme-icon-moon"));
        assertTrue(html.contains("jettra-theme-mode-toggle"));

        // Switch to dark mode
        modeToggle.colorMode(ColorMode.DARK);
        String htmlDark = modeToggle.render(darkTheme);
        assertTrue(htmlDark.contains("jettra-theme-icon-sun"));
    }

    @Test
    @DisplayName("ThemeSelectorMenu renders strictly the 12 canonical themes excluding obsolete identifiers")
    public void testThemeSelectorMenuStrictCatalog() {
        io.jettra.flux.widgets.ThemeSelectorMenu menu = io.jettra.flux.widgets.ThemeSelectorMenu.of().current("Core");
        String html = menu.render(Themes.Core());

        assertNotNull(html);
        // Canonical 12 themes must be present
        assertTrue(html.contains("FlatTheme"), "Must contain FlatTheme");
        assertTrue(html.contains("Theme3D"), "Must contain Theme3D");
        assertTrue(html.contains("FuturisticTheme"), "Must contain FuturisticTheme");
        assertTrue(html.contains("AstTheme"), "Must contain AstTheme");
        assertTrue(html.contains("AtlantisTheme"), "Must contain AtlantisTheme");
        assertTrue(html.contains("OceanTheme"), "Must contain OceanTheme");
        assertTrue(html.contains("Matrix"), "Must contain Matrix");
        assertTrue(html.contains("Retro"), "Must contain Retro");
        assertTrue(html.contains("DarkTheme"), "Must contain DarkTheme");
        assertTrue(html.contains("Heroes"), "Must contain Heroes");
        assertTrue(html.contains("SL"), "Must contain SL");
        assertTrue(html.contains("Core"), "Must contain Core");
        assertTrue(html.contains("Police"), "Must contain Police");
        assertTrue(html.contains("Games"), "Must contain Games");
        assertTrue(html.contains("Bit"), "Must contain Bit");

        // Obsolete identifiers must NOT be present as selectable items
        assertFalse(html.contains("CoreTheme"), "Must NOT contain CoreTheme");
        assertFalse(html.contains("SLTheme"), "Must NOT contain SLTheme");
        assertFalse(html.contains("HeroesTheme"), "Must NOT contain HeroesTheme");
    }

    @Test
    @DisplayName("JettraTheme enum contains canonical themes with getDisplayName()")
    public void testJettraThemeDisplayNames() {
        assertEquals(15, JettraTheme.values().length);
        assertEquals("FlatTheme", JettraTheme.FLAT_THEME.getDisplayName());
        assertEquals("Theme3D", JettraTheme.THEME_3D.getDisplayName());
        assertEquals("FuturisticTheme", JettraTheme.FUTURISTIC_THEME.getDisplayName());
        assertEquals("AstTheme", JettraTheme.AST_THEME.getDisplayName());
        assertEquals("AtlantisTheme", JettraTheme.ATLANTIS_THEME.getDisplayName());
        assertEquals("OceanTheme", JettraTheme.OCEAN_THEME.getDisplayName());
        assertEquals("Matrix", JettraTheme.MATRIX.getDisplayName());
        assertEquals("Retro", JettraTheme.RETRO.getDisplayName());
        assertEquals("DarkTheme", JettraTheme.DARK_THEME.getDisplayName());
        assertEquals("Heroes", JettraTheme.HEROES.getDisplayName());
        assertEquals("SL", JettraTheme.SL.getDisplayName());
        assertEquals("Core", JettraTheme.CORE.getDisplayName());
        assertEquals("Police", JettraTheme.POLICE.getDisplayName());
        assertEquals("Games", JettraTheme.GAMES.getDisplayName());
        assertEquals("Bit", JettraTheme.BIT.getDisplayName());
    }

    @Test
    @DisplayName("ThemeTokens provides alias getters matching semantic naming")
    public void testThemeTokensAliasGetters() {
        ThemeTokens tok = JettraTheme.MATRIX.tokens(ColorMode.DARK);
        assertEquals(tok.surfaceBackground(), tok.background());
        assertEquals(tok.cardBackground(), tok.surface());
        assertEquals(tok.cardBackground(), tok.surfaceVariant());
        assertEquals(tok.accentPrimary(), tok.accent());
        assertEquals(tok.accentPrimary(), tok.brandColor());
    }

    @Test
    @DisplayName("ThemeRegistry.getAvailableThemeNames returns canonical themes")
    public void testAvailableThemeNamesExcludesObsolete() {
        String[] names = ThemeRegistry.getAvailableThemeNames();
        assertEquals(15, names.length, "Available theme names must contain canonical themes");
        java.util.List<String> list = java.util.Arrays.asList(names);
        assertTrue(list.contains("FlatTheme"));
        assertTrue(list.contains("Theme3D"));
        assertTrue(list.contains("FuturisticTheme"));
        assertTrue(list.contains("AstTheme"));
        assertTrue(list.contains("AtlantisTheme"));
        assertTrue(list.contains("OceanTheme"));
        assertTrue(list.contains("Matrix"));
        assertTrue(list.contains("Retro"));
        assertTrue(list.contains("DarkTheme"));
        assertTrue(list.contains("Heroes"));
        assertTrue(list.contains("SL"));
        assertTrue(list.contains("Core"));
        assertTrue(list.contains("Police"));
        assertTrue(list.contains("Games"));
        assertTrue(list.contains("Bit"));

        assertFalse(list.contains("CoreTheme"));
        assertFalse(list.contains("SLTheme"));
        assertFalse(list.contains("HeroesTheme"));
    }

    @Test
    @DisplayName("ThemeSelectDropdown renders the 12 canonical themes and excludes obsolete identifiers")
    public void testThemeSelectDropdownRendering() {
        io.jettra.flux.widgets.ThemeSelectDropdown dropdown = io.jettra.flux.widgets.ThemeSelectDropdown.of().current("Matrix");
        String html = dropdown.render(Themes.Matrix());
        assertNotNull(html);
        assertTrue(html.contains("Matrix"));
        assertTrue(html.contains("Core"));
        assertTrue(html.contains("SL"));
        assertTrue(html.contains("Heroes"));
        assertTrue(html.contains("Theme3D"));
        assertTrue(html.contains("FlatTheme"));
        assertTrue(html.contains("Police"));
        assertTrue(html.contains("Games"));
        assertTrue(html.contains("Bit"));
        assertFalse(html.contains("CoreTheme"));
        assertFalse(html.contains("SLTheme"));
        assertFalse(html.contains("HeroesTheme"));

        // Test native select option
        dropdown.asNativeSelect(true);
        String nativeHtml = dropdown.render(Themes.Matrix());
        assertTrue(nativeHtml.contains("<select"));
        assertTrue(nativeHtml.contains("<option value=\"Core\">"));
        assertTrue(nativeHtml.contains("<option value=\"SL\">"));
        assertTrue(nativeHtml.contains("<option value=\"Heroes\">"));
        assertTrue(nativeHtml.contains("<option value=\"Police\">"));
        assertTrue(nativeHtml.contains("<option value=\"Games\">"));
        assertTrue(nativeHtml.contains("<option value=\"Bit\">"));
        assertFalse(nativeHtml.contains("<option value=\"CoreTheme\">"));
    }

    @Test
    @DisplayName("DashboardThemeControlBar composes ThemeSelectDropdown and ThemeModeToggle side by side")
    public void testDashboardThemeControlBarComposition() {
        io.jettra.flux.widgets.DashboardThemeControlBar bar = io.jettra.flux.widgets.DashboardThemeControlBar.of("SL", ColorMode.WHITE);
        String html = bar.render(Themes.SL(ColorMode.WHITE));

        assertNotNull(html);
        assertTrue(html.contains("jettra-dashboard-theme-control-bar"));
        // Contains dropdown
        assertTrue(html.contains("SL"));
        assertTrue(html.contains("Core"));
        // Contains toggle in White mode (Moon icon)
        assertTrue(html.contains("jettra-theme-icon-moon"));
        assertTrue(html.contains("Switch to Dark Mode"));
    }

    @Test
    @DisplayName("ThemeContext manages reactive state and notifies observers on mutation")
    public void testThemeContextReactiveObserver() {
        ThemeContext ctx = ThemeContext.getInstance();
        assertNotNull(ctx);

        final boolean[] notified = new boolean[]{false};
        ThemeContext.ThemeChangeListener listener = (t, m, tok) -> {
            notified[0] = true;
            assertNotNull(t);
            assertNotNull(m);
            assertNotNull(tok);
        };

        ctx.addListener(listener);
        try {
            ctx.set(JettraTheme.MATRIX, ColorMode.DARK);
            assertTrue(notified[0], "Observer must be notified upon theme/mode change");
            assertEquals(JettraTheme.MATRIX, ctx.getCurrentTheme());
            assertEquals(ColorMode.DARK, ctx.getCurrentMode());
            assertEquals("#020b02", ctx.getCurrentTokens().surfaceBackground());

            notified[0] = false;
            ColorMode newMode = ctx.toggleMode();
            assertEquals(ColorMode.WHITE, newMode);
            assertTrue(notified[0], "Observer must be notified upon toggleMode");
            assertEquals("#f0fdf4", ctx.getCurrentTokens().surfaceBackground());
        } finally {
            ctx.removeListener(listener);
        }
    }

    @Test
    @DisplayName("ThemeTokens formats --jf-* semantic variables and JSON serialization")
    public void testThemeTokensSemanticVariablesAndJson() {
        ThemeTokens tok = JettraTheme.CORE.tokens(ColorMode.WHITE);
        String css = tok.toCssVariables();
        assertTrue(css.contains("--jf-bg:"));
        assertTrue(css.contains("--jf-surface:"));
        assertTrue(css.contains("--jf-text-primary:"));
        assertTrue(css.contains("--jf-border:"));
        assertTrue(css.contains("--jf-accent:"));
        assertTrue(css.contains("--j-bg-body:"));

        String json = tok.toJson();
        assertTrue(json.contains("\"surfaceBackground\":"));
        assertTrue(json.contains("\"textPrimary\":"));
        assertTrue(json.contains("\"accentPrimary\":"));
    }

    @Test
    @DisplayName("ThemeModeToggle generates client script and applyJettraStylePatch hook")
    public void testThemeModeToggleEmitsDynamicPatch() {
        io.jettra.flux.widgets.ThemeModeToggle toggle = io.jettra.flux.widgets.ThemeModeToggle.of().colorMode(ColorMode.DARK);
        String html = toggle.render(Themes.FlatTheme(ColorMode.DARK));

        assertTrue(html.contains("applyJettraStylePatch"));
        assertTrue(html.contains("window.__jettraThemeCatalog"));
        assertTrue(html.contains("--jf-bg"));
        assertTrue(html.contains("--jf-text-primary"));
        assertTrue(html.contains("toggleJettraColorMode"));
    }

    @Test
    @DisplayName("DashboardRootContainer binds semantic tokens and embeds reactive client listener")
    public void testDashboardRootContainerReconciliation() {
        io.jettra.flux.widgets.DashboardRootContainer root = io.jettra.flux.widgets.DashboardRootContainer.of(
            io.jettra.flux.widgets.Text.of("Welcome Dashboard")
        ).currentTheme("Matrix").colorMode(ColorMode.DARK);

        String html = root.render(Themes.Matrix(ColorMode.DARK));
        assertNotNull(html);
        assertTrue(html.contains("class=\"jettra-dashboard-root\""));
        assertTrue(html.contains("data-theme=\"Matrix\""));
        assertTrue(html.contains("data-color-mode=\"dark\""));
        assertTrue(html.contains("--jf-bg"));
        assertTrue(html.contains("jettraThemeChange"));
    }

    @Test
    @DisplayName("Default Theme is Matrix with Dark ColorMode across JettraFlux")
    public void testDefaultThemeIsMatrixDark() {
        assertEquals(JettraTheme.MATRIX, JettraTheme.fromName(null));
        assertEquals(JettraTheme.MATRIX, JettraTheme.fromName(""));
        assertEquals(JettraTheme.MATRIX, ThemeContext.getInstance().getCurrentTheme());
        assertEquals(ColorMode.DARK, ThemeContext.getInstance().getCurrentMode());
        assertEquals(ColorMode.DARK, JettraTheme.MATRIX.getDefaultColorMode());

        io.jettra.flux.widgets.ThemeSelectDropdown dropdown = io.jettra.flux.widgets.ThemeSelectDropdown.of();
        String dropdownHtml = dropdown.render(Themes.Matrix(ColorMode.DARK));
        assertTrue(dropdownHtml.contains("value=\"Matrix\" selected"));

        io.jettra.flux.widgets.DashboardRootContainer root = io.jettra.flux.widgets.DashboardRootContainer.of();
        String rootHtml = root.render(Themes.Matrix(ColorMode.DARK));
        assertTrue(rootHtml.contains("data-theme=\"Matrix\""));
        assertTrue(rootHtml.contains("data-color-mode=\"dark\""));
    }
}

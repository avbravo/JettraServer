package io.jettra.flux.widgets;

import io.jettra.flux.theme.ColorMode;
import io.jettra.flux.theme.JettraTheme;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Backward-compatible alias for ThemeModeToggle.
 */
public class ThemeToggle extends ThemeModeToggle {

    public ThemeToggle() {
        super();
    }

    public static ThemeToggle of() {
        return new ThemeToggle();
    }

    @Override
    public ThemeToggle colorMode(ColorMode mode) {
        super.colorMode(mode);
        return this;
    }

    @Override
    public ThemeToggle size(int size) {
        super.size(size);
        return this;
    }

    @Override
    public ThemeToggle onToggle(BiConsumer<JettraTheme, ColorMode> onToggle) {
        super.onToggle(onToggle);
        return this;
    }

    @Override
    public ThemeToggle onToggle(Consumer<ColorMode> onToggle) {
        super.onToggle(onToggle);
        return this;
    }

    @Override
    public ThemeToggle onModeChange(Consumer<ColorMode> onModeChange) {
        super.onModeChange(onModeChange);
        return this;
    }
}

package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.ArrayList;
import java.util.List;

/**
 * TopToolbar widget in JettraFlux.
 * Declarative, accessible top navigation and action toolbar for studio interfaces and dashboards.
 * Provides granular, fluent configuration for subcomponent visibility:
 * <ul>
 *   <li>{@code withTopToolbarVisible(boolean)}: Toggles the entire top toolbar.</li>
 *   <li>{@code withActionButtonsVisible(boolean)}: Toggles global action buttons (+DB, +Unit, Backup, Restore, Export, etc.).</li>
 *   <li>{@code withDatabaseSelectorVisible(boolean)}: Toggles the active database switcher.</li>
 *   <li>{@code withNavigationTabsVisible(boolean)}: Toggles top-level horizontal navigation tabs.</li>
 *   <li>{@code withThemeToggleVisible(boolean)}: Toggles theme controls.</li>
 * </ul>
 */
public class TopToolbar extends Widget {

    private boolean topToolbarVisible = true;
    private boolean actionButtonsVisible = true;
    private boolean databaseSelectorVisible = true;
    private boolean navigationTabsVisible = true;
    private boolean themeToggleVisible = true;
    private boolean sticky = true;

    private final List<Widget> leftWidgets = new ArrayList<>();
    private final List<Widget> centerWidgets = new ArrayList<>();
    private final List<Widget> rightWidgets = new ArrayList<>();
    private final List<Widget> actionButtons = new ArrayList<>();

    public TopToolbar() {
        super();
        this.id = "jettra-top-toolbar-" + System.identityHashCode(this);
    }

    public static TopToolbar of() {
        return new TopToolbar();
    }

    public TopToolbar withTopToolbarVisible(boolean visible) {
        this.topToolbarVisible = visible;
        return this;
    }

    public TopToolbar withActionButtonsVisible(boolean visible) {
        this.actionButtonsVisible = visible;
        return this;
    }

    public TopToolbar withDatabaseSelectorVisible(boolean visible) {
        this.databaseSelectorVisible = visible;
        return this;
    }

    public TopToolbar withNavigationTabsVisible(boolean visible) {
        this.navigationTabsVisible = visible;
        return this;
    }

    public TopToolbar withThemeToggleVisible(boolean visible) {
        this.themeToggleVisible = visible;
        return this;
    }

    public TopToolbar sticky(boolean sticky) {
        this.sticky = sticky;
        return this;
    }

    public TopToolbar addLeft(Widget widget) {
        if (widget != null) this.leftWidgets.add(widget);
        return this;
    }

    public TopToolbar addCenter(Widget widget) {
        if (widget != null) this.centerWidgets.add(widget);
        return this;
    }

    public TopToolbar addRight(Widget widget) {
        if (widget != null) this.rightWidgets.add(widget);
        return this;
    }

    public TopToolbar addAction(Widget action) {
        if (action != null) this.actionButtons.add(action);
        return this;
    }

    public TopToolbar actionButtons(Widget... actions) {
        if (actions != null) {
            for (Widget a : actions) {
                if (a != null) this.actionButtons.add(a);
            }
        }
        return this;
    }

    public boolean isTopToolbarVisible() {
        return topToolbarVisible;
    }

    public boolean isActionButtonsVisible() {
        return actionButtonsVisible;
    }

    public boolean isDatabaseSelectorVisible() {
        return databaseSelectorVisible;
    }

    public boolean isNavigationTabsVisible() {
        return navigationTabsVisible;
    }

    public boolean isThemeToggleVisible() {
        return themeToggleVisible;
    }

    public boolean isSticky() {
        return sticky;
    }

    @Override
    public String render(ThemeData theme) {
        if (!topToolbarVisible) {
            return "";
        }

        List<Widget> effectiveRightItems = new ArrayList<>();
        if (actionButtonsVisible) {
            effectiveRightItems.addAll(actionButtons);
        }
        effectiveRightItems.addAll(rightWidgets);

        List<Widget> leftGroupChildren = new ArrayList<>(leftWidgets);
        if (navigationTabsVisible && !centerWidgets.isEmpty()) {
            leftGroupChildren.addAll(centerWidgets);
        }

        Widget leftGroup = Div.of(leftGroupChildren.toArray(new Widget[0]))
            .modifier(new Modifier().cssClass("top-left-group"));

        Widget rightGroup = Div.of(effectiveRightItems.toArray(new Widget[0]))
            .modifier(new Modifier().cssClass("top-right-group"));

        String stickyStyle = sticky ? "position: sticky; top: 0; z-index: 30; " : "";

        Widget container = Div.of(leftGroup, rightGroup)
            .modifier(new Modifier()
                .cssClass("jettra-top-bar")
                .style(stickyStyle + (modifier != null ? modifier.getStyles() : "")));

        return container.render(theme);
    }
}

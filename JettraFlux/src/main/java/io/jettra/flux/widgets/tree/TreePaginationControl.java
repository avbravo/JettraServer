package io.jettra.flux.widgets.tree;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import io.jettra.flux.widgets.Badge;
import io.jettra.flux.widgets.Button;
import io.jettra.flux.widgets.Div;
import io.jettra.flux.widgets.Icon;
import io.jettra.flux.widgets.Span;
import io.jettra.flux.widgets.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Composite Tree Pagination Control for JettraFlux (Composite Pattern).
 * Renders an interactive, per-node paginator within hierarchical Tree View nodes,
 * preventing memory overload in client DOM and JVM when rendering large datasets.
 */
public class TreePaginationControl extends Widget {

    private final String unitKey;
    private final int currentPage;
    private final int pageSize;
    private final int totalItems;
    private final int totalPages;
    private String baseUrl = "";
    private String customJsHandler = null;

    public TreePaginationControl(String unitKey, int currentPage, int pageSize, int totalItems) {
        this.unitKey = Objects.requireNonNull(unitKey, "unitKey must not be null");
        this.currentPage = Math.max(1, currentPage);
        this.pageSize = Math.max(1, pageSize);
        this.totalItems = Math.max(0, totalItems);
        this.totalPages = Math.max(1, (int) Math.ceil((double) this.totalItems / this.pageSize));
    }

    public static TreePaginationControl of(String unitKey, int currentPage, int pageSize, int totalItems) {
        return new TreePaginationControl(unitKey, currentPage, pageSize, totalItems);
    }

    public TreePaginationControl baseUrl(String baseUrl) {
        this.baseUrl = baseUrl != null ? baseUrl : "";
        return this;
    }

    public TreePaginationControl customJsHandler(String customJsHandler) {
        this.customJsHandler = customJsHandler;
        return this;
    }

    public String getUnitKey() {
        return unitKey;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean hasPrevious() {
        return currentPage > 1;
    }

    public boolean hasNext() {
        return currentPage < totalPages;
    }

    /**
     * Builds the composite widget tree using strictly native JettraFlux components.
     */
    public Widget buildComposite() {
        int fromItem = totalItems == 0 ? 0 : (currentPage - 1) * pageSize + 1;
        int toItem = Math.min(currentPage * pageSize, totalItems);

        // Info segment (Left)
        Span infoSpan = Span.of(
            Icon.of("fas fa-layer-group").modifier(new Modifier().style("margin-right: 4px; color: var(--j-primary, #38bdf8);")),
            Text.of(" " + fromItem + "–" + toItem + " de " + String.format("%,d", totalItems)),
            Span.of(" ").modifier(new Modifier().style("margin: 0 4px;")),
            Badge.of("Pág. " + currentPage + "/" + totalPages, "info").size("sm")
        );
        infoSpan.modifier(new Modifier().style("display: inline-flex; align-items: center; font-size: 11px; color: var(--j-text-secondary, #94a3b8); font-weight: 500;"));

        // Navigation controls (Right)
        List<Widget> navControls = new ArrayList<>();

        // Previous button
        String prevAction = buildPageAction(currentPage - 1);
        Button prevBtn = Button.of(Icon.of("fas fa-chevron-left"));
        String baseBtnStyle = "display: inline-flex; align-items: center; justify-content: center; width: 22px; height: 22px; border-radius: 4px; border: 1px solid rgba(255,255,255,0.12); font-size: 10px; padding: 0; transition: all 0.15s ease;";
        Modifier prevMod = new Modifier()
            .attribute("type", "button")
            .attribute("title", "Página Anterior (" + (currentPage - 1) + ")");
        if (hasPrevious()) {
            prevMod.attribute("onclick", prevAction)
                   .style(baseBtnStyle + " background: rgba(56, 189, 248, 0.15); color: #38bdf8; cursor: pointer;");
        } else {
            prevMod.attribute("disabled", "disabled")
                   .style(baseBtnStyle + " background: rgba(255,255,255,0.03); color: rgba(255,255,255,0.2); cursor: not-allowed; opacity: 0.5;");
        }
        prevBtn.modifier(prevMod);
        navControls.add(prevBtn);

        // Spacer
        navControls.add(Span.of(" ").modifier(new Modifier().style("margin: 0 2px;")));

        // Page Badge / Jump
        Span pageIndicator = Span.of(Text.of(String.valueOf(currentPage)));
        pageIndicator.modifier(new Modifier().style("display: inline-flex; align-items: center; justify-content: center; min-width: 20px; height: 20px; font-size: 10.5px; font-weight: 700; color: #fff; background: rgba(56, 189, 248, 0.25); border: 1px solid rgba(56, 189, 248, 0.4); border-radius: 4px; padding: 0 4px;"));
        navControls.add(pageIndicator);

        // Spacer
        navControls.add(Span.of(" ").modifier(new Modifier().style("margin: 0 2px;")));

        // Next button
        String nextAction = buildPageAction(currentPage + 1);
        Button nextBtn = Button.of(Icon.of("fas fa-chevron-right"));
        Modifier nextMod = new Modifier()
            .attribute("type", "button")
            .attribute("title", "Página Siguiente (" + (currentPage + 1) + ")");
        if (hasNext()) {
            nextMod.attribute("onclick", nextAction)
                   .style(baseBtnStyle + " background: rgba(56, 189, 248, 0.15); color: #38bdf8; cursor: pointer;");
        } else {
            nextMod.attribute("disabled", "disabled")
                   .style(baseBtnStyle + " background: rgba(255,255,255,0.03); color: rgba(255,255,255,0.2); cursor: not-allowed; opacity: 0.5;");
        }
        nextBtn.modifier(nextMod);
        navControls.add(nextBtn);

        Div controlsDiv = Div.of(navControls.toArray(new Widget[0]));
        controlsDiv.modifier(new Modifier().style("display: inline-flex; align-items: center; gap: 4px;"));

        // Outer container
        Div container = Div.of(infoSpan, controlsDiv);
        container.modifier(new Modifier()
            .attribute("class", "tree-pagination-control")
            .attribute("data-unit", unitKey)
            .attribute("data-page", String.valueOf(currentPage))
            .attribute("data-total-pages", String.valueOf(totalPages))
            .style("display: flex; align-items: center; justify-content: space-between; padding: 5px 10px; margin: 4px 0 6px 18px; border-radius: 6px; background: rgba(15, 23, 42, 0.65); border: 1px solid rgba(56, 189, 248, 0.2); backdrop-filter: blur(8px); box-shadow: 0 2px 6px rgba(0,0,0,0.2);")
        );

        return container;
    }

    private String buildPageAction(int targetPage) {
        if (customJsHandler != null && !customJsHandler.isEmpty()) {
            return customJsHandler.replace("{page}", String.valueOf(targetPage)).replace("{unit}", unitKey);
        }
        String sep = baseUrl.contains("?") ? "&" : "?";
        return "window.location.href='" + baseUrl + sep + "tree_unit=" + unitKey + "&tree_page=" + targetPage + "'";
    }

    @Override
    public String render(ThemeData theme) {
        return buildComposite().render(theme);
    }
}

package io.jettra.server.example.flux;

import io.jettra.flux.core.FluxView;
import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.Themes;
import io.jettra.flux.widgets.Badge;
import io.jettra.flux.widgets.Button;
import io.jettra.flux.widgets.Card;
import io.jettra.flux.widgets.Column;
import io.jettra.flux.widgets.Datatable;
import io.jettra.flux.widgets.Div;
import io.jettra.flux.widgets.Form;
import io.jettra.flux.widgets.Header;
import io.jettra.flux.widgets.InputHidden;
import io.jettra.flux.widgets.Label;
import io.jettra.flux.widgets.Link;
import io.jettra.flux.widgets.Paragraph;
import io.jettra.flux.widgets.Row;
import io.jettra.flux.widgets.Text;
import io.jettra.flux.widgets.TextInput;
import io.jettra.server.example.model.Product;
import io.jettra.server.example.service.ProductService;
import io.jettra.server.example.util.Messages;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista de Catálogo Reactivo construida íntegramente en Java puro con componentes JettraFlux.
 * Ofrece la misma funcionalidad transaccional y visual que productos.xhtml bajo el
 * paradigma reactivo declarativo de JettraFlux, utilizando componentes oficiales y
 * etiquetas de messages.properties, sin usar StringBuilder ni concatenaciones manuales de HTML.
 */
public class CatalogoFlux extends FluxView {

    private final ProductService productService;
    private final List<Product> products;

    public CatalogoFlux() {
        this(null, new ArrayList<>());
    }

    public CatalogoFlux(ProductService productService) {
        this(productService, productService != null ? productService.listAll() : new ArrayList<>());
    }

    public CatalogoFlux(ProductService productService, List<Product> products) {
        this.productService = productService;
        this.products = products != null ? products : new ArrayList<>();
    }

    @Override
    public Widget build() {
        List<Product> currentProducts = productService != null ? productService.listAll() : this.products;
        int totalItems = currentProducts.size();
        int totalStock = currentProducts.stream().mapToInt(Product::getStock).sum();
        double totalValue = currentProducts.stream().mapToDouble(p -> p.getPrice() * p.getStock()).sum();

        // 1. Cabecera: Badge, Título, Subtítulo y Badge de Tecnología Reactiva
        Widget titleBadge = Badge.of(Messages.get("catalogo.flux.badge")).severity("primary")
                .modifier(new Modifier().style("margin-right: 0.5rem; font-weight: 700;"));
        Widget titleHeader = Header.of(2, Messages.get("catalogo.flux.title"))
                .modifier(new Modifier().style("margin: 0; font-size: 1.35rem; font-weight: 700;"));
        Widget subtitle = Paragraph.of(Messages.get("catalogo.flux.subtitle"))
                .modifier(new Modifier().style("margin: 0.35rem 0 0 0; font-size: 0.85rem; color: var(--text-muted);"));
        Widget techBadge = Badge.active("Java 25 Reactive DSL")
                .modifier(new Modifier().style("font-weight: 700; padding: 0.35rem 0.75rem; border-radius: 6px; font-size: 0.8rem;"));

        Widget headerLeft = Column.of(
                Row.of(titleBadge, titleHeader).modifier(new Modifier().style("display: flex; align-items: center; gap: 0.5rem;")),
                subtitle
        );

        Widget headerSection = Row.of(headerLeft, techBadge)
                .modifier(new Modifier().style("display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--border-subtle); padding-bottom: 1rem; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 0.5rem; width: 100%;"));

        // 2. Tarjetas KPI de Resumen del Inventario
        Widget cardTotal = Card.of(
                Column.of(
                        Text.of(Messages.get("catalogo.flux.summary.total")).modifier(new Modifier().cssClass("kpi-title")),
                        Text.of(String.valueOf(totalItems)).modifier(new Modifier().cssClass("kpi-value").style("color: var(--primary); font-size: 1.5rem; font-weight: 800; margin: 0.4rem 0;")),
                        Text.of(Messages.get("product.units") + " registradas en JPA").modifier(new Modifier().cssClass("kpi-sub"))
                )
        ).modifier(new Modifier().cssClass("ui-card kpi-card").style("background: var(--bg-subtle); padding: 1.2rem; border-radius: 10px; border: 1px solid var(--border-subtle);"));

        Widget cardStock = Card.of(
                Column.of(
                        Text.of(Messages.get("catalogo.flux.summary.stock")).modifier(new Modifier().cssClass("kpi-title")),
                        Text.of(String.valueOf(totalStock)).modifier(new Modifier().cssClass("kpi-value").style("color: var(--success); font-size: 1.5rem; font-weight: 800; margin: 0.4rem 0;")),
                        Text.of("Unidades físicas en almacén").modifier(new Modifier().cssClass("kpi-sub"))
                )
        ).modifier(new Modifier().cssClass("ui-card kpi-card").style("background: var(--bg-subtle); padding: 1.2rem; border-radius: 10px; border: 1px solid var(--border-subtle);"));

        Widget cardValue = Card.of(
                Column.of(
                        Text.of(Messages.get("catalogo.flux.summary.value")).modifier(new Modifier().cssClass("kpi-title")),
                        Text.of(String.format("$%.2f", totalValue)).modifier(new Modifier().cssClass("kpi-value").style("color: var(--warning); font-size: 1.5rem; font-weight: 800; margin: 0.4rem 0;")),
                        Text.of("Valoración en tiempo real").modifier(new Modifier().cssClass("kpi-sub"))
                )
        ).modifier(new Modifier().cssClass("ui-card kpi-card").style("background: var(--bg-subtle); padding: 1.2rem; border-radius: 10px; border: 1px solid var(--border-subtle);"));

        Widget kpiGrid = Div.of(cardTotal, cardStock, cardValue)
                .modifier(new Modifier().cssClass("kpi-grid").style("display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.25rem; margin-bottom: 1.5rem; width: 100%;"));

        // 3. Formulario Reactivo JettraFlux para Añadir Productos
        Widget formTitle = Header.of(4, Messages.get("catalogo.flux.form.title"))
                .modifier(new Modifier().style("margin: 0 0 0.75rem 0; font-size: 0.95rem; font-weight: 700; color: var(--text-main);"));

        Widget fieldId = Div.of(
                Label.of(Messages.get("product.code")).modifier(new Modifier().style("display:block; font-size:0.75rem; color:var(--text-muted); font-weight:600; margin-bottom:0.25rem;")),
                TextInput.builder().name("id").placeholder("PRD-10").required(true).modifier(new Modifier().cssClass("app-input")).build()
        ).modifier(new Modifier().style("flex: 1; min-width: 110px;"));

        Widget fieldName = Div.of(
                Label.of(Messages.get("product.name")).modifier(new Modifier().style("display:block; font-size:0.75rem; color:var(--text-muted); font-weight:600; margin-bottom:0.25rem;")),
                TextInput.builder().name("name").placeholder("Servidor Cloud Jettra").required(true).modifier(new Modifier().cssClass("app-input")).build()
        ).modifier(new Modifier().style("flex: 2; min-width: 180px;"));

        Widget fieldCategory = Div.of(
                Label.of(Messages.get("product.category")).modifier(new Modifier().style("display:block; font-size:0.75rem; color:var(--text-muted); font-weight:600; margin-bottom:0.25rem;")),
                TextInput.builder().name("category").placeholder("Hardware").value("Hardware").modifier(new Modifier().cssClass("app-input")).build()
        ).modifier(new Modifier().style("flex: 1.5; min-width: 130px;"));

        Widget fieldPrice = Div.of(
                Label.of(Messages.get("product.price") + " ($)").modifier(new Modifier().style("display:block; font-size:0.75rem; color:var(--text-muted); font-weight:600; margin-bottom:0.25rem;")),
                TextInput.builder().name("price").placeholder("1250.00").value("1250.00").inputType("number").attribute("step", "0.01").modifier(new Modifier().cssClass("app-input")).build()
        ).modifier(new Modifier().style("flex: 1; min-width: 100px;"));

        Widget fieldStock = Div.of(
                Label.of(Messages.get("product.stock")).modifier(new Modifier().style("display:block; font-size:0.75rem; color:var(--text-muted); font-weight:600; margin-bottom:0.25rem;")),
                TextInput.builder().name("stock").placeholder("10").value("10").inputType("number").modifier(new Modifier().cssClass("app-input")).build()
        ).modifier(new Modifier().style("flex: 1; min-width: 80px;"));

        Widget btnSubmit = Div.of(
                Button.of(Messages.get("catalogo.flux.btn.save"))
                        .attribute("type", "submit")
                        .modifier(new Modifier().cssClass("app-btn-primary"))
        ).modifier(new Modifier().style("align-self: flex-end;"));

        Widget addForm = Form.of(
                InputHidden.of("action", "save"),
                InputHidden.of("returnTo", "/dashboard.xhtml?view=catalogo-flux"),
                fieldId,
                fieldName,
                fieldCategory,
                fieldPrice,
                fieldStock,
                btnSubmit
        ).action("/productos.xhtml").method("POST").modifier(new Modifier().cssClass("responsive-form"));

        Widget formPanel = Card.of(
                Column.of(formTitle, addForm)
        ).modifier(new Modifier().cssClass("ui-panel").style("background: var(--bg-subtle); padding: 1.25rem; border-radius: 10px; border: 1px solid var(--border-subtle); margin-bottom: 1.5rem; width: 100%;"));

        // 4. Tabla de Datos Reactiva JettraFlux (Datatable)
        List<Widget> tableHeaders = List.of(
                Text.of(Messages.get("product.code")),
                Text.of(Messages.get("product.name")),
                Text.of(Messages.get("product.category")),
                Text.of(Messages.get("product.price")),
                Text.of(Messages.get("product.stock")),
                Text.of(Messages.get("product.actions")).modifier(new Modifier().style("text-align: right;"))
        );

        List<List<Widget>> tableRows = new ArrayList<>();
        for (Product p : currentProducts) {
            Widget deleteButton = Button.of(Messages.get("product.action.delete"))
                    .attribute("type", "submit")
                    .modifier(new Modifier().cssClass("app-btn-danger").style("padding: 0.25rem 0.6rem; font-size: 0.8rem;"));

            Widget deleteForm = Form.of(
                    InputHidden.of("action", "delete"),
                    InputHidden.of("id", p.getId()),
                    InputHidden.of("returnTo", "/dashboard.xhtml?view=catalogo-flux"),
                    deleteButton
            ).action("/productos.xhtml").method("POST").modifier(new Modifier().style("display: inline; text-align: right;"));

            List<Widget> rowCells = List.of(
                    Text.of(p.getId()).modifier(new Modifier().style("font-weight: 700; color: var(--primary);")),
                    Text.of(p.getName()).modifier(new Modifier().style("font-weight: 500; color: var(--text-main);")),
                    Badge.neutral(p.getCategory()),
                    Text.of(String.format("$%.2f", p.getPrice())).modifier(new Modifier().style("color: var(--success); font-weight: 700;")),
                    Text.of(p.getStock() + " " + Messages.get("product.units")),
                    deleteForm
            );
            tableRows.add(rowCells);
        }

        Widget dataTable = Datatable.ofWidgets(tableHeaders, tableRows)
                .modifier(new Modifier().cssClass("app-table").style("width: 100%; border-collapse: collapse;"));

        Widget tableContainer = Div.of(dataTable)
                .modifier(new Modifier().cssClass("table-container ui-card").style("border: 1px solid var(--border-subtle); border-radius: 10px; overflow: hidden; margin-bottom: 1.5rem; width: 100%;"));

        // 5. Enlaces y Botones de Navegación
        Widget linkDashboard = Link.of("/dashboard.xhtml", "Volver al Dashboard General")
                .modifier(new Modifier().cssClass("app-btn-secondary").style("text-decoration:none;"));
        Widget linkProducts = Link.of("/productos.xhtml", "Ver Catálogo PrimeFaces (.xhtml)")
                .modifier(new Modifier().cssClass("app-btn-secondary").style("text-decoration:none;"));
        Widget linkApi = Link.of("/api/productos", "Ver API RESTful (JSON)")
                .target("_blank")
                .modifier(new Modifier().cssClass("app-btn-secondary").style("text-decoration:none;"));

        Widget footerNav = Row.of(linkDashboard, linkProducts, linkApi)
                .modifier(new Modifier().style("display: flex; gap: 0.75rem; flex-wrap: wrap; margin-top: 1rem;"));

        // Árbol raíz compuesto exclusivamente por Widgets JettraFlux
        return Column.of(
                headerSection,
                kpiGrid,
                formPanel,
                tableContainer,
                footerNav
        ).modifier(new Modifier().style("width: 100%; display: flex; flex-direction: column; gap: 0.5rem;"));
    }

    /**
     * Renderizado HTML nativo a través del motor de componentes JettraFlux.
     * Cero uso de concatenación manual o StringBuilder en esta clase.
     */
    public String renderSectionHtml() {
        return build().render(Themes.Core());
    }

    public void renderSectionHtml(PrintWriter out) {
        if (out != null) {
            out.println(renderSectionHtml());
        }
    }
}

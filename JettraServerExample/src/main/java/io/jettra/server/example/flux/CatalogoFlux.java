package io.jettra.server.example.flux;

import io.jettra.flux.core.FluxView;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import io.jettra.flux.widgets.Button;
import io.jettra.flux.widgets.Div;
import io.jettra.flux.widgets.Text;
import io.jettra.server.example.model.Product;
import io.jettra.server.example.service.ProductService;
import io.jettra.server.example.util.Messages;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista de Catálogo Reactivo construida íntegramente en Java puro con JettraFlux.
 * Ofrece la misma funcionalidad transaccional y visual que productos.xhtml pero bajo el
 * paradigma reactivo declarativo de JettraFlux, utilizando etiquetas de messages.properties.
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
        List<Widget> children = new ArrayList<>();

        // 1. Título y subtítulo
        children.add(Div.of(
            Text.of(Messages.get("catalogo.flux.title")),
            Text.of(" - " + Messages.get("catalogo.flux.subtitle"))
        ));

        // 2. Resumen KPI
        int totalItems = products.size();
        int totalStock = products.stream().mapToInt(Product::getStock).sum();
        double totalValue = products.stream().mapToDouble(p -> p.getPrice() * p.getStock()).sum();

        children.add(Div.of(
            Text.of(Messages.get("catalogo.flux.summary.total") + " " + totalItems),
            Text.of(" | " + Messages.get("catalogo.flux.summary.stock") + " " + totalStock),
            Text.of(" | " + Messages.get("catalogo.flux.summary.value") + " $" + String.format("%.2f", totalValue))
        ));

        // 3. Botones de acción declarativos
        children.add(Div.of(
            Button.of(Messages.get("catalogo.flux.btn.save")),
            Button.of(Messages.get("catalogo.flux.btn.refresh"))
        ));

        return Div.of(children.toArray(new Widget[0]));
    }

    /**
     * Renderizado HTML nativo y responsivo de alta fidelidad para la sección central del Dashboard.
     */
    public void renderSectionHtml(java.io.PrintWriter out) {
        if (out != null) {
            out.println(renderSectionHtml());
        }
    }

    public String renderSectionHtml() {
        List<Product> currentProducts = productService != null ? productService.listAll() : this.products;
        int totalItems = currentProducts.size();
        int totalStock = currentProducts.stream().mapToInt(Product::getStock).sum();
        double totalValue = currentProducts.stream().mapToDouble(p -> p.getPrice() * p.getStock()).sum();

        StringBuilder sb = new StringBuilder();

        // Cabecera del Catálogo Flux
        sb.append("        <div style=\"display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--border-subtle); padding-bottom: 1rem; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 0.5rem;\">\n");
        sb.append("          <div>\n");
        sb.append("            <div style=\"display: flex; align-items: center; gap: 0.5rem;\">\n");
        sb.append("              <span style=\"background: var(--primary); color: white; padding: 0.2rem 0.55rem; border-radius: 6px; font-size: 0.75rem; font-weight: 700;\">")
          .append(escapeHtml(Messages.get("catalogo.flux.badge"))).append("</span>\n");
        sb.append("              <h2 style=\"margin: 0; font-size: 1.35rem; color: var(--text-main); font-weight: 700;\">")
          .append(escapeHtml(Messages.get("catalogo.flux.title"))).append("</h2>\n");
        sb.append("            </div>\n");
        sb.append("            <p style=\"margin: 0.35rem 0 0 0; font-size: 0.85rem; color: var(--text-muted);\">")
          .append(escapeHtml(Messages.get("catalogo.flux.subtitle"))).append("</p>\n");
        sb.append("          </div>\n");
        sb.append("          <span style=\"background: var(--primary-light); color: var(--primary); font-weight: 700; padding: 0.35rem 0.75rem; border-radius: 6px; font-size: 0.8rem;\">Java 25 Reactive DSL</span>\n");
        sb.append("        </div>\n");

        // Tarjetas KPI de Resumen del Inventario
        sb.append("        <div class=\"kpi-grid\" style=\"margin-bottom: 1.5rem;\">\n");
        sb.append("          <div class=\"ui-card\" style=\"background: var(--bg-subtle); padding: 1.2rem; border-radius: 10px; border: 1px solid var(--border-subtle);\">\n");
        sb.append("            <div class=\"kpi-title\">").append(escapeHtml(Messages.get("catalogo.flux.summary.total"))).append("</div>\n");
        sb.append("            <div class=\"kpi-value\" style=\"color: var(--primary);\">").append(totalItems).append("</div>\n");
        sb.append("            <div class=\"kpi-sub\">").append(escapeHtml(Messages.get("product.units"))).append(" registradas en JPA</div>\n");
        sb.append("          </div>\n");

        sb.append("          <div class=\"ui-card\" style=\"background: var(--bg-subtle); padding: 1.2rem; border-radius: 10px; border: 1px solid var(--border-subtle);\">\n");
        sb.append("            <div class=\"kpi-title\">").append(escapeHtml(Messages.get("catalogo.flux.summary.stock"))).append("</div>\n");
        sb.append("            <div class=\"kpi-value\" style=\"color: var(--success);\">").append(totalStock).append("</div>\n");
        sb.append("            <div class=\"kpi-sub\">Unidades físicas en almacén</div>\n");
        sb.append("          </div>\n");

        sb.append("          <div class=\"ui-card\" style=\"background: var(--bg-subtle); padding: 1.2rem; border-radius: 10px; border: 1px solid var(--border-subtle);\">\n");
        sb.append("            <div class=\"kpi-title\">").append(escapeHtml(Messages.get("catalogo.flux.summary.value"))).append("</div>\n");
        sb.append("            <div class=\"kpi-value\" style=\"color: var(--warning);\">$").append(String.format("%.2f", totalValue)).append("</div>\n");
        sb.append("            <div class=\"kpi-sub\">Valoración en tiempo real</div>\n");
        sb.append("          </div>\n");
        sb.append("        </div>\n");

        // Formulario Reactivo JettraFlux para Añadir Productos
        sb.append("        <div class=\"ui-panel\" style=\"background: var(--bg-subtle); padding: 1.25rem; border-radius: 10px; border: 1px solid var(--border-subtle); margin-bottom: 1.5rem;\">\n");
        sb.append("          <div style=\"font-size: 0.9rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.75rem;\">")
          .append(escapeHtml(Messages.get("catalogo.flux.form.title"))).append("</div>\n");
        sb.append("          <form method=\"post\" action=\"/productos.xhtml\" class=\"responsive-form\">\n");
        sb.append("            <input type=\"hidden\" name=\"action\" value=\"save\" />\n");
        sb.append("            <input type=\"hidden\" name=\"returnTo\" value=\"/dashboard.xhtml?view=catalogo-flux\" />\n");
        sb.append("            <div style=\"flex: 1; min-width: 110px;\"><label style=\"display:block; font-size:0.75rem; color:var(--text-muted); font-weight:600; margin-bottom:0.25rem;\">")
          .append(escapeHtml(Messages.get("product.code"))).append("</label><input type=\"text\" name=\"id\" required class=\"app-input\" placeholder=\"PRD-10\" /></div>\n");
        sb.append("            <div style=\"flex: 2; min-width: 180px;\"><label style=\"display:block; font-size:0.75rem; color:var(--text-muted); font-weight:600; margin-bottom:0.25rem;\">")
          .append(escapeHtml(Messages.get("product.name"))).append("</label><input type=\"text\" name=\"name\" required class=\"app-input\" placeholder=\"Servidor Cloud Jettra\" /></div>\n");
        sb.append("            <div style=\"flex: 1.5; min-width: 130px;\"><label style=\"display:block; font-size:0.75rem; color:var(--text-muted); font-weight:600; margin-bottom:0.25rem;\">")
          .append(escapeHtml(Messages.get("product.category"))).append("</label><input type=\"text\" name=\"category\" value=\"Hardware\" class=\"app-input\" /></div>\n");
        sb.append("            <div style=\"flex: 1; min-width: 100px;\"><label style=\"display:block; font-size:0.75rem; color:var(--text-muted); font-weight:600; margin-bottom:0.25rem;\">")
          .append(escapeHtml(Messages.get("product.price"))).append("</label><input type=\"number\" step=\"0.01\" name=\"price\" value=\"1250.00\" class=\"app-input\" /></div>\n");
        sb.append("            <div style=\"flex: 1; min-width: 80px;\"><label style=\"display:block; font-size:0.75rem; color:var(--text-muted); font-weight:600; margin-bottom:0.25rem;\">")
          .append(escapeHtml(Messages.get("product.stock"))).append("</label><input type=\"number\" name=\"stock\" value=\"10\" class=\"app-input\" /></div>\n");
        sb.append("            <div style=\"align-self: flex-end;\"><button type=\"submit\" class=\"app-btn-primary\">")
          .append(escapeHtml(Messages.get("catalogo.flux.btn.save"))).append("</button></div>\n");
        sb.append("          </form>\n");
        sb.append("        </div>\n");

        // Tabla de Datos Reactiva JettraFlux
        sb.append("        <div class=\"table-container ui-card\" style=\"border: 1px solid var(--border-subtle); border-radius: 10px; overflow: hidden; margin-bottom: 1.5rem;\">\n");
        sb.append("          <table class=\"app-table\">\n");
        sb.append("            <thead>\n");
        sb.append("              <tr>\n");
        sb.append("                <th>").append(escapeHtml(Messages.get("product.code"))).append("</th>\n");
        sb.append("                <th>").append(escapeHtml(Messages.get("product.name"))).append("</th>\n");
        sb.append("                <th>").append(escapeHtml(Messages.get("product.category"))).append("</th>\n");
        sb.append("                <th>").append(escapeHtml(Messages.get("product.price"))).append("</th>\n");
        sb.append("                <th>").append(escapeHtml(Messages.get("product.stock"))).append("</th>\n");
        sb.append("                <th style=\"text-align: right;\">").append(escapeHtml(Messages.get("product.actions"))).append("</th>\n");
        sb.append("              </tr>\n");
        sb.append("            </thead>\n");
        sb.append("            <tbody>\n");

        if (currentProducts.isEmpty()) {
            sb.append("              <tr><td colspan=\"6\" style=\"text-align:center; padding: 2rem; color: var(--text-muted);\">")
              .append(escapeHtml(Messages.get("catalogo.flux.empty"))).append("</td></tr>\n");
        } else {
            for (Product p : currentProducts) {
                sb.append("              <tr>\n");
                sb.append("                <td style=\"font-weight: 700; color: var(--primary);\">").append(escapeHtml(p.getId())).append("</td>\n");
                sb.append("                <td style=\"font-weight: 500; color: var(--text-main);\">").append(escapeHtml(p.getName())).append("</td>\n");
                sb.append("                <td><span style=\"background: var(--bg-subtle); color: var(--text-muted); padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.8rem; border: 1px solid var(--border-subtle);\">")
                  .append(escapeHtml(p.getCategory())).append("</span></td>\n");
                sb.append("                <td style=\"color: var(--success); font-weight: 700;\">$").append(String.format("%.2f", p.getPrice())).append("</td>\n");
                sb.append("                <td style=\"color: var(--text-main);\">").append(p.getStock()).append(" ").append(escapeHtml(Messages.get("product.units"))).append("</td>\n");
                sb.append("                <td style=\"text-align: right;\">\n");
                sb.append("                  <form method=\"post\" action=\"/productos.xhtml\" style=\"display: inline;\">\n");
                sb.append("                    <input type=\"hidden\" name=\"action\" value=\"delete\" />\n");
                sb.append("                    <input type=\"hidden\" name=\"id\" value=\"").append(escapeHtml(p.getId())).append("\" />\n");
                sb.append("                    <input type=\"hidden\" name=\"returnTo\" value=\"/dashboard.xhtml?view=catalogo-flux\" />\n");
                sb.append("                    <button type=\"submit\" class=\"app-btn-danger\" style=\"padding: 0.25rem 0.6rem; font-size: 0.8rem;\">")
                  .append(escapeHtml(Messages.get("product.action.delete"))).append("</button>\n");
                sb.append("                  </form>\n");
                sb.append("                </td>\n");
                sb.append("              </tr>\n");
            }
        }

        sb.append("            </tbody>\n");
        sb.append("          </table>\n");
        sb.append("        </div>\n");

        // Botones inferiores de navegación
        sb.append("        <div style=\"display: flex; gap: 0.75rem; flex-wrap: wrap;\">\n");
        sb.append("          <a href=\"/dashboard.xhtml\" class=\"app-btn-secondary\" style=\"text-decoration:none;\">Volver al Dashboard General</a>\n");
        sb.append("          <a href=\"/productos.xhtml\" class=\"app-btn-secondary\" style=\"text-decoration:none;\">Ver Catálogo PrimeFaces (.xhtml)</a>\n");
        sb.append("          <a href=\"/api/productos\" target=\"_blank\" class=\"app-btn-secondary\" style=\"text-decoration:none;\">Ver API RESTful (JSON)</a>\n");
        sb.append("        </div>\n");

        return sb.toString();
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}

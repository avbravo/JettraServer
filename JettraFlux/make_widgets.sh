#!/bin/bash
DIR="/home/avbravo/NetBeansProjects/jettrastack_local/JettraWorkspace/FolderServer/JettraFlux/src/main/java/io/jettra/flux/widgets"
mkdir -p "$DIR"

# Galleria
cat << 'INNER_EOF' > "$DIR/Galleria.java"
package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;
public class Galleria extends Widget {
    private final List<String> images;
    private Galleria(List<String> images) { this.images = images; }
    public static Galleria of(String... images) { return new Galleria(Arrays.asList(images)); }
    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, "galleria-container")).append(" style=\"display:flex; flex-wrap:wrap; gap:10px;\">");
        for (String img : images) sb.append("<img src=\"").append(img).append("\" style=\"width:100px; height:100px; object-fit:cover; border-radius:4px;\" />");
        sb.append("</div>");
        return sb.toString();
    }
}
INNER_EOF

# ImageCompare
cat << 'INNER_EOF' > "$DIR/ImageCompare.java"
package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
public class ImageCompare extends Widget {
    private final String img1, img2;
    private ImageCompare(String img1, String img2) { this.img1 = img1; this.img2 = img2; }
    public static ImageCompare of(String img1, String img2) { return new ImageCompare(img1, img2); }
    @Override public String render(ThemeData theme) {
        return "<div " + renderCommonAttributes(theme, "image-compare") + " style=\"display:flex; gap:10px;\"><img src=\"" + img1 + "\" style=\"width:200px; height:200px; object-fit:cover;\" /><img src=\"" + img2 + "\" style=\"width:200px; height:200px; object-fit:cover;\" /></div>";
    }
}
INNER_EOF

# Breadcrumb
cat << 'INNER_EOF' > "$DIR/Breadcrumb.java"
package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;
public class Breadcrumb extends Widget {
    private final List<String> items;
    private Breadcrumb(List<String> items) { this.items = items; }
    public static Breadcrumb of(String... items) { return new Breadcrumb(Arrays.asList(items)); }
    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder("<nav ").append(renderCommonAttributes(theme, "breadcrumb-nav")).append("><ol style=\"list-style:none; padding:0; display:flex; gap:8px; margin:0;\">");
        for (int i = 0; i < items.size(); i++) {
            sb.append("<li style=\"color: var(--primary-color);\">").append(items.get(i)).append("</li>");
            if (i < items.size() - 1) sb.append("<li>/</li>");
        }
        sb.append("</ol></nav>");
        return sb.toString();
    }
}
INNER_EOF

# Steps
cat << 'INNER_EOF' > "$DIR/Steps.java"
package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;
public class Steps extends Widget {
    private final List<String> items;
    private int activeIndex = 0;
    private Steps(List<String> items) { this.items = items; }
    public static Steps of(String... items) { return new Steps(Arrays.asList(items)); }
    public Steps activeIndex(int index) { this.activeIndex = index; return this; }
    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder("<div ").append(renderCommonAttributes(theme, "steps-container")).append(" style=\"display:flex; justify-content:space-between;\">");
        for (int i = 0; i < items.size(); i++) {
            String color = (i <= activeIndex) ? "var(--primary-color)" : "var(--surface-alt, #ccc)";
            sb.append("<div style=\"display:flex; flex-direction:column; align-items:center; flex:1;\"><div style=\"width:30px; height:30px; border-radius:50%; background:").append(color).append("; color:white; display:flex; align-items:center; justify-content:center;\">").append(i+1).append("</div><span style=\"margin-top:8px; font-weight:bold; color:").append(color).append(";\">").append(items.get(i)).append("</span></div>");
        }
        sb.append("</div>");
        return sb.toString();
    }
}
INNER_EOF

# Menus (Menubar, TieredMenu, PlainMenu, OverlayMenu, MegaMenu, ContextMenu)
for MENU in Menubar TieredMenu PlainMenu OverlayMenu MegaMenu ContextMenu; do
cat << INNER_EOF > "$DIR/$MENU.java"
package io.jettra.flux.widgets;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;
public class $MENU extends Widget {
    private final List<WidgetLet> items;
    private $MENU(List<WidgetLet> items) { this.items = items; }
    public static $MENU of(WidgetLet... items) { return new $MENU(Arrays.asList(items)); }
    @Override public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder("<div ").append(renderCommonAttributes(theme, "menu-$MENU")).append(" style=\"display:flex; gap:15px; background:var(--surface-color); padding:10px; border-radius:8px; border:1px solid rgba(128,128,128,0.2);\">");
        for (WidgetLet item : items) {
            sb.append(item.render(theme));
        }
        sb.append("</div>");
        return sb.toString();
    }
}
INNER_EOF
done


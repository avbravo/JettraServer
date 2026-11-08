package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class Accordion extends Widget {
    private final List<AccordionTab> tabs;

    private Accordion(List<AccordionTab> tabs) {
        this.tabs = tabs;
    }

    public static Accordion of(AccordionTab... tabs) {
        return new Accordion(Arrays.asList(tabs));
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-accordion")).append(">\n");
        for (AccordionTab tab : tabs) {
            sb.append(tab.render(theme));
        }
        sb.append("</div>\n");
        sb.append("<script>\n");
        sb.append("function toggleAccordion(el) {\n");
        sb.append("  var tab = el.parentElement;\n");
        sb.append("  var content = tab.querySelector('.espresso-accordion-content');\n");
        sb.append("  var icon = el.querySelector('.espresso-accordion-header-icon i');\n");
        sb.append("  if (content.style.display === 'block') {\n");
        sb.append("    content.style.display = 'none';\n");
        sb.append("    tab.classList.remove('espresso-accordion-tab-active');\n");
        sb.append("    if(icon) { icon.classList.remove('fa-chevron-down'); icon.classList.add('fa-chevron-right'); }\n");
        sb.append("  } else {\n");
        sb.append("    content.style.display = 'block';\n");
        sb.append("    tab.classList.add('espresso-accordion-tab-active');\n");
        sb.append("    if(icon) { icon.classList.remove('fa-chevron-right'); icon.classList.add('fa-chevron-down'); }\n");
        sb.append("  }\n");
        sb.append("}\n");
        sb.append("</script>\n");
        return sb.toString();
    }
}

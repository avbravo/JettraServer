package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Modern, responsive application footer component for JettraFlux layouts.
 * Encapsulates semantic HTML5 &lt;footer&gt; tags, slots for left/center/right content,
 * dynamic copyright year resolution, and live system status indicators.
 */
public class AppFooter extends Widget {

    private String appName = "JettraDB";
    private String version = "1.0.0";
    private int copyrightYear = Year.now().getValue();
    private String copyrightHolder = "JettraStack";
    private String connectionStatus = "Online";
    private String statusColor = "#10b981"; // Emerald green
    private final List<FooterLink> links = new ArrayList<>();

    private List<Widget> customLeftSlot = null;
    private List<Widget> customCenterSlot = null;
    private List<Widget> customRightSlot = null;

    public AppFooter() {}

    public AppFooter(FooterMetadata metadata) {
        if (metadata != null) {
            this.appName = metadata.appName();
            this.version = metadata.version();
            this.copyrightYear = metadata.copyrightYear();
            this.copyrightHolder = metadata.copyrightHolder();
            this.connectionStatus = metadata.connectionStatus();
            this.links.addAll(metadata.links());
        }
    }

    public static AppFooter of() {
        return new AppFooter();
    }

    public static AppFooter of(FooterMetadata metadata) {
        return new AppFooter(metadata);
    }

    public AppFooter appName(String appName) {
        if (appName != null && !appName.isBlank()) this.appName = appName;
        return this;
    }

    public AppFooter version(String version) {
        if (version != null && !version.isBlank()) this.version = version;
        return this;
    }

    public AppFooter copyright(String holder) {
        if (holder != null && !holder.isBlank()) this.copyrightHolder = holder;
        return this;
    }

    public AppFooter copyrightYear(int year) {
        if (year > 0) this.copyrightYear = year;
        return this;
    }

    public AppFooter status(String status) {
        if (status != null && !status.isBlank()) this.connectionStatus = status;
        return this;
    }

    public AppFooter status(String status, String color) {
        if (status != null && !status.isBlank()) this.connectionStatus = status;
        if (color != null && !color.isBlank()) this.statusColor = color;
        return this;
    }

    public AppFooter link(String text, String href) {
        this.links.add(FooterLink.of(text, href));
        return this;
    }

    public AppFooter link(String text, String href, String icon) {
        this.links.add(FooterLink.of(text, href, icon));
        return this;
    }

    public AppFooter links(List<FooterLink> links) {
        if (links != null) {
            this.links.clear();
            this.links.addAll(links);
        }
        return this;
    }

    public AppFooter leftSlot(Widget... widgets) {
        this.customLeftSlot = Arrays.asList(widgets);
        return this;
    }

    public AppFooter centerSlot(Widget... widgets) {
        this.customCenterSlot = Arrays.asList(widgets);
        return this;
    }

    public AppFooter rightSlot(Widget... widgets) {
        this.customRightSlot = Arrays.asList(widgets);
        return this;
    }

    public String getAppName() {
        return appName;
    }

    public String getVersion() {
        return version;
    }

    public int getCopyrightYear() {
        return copyrightYear;
    }

    public String getCopyrightHolder() {
        return copyrightHolder;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public List<FooterLink> getLinks() {
        return List.copyOf(links);
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<footer ")
          .append(renderCommonAttributes(theme, "jettra-app-footer espresso-footer"))
          .append(" style=\"display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px; padding:12px 24px; min-height:48px; border-top:1px solid var(--j-border, var(--jf-border, #e2e8f0)); background:var(--j-bg-surface, var(--jf-surface, #ffffff)); color:var(--j-text-secondary, var(--jf-text-secondary, #64748b)); font-size:12px; font-family:'Inter', -apple-system, sans-serif; box-sizing:border-box; width:100%; flex-shrink:0;\">\n");

        // 1. Left Section: Branding & Copyright
        sb.append("  <div class=\"footer-left-slot\" style=\"display:flex; align-items:center; gap:8px; flex-wrap:wrap;\">\n");
        if (customLeftSlot != null && !customLeftSlot.isEmpty()) {
            for (Widget w : customLeftSlot) {
                sb.append("    ").append(w.render(theme)).append("\n");
            }
        } else {
            sb.append("    <span style=\"font-weight:700; color:var(--j-text-primary, var(--jf-text-primary, #0f172a));\">")
              .append(escape(appName)).append("</span>\n");
            sb.append("    <span style=\"opacity:0.6;\">•</span>\n");
            sb.append("    <span>&copy; ").append(copyrightYear).append(" ").append(escape(copyrightHolder))
              .append(". All rights reserved.</span>\n");
        }
        sb.append("  </div>\n");

        // 2. Center Section: Navigation & Support Links
        sb.append("  <div class=\"footer-center-slot\" style=\"display:flex; align-items:center; gap:16px; flex-wrap:wrap;\">\n");
        if (customCenterSlot != null && !customCenterSlot.isEmpty()) {
            for (Widget w : customCenterSlot) {
                sb.append("    ").append(w.render(theme)).append("\n");
            }
        } else {
            for (FooterLink link : links) {
                String targetAttr = link.target().isEmpty() ? "" : " target=\"" + escape(link.target()) + "\" rel=\"noopener noreferrer\"";
                sb.append("    <a href=\"").append(escape(link.href())).append("\"").append(targetAttr)
                  .append(" style=\"color:var(--j-text-secondary, var(--jf-text-secondary, #64748b)); text-decoration:none; display:inline-flex; align-items:center; gap:5px; transition:color 0.15s ease;\" onmouseover=\"this.style.color='var(--j-primary, var(--jf-accent, #0284c7))'\" onmouseout=\"this.style.color='var(--j-text-secondary, var(--jf-text-secondary, #64748b))'\">");
                if (!link.icon().isEmpty()) {
                    sb.append("<i class=\"").append(escape(link.icon())).append("\" style=\"font-size:11px;\"></i>");
                }
                sb.append("<span>").append(escape(link.text())).append("</span></a>\n");
            }
        }
        sb.append("  </div>\n");

        // 3. Right Section: Connection Status & Version Badges
        sb.append("  <div class=\"footer-right-slot\" style=\"display:flex; align-items:center; gap:10px; flex-wrap:wrap;\">\n");
        if (customRightSlot != null && !customRightSlot.isEmpty()) {
            for (Widget w : customRightSlot) {
                sb.append("    ").append(w.render(theme)).append("\n");
            }
        } else {
            if (!connectionStatus.isEmpty()) {
                sb.append("    <span class=\"footer-status-chip\" style=\"display:inline-flex; align-items:center; gap:5px; padding:2px 8px; border-radius:12px; font-size:11px; font-weight:600; background:rgba(16,185,129,0.1); color:").append(statusColor).append("; border:1px solid rgba(16,185,129,0.25);\">\n");
                sb.append("      <span style=\"width:6px; height:6px; border-radius:50%; background:").append(statusColor).append("; display:inline-block;\"></span>\n");
                sb.append("      ").append(escape(connectionStatus)).append("\n");
                sb.append("    </span>\n");
            }
            if (!version.isEmpty()) {
                sb.append("    <span class=\"footer-version-badge\" style=\"display:inline-flex; align-items:center; padding:2px 7px; border-radius:6px; font-size:11px; font-weight:600; background:var(--j-bg-subsurface, var(--jf-surface-hover, #f1f5f9)); color:var(--j-text-muted, var(--jf-text-secondary, #64748b)); border:1px solid var(--j-border, var(--jf-border, #e2e8f0));\">\n");
                sb.append("      ").append(escape(version)).append("\n");
                sb.append("    </span>\n");
            }
        }
        sb.append("  </div>\n");

        sb.append("</footer>\n");
        return sb.toString();
    }

    private String escape(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}

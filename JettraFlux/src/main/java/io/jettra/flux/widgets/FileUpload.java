package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Modern, accessible File Upload and Selection component for JettraFlux.
 * Supports:
 * - Native HTML5 file input selection with MIME/extension filtering
 * - Declarative label, placeholder, and event listener bindings
 * - Backward compatibility with widget child composition
 */
public class FileUpload extends Widget {
    private final List<Widget> children;
    private String name = "file";
    private String accept = "*/*";
    private String label = "";
    private String buttonText = "Choose File";
    private String placeholder = "No file chosen";
    private String onChangeJs;
    private boolean multiple = false;
    private String inputId;

    private FileUpload(List<Widget> children) {
        this.children = children != null ? new ArrayList<>(children) : new ArrayList<>();
    }

    public static FileUpload of(Widget... children) {
        return new FileUpload(Arrays.asList(children));
    }

    public static FileUpload of(String label, String url) {
        return new FileUpload(Arrays.asList(Text.of(label), Text.of(url)));
    }

    public static FileUpload of() {
        return new FileUpload(new ArrayList<>());
    }

    public static FileUpload of(String name) {
        FileUpload fu = new FileUpload(new ArrayList<>());
        fu.name = name;
        return fu;
    }

    public static FileUpload file(String name, String accept) {
        FileUpload fu = new FileUpload(new ArrayList<>());
        fu.name = name;
        fu.accept = accept;
        return fu;
    }

    public static FileUpload file(String name, String accept, String label) {
        FileUpload fu = new FileUpload(new ArrayList<>());
        fu.name = name;
        fu.accept = accept;
        fu.label = label;
        return fu;
    }

    public FileUpload name(String name) {
        this.name = name;
        return this;
    }

    public FileUpload accept(String accept) {
        this.accept = accept;
        return this;
    }

    public FileUpload label(String label) {
        this.label = label;
        return this;
    }

    public FileUpload buttonText(String buttonText) {
        this.buttonText = buttonText;
        return this;
    }

    public FileUpload placeholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public FileUpload onChange(String js) {
        this.onChangeJs = js;
        return this;
    }

    public FileUpload multiple(boolean multiple) {
        this.multiple = multiple;
        return this;
    }

    public FileUpload inputId(String inputId) {
        this.inputId = inputId;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        if (!children.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("<div ").append(renderCommonAttributes(theme, "espresso-fileupload")).append(">\n");
            for (Widget child : children) {
                sb.append(child.render(theme));
            }
            sb.append("</div>\n");
            return sb.toString();
        }

        String fieldId = inputId != null ? inputId : (modifier.getAttributes().get("id") != null ? modifier.getAttributes().get("id") : "fu_" + System.identityHashCode(this));
        String onchangeAttr = (onChangeJs != null && !onChangeJs.isBlank()) ? " onchange=\"" + onChangeJs.replace("\"", "&quot;") + "\"" : "";
        String multipleAttr = multiple ? " multiple" : "";
        String acceptAttr = (accept != null && !accept.isBlank()) ? " accept=\"" + accept.replace("\"", "&quot;") + "\"" : "";

        StringBuilder sb = new StringBuilder();
        sb.append("<div ").append(renderCommonAttributes(theme, "espresso-fileupload file-upload-wrapper")).append(" style=\"display:flex; flex-direction:column; gap:4px; width:100%;\">\n");

        if (label != null && !label.isBlank()) {
            sb.append("  <label for=\"").append(fieldId).append("\" class=\"espresso-label\" style=\"font-size:12px; font-weight:600; color:var(--j-text-muted,#94a3b8); margin-bottom:2px;\">")
              .append(label).append("</label>\n");
        }

        sb.append("  <div class=\"file-upload-input-group\" style=\"display:flex; align-items:center; gap:8px; background:var(--j-bg-surface,rgba(15,23,42,0.6)); border:1px solid var(--j-border,rgba(255,255,255,0.12)); border-radius:6px; padding:6px 10px;\">\n");
        sb.append("    <input type=\"file\" name=\"").append(name).append("\" id=\"").append(fieldId).append("\"")
          .append(acceptAttr).append(multipleAttr).append(onchangeAttr)
          .append(" style=\"font-size:12px; color:var(--j-text-primary,#f8fafc); width:100%; cursor:pointer;\" />\n");
        sb.append("  </div>\n");
        sb.append("</div>\n");

        return sb.toString();
    }
}

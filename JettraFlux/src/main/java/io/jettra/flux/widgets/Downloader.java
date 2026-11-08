package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.download.DownloadSecurity;
import io.jettra.flux.theme.ThemeData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Modern JettraFlux Downloader widget in Java 25+.
 * Wraps children widgets or creates dedicated download action buttons
 * that trigger seamless client-side file downloads without page reload.
 */
public class Downloader extends Widget {

    private final List<Widget> children;
    private String downloadUrl;
    private String fileName;

    private Downloader(List<Widget> children) {
        this.children = new ArrayList<>(children);
    }

    public static Downloader of(Widget... children) {
        return new Downloader(Arrays.asList(children));
    }

    public static Downloader of(String label, String downloadUrl, String fileName) {
        Downloader d = new Downloader(List.of(Text.of(label)));
        d.downloadUrl = downloadUrl;
        d.fileName = (fileName != null && !fileName.isBlank())
            ? DownloadSecurity.sanitizeFileName(fileName)
            : null;
        return d;
    }

    public Downloader downloadUrl(String url) {
        this.downloadUrl = url;
        return this;
    }

    public Downloader fileName(String fileName) {
        this.fileName = (fileName != null && !fileName.isBlank())
            ? DownloadSecurity.sanitizeFileName(fileName)
            : null;
        return this;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        if (downloadUrl != null && !downloadUrl.isBlank()) {
            sb.append("<a href=\"").append(downloadUrl).append("\" ");
            if (fileName != null && !fileName.isBlank()) {
                sb.append("download=\"").append(fileName).append("\" ");
            }
            sb.append(renderCommonAttributes(theme, "espresso-downloader j-btn j-btn-primary")).append(">\n");
            for (Widget child : children) {
                sb.append(child.render(theme));
            }
            sb.append("</a>\n");
        } else {
            sb.append("<div ").append(renderCommonAttributes(theme, "espresso-downloader")).append(">\n");
            for (Widget child : children) {
                sb.append(child.render(theme));
            }
            sb.append("</div>\n");
        }
        return sb.toString();
    }
}

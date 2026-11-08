package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.Objects;

/**
 * Reusable JettraFlux component representing an on-demand sample database catalog card.
 * Encapsulates radio selection, database metadata, engine type tags, dynamic status badges,
 * and lifecycle action controls (Install, Explore, Uninstall).
 */
public class SampleDatabaseCard extends Widget {

    /**
     * Java 25 Record encapsulating the sample database visual card state.
     */
    public record SampleDatabaseData(
        String databaseName,
        String engineType,
        String displayName,
        String description,
        int estimatedRecords,
        int recordCount,
        boolean installed,
        String icon,
        String radioName,
        String onInstallJs,
        String onExploreJs,
        String onUninstallJs
    ) {
        public SampleDatabaseData {
            databaseName = databaseName != null ? databaseName.trim() : "";
            engineType = engineType != null ? engineType.trim() : "DOCUMENT";
            displayName = displayName != null ? displayName.trim() : databaseName;
            description = description != null ? description.trim() : "";
            icon = icon != null && !icon.isBlank() ? icon.trim() : "fas fa-database";
            radioName = radioName != null && !radioName.isBlank() ? radioName.trim() : "target_db";
            onInstallJs = onInstallJs != null ? onInstallJs : "";
            onExploreJs = onExploreJs != null ? onExploreJs : "";
            onUninstallJs = onUninstallJs != null ? onUninstallJs : "";
        }
    }

    private final SampleDatabaseData data;

    public SampleDatabaseCard(SampleDatabaseData data) {
        this.data = Objects.requireNonNull(data, "SampleDatabaseData must not be null");
    }

    public static SampleDatabaseCard of(SampleDatabaseData data) {
        return new SampleDatabaseCard(data);
    }

    public static Builder builder() {
        return new Builder();
    }

    public SampleDatabaseData getData() {
        return data;
    }

    @Override
    public String render(ThemeData theme) {
        String dbName = data.databaseName();
        boolean isInstalled = data.installed();
        int count = data.recordCount() > 0 ? data.recordCount() : data.estimatedRecords();

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"sample-db-card ")
          .append(modifier != null ? modifier.getClasses() : "").append("\" ")
          .append("id=\"sample-card-").append(dbName).append("\" ")
          .append("style=\"display: flex; align-items: flex-start; justify-content: space-between; gap: 14px; ")
          .append("padding: 12px 14px; border-radius: 8px; margin-bottom: 8px; ")
          .append("background: rgba(30, 41, 59, 0.4); border: 1px solid rgba(255, 255, 255, 0.08); ")
          .append(modifier != null ? modifier.getStyles() : "").append("\">\n");

        // 1. Radio selector column
        sb.append("  <div style=\"display: flex; align-items: center; justify-content: center; padding-right: 8px; padding-top: 2px; flex-shrink: 0;\">\n");
        sb.append("    <input type=\"radio\" name=\"").append(data.radioName()).append("\" value=\"").append(dbName).append("\" ")
          .append("id=\"radio_sample_").append(dbName).append("\" ")
          .append("style=\"cursor: pointer; accent-color: #ec4899; width: 16px; height: 16px;\" />\n");
        sb.append("  </div>\n");

        // 2. Main content column (Title, Badges, Description)
        sb.append("  <div style=\"flex: 1; min-width: 0;\">\n");
        sb.append("    <div style=\"display: flex; align-items: center; margin-bottom: 4px; flex-wrap: wrap; gap: 8px;\">\n");
        
        // Icon
        sb.append("      <i class=\"").append(data.icon()).append("\" style=\"color: #ec4899; font-size: 14px;\"></i>\n");
        
        // Database Name
        sb.append("      <span style=\"font-weight: 700; color: #f8fafc; font-size: 13px;\">").append(dbName).append("</span>\n");
        
        // Engine Badge
        sb.append("      <span style=\"font-size: 10px; font-weight: 700; padding: 2px 6px; border-radius: 4px; ")
          .append("background: rgba(56, 189, 248, 0.15); color: #38bdf8; border: 1px solid rgba(56, 189, 248, 0.3);\">")
          .append(data.engineType()).append("</span>\n");

        // Status Badge
        if (isInstalled) {
            sb.append("      <span id=\"sample-status-").append(dbName).append("\" style=\"font-size: 10px; font-weight: 700; padding: 2px 8px; border-radius: 12px; ")
              .append("background: rgba(34, 197, 94, 0.15); color: #4ade80; border: 1px solid rgba(34, 197, 94, 0.3); display: inline-flex; align-items: center; gap: 4px;\">")
              .append("<i class=\"fas fa-check-circle\"></i> Installed (").append(count).append(" records)</span>\n");
        } else {
            sb.append("      <span id=\"sample-status-").append(dbName).append("\" style=\"font-size: 10px; font-weight: 600; padding: 2px 8px; border-radius: 12px; ")
              .append("background: rgba(148, 163, 184, 0.1); color: #94a3b8; border: 1px solid rgba(148, 163, 184, 0.25); display: inline-flex; align-items: center; gap: 4px;\">")
              .append("<i class=\"fas fa-download\"></i> Available (~").append(data.estimatedRecords()).append(" records)</span>\n");
        }

        sb.append("    </div>\n");

        // Description
        if (!data.description().isBlank()) {
            sb.append("    <p style=\"font-size: 11.5px; color: #94a3b8; margin: 0; line-height: 1.4;\">")
              .append(data.description()).append("</p>\n");
        }
        sb.append("  </div>\n");

        // 3. Action Buttons Column
        sb.append("  <div id=\"sample-actions-").append(dbName).append("\" style=\"display: flex; align-items: center; gap: 6px; flex-shrink: 0;\">\n");
        if (isInstalled) {
            String exploreJs = !data.onExploreJs().isBlank() ? data.onExploreJs() : "window.location.href='/engines?target_db=" + dbName + "';";
            String uninstallJs = !data.onUninstallJs().isBlank() ? data.onUninstallJs() : "openConfirmUninstallSampleDbModal('" + dbName + "');";

            sb.append("    <button type=\"button\" class=\"btn-action btn-secondary\" style=\"padding: 5px 10px; font-size: 11px;\" onclick=\"")
              .append(exploreJs).append("\"><i class=\"fas fa-external-link-alt\"></i> Explore</button>\n");
            sb.append("    <button type=\"button\" style=\"padding: 5px 10px; font-size: 11px; background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.3); color: #f87171; border-radius: 6px; cursor: pointer; display: inline-flex; align-items: center; gap: 4px;\" onclick=\"")
              .append(uninstallJs).append("\"><i class=\"fas fa-trash-alt\"></i> Uninstall</button>\n");
        } else {
            String installJs = !data.onInstallJs().isBlank() ? data.onInstallJs() : "installSampleDb('" + dbName + "');";

            sb.append("    <button type=\"button\" class=\"btn-action btn-secondary\" style=\"padding: 5px 12px; font-size: 11px; color: #ec4899; border-color: rgba(236, 72, 153, 0.3); cursor: pointer;\" onclick=\"")
              .append(installJs).append("\"><i class=\"fas fa-download\"></i> Install</button>\n");
        }
        sb.append("  </div>\n");

        sb.append("</div>\n");
        return sb.toString();
    }

    public static class Builder {
        private String databaseName = "";
        private String engineType = "DOCUMENT";
        private String displayName = "";
        private String description = "";
        private int estimatedRecords = 100;
        private int recordCount = 0;
        private boolean installed = false;
        private String icon = "fas fa-database";
        private String radioName = "target_db";
        private String onInstallJs = "";
        private String onExploreJs = "";
        private String onUninstallJs = "";

        public Builder databaseName(String databaseName) {
            this.databaseName = databaseName;
            return this;
        }

        public Builder engineType(String engineType) {
            this.engineType = engineType;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder estimatedRecords(int estimatedRecords) {
            this.estimatedRecords = estimatedRecords;
            return this;
        }

        public Builder recordCount(int recordCount) {
            this.recordCount = recordCount;
            return this;
        }

        public Builder installed(boolean installed) {
            this.installed = installed;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder radioName(String radioName) {
            this.radioName = radioName;
            return this;
        }

        public Builder onInstallJs(String onInstallJs) {
            this.onInstallJs = onInstallJs;
            return this;
        }

        public Builder onExploreJs(String onExploreJs) {
            this.onExploreJs = onExploreJs;
            return this;
        }

        public Builder onUninstallJs(String onUninstallJs) {
            this.onUninstallJs = onUninstallJs;
            return this;
        }

        public SampleDatabaseCard build() {
            return new SampleDatabaseCard(new SampleDatabaseData(
                databaseName, engineType, displayName, description,
                estimatedRecords, recordCount, installed, icon, radioName,
                onInstallJs, onExploreJs, onUninstallJs
            ));
        }
    }
}

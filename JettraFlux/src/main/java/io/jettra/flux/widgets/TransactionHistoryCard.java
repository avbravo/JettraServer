package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import java.util.Arrays;
import java.util.List;

public class TransactionHistoryCard extends Widget {
    private final String title;
    private final List<TransactionItem> items;

    public static class TransactionItem {
        public final String iconClass;
        public final String iconBgColor;
        public final String title;
        public final String date;
        public final String amount;
        public final boolean isPositive;

        public TransactionItem(String iconClass, String iconBgColor, String title, String date, String amount, boolean isPositive) {
            this.iconClass = iconClass;
            this.iconBgColor = iconBgColor;
            this.title = title;
            this.date = date;
            this.amount = amount;
            this.isPositive = isPositive;
        }
    }

    private TransactionHistoryCard(String title, List<TransactionItem> items) {
        this.title = title;
        this.items = items;
    }

    public static TransactionHistoryCard of(String title, TransactionItem... items) {
        return new TransactionHistoryCard(title, Arrays.asList(items));
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='chart-card-header'>");
        sb.append("  <div class='stat-header' style='color: var(--on-surface-color); font-size: 1rem;'>").append(title).append("</div>");
        sb.append("  <div><i class='fas fa-sync' style='color: #94a3b8; cursor: pointer; margin-right: 10px;'></i><i class='fas fa-filter' style='color: #94a3b8; cursor: pointer;'></i></div>");
        sb.append("</div>");

        for (TransactionItem item : items) {
            sb.append("<div class='transaction-item'>");
            sb.append("  <div class='tx-icon' style='background: ").append(item.iconBgColor).append(";'><i class='").append(item.iconClass).append("'></i></div>");
            sb.append("  <div class='tx-details'>");
            sb.append("    <p class='tx-title'>").append(item.title).append("</p>");
            sb.append("    <p class='tx-date'>").append(item.date).append("</p>");
            sb.append("  </div>");
            String amountClass = item.isPositive ? "tx-amount positive" : "tx-amount negative";
            sb.append("  <div class='").append(amountClass).append("'>").append(item.amount).append("</div>");
            sb.append("</div>");
        }

        return Card.of(Paragraph.of(sb.toString())).render(theme);
    }
}

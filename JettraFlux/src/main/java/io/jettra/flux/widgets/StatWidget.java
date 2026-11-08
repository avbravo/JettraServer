package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

/**
 * StatWidget - Alternative composable metric presentation component in JettraFlux.
 * Encapsulates title, numeric value, subtitle technical detail, and status badge.
 */
public class StatWidget extends Widget {
    private final MetricCard innerCard;

    public StatWidget(MetricCard innerCard) {
        this.innerCard = innerCard != null ? innerCard : new MetricCard();
    }

    public static StatWidget of(String title, String value) {
        return new StatWidget(MetricCard.of(title, value));
    }

    public static StatWidget of(String icon, String color, String title, String value, String subtext, Badge badge) {
        return new StatWidget(MetricCard.of(icon, color, title, value, subtext, badge));
    }

    public static MetricCard.MetricCardBuilder builder() {
        return MetricCard.builder();
    }

    public MetricCard getInnerCard() {
        return innerCard;
    }

    @Override
    public String render(ThemeData theme) {
        return innerCard.render(theme);
    }
}

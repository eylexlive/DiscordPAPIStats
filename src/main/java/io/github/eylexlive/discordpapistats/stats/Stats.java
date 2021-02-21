package io.github.eylexlive.discordpapistats.stats;

import io.github.eylexlive.discordpapistats.util.config.ConfigUtil;

public final class Stats {

    private String name, placeholder;

    private boolean filtered;

    public Stats(String name, String placeholder, boolean filtered) {
        this.name = name;
        this.placeholder = placeholder;
        this.filtered = filtered;
    }

    public Stats(String name, String placeholder) {
        this.name = name;
        this.placeholder = placeholder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public boolean isFiltered() {
        return filtered;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }

    public String getTableName() {
        return name + ConfigUtil.getStringSafely("stats-separator") + placeholder;
    }
}

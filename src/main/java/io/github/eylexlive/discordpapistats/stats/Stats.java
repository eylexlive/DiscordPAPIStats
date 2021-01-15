package io.github.eylexlive.discordpapistats.stats;

public final class Stats {

    private final String name, placeholder;

    public Stats(String name, String placeholder) {
        this.name = name;
        this.placeholder = placeholder;
    }

    public String getName() {
        return name;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public String getTableName() {
        return name + "%" + getPlaceholder();
    }
}

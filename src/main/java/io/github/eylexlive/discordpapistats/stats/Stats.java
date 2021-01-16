package io.github.eylexlive.discordpapistats.stats;

public final class Stats {

    private String name, placeholder;

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

    public String getTableName() {
        return name + "%" + getPlaceholder();
    }
}

package io.github.eylexlive.discordpapistats.util.config;

import io.github.eylexlive.discordpapistats.DiscordPAPIStats;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public final class Config extends YamlConfiguration {

    private final DiscordPAPIStats plugin = DiscordPAPIStats.getInstance();

    private final File file;

    public Config(String path) {
        final String str = path.endsWith(".yml") ? path : path + ".yml";
        file = new File(plugin.getDataFolder(), str);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(str, false);
        }
        copyDefaults();
        reload();
    }

    public void reload() {
        try {
            super.load(this.file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyDefaults() {
        final InputStream inputStream = plugin.getResource(file.getName());

        if (inputStream == null) return;

        final InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        final YamlConfiguration[] cfg =
                {
                        YamlConfiguration.loadConfiguration(file), YamlConfiguration.loadConfiguration(reader)
                };

        final Set<String> keys = cfg[1].getConfigurationSection("").getKeys(true);
        final boolean hasUpdate = keys.stream().anyMatch(key ->
                !cfg[0].contains(key)
        );

        if (!hasUpdate) return;

        keys.stream().filter(key ->
                !cfg[0].contains(key)
        ).forEach(key ->
                cfg[0].set(
                        key, cfg[1].get(key)
                )
        );
        try { cfg[0].save(file); } catch (IOException ignored) { }
    }
}

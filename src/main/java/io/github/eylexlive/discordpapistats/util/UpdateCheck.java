package io.github.eylexlive.discordpapistats.util;

import io.github.eylexlive.discordpapistats.DiscordPAPIStats;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public final class UpdateCheck {

    private final DiscordPAPIStats plugin;

    public UpdateCheck(DiscordPAPIStats plugin) {
        this.plugin = plugin;
        checkUpdate();
    }

    private void checkUpdate() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->  {
            log(
                    "--------------------------------",
                    "    DiscordPAPIStats    ",
                    " "
            );

            if (isAvailable()) {
                log(
                        " A new update is available at ",
                        " spigotmc.org/resources/87888 ",
                        " "
                );

            } else {
                log(
                        " Not update found. ",
                        " Last version running! ",
                        " "
                );
            }

            log("--------------------------------");
        });
    }

    private boolean isAvailable() {
        final String spigotPluginVersion;
        try {
            final URLConnection urlConnection = new URL(
                    "https://api.spigotmc.org/legacy/update.php?resource=87888"
            ).openConnection();

            spigotPluginVersion = new BufferedReader(
                    new InputStreamReader(
                            urlConnection.getInputStream())
            ).readLine();

        } catch (IOException e) {
            return false;
        }

        return !plugin.getDescription().getVersion().equals(spigotPluginVersion);
    }

    private void log(String... strings) {
        for (String str : strings) {
            System.out.println(str);
        }
    }
}

package io.github.eylexlive.discordpapistats.command;

import io.github.eylexlive.discordpapistats.DiscordPAPIStats;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public final class DiscordStatsTabCompleter implements TabCompleter {

    private final DiscordPAPIStats plugin;

    public DiscordStatsTabCompleter(DiscordPAPIStats plugin) {
        this.plugin = plugin;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.isOp())
            return null;

        if (args.length <= 1)
            return Arrays.asList(
                    "create",
                    "delete",
                    "setName",
                    "setPlaceholder",
                    "list",
                    "reload"
            );

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "delete":
                case "setname":
                case "setplaceholder":
                    return plugin.getStatsManager().getStatsNames();
            }

            return null;
        }

        return null;
    }
}

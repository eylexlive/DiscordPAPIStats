package io.github.eylexlive.discordpapistats.command;

import io.github.eylexlive.discordpapistats.DiscordPAPIStats;
import io.github.eylexlive.discordpapistats.stats.Stats;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DiscordStatsTabCompleter implements TabCompleter {

    private final DiscordPAPIStats plugin;

    public DiscordStatsTabCompleter(DiscordPAPIStats plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.isOp())
            return new ArrayList<>();
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
            final List<Stats> stats = plugin.getStatsManager().getStatsList();

            final List<String> list = new ArrayList<>();

            stats.forEach(stat -> list.add(stat.getName()));
            switch (args[0]) {
                case "delete":
                case "setName":
                case "setPlaceholder":
                    return list;
            }
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }
}

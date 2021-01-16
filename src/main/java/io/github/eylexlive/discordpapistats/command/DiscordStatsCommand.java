package io.github.eylexlive.discordpapistats.command;

import io.github.eylexlive.discordpapistats.DiscordPAPIStats;
import io.github.eylexlive.discordpapistats.stats.Stats;
import io.github.eylexlive.discordpapistats.stats.StatsManager;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class DiscordStatsCommand implements CommandExecutor {

    private final DiscordPAPIStats plugin;

    private final String[] mainMsg;

    public DiscordStatsCommand(DiscordPAPIStats plugin) {
        this.plugin = plugin;
        this.mainMsg = new String[] {
                "",
                "§fDiscordPAPIStats running on the server. Version: §ev" + plugin.getDescription().getVersion(),
                "§fMade by §eEylexLive §fDiscord: §eUmut Erarslan#8378",
                "",
        };
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player))
            return true;

        final StatsManager statsManager = plugin.getStatsManager();
        final Player player = (Player) sender;

        if (player.isOp()) {
            if (args.length == 0) {
                player.sendMessage(mainMsg);
                player.sendMessage(new String[] {
                                "",
                                "   §8▸ §f/dcstats create §e<name> <placeholder>",
                                "   §8▸ §f/dcstats delete §e<name>",
                                "   §8▸ §f/dcstats list",
                                "   §8▸ §f/dcstats reload",
                                "",
                        }
                );
            }

            else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("list")) {
                    player.sendMessage(
                            "§fStats list:"
                    );
                    statsManager.getStatsList().forEach(stats -> player.sendMessage(
                            "§8- §e" + stats.getName() + "§8: §f" + stats.getPlaceholder()
                    ));
                }

                else if (args[0].equalsIgnoreCase("reload")) {
                    plugin.getConfig().reload();
                    player.sendMessage(
                            "§aConfig reloaded!"
                    );
                }

            }

            else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("delete")) {
                    final Stats stats = statsManager.getStatsByName(args[1]);
                    if (stats == null) {
                        player.sendMessage(
                                "§cInvalid stats."
                        );
                        return true;
                    }

                    if (!statsManager.deleteStats(stats)) {
                        player.sendMessage(
                                "§cAn error occurred while deleting stats."
                        );

                    } else {
                        player.sendMessage(
                                "§aStats successfully deleted."
                        );
                    }
                }
            }

            else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("create")) {
                    final String placeholder = args[2], name = args[1];
                    if (placeholder.contains("%")) {
                        player.sendMessage(
                                "§cYou must enter the placeholder value without '%' in it."
                        );
                        return true;
                    }

                    final String replaced = PlaceholderAPI.setPlaceholders(player, "%" + placeholder + "%");
                    if (replaced.equals("%" + placeholder + "%")) {
                        player.sendMessage(
                                "§cThe placeholder entered does not return a valid value. You may need to download expansion."
                        );
                        return true;
                    }

                    final Stats stats = new Stats(name, placeholder);
                    if (statsManager.getStatsByName(stats.getName()) != null) {
                        player.sendMessage(
                                "§cStats already exists."
                        );
                        return true;
                    }

                    if (!statsManager.createStats(stats)) {
                        player.sendMessage(
                                "§cAn error occurred while creating stats."
                        );

                    } else {
                        player.sendMessage(
                                "§aStats successfully created."
                        );
                    }
                }
            }

        } else {
            player.sendMessage(mainMsg);
        }
        return true;
    }
}

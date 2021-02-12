package io.github.eylexlive.discordpapistats.command;

import io.github.eylexlive.discordpapistats.DiscordPAPIStats;
import io.github.eylexlive.discordpapistats.stats.Stats;
import io.github.eylexlive.discordpapistats.stats.StatsManager;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;


public final class DiscordStatsCommand implements CommandExecutor, TabCompleter {

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
        final StatsManager statsManager = plugin.getStatsManager();

        if (sender.isOp()) {
            if (args.length == 0) {
                sender.sendMessage(mainMsg);
                sender.sendMessage(new String[] {
                                "",
                                "   §8▸ §f/dcstats create §e<stats name> <placeholder>",
                                "   §8▸ §f/dcstats delete §e<stats name>",
                                "   §8▸ §f/dcstats setName §e<stats name> <new name>",
                                "   §8▸ §f/dcstats setPlaceholder §e<stats name> <new placeholder>",
                                "   §8▸ §f/dcstats list",
                                "   §8▸ §f/dcstats reload",
                                "",
                        }
                );
            }

            else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("list")) {
                    sender.sendMessage(
                            "§fStats list:"
                    );
                    statsManager.getStatsList().forEach(stats -> sender.sendMessage(
                            "§8- §e" + stats.getName() + "§8: §f" + stats.getPlaceholder()
                    ));
                }

                else if (args[0].equalsIgnoreCase("reload")) {
                    plugin.getConfig().reload();
                    sender.sendMessage(
                            "§aConfig reloaded!"
                    );
                }

            }

            else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("delete")) {
                    final Stats stats = statsManager.getStatsByName(
                            args[1], true
                    );
                    if (stats == null) {
                        sender.sendMessage(
                                "§cInvalid stats."
                        );
                        return true;
                    }

                    if (!statsManager.deleteStats(stats)) {
                        sender.sendMessage(
                                "§cAn error occurred while deleting stats."
                        );

                    } else {
                        sender.sendMessage(
                                "§aStats successfully deleted."
                        );
                    }
                }
            }

            else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("create")) {
                    final ValidateState state = isValidate(
                            args[2]
                    );
                    if (!state.isValidate()) {
                        sender.sendMessage("§c" + state.getCause());
                        return true;
                    }

                    final Stats stats = new Stats(args[1], args[2]);
                    if (statsManager.getStatsByName(stats.getName(), true) != null) {
                        sender.sendMessage(
                                "§cStats already exists."
                        );
                        return true;
                    }

                    if (!statsManager.createStats(stats)) {
                        sender.sendMessage(
                                "§cAn error occurred while creating stats."
                        );

                    } else {
                        sender.sendMessage(
                                "§aStats successfully created."
                        );
                    }
                }

                else if (args[0].equalsIgnoreCase("setPlaceholder")) {
                    final Stats stats = statsManager.getStatsByName(
                            args[1], true
                    );
                    if (stats == null) {
                        sender.sendMessage(
                                "§cInvalid stats."
                        );
                        return true;
                    }

                    final ValidateState state = isValidate(
                            args[2]
                    );
                    if (!state.isValidate()) {
                        sender.sendMessage("§c" + state.getCause());
                        return true;
                    }

                    if (!statsManager.setPlaceholder(stats, args[2])) {
                        sender.sendMessage(
                                "§cAn error occurred while setting stats placeholder.\n"
                                        +
                                        "(You can try to try again.)"
                        );

                    } else {
                        sender.sendMessage(
                                "§aStats placeholder has been successfully changed to §f" + args[2]
                        );
                    }
                }

                else if (args[0].equalsIgnoreCase("setName")) {
                    final Stats stats = statsManager.getStatsByName(
                            args[1], true
                    );
                    if (stats == null) {
                        sender.sendMessage(
                                "§cInvalid stats."
                        );
                        return true;
                    }

                    if (!statsManager.setName(stats, args[2])) {
                        sender.sendMessage(
                                "§cAn error occurred while setting stats name.\n"
                                        +
                                        "(You can try to try again.)"
                        );

                    } else {
                        sender.sendMessage(
                                "§aStats name has been successfully changed to §f" + args[2]
                        );
                    }
                }
            }

        } else {
            sender.sendMessage(mainMsg);
        }

        return true;
    }

    private ValidateState isValidate(String placeholder) {
        if (Bukkit.getOnlinePlayers().size() < 1)  {
            return new ValidateState(
                    "Unable to validate placeholder! (Try to use this command in game)",
                    false
            );
        }

        final Player player = Bukkit.getOnlinePlayers()
                .iterator()
                .next();

        if (placeholder.contains("%"))
            return new ValidateState(
                    "You must enter the placeholder value without '%' in it.",
                    false
            );

        if (PlaceholderAPI.setPlaceholders(player, "%" + placeholder + "%").equals("%" + placeholder + "%"))
            return new ValidateState(
                    "The placeholder entered does not return a valid value. You may need to download expansion.",
                    false
            );

        return new ValidateState(
                " ",
                true
        );
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

    private class ValidateState {

        private final String cause;

        private final boolean validate;

        public ValidateState(String cause, boolean validate) {
            this.cause = cause;
            this.validate = validate;
        }

        public String getCause() {
            return cause;
        }

        public boolean isValidate() {
            return validate;
        }
    }
}

package io.github.eylexlive.discordpapistats.command.discord;

import io.github.eylexlive.discordpapistats.DiscordPAPIStats;
import io.github.eylexlive.discordpapistats.stats.StatsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Objects;

public final class StatsCommand  extends ListenerAdapter {

    private final DiscordPAPIStats plugin;

    public StatsCommand(DiscordPAPIStats plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        final User user = event.getAuthor();
        if (user.isBot())
            return;

        final MessageChannel channel = event.getChannel();

        final String command = plugin.getConfig().getString("stats-command");
        final String[] parts = event.getMessage().getContentRaw().split(" ");

        if (parts[0].equalsIgnoreCase(command)) {
            if (parts.length == 1) {
                channel.sendMessage(
                        "> Correct usage: `" + command + " <player>" + "`"
                ).queue();
            }

            else if (parts.length == 2) {
                final EmbedBuilder embed = new EmbedBuilder();

                final String name = parts[1];

                embed.setDescription(
                        Objects.requireNonNull(plugin.getConfig().getString("stats-embed.description")).replace("{player}", name)
                );

                final Player player = plugin.getServer().getPlayerExact(name);
                final boolean online = player != null;

                embed.setAuthor(
                        Objects.requireNonNull(
                                plugin.getConfig().getString("stats-embed.author")
                        ).replace("{online_status}", online ? "Online" : "Offline"),
                        null,
                        online ? "https://eylexlive.github.io/green.png" : "https://eylexlive.github.io/red.png"
                );

                Color color;
                try {
                    final Field field = Color.class.getField(
                            Objects.requireNonNull(plugin.getConfig().getString("stats-embed.color"))
                    );
                    color = (Color) field.get(null);
                } catch (Exception e) {
                    color = Color.gray;
                }

                embed.setColor(color);

                embed.setThumbnail(
                        Objects.requireNonNull(
                                plugin.getConfig().getString("avatar-api")
                        ).replace("{player}", name)
                );

                embed.setFooter(
                        user.getName() + "#" + user.getDiscriminator(),
                        user.getAvatarUrl()
                );
                embed.setTimestamp(Instant.now());

                final StatsManager statsManager = plugin.getStatsManager();
                statsManager.getStatsList().forEach(stats ->  {
                    final String statsValue = (
                            online ? statsManager.getStats(stats, player) : statsManager.getStats(stats, name)
                    );

                    final String fieldFormat = Objects.requireNonNull(
                            plugin.getConfig().getString("stats-embed.field-format")
                    ).replace("{stats_name}", stats.getName()).replace("{stats_value}", statsValue);

                    final String[] fieldParts = fieldFormat.split("%VALUE");

                    embed.addField(fieldParts[0], fieldParts[1], true);
                });

                channel.sendMessage(embed.build()).queue();
            }
        }
    }
}

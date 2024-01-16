package net.cursedbreath.bansystemv3.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.cursedbreath.bansystemv3.BanSystem_Velocity;
import net.cursedbreath.bansystemv3.pluginmessaging.SendStatusUpdate;
import net.cursedbreath.bansystemv3.utils.Formatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.UUID;

public class CMD_Punish {

    public static BrigadierCommand createBrigadierCommand(final ProxyServer proxyServer, Logger logger) {

        LiteralCommandNode<CommandSource> banCommandNode = LiteralArgumentBuilder.<CommandSource>literal("Ban")
                .requires(source -> source.hasPermission("bansys.ban"))

                // Adds Help Message to the Command when Arguments are empty

                .executes(context -> {

                    Component header = MiniMessage.miniMessage().deserialize(BanSystem_Velocity.getDataManager().getMessage("idlistheader"));

                    context.getSource().sendMessage(header);

                    for (String id : BanSystem_Velocity.getDataManager().getIDs()) {

                        if (context.getSource().hasPermission("bansys.ban." + id) || context.getSource().hasPermission("bansys.*")) {

                            if(BanSystem_Velocity.getDataManager().isAdminOnly(Integer.parseInt(id))) {

                                if(context.getSource().hasPermission("bansys.admin") || context.getSource().hasPermission("bansys.*")) {

                                    Component idMessage = MiniMessage.miniMessage().deserialize("<red>ID: <gold><id> <red>Reason: <gold><reason><reset>",
                                    Placeholder.parsed("id", id),
                                    Placeholder.parsed("reason", BanSystem_Velocity.getDataManager().getReason(Integer.parseInt(id))));

                                    context.getSource().sendMessage(idMessage);

                                }

                            } else {

                                Component idMessage = MiniMessage.miniMessage().deserialize("<red>ID: <gold><id> <red>Reason: <gold><reason><reset>",
                                        Placeholder.parsed("id", id),
                                        Placeholder.parsed("reason", BanSystem_Velocity.getDataManager().getReason(Integer.parseInt(id))));

                                context.getSource().sendMessage(idMessage);

                            }

                        }

                    }

                    return Command.SINGLE_SUCCESS;

                })

                // Adds Required Argument "targetname" to the Command

                .then(RequiredArgumentBuilder.<CommandSource, String>argument("targetname", StringArgumentType.string())
                        .suggests((context, builder) -> {

                            for (Player player : proxyServer.getAllPlayers()) {

                                if (context.getArguments().isEmpty()) {
                                    builder.suggest(player.getUsername());
                                } else {
                                    if (player.getUsername().startsWith(context.getArgument("targetname", String.class))) {
                                        builder.suggest(player.getUsername());
                                    }
                                }

                            }

                            return builder.buildFuture();
                        })

                        .executes(context -> {

                            Component noIDSpecifiedMessage = MiniMessage.miniMessage().deserialize(BanSystem_Velocity.getDataManager().getMessage("noid"),
                                    Placeholder.parsed("prefix", BanSystem_Velocity.getDataManager().getPrefix()));

                            context.getSource().sendMessage(noIDSpecifiedMessage);

                            return Command.SINGLE_SUCCESS;

                        })
                        .then(RequiredArgumentBuilder.<CommandSource, Integer>argument("id", IntegerArgumentType.integer())
                                .suggests((context, builder) -> {

                                    for (String id : BanSystem_Velocity.getDataManager().getIDs()) {

                                        if (context.getSource().hasPermission("bansys.ban." + id)) {

                                            builder.suggest(id);

                                        }

                                    }

                                    return builder.buildFuture();

                                })

                                .executes(context -> {

                                    // Define all needed Variables

                                    String targetname = context.getArgument("targetname", String.class);

                                    int id = context.getArgument("id", Integer.class);

                                    UUID creator = UUID.fromString("00000000-0000-0000-0000-000000000000");

                                    String creatorname = "CONSOLE";

                                    String reason = BanSystem_Velocity.getDataManager().getReason(id);


                                    // Check if Player has Permission to use the ID


                                    if (!context.getSource().hasPermission("bansys.ban." + id)) {

                                        Component noPermissionMessage = MiniMessage.miniMessage().deserialize(BanSystem_Velocity.getDataManager().getMessage("notenoughpermissions"), Placeholder.parsed("prefix", BanSystem_Velocity.getDataManager().getPrefix()));

                                        context.getSource().sendMessage(noPermissionMessage);

                                        return Command.SINGLE_SUCCESS;

                                    }


                                    // Checks if ID Has AdminOnly Flag and if the Sender has Permission to use it

                                    if(BanSystem_Velocity.getDataManager().isAdminOnly(id) && !context.getSource().hasPermission("bansys.admin")) {

                                        Component noPermissionMessage = MiniMessage.miniMessage().deserialize(BanSystem_Velocity.getDataManager().getMessage("notenoughpermissions"), Placeholder.parsed("prefix", BanSystem_Velocity.getDataManager().getPrefix()));

                                        context.getSource().sendMessage(noPermissionMessage);

                                        return Command.SINGLE_SUCCESS;

                                    }


                                    // Check if ID exists


                                    if (reason == null) {

                                        Component wrongIDMessage = MiniMessage.miniMessage().deserialize(BanSystem_Velocity.getDataManager().getMessage("wrongid"),
                                                Placeholder.parsed("prefix", BanSystem_Velocity.getDataManager().getPrefix()));

                                        context.getSource().sendMessage(wrongIDMessage);

                                        return Command.SINGLE_SUCCESS;

                                    }


                                    // Check if sender is Player or Console


                                    if (context.getSource() instanceof Player) {

                                        creator = ((Player) context.getSource()).getUniqueId();

                                        creatorname = ((Player) context.getSource()).getUsername();

                                    }


                                    // Check if Target Player is online


                                    if (proxyServer.getPlayer(targetname).isPresent()) {

                                        // Code for Online Players START


                                        // Checks if Target player has Permission to bypass the Punishment


                                        if (proxyServer.getPlayer(targetname).get().hasPermission("bansys.bypass")) {

                                            Component bypassMessage = MiniMessage.miniMessage().deserialize(BanSystem_Velocity.getDataManager().getMessage("playerbypass"),
                                                    Placeholder.parsed("player", targetname));

                                            context.getSource().sendMessage(bypassMessage);

                                            return Command.SINGLE_SUCCESS;

                                        }


                                        // Define more Variables

                                        UUID targetUUID = proxyServer.getPlayer(targetname).get().getUniqueId();

                                        int punishmentCount = BanSystem_Velocity.getBanManager().repeatedPunishCounter(targetUUID, BanSystem_Velocity.getDataManager().getReason(id));

                                        String type = BanSystem_Velocity.getDataManager().getType(id, punishmentCount);

                                        long timeToBan = BanSystem_Velocity.getDataManager().calculatePunishDuration(id, punishmentCount);

                                        long duration = Instant.now().getEpochSecond() + timeToBan;

                                        // Run Code for the Different Punishment Types

                                        context.getSource().sendMessage(Component.text("TYPE: " + type));

                                        if (type.equalsIgnoreCase("chat")) {

                                            Player Target = proxyServer.getPlayer(targetname).get();

                                            Component targetMuteMessage = MiniMessage.miniMessage().deserialize(String.join("\n", BanSystem_Velocity.getDataManager().getScreen("chat")),
                                                    Placeholder.parsed("creator", creatorname),
                                                    Placeholder.parsed("reason", reason),
                                                    Placeholder.parsed("until", Formatter.formatDateTime(duration)),
                                                    Placeholder.parsed("remtime", Formatter.formatTime(timeToBan)));

                                            Target.sendMessage(targetMuteMessage);


                                            BanSystem_Velocity.getBanManager().punishPlayer(targetUUID, creator, reason, type, duration);

                                            SendStatusUpdate.sendStatusUpdate("status", targetUUID.toString(), true);

                                            for(Player player : proxyServer.getAllPlayers()) {

                                                if(player.hasPermission("bansys.notify")) {

                                                    Component notifyMessage = MiniMessage.miniMessage().deserialize(BanSystem_Velocity.getDataManager().getMessage("notifyTeamByMute"),
                                                            Placeholder.parsed("player", targetname),
                                                            Placeholder.parsed("creator", creatorname),
                                                            Placeholder.parsed("reason", reason));

                                                    player.sendMessage(notifyMessage);

                                                }

                                            }

                                        }

                                        if (type.equalsIgnoreCase("network")) {

                                            Component targetBanMessage = MiniMessage.miniMessage().deserialize(String.join("\n", BanSystem_Velocity.getDataManager().getScreen("network")),
                                                    Placeholder.parsed("creator", creatorname),
                                                    Placeholder.parsed("reason", reason),
                                                    Placeholder.parsed("until", Formatter.formatDateTime(duration)),
                                                    Placeholder.parsed("remtime", Formatter.formatTime(timeToBan)));

                                            proxyServer.getPlayer(targetname).get().disconnect(targetBanMessage);

                                            BanSystem_Velocity.getBanManager().punishPlayer(targetUUID, creator, reason, type, duration);

                                            for(Player player : proxyServer.getAllPlayers()) {

                                                if(player.hasPermission("bansys.notify")) {

                                                    Component notifyMessage = MiniMessage.miniMessage().deserialize(BanSystem_Velocity.getDataManager().getMessage("notifyTeamByBan"),
                                                            Placeholder.parsed("player", targetname),
                                                            Placeholder.parsed("creator", creatorname),
                                                            Placeholder.parsed("reason", reason));

                                                    player.sendMessage(notifyMessage);

                                                }

                                            }

                                        }

                                        if (type.equalsIgnoreCase("kick")) {

                                            Component targetKickMessage = MiniMessage.miniMessage().deserialize(String.join("\n", BanSystem_Velocity.getDataManager().getScreen("kick")),
                                                    Placeholder.parsed("creator", creatorname),
                                                    Placeholder.parsed("reason", reason));

                                            proxyServer.getPlayer(targetname).get().disconnect(targetKickMessage);

                                            BanSystem_Velocity.getBanManager().punishPlayer(targetUUID, creator, reason, type, duration);

                                            for(Player player : proxyServer.getAllPlayers()) {

                                                if(player.hasPermission("bansys.notify")) {

                                                    Component notifyMessage = MiniMessage.miniMessage().deserialize(BanSystem_Velocity.getDataManager().getMessage("notifyTeamByKick"),
                                                            Placeholder.parsed("player", targetname),
                                                            Placeholder.parsed("creator", creatorname),
                                                            Placeholder.parsed("reason", reason));

                                                    player.sendMessage(notifyMessage);

                                                }

                                            }

                                        }

                                        // Code for Online Players END

                                    } else {

                                        // Code for Offline Players

                                        if (!BanSystem_Velocity.getPlayerManager().playerExists(targetname)) {

                                            Component playerDoesNotExistMessage = MiniMessage.miniMessage().deserialize(BanSystem_Velocity.getDataManager().getMessage("playerdoesnotexist"));

                                            context.getSource().sendMessage(playerDoesNotExistMessage);

                                            return Command.SINGLE_SUCCESS;

                                        }

                                        UUID targetUUID = BanSystem_Velocity.getPlayerManager().getUUID(targetname);

                                        int punishmentCount = BanSystem_Velocity.getBanManager().repeatedPunishCounter(targetUUID, BanSystem_Velocity.getDataManager().getReason(id));

                                        String type = BanSystem_Velocity.getDataManager().getType(id, punishmentCount);

                                        long timeToBan = BanSystem_Velocity.getDataManager().calculatePunishDuration(id, punishmentCount);

                                        long duration = Instant.now().getEpochSecond() + timeToBan;

                                        if (type.equalsIgnoreCase("chat")) {

                                            BanSystem_Velocity.getBanManager().punishPlayer(targetUUID, creator, reason, type, duration);

                                            ByteArrayDataOutput out = ByteStreams.newDataOutput();
                                            out.writeBoolean(true);

                                            SendStatusUpdate.sendStatusUpdate("status", targetUUID.toString(), true);

                                            BanSystem_Velocity.players.add(proxyServer.getPlayer(targetUUID).get());

                                            for(Player player : proxyServer.getAllPlayers()) {

                                                if(player.hasPermission("bansys.notify")) {

                                                    Component notifyMessage = MiniMessage.miniMessage().deserialize(BanSystem_Velocity.getDataManager().getMessage("notifyTeamByMute"),
                                                            Placeholder.parsed("player", targetname),
                                                            Placeholder.parsed("creator", creatorname),
                                                            Placeholder.parsed("reason", reason));

                                                    player.sendMessage(notifyMessage);

                                                }

                                            }

                                        }

                                        if (type.equalsIgnoreCase("ban")) {

                                            BanSystem_Velocity.getBanManager().punishPlayer(targetUUID, creator, reason, type, duration);

                                            for(Player player : proxyServer.getAllPlayers()) {

                                                if(player.hasPermission("bansys.notify")) {

                                                    Component notifyMessage = MiniMessage.miniMessage().deserialize(BanSystem_Velocity.getDataManager().getMessage("notifyTeamByBan"),
                                                            Placeholder.parsed("player", targetname),
                                                            Placeholder.parsed("creator", creatorname),
                                                            Placeholder.parsed("reason", reason));

                                                    player.sendMessage(notifyMessage);

                                                }

                                            }

                                        }


                                        // Code for Offline Players End

                                    }

                                    return Command.SINGLE_SUCCESS;

                                })

                        )

                ).build();

                // Adds Required Argument "id" to the Command



        return new BrigadierCommand(banCommandNode);
    }

}
package net.cursedbreath.bansystemv3.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import net.cursedbreath.bansystemv3.BanSystem_Velocity;
import net.cursedbreath.bansystemv3.interfaces.PlayerManager;
import net.cursedbreath.bansystemv3.utils.Formatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CMD_History {

    public static BrigadierCommand createBrigadierCommand(final ProxyServer proxyServer, Logger logger) {
        LiteralCommandNode<CommandSource> historyCommandNode = LiteralArgumentBuilder.<CommandSource>literal("Unban")
                .requires(source -> source.hasPermission("bansys.ban"))

                // Adds Help Message to the Command when Arguments are empty

                .executes(context -> {

                    Component message = MiniMessage.miniMessage().deserialize(BanSystem_Velocity.getDataManager().getMessage("noargumentfound"));

                    context.getSource().sendMessage(message);

                    return Command.SINGLE_SUCCESS;

                })

                // Adds Required Argument "targetname" to the Command

                .then(RequiredArgumentBuilder.<CommandSource, String>argument("targetname", StringArgumentType.string())
                    .suggests((context, builder) -> {

                        builder.suggest("CursedbreathDev");

                        return builder.buildFuture();
                    })

                    .executes(context -> {

                        String playername = context.getArgument("targetname", String.class);

                        UUID targetUUID = BanSystem_Velocity.getPlayerManager().getUUID(playername);

                        ResultSet result = BanSystem_Velocity.getBanManager().getPunishmentHistory(targetUUID);

                        Component historyheader = MiniMessage.miniMessage().deserialize(BanSystem_Velocity.getDataManager().getMessage("historyheader"),
                                Placeholder.parsed("player", playername));

                        context.getSource().sendMessage(historyheader);

                        if(result != null) {

                            try {

                                while (result.next()) {

                                    Component message = MiniMessage.miniMessage().deserialize(String.join("\n", BanSystem_Velocity.getDataManager().getScreen("history")),
                                            Placeholder.parsed("type", result.getString("punishment")),
                                            Placeholder.parsed("reason", result.getString("reason")),
                                            Placeholder.parsed("creator", BanSystem_Velocity.getPlayerManager().getName(UUID.fromString(result.getString("creator")))),
                                            Placeholder.parsed("until", Formatter.formatTime(Long.parseLong(result.getString("duration")))));

                                    context.getSource().sendMessage(message);

                                }

                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }

                        }

                        return Command.SINGLE_SUCCESS;

                    })
                ).build();


    return new BrigadierCommand(historyCommandNode);
    }

}

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
import net.cursedbreath.bansystemv3.pluginmessaging.SendStatusUpdate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CMD_Unban {

    public static BrigadierCommand createBrigadierCommand(final ProxyServer proxyServer, Logger logger) {
        LiteralCommandNode<CommandSource> unbanCommandNode = LiteralArgumentBuilder.<CommandSource>literal("Unban")
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

                        ResultSet bannedNames = BanSystem_Velocity.getBanManager().getAllBannedNames();
                        try {

                            if(bannedNames.next()) {

                                builder.suggest(BanSystem_Velocity.getPlayerManager().getName(UUID.fromString(bannedNames.getString("banned"))));

                            }

                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        return builder.buildFuture();
                    })

                    .executes(context -> {

                        String playername = context.getArgument("targetname", String.class);

                        UUID targetUUID = BanSystem_Velocity.getPlayerManager().getUUID(playername);

                        if (BanSystem_Velocity.getBanManager().isMuted(targetUUID) || BanSystem_Velocity.getBanManager().isBanned(targetUUID)) {

                            BanSystem_Velocity.getBanManager().removePunishment(targetUUID);

                            SendStatusUpdate.sendStatusUpdate("status", targetUUID.toString(), BanSystem_Velocity.getBanManager().isMuted(targetUUID));

                        }

                        return Command.SINGLE_SUCCESS;

                    })
                ).build();


    return new BrigadierCommand(unbanCommandNode);
    }

}

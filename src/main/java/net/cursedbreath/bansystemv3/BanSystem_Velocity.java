package net.cursedbreath.bansystemv3;

import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.*;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.Getter;
import net.cursedbreath.bansystemv3.commands.CMD_History;
import net.cursedbreath.bansystemv3.commands.CMD_Punish;
import net.cursedbreath.bansystemv3.commands.CMD_Unban;
import net.cursedbreath.bansystemv3.interfaces.DataManager;
import net.cursedbreath.bansystemv3.interfaces.BanManager;
import net.cursedbreath.bansystemv3.interfaces.PlayerManager;
import net.cursedbreath.bansystemv3.listener.ChatListener;
import net.cursedbreath.bansystemv3.listener.LoginListener;
import net.cursedbreath.bansystemv3.mysql.MySQLConnectionPool;
import net.cursedbreath.bansystemv3.pluginmessaging.PluginMessageListener;
import net.cursedbreath.bansystemv3.pluginmessaging.SendStatusUpdate;
import net.cursedbreath.bansystemv3.utils.BanManagerMySQL;
import net.cursedbreath.bansystemv3.utils.DataManagerConfigs;
import net.cursedbreath.bansystemv3.utils.PlayerManagerMySQL;
import org.slf4j.Logger;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "bansystem-velocity",
        name = "BanSystem-Velocity",
        version = BuildConstants.VERSION
)
public class BanSystem_Velocity {

    private static final String KEY = "bansystemv3";


    public static List<Player> players = new ArrayList<>();

    public static HashMap<Player, Integer> mutedCounter = new HashMap<>();

    @Getter
    private static final MinecraftChannelIdentifier BANSYSTEM_CHANNEL = MinecraftChannelIdentifier.from("bansystem:mutes");

    private static final File folder = new File("plugins/BanSystem-velocity/");

    @Getter
    private static BanSystem_Velocity instance;

    @Getter
    private static ProxyServer proxyServer;

    @Getter
    private static EventManager eventManager;

    @Getter
    private static CommandManager commandManager;

    /**
     * Gets Data from Configs
     */
    @Getter
    private static DataManager dataManager;

    /**
     * Handles the Punishments ( Bans, Mutes, Kicks )
     */
    @Getter
    private static BanManager banManager;

    /**
     * Object to handle the Player Data ( Check if Player exists, create Player usw. )
     */
    @Getter
    private static PlayerManager playerManager;

    @Getter
    private static PluginManager pluginManager;

    @Getter
    private static MySQLConnectionPool connectionPool;

    @Getter
    @Inject
    private static Logger logger;

    @Inject
    public BanSystem_Velocity(ProxyServer proxyServer, Logger logger) {
        BanSystem_Velocity.proxyServer = proxyServer;
        instance = this;
        BanSystem_Velocity.logger = logger;
        eventManager = proxyServer.getEventManager();
        commandManager = proxyServer.getCommandManager();
        pluginManager = proxyServer.getPluginManager();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        logger.info("BanSystem is Starting");

        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        proxyServer.getChannelRegistrar().register(BANSYSTEM_CHANNEL);

        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        eventManager.register(this, new ChatListener());
        eventManager.register(this, new LoginListener(proxyServer));
        eventManager.register(this, new PluginMessageListener());

        dataManager = new DataManagerConfigs(folder, proxyServer, logger);

        dataManager.checkForConfigs();

        connectionPool = dataManager.connectToDB();

        dataManager.getPrefixFromConfig();

        banManager = new BanManagerMySQL(connectionPool, proxyServer, logger);

        playerManager = new PlayerManagerMySQL(connectionPool, proxyServer);

        registerCommands();

        proxyServer.getScheduler().buildTask(this , ()-> {

            for(Player player : proxyServer.getAllPlayers()) {

                if(BanSystem_Velocity.getBanManager().isMuted(player.getUniqueId())) {

                    ResultSet baninformation = BanSystem_Velocity.getBanManager().getPunishmentInformations(player.getUniqueId());

                    long until = 0;
                    try {
                        baninformation.next();
                        until = baninformation.getLong("duration");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    if(until <= System.currentTimeMillis()) {
                        banManager.removePunishment(player.getUniqueId());
                    }

                    SendStatusUpdate.sendStatusUpdate("status", player.getUniqueId().toString(), BanSystem_Velocity.getBanManager().isMuted(player.getUniqueId()));


                }

            }

        }).repeat(1, TimeUnit.MINUTES).schedule();

    }

    private void registerCommands() {

        CommandMeta banCommandMeta = commandManager.metaBuilder("ban")
                .aliases("b", "mute", "m", "kick", "k")
                .build();

        BrigadierCommand banCommand = CMD_Punish.createBrigadierCommand(proxyServer, logger);

        commandManager.register(banCommandMeta, banCommand);


        CommandMeta unbanCommandMeta = commandManager.metaBuilder("unban")
                .aliases("pardon", "unmute")
                .build();

        BrigadierCommand unBanCommand = CMD_Unban.createBrigadierCommand(proxyServer, logger);

        commandManager.register(unbanCommandMeta, unBanCommand);


        CommandMeta historyCommandMeta = commandManager.metaBuilder("history")
                .aliases("h")
                .build();

        BrigadierCommand historyCommand = CMD_History.createBrigadierCommand(proxyServer, logger);

        commandManager.register(historyCommandMeta, historyCommand);


    }

}

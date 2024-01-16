package net.cursedbreath.bansystemv3.utils;

import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.cursedbreath.bansystemv3.BanSystem_Velocity;
import net.cursedbreath.bansystemv3.config.Config;
import net.cursedbreath.bansystemv3.config.ConfigSection;
import net.cursedbreath.bansystemv3.interfaces.DataManager;
import net.cursedbreath.bansystemv3.mysql.MySQLConnectionPool;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.*;
import java.util.List;
import java.util.Set;

public class DataManagerConfigs implements DataManager {

    private Logger logger;

    private ProxyServer proxyServer;

    private final Config config;

    private final Config messages;

    private final File folder;

    @Getter
    private String prefix;

    public DataManagerConfigs(File path, ProxyServer proxyServer, Logger logger) {
        this.config = new Config(new File(path.getPath() + "/config.yml"));
        this.messages = new Config(new File(path.getPath() + "/messages.yml"));
        this.folder = path;
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Override
    public void checkForConfigs() {
        File configFile = new File(folder.getPath() + "/config.yml");
        File messageFile = new File(folder.getPath() + "/messages.yml");
        if(!folder.exists()) {
            logger.info("Folder not found, trying to create a new one");
            folder.mkdir();
        }

        if(!configFile.exists()) {

            logger.info("Config.yml not found, trying to create a new one");

            try {
                InputStream is = BanSystem_Velocity.getInstance().getClass().getResourceAsStream("/config.yml");
                OutputStream os = new FileOutputStream(configFile);
                byte[] buffer = new byte[4096];
                int length;
                while((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.close();
                is.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if(!messageFile.exists()) {

            logger.info("messages.yml not found, trying to create a new one");

            try {
                InputStream is = BanSystem_Velocity.getInstance().getClass().getResourceAsStream("/messages.yml");
                OutputStream os = new FileOutputStream(messageFile);
                byte[] buffer = new byte[4096];
                int length;
                while((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.close();
                is.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void getPrefixFromConfig() {

        if(this.config.getString("prefix") == null) {
            logger.error("Cant get prefix from config Loading Default Prefix");
            this.prefix = "<red>[<gold><b>BanSystem</b><red>]<reset> ";
        } else {
            this.prefix = this.config.getString("prefix");
        }
    }

    @Override
    public @NotNull String getMessage(String key) {
        try {
            this.messages.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String message = messages.getString(key);

        if(message == null) {
            logger.error("Cant get " + key + " from messages config");
            return null;
        }

        return message;
    }

    @Override
    public String getTimePattern(String key) {

        try {
            this.messages.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return messages.getString(key);

    }

    @Override
    public List<String> getScreen(String key) {

        try {
            this.messages.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ConfigSection ScreenSection = this.messages.getSection("screens");

        if(ScreenSection == null) {
            logger.error("Failed to load screen: " + key);
            return null;
        }

        List<String> screen = ScreenSection.getStringList(key.toLowerCase());

        return screen;

    }

    @Override
    public MySQLConnectionPool connectToDB() {

        try {
            this.config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ConfigSection mysqlSection = this.config.getSection("mysql");

        if(mysqlSection == null) {
            logger.error("Failed to load mysql section");
            return null;
        }

        boolean enabled = mysqlSection.getBoolean("enabled");


        if(!enabled) {
            logger.info("Please enable MySQL in the configuration as it is the only supported option at present.");

            return null;
        }

        String host = mysqlSection.getString("host");
        int port = mysqlSection.getInt("port");
        String database = mysqlSection.getString("database");
        String username = mysqlSection.getString("username");
        String password = mysqlSection.getString("password");

        return new MySQLConnectionPool("jdbc:mysql://" + host + ":" + port + "/" + database, username, password, 20, logger);

    }

    @Override
    public Set<String> getIDs() {

        try {
            this.config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        ConfigSection banIDKeySection = this.config.getSection("banids");

        if(banIDKeySection == null) {
            logger.error("Failed to load banids section");
            return null;
        }

        return banIDKeySection.getKeys();

    }

    @Override
    public long calculatePunishDuration(int id, int countedPunishments) {

        ConfigSection banIDKeySection = this.config.getSection("banids");

        ConfigSection idSection = banIDKeySection.getSection(String.valueOf(id));

        ConfigSection lvlSection = idSection.getSection("lvl");

        ConfigSection lvlData = lvlSection.getSection(String.valueOf(countedPunishments));

        if(lvlData == null) {

            lvlData = lvlSection.getSection(lvlSection.getKeys().size()+"");

        }

        return lvlData.getLong("duration");

    }

    @Override
    public String getType(int id, int countedPunishments) {

        ConfigSection banIDKeySection = this.config.getSection("banids");

        ConfigSection idSection = banIDKeySection.getSection(String.valueOf(id));

        ConfigSection lvlSection = idSection.getSection("lvl");

        ConfigSection lvlData = lvlSection.getSection(String.valueOf(countedPunishments));

        if(lvlData == null) {

            lvlData = lvlSection.getSection((lvlSection.getKeys().size()-1)+"");

        }

        return lvlData.getString("type");

    }

    @Override
    public String getReason(int id) {

        try {
            this.config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ConfigSection banIDKeySection = this.config.getSection("banids");

        if(banIDKeySection == null) {
            logger.error("Failed to load banids section");
            return null;
        }

        ConfigSection idSection = banIDKeySection.getSection(String.valueOf(id));

        if(idSection == null) {
            logger.error("Failed to load ID: " + id);
            return null;
        }

        return idSection.getString("reason");

    }

    @Override
    public boolean isAdminOnly(int id) {

        try {
            this.config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ConfigSection banIDKeySection = this.config.getSection("banids");

        if(banIDKeySection == null) {
            logger.error("Failed to load banids section");
            return false;
        }

        ConfigSection idSection = banIDKeySection.getSection(String.valueOf(id));

        if(idSection == null) {
            logger.error("Failed to load ID: " + id);
            return false;
        }

        return idSection.getBoolean("onlyAdmin");

    }

}

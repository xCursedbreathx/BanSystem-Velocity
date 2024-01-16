package net.cursedbreath.bansystemv3.interfaces;

import net.cursedbreath.bansystemv3.mysql.MySQLConnectionPool;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public interface DataManager {

    void checkForConfigs();

    void getPrefixFromConfig();

    @NotNull String getMessage(String key);

    List<String> getScreen(String key);

    String getTimePattern(String key);

    Set<String> getIDs();

    long calculatePunishDuration(int id, int countedPunishments);

    String getType(int id, int countedPunishments);

    String getReason(int id);

    MySQLConnectionPool connectToDB();

    String getPrefix();

    boolean isAdminOnly(int id);

}

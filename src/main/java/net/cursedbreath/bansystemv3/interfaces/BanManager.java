package net.cursedbreath.bansystemv3.interfaces;

import com.velocitypowered.api.command.CommandSource;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public interface BanManager {

    void punishPlayer(UUID player, UUID creator, String reason, String type, long duration);

    boolean isBanned(UUID playerUniqueID);

    boolean isMuted(UUID playerUniqueID);

    ResultSet getPunishmentInformations(UUID player);

    ResultSet getPunishmentHistory(UUID player);

    ResultSet getAllBannedNames();

    int repeatedPunishCounter(UUID player, String reason);

    void removePunishment(UUID player);

}

package net.cursedbreath.bansystemv3.interfaces;

import com.velocitypowered.api.proxy.Player;

import java.util.UUID;

public interface PlayerManager {

    boolean playerExists(String name);

    boolean playerExists(UUID uuid);

    void createPlayer(Player player);

    UUID getUUID(String name);

    String getName(UUID uuid);

    void updatePlayerName(Player player);

}

package net.cursedbreath.bansystemv3.utils;

import java.util.UUID;

public interface BanManager {

    void banPlayer(UUID player, UUID creator, String reason, long duration);

    void banPlayer(String name, UUID creator, String reason, long duration);

    void mutePlayer(UUID player, UUID creator, String reason, long duration);

    void mutePlayer(String name, UUID creator, String reason, long duration);

    void kickPlayer(UUID player, UUID creator, String reason);

    void kickPlayer(String name, UUID creator, String reason);

}

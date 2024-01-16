package net.cursedbreath.bansystemv3.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.cursedbreath.bansystemv3.BanSystem_Velocity;
import net.cursedbreath.bansystemv3.pluginmessaging.SendStatusUpdate;
import net.cursedbreath.bansystemv3.utils.Formatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.sql.ResultSet;
import java.util.UUID;

public class LoginListener {

    private ProxyServer proxyServer;

    public LoginListener(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Subscribe
    public void onPostLogin(LoginEvent event) {

        BanSystem_Velocity.mutedCounter.put(event.getPlayer(), 0);

        Player player = event.getPlayer();

        if(!BanSystem_Velocity.getPlayerManager().playerExists(player.getUniqueId())) {

            BanSystem_Velocity.getPlayerManager().createPlayer(player);
            return;

        }

        if(BanSystem_Velocity.getPlayerManager().getName(player.getUniqueId()).equalsIgnoreCase(player.getUsername())) {

            BanSystem_Velocity.getPlayerManager().updatePlayerName(player);

        }

        if(BanSystem_Velocity.getBanManager().isBanned(player.getUniqueId())) {

            ResultSet baninformation = BanSystem_Velocity.getBanManager().getPunishmentInformations(player.getUniqueId());

            try {

                while(baninformation.next()) {

                    long until = baninformation.getLong("duration");

                    long remainingTime = until - (System.currentTimeMillis());

                    if(until == 0) {

                        String creatorname = BanSystem_Velocity.getPlayerManager().getName(UUID.fromString(baninformation.getString("creator")));

                        String reason = baninformation.getString("reason");

                        Component targetBanMessage = MiniMessage.miniMessage().deserialize(String.join("\n", BanSystem_Velocity.getDataManager().getScreen("network")),
                                Placeholder.parsed("creator", creatorname),
                                Placeholder.parsed("reason", reason),
                                Placeholder.parsed("until", "PERMANENT"),
                                Placeholder.parsed("remtime", "-1"));

                        event.getPlayer().disconnect(targetBanMessage);

                    }

                    if(until >= (System.currentTimeMillis())) {

                        if(BanSystem_Velocity.getBanManager().isMuted(player.getUniqueId())) {

                            SendStatusUpdate.sendStatusUpdate("status", player.getUniqueId().toString(), BanSystem_Velocity.getBanManager().isMuted(player.getUniqueId()));

                        }

                        BanSystem_Velocity.getBanManager().removePunishment(player.getUniqueId());

                        return;

                    }

                    String creatorname = BanSystem_Velocity.getPlayerManager().getName(UUID.fromString(baninformation.getString("creator")));

                    String reason = baninformation.getString("reason");

                    Component targetBanMessage = MiniMessage.miniMessage().deserialize(String.join("\n", BanSystem_Velocity.getDataManager().getScreen("network")),
                            Placeholder.parsed("creator", creatorname),
                            Placeholder.parsed("reason", reason),
                            Placeholder.parsed("until", Formatter.formatTime(until)),
                            Placeholder.parsed("remtime", Formatter.formatTime(remainingTime)));

                    event.getPlayer().disconnect(targetBanMessage);

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(BanSystem_Velocity.getBanManager().isMuted(player.getUniqueId())) {

            SendStatusUpdate.sendStatusUpdate("status", player.getUniqueId().toString(), BanSystem_Velocity.getBanManager().isMuted(player.getUniqueId()));

        }

    }

}

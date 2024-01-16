package net.cursedbreath.bansystemv3.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import net.cursedbreath.bansystemv3.BanSystem_Velocity;
import net.cursedbreath.bansystemv3.utils.Formatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Inserting;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

public class ChatListener {

    public ChatListener() {
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {

        if(BanSystem_Velocity.getBanManager().isMuted(event.getPlayer().getUniqueId())) {

            ResultSet resultSet = BanSystem_Velocity.getBanManager().getPunishmentInformations(event.getPlayer().getUniqueId());

            try {

                if(resultSet.next()) {

                    long until = resultSet.getLong("duration");

                    if(until > Instant.now().getEpochSecond()) {

                        Component mutedMessage = MiniMessage.miniMessage().deserialize(String.join("\n", BanSystem_Velocity.getDataManager().getScreen("chat")),
                                Placeholder.parsed("reason", resultSet.getString("reason")),
                                Placeholder.parsed("creator", BanSystem_Velocity.getPlayerManager().getName(UUID.fromString(resultSet.getString("creator")))),
                                Placeholder.parsed("until", Formatter.formatDateTime(until * 1000)),
                                Placeholder.parsed("remtime", Formatter.formatTime(until- Instant.now().getEpochSecond())));

                        event.getPlayer().sendMessage(mutedMessage);

                    }
                    else
                    {

                        BanSystem_Velocity.getLogger().error("Player " + event.getPlayer().getUsername() + " has been unmuted.");

                        BanSystem_Velocity.getBanManager().removePunishment(event.getPlayer().getUniqueId());

                    }

                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }


        }

    }

}

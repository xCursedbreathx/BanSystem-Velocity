package net.cursedbreath.bansystemv3.pluginmessaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.cursedbreath.bansystemv3.BanSystem_Velocity;
import net.cursedbreath.bansystemv3.utils.Formatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PluginMessageListener {

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) throws SQLException {

        if(event.getSource() instanceof Player) {

            Player player = (Player) event.getSource();

            if(event.getIdentifier() != BanSystem_Velocity.getBANSYSTEM_CHANNEL()) return;

            ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
            String action = in.readUTF();

            if(action.equalsIgnoreCase("request")) {

                SendStatusUpdate.sendStatusUpdate("status", player.getUniqueId().toString(), BanSystem_Velocity.getBanManager().isMuted(player.getUniqueId()));

            }


        }

    }

}

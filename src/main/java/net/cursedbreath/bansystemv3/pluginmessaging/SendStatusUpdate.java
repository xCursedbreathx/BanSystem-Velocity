package net.cursedbreath.bansystemv3.pluginmessaging;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.cursedbreath.bansystemv3.BanSystem_Velocity;

public class SendStatusUpdate {

    public static void sendStatusUpdate(String action, String uuid, boolean status) {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(action);
        out.writeUTF(uuid);
        out.writeBoolean(status);

        for(RegisteredServer server : BanSystem_Velocity.getProxyServer().getAllServers()) {

            server.sendPluginMessage(BanSystem_Velocity.getBANSYSTEM_CHANNEL(), out.toByteArray());

        }
    }

}

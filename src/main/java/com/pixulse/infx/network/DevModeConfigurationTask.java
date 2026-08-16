package com.pixulse.infx.network;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.InfiniteXDevMode;
import java.util.function.Consumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;

/**
 * Sends the server's dev mode switch during the login configuration phase.
 * The client rejects the connection if its own dev mode switch differs, and
 * the server enforces the same symmetric check when the client acknowledges.
 */
final class DevModeConfigurationTask implements ICustomConfigurationTask {
    static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type(InfiniteX.id("dev_mode_status"));

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        sender.accept(new Network.DevModeStatusPayload(InfiniteXDevMode.isServerEnabled()));
    }

    @Override
    public ConfigurationTask.Type type() {
        return TYPE;
    }
}

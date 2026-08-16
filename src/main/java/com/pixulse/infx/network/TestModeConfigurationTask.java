package com.pixulse.infx.network;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.InfiniteXTestMode;
import java.util.function.Consumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;

/**
 * Sends the server's test mode switch during the login configuration phase.
 * The client rejects the connection if its own test mode switch differs, and
 * the server enforces the same symmetric check when the client acknowledges.
 */
final class TestModeConfigurationTask implements ICustomConfigurationTask {
    static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type(InfiniteX.id("test_mode_status"));

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        sender.accept(new Network.TestModeStatusPayload(InfiniteXTestMode.isServerEnabled()));
    }

    @Override
    public ConfigurationTask.Type type() {
        return TYPE;
    }
}

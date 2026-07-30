package com.pixulse.infx.network;

import com.pixulse.infx.InfiniteX;
import java.util.function.Consumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;

/** Sends the server's startup-only test-mode setting before a player joins the world. */
final class TestModeConfigurationTask implements ICustomConfigurationTask {
    static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type(InfiniteX.id("test_mode"));

    private final boolean serverTestMode;

    TestModeConfigurationTask(boolean serverTestMode) {
        this.serverTestMode = serverTestMode;
    }

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        sender.accept(new Network.TestModePayload(this.serverTestMode));
    }

    @Override
    public ConfigurationTask.Type type() {
        return TYPE;
    }
}

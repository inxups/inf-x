package com.pixulse.infx.network;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.recipe.RecipeRules;
import java.util.function.Consumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;

/**
 * Sends the server-authoritative crafting rules during the login
 * configuration phase, before the client receives the recipe map.
 */
final class RecipeRulesConfigurationTask implements ICustomConfigurationTask {
    static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type(InfiniteX.id("recipe_rules"));

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        sender.accept(new Network.RecipeRulesPayload(RecipeRules.resolvedRules()));
    }

    @Override
    public ConfigurationTask.Type type() {
        return TYPE;
    }
}

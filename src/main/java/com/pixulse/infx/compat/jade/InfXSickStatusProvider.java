package com.pixulse.infx.compat.jade;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.entity.Livestock;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

/** Adds the InfX wellness state to Jade's entity tooltip for sick livestock. */
public final class InfXSickStatusProvider implements IEntityComponentProvider {
    public static final InfXSickStatusProvider INSTANCE = new InfXSickStatusProvider();

    private static final Component SICK = Component.translatable("tooltip.infx.sick");

    private InfXSickStatusProvider() {}

    @Override
    public @NonNull Identifier getUid() {
        return InfiniteX.id("sick_status");
    }

    @Override
    public boolean isRequired() {
        // This is a derived status line, not an independent Jade feature toggle.
        return true;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (shouldDisplay(accessor.getEntity())) {
            tooltip.add(IThemeHelper.get().danger(SICK), getUid());
        }
    }

    /** Returns whether an entity currently has the InfX sick-livestock status. */
    public static boolean shouldDisplay(Entity entity) {
        return entity instanceof Animal animal
                && Livestock.hasSickSkin(animal)
                && !Livestock.isWell(animal);
    }
}

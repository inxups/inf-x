package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.R196PortalBlock.PortalType;
import com.pixulse.infx.block.UnderworldPortalBlock;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** POI indexes let ordinary portal surfaces find only their matching destination type. */
public final class ModPoiTypes {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, InfiniteX.MOD_ID);

    public static final DeferredHolder<PoiType, PoiType> UNDERWORLD_PORTAL = POI_TYPES.register(
            "underworld_portal",
            () -> new PoiType(underworldPortalStates(), 0, 1));
    public static final DeferredHolder<PoiType, PoiType> NETHER_PORTAL = POI_TYPES.register(
            "nether_portal",
            () -> new PoiType(portalStates(ModBlocks.NETHER_PORTAL.get()), 0, 1));
    public static final DeferredHolder<PoiType, PoiType> RETURN_SPAWN_PORTAL = POI_TYPES.register(
            "return_spawn_portal",
            () -> new PoiType(portalStates(ModBlocks.RETURN_SPAWN_PORTAL.get()), 0, 1));

    private ModPoiTypes() {}

    public static void register(IEventBus modBus) {
        POI_TYPES.register(modBus);
    }

    public static DeferredHolder<PoiType, PoiType> forPortal(PortalType portalType) {
        return switch (portalType) {
            case UNDERWORLD -> UNDERWORLD_PORTAL;
            case NETHER -> NETHER_PORTAL;
            case RETURN_SPAWN -> RETURN_SPAWN_PORTAL;
        };
    }

    private static Set<BlockState> underworldPortalStates() {
        return ModBlocks.UNDERWORLD_PORTAL.get().getStateDefinition().getPossibleStates().stream()
                .filter(state -> !state.getValue(UnderworldPortalBlock.RUNE_GATE))
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<BlockState> portalStates(Block portal) {
        return Set.copyOf(portal.getStateDefinition().getPossibleStates());
    }
}

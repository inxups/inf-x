package com.pixulse.infx.item;

import com.pixulse.infx.item.material.InfxMaterial;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.component.BlocksAttacks;

/**
 * Per-material blocking stats for {@link EquipmentType#SHIELD}. Blocking itself is fully
 * component-driven in 26.x (see vanilla {@code Item.use} / {@code LivingEntity.getItemBlockingWith});
 * this only supplies the {@link BlocksAttacks} values that scale with material tier.
 *
 * <p>Design (synthesizing the reference sources — MITE has no shield of its own, so these are
 * original-to-InfiniteX values following the material-tiering pattern of MITE 26.2 spears):
 * <ul>
 *   <li>{@code blockFactor} — fraction of frontal, non-bypass damage blocked (0..1). Low tiers
 *       block partially (wood 60%), top tier blocks fully (adamantium 100%).</li>
 *   <li>{@code blockDelaySeconds} — raise time before blocking begins. Better materials raise
 *       faster.</li>
 *   <li>{@code disableCooldownScale} — multiplier on the axe-induced disable cooldown. Better
 *       shields recover faster.</li>
 *   <li>{@code wearFactor} — durability lost per block is {@code 1 + wearFactor*damage} (after the
 *       3.0 threshold). Better materials take less wear.</li>
 * </ul>
 *
 * <p>Values are starting points pending playtesting (相对强度三问).
 */
public final class ShieldSpec {
    private ShieldSpec() {}

    private record Stats(float blockFactor, float blockDelaySeconds, float disableCooldownScale, float wearFactor) {}

    private static Stats stats(InfxMaterial material) {
        return switch (material) {
            case WOOD -> new Stats(0.60F, 0.30F, 1.25F, 1.00F);
            case ANCIENT_METAL -> new Stats(0.85F, 0.18F, 0.85F, 0.65F);
            case ADAMANTIUM -> new Stats(1.00F, 0.10F, 0.45F, 0.30F);
            default -> throw new IllegalArgumentException("No shield spec for " + material);
        };
    }

    public static BlocksAttacks blocksAttacks(InfxMaterial material, HolderLookup.Provider context) {
        Stats s = stats(material);
        return new BlocksAttacks(
                s.blockDelaySeconds(),
                s.disableCooldownScale(),
                // 90° frontal arc, all non-bypass damage types; base 0 + factor*damage.
                List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, s.blockFactor())),
                // Durability damage only kicks in past a 3.0-damage hit.
                new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, s.wearFactor()),
                Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                Optional.of(SoundEvents.SHIELD_BLOCK),
                Optional.of(SoundEvents.SHIELD_BREAK));
    }
}

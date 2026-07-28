package com.pixulse.infx.effect.curse;

import com.pixulse.infx.entity.MiteWitch;
import com.pixulse.infx.registry.ModAttachments;
import com.pixulse.infx.registry.ModMobEffects;
import com.pixulse.infx.registry.tag.ModTags;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Authoritative curse lifecycle plus the two behavior hooks that require Mixins. */
public final class CurseManager {
    public static final int REALIZATION_DELAY = 6000;
    public static final int CURSED_AIR_SUPPLY = 90;

    private CurseManager() {}

    public static boolean addPending(ServerPlayer player, MiteWitch witch, CurseType type) {
        MinecraftServer server = player.level().getServer();
        long realization = server.overworld().getGameTime() + REALIZATION_DELAY;
        return witch.isAlive() && CurseData.get(server)
                .add(player.getUUID(), witch.getUUID(), type, realization);
    }

    public static void tick(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        CurseData data = CurseData.get(server);
        Optional<CurseData.Entry> before = data.entry(player.getUUID());
        Optional<CurseData.Entry> current = data.realizeIfDue(
                player.getUUID(), server.overworld().getGameTime());

        if (current.isEmpty() || !current.orElseThrow().realized()) {
            setStatus(player, CurseStatus.NONE);
            removeLegacyMarker(player);
            return;
        }

        CurseData.Entry entry = current.orElseThrow();
        boolean newlyRealized = before.isPresent() && !before.orElseThrow().realized();
        setStatus(player, new CurseStatus(entry.type().id(), entry.known()));
        removeLegacyMarker(player);
        if (newlyRealized) {
            player.sendOverlayMessage(
                    net.minecraft.network.chat.Component.translatable("message.infx.curse.realized")
                            .withStyle(ChatFormatting.DARK_PURPLE));
            curseParticles(player);
        }
        enforceContinuousEffects(player, entry.type());
    }

    public static CurseStatus status(Player player) {
        return player.getData(ModAttachments.CURSE_STATUS);
    }

    public static boolean hasCurse(Player player, CurseType type) {
        return status(player).is(type);
    }

    public static void reveal(Player player, CurseType type) {
        if (!(player instanceof ServerPlayer serverPlayer) || !hasCurse(player, type)) return;
        CurseData data = CurseData.get(serverPlayer.level().getServer());
        CurseData.Entry before = data.entry(player.getUUID()).orElse(null);
        if (before == null || before.known()) return;
        CurseData.Entry learned = data.learn(player.getUUID()).orElseThrow();
        setStatus(serverPlayer, new CurseStatus(type.id(), true));
        serverPlayer.sendOverlayMessage(
                net.minecraft.network.chat.Component.translatable(
                                "message.infx.curse.learned", learned.type().title())
                        .withStyle(ChatFormatting.DARK_PURPLE));
        curseParticles(serverPlayer);
    }

    public static void removeFromPlayer(ServerPlayer player) {
        Optional<CurseData.Entry> removed =
                CurseData.get(player.level().getServer()).remove(player.getUUID());
        setStatus(player, CurseStatus.NONE);
        removeLegacyMarker(player);
        if (removed.filter(CurseData.Entry::realized).isPresent()) {
            player.sendOverlayMessage(
                    net.minecraft.network.chat.Component.translatable("message.infx.curse.lifted")
                            .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    public static void removeForWitch(MinecraftServer server, UUID witch) {
        Map<UUID, CurseData.Entry> removed = CurseData.get(server).removeForWitch(witch);
        removed.forEach((playerId, entry) -> {
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player == null) return;
            setStatus(player, CurseStatus.NONE);
            removeLegacyMarker(player);
            if (entry.realized()) {
                player.sendOverlayMessage(
                        net.minecraft.network.chat.Component.translatable("message.infx.curse.lifted")
                                .withStyle(ChatFormatting.DARK_PURPLE));
            }
        });
    }

    public static int durabilityDamage(int amount, LivingEntity owner) {
        if (amount <= 0
                || !(owner instanceof Player player)
                || player.hasInfiniteMaterials()
                || !hasCurse(player, CurseType.EQUIPMENT_DECAYS_FASTER)) {
            return amount;
        }
        reveal(player, CurseType.EQUIPMENT_DECAYS_FASTER);
        return Math.multiplyExact(amount, 2);
    }

    public static Vec3 entangledInput(Player player, Vec3 input) {
        if (!hasCurse(player, CurseType.ENTANGLEMENT)) return input;
        BlockPos feet = BlockPos.containing(player.getX(), player.getBoundingBox().minY + 0.001D, player.getZ());
        var state = player.level().getBlockState(feet);
        double factor;
        if (state.is(ModTags.Blocks.CURSE_VINES)) {
            factor = 0.2D;
        } else if (state.is(ModTags.Blocks.CURSE_PLANTS)) {
            factor = 0.4D;
        } else {
            return input;
        }
        reveal(player, CurseType.ENTANGLEMENT);
        return new Vec3(input.x * factor, input.y, input.z * factor);
    }

    private static void enforceContinuousEffects(ServerPlayer player, CurseType type) {
        if (type == CurseType.CANNOT_HOLD_BREATH) {
            if (player.getAirSupply() > CURSED_AIR_SUPPLY) {
                player.setAirSupply(CURSED_AIR_SUPPLY);
            }
            if (isLosingAirInWater(player)) {
                reveal(player, type);
            }
        } else if (type == CurseType.CANNOT_RUN && player.isSprinting()) {
            player.setSprinting(false);
            reveal(player, type);
        } else if (type == CurseType.CANNOT_WEAR_ARMOR) {
            dropArmor(player);
        }
    }

    private static boolean isLosingAirInWater(ServerPlayer player) {
        BlockPos eye = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        return player.isEyeInFluid(FluidTags.WATER)
                && !player.level().getBlockState(eye).is(Blocks.BUBBLE_COLUMN)
                && !player.is(EntityTypeTags.CAN_BREATHE_UNDER_WATER)
                && !MobEffectUtil.hasWaterBreathing(player)
                && !player.getAbilities().invulnerable;
    }

    private static void dropArmor(ServerPlayer player) {
        boolean dropped = false;
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            ItemStack equipped = player.getItemBySlot(slot);
            if (equipped.isEmpty()) continue;
            ItemStack drop = equipped.copy();
            player.setItemSlot(slot, ItemStack.EMPTY);
            player.drop(drop, false);
            dropped = true;
        }
        if (dropped) reveal(player, CurseType.CANNOT_WEAR_ARMOR);
    }

    private static void setStatus(ServerPlayer player, CurseStatus status) {
        if (!status.equals(player.getData(ModAttachments.CURSE_STATUS))) {
            player.setData(ModAttachments.CURSE_STATUS, status);
        }
    }

    private static void removeLegacyMarker(ServerPlayer player) {
        player.removeEffect(ModMobEffects.WITCH_CURSE);
    }

    private static void curseParticles(ServerPlayer player) {
        player.level().sendParticles(
                ParticleTypes.WITCH,
                player.getX(),
                player.getEyeY(),
                player.getZ(),
                20,
                0.35D,
                0.5D,
                0.35D,
                0.02D);
    }
}

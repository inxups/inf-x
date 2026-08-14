package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.InfiniteXTestMode;
import com.pixulse.infx.data.food.SurvivalData;
import com.pixulse.infx.data.food.SurvivalRules;
import com.pixulse.infx.registry.InfXAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** INFX debug-profile, sleep and capacity-scaled food interfaces (all custom hotkeys removed). */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class ClientControls {

    private static boolean debugConfigured;

    private static final FoodBarSprites NORMAL_FOOD_SPRITES = new FoodBarSprites(
            Identifier.withDefaultNamespace("hud/food_empty"),
            Identifier.withDefaultNamespace("hud/food_half"),
            Identifier.withDefaultNamespace("hud/food_full"));
    private static final FoodBarSprites HUNGER_FOOD_SPRITES = new FoodBarSprites(
            Identifier.withDefaultNamespace("hud/food_empty_hunger"),
            Identifier.withDefaultNamespace("hud/food_half_hunger"),
            Identifier.withDefaultNamespace("hud/food_full_hunger"));

    private static final RandomSource RANDOM = RandomSource.create();

    private ClientControls() {}

    @SubscribeEvent
    public static void clientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;
        configureDebugOverlay(minecraft);
    }

    private static void configureDebugOverlay(Minecraft minecraft) {
        if (debugConfigured) return;
        boolean testMode = InfiniteXTestMode.isClientEnabled();
        if (testMode) {
            if (!minecraft.debugEntries.isUsingProfile(DebugScreenProfile.DEFAULT)) {
                minecraft.debugEntries.loadProfile(DebugScreenProfile.DEFAULT);
            }
            debugConfigured = true;
            return;
        }
        for (var id : DebugScreenEntries.allEntries().keySet()) {
            DebugScreenEntryStatus status = debugStatus(false, id);
            if (minecraft.debugEntries.getStatus(id) != status) minecraft.debugEntries.setStatus(id, status);
        }
        debugConfigured = true;
    }

    static DebugScreenEntryStatus debugStatus(boolean testMode, Identifier id) {
        if (testMode) {
            return DebugScreenEntries.PROFILES.get(DebugScreenProfile.DEFAULT)
                    .getOrDefault(id, DebugScreenEntryStatus.NEVER);
        }
        return id.equals(DebugScreenEntries.FPS)
                ? DebugScreenEntryStatus.IN_OVERLAY
                : DebugScreenEntryStatus.NEVER;
    }

    @SubscribeEvent
    public static void removeLeaveBedButton(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InBedChatScreen)) return;
        for (var listener : java.util.List.copyOf(event.getListenersList())) {
            if (listener instanceof Button) event.removeListener(listener);
        }
    }

    /**
     * Draws the food bar with only the current level-scaled capacity slots, mirroring how the
     * vanilla heart bar only shows the current max health. Position and sprites follow the
     * vanilla 26.1.2 food layer so nothing else in the HUD shifts.
     */
    @SubscribeEvent
    public static void renderScaledFoodBar(RenderGuiLayerEvent.Pre event) {
        if (!event.getName().equals(VanillaGuiLayers.FOOD_LEVEL)) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || hasMountHearts(minecraft.player.getVehicle())
                || !shouldRenderFoodBar(minecraft.player.isCreative(), minecraft.player.isSpectator())) return;
        event.setCanceled(true);
        var graphics = event.getGuiGraphics();
        var data = minecraft.player.getData(InfXAttachments.SURVIVAL);
        int food = foodBarFood(data);
        int slots = (int) Math.ceil(SurvivalRules.foodCap(minecraft.player.experienceLevel) / 2.0D);
        int rows = Math.max(1, (slots + 9) / 10);
        int xRight = graphics.guiWidth() / 2 + 91;
        int yBase = graphics.guiHeight() - minecraft.gui.rightHeight;
        FoodBarSprites sprites = foodBarSprites(minecraft.player.hasEffect(MobEffects.HUNGER));
        boolean shake = shouldShakeFoodBar(
                data.isStarving() ? 0.0D : data.satiation(), food, minecraft.gui.getGuiTicks());
        for (int index = 0; index < slots; index++) {
            int row = index / 10;
            int column = index % 10;
            int x = xRight - column * 8 - 9;
            int y = yBase - row * 10;
            if (shake) {
                y += RANDOM.nextInt(3) - 1;
            }
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprites.empty(), x, y, 9, 9);
            if (index * 2 + 1 < food) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprites.full(), x, y, 9, 9);
            } else if (index * 2 + 1 == food) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprites.half(), x, y, 9, 9);
            }
        }
        minecraft.gui.rightHeight += rows * 10;
    }

    /** Starving players show a fully empty bar, matching the mirrored FoodData. */
    static int foodBarFood(SurvivalData data) {
        return data.isStarving() ? 0 : (int) Math.ceil(data.nutrition());
    }

    /**
     * Mirrors the vanilla shake: while saturation is spent, the bar jitters on ticks
     * aligned to {@code guiTicks % (food * 3 + 1) == 0}, pulsing faster as food drops.
     */
    static boolean shouldShakeFoodBar(double satiation, int food, int guiTicks) {
        return satiation <= 0.0D && guiTicks % (food * 3 + 1) == 0;
    }

    /** Vanilla hides the food bar while riding a mount that shows its own hearts. */
    static boolean hasMountHearts(Entity vehicle) {
        return vehicle instanceof LivingEntity living
                && living.showVehicleHealth()
                && vehicleHearts(living.getMaxHealth()) > 0;
    }

    /** Mirrors the vanilla vehicle-hearts count with its 30-heart cap. */
    static int vehicleHearts(float maxHealth) {
        return Math.min((int) (maxHealth + 0.5F) / 2, 30);
    }

    static FoodBarSprites foodBarSprites(boolean hunger) {
        return hunger ? HUNGER_FOOD_SPRITES : NORMAL_FOOD_SPRITES;
    }

    record FoodBarSprites(Identifier empty, Identifier half, Identifier full) {}

    static int registeredKeyCount() {
        return 0;
    }

    static boolean shouldRenderFoodBar(boolean creative, boolean spectator) {
        return !creative && !spectator;
    }
}

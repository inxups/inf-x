package com.pixulse.infx.event.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.client.*;
import com.pixulse.infx.screen.gui.MetalAnvilScreen;
import com.pixulse.infx.screen.gui.TimedWorkbenchScreen;
import com.pixulse.infx.recipe.RecipeRules;
import com.pixulse.infx.entity.InfxBat;
import com.pixulse.infx.entity.InfxCreeper;
import com.pixulse.infx.entity.InfxSilverfish;
import com.pixulse.infx.entity.InfxSkeleton;
import com.pixulse.infx.entity.InfxSlime;
import com.pixulse.infx.entity.InfxSpider;
import com.pixulse.infx.entity.InfxWolf;
import com.pixulse.infx.registry.InfXBlockEntityTypes;
import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.registry.InfXMenus;

import java.util.Collection;
import java.util.List;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.minecraft.client.model.animal.squid.SquidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.CodRenderer;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.client.renderer.entity.OcelotRenderer;
import net.minecraft.client.renderer.entity.PufferfishRenderer;
import net.minecraft.client.renderer.entity.SalmonRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.SquidRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.TropicalFishRenderer;
import net.minecraft.client.renderer.entity.WitchRenderer;
import net.minecraft.client.renderer.entity.WitherSkeletonRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.ZombifiedPiglinRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;

@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private static RecipeMap syncedRecipes = RecipeMap.EMPTY;

    private ClientEvents() {}

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        InfXMenus.WORKBENCHES.forEach(menu -> event.register(menu.get(), TimedWorkbenchScreen::new));
        event.register(InfXMenus.METAL_ANVIL.get(), MetalAnvilScreen::new);
        event.register(InfXMenus.EMERALD_ENCHANTING.get(), EnchantmentScreen::new);
        event.register(InfXMenus.DIAMOND_ENCHANTING.get(), EnchantmentScreen::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SafeModel.LAYER, SafeModel::createBodyLayer);
        event.registerLayerDefinition(EarthElementalModel.LAYER, EarthElementalModel::createBodyLayer);
        event.registerLayerDefinition(InvisibleStalkerModel.LAYER, InvisibleStalkerModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(InfiniteX.id("safe"), SafeSpecialRenderer.MAP_CODEC);
        event.register(InfiniteX.id("shield"), InfXShieldSpecialRenderer.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // World geometry only 鈥?chunk mesh is particle-only (see ModModelProvider).
        event.registerBlockEntityRenderer(InfXBlockEntityTypes.SAFE.get(), SafeRenderer::new);
        event.registerEntityRenderer(InfXEntityTypes.INVISIBLE_STALKER.get(), InvisibleStalkerRenderer::new);
        event.registerEntityRenderer(
                InfXEntityTypes.GHOUL.get(),
                context -> new EntityRenderers.ZombieTexture(context, InfXEntityTypes.GHOUL.get()));
        event.registerEntityRenderer(
                InfXEntityTypes.SHADOW.get(),
                context -> new EntityRenderers.ZombieTexture(context, InfXEntityTypes.SHADOW.get()));
        event.registerEntityRenderer(
                InfXEntityTypes.WIGHT.get(),
                context -> new EntityRenderers.ZombieTexture(context, InfXEntityTypes.WIGHT.get()));
        event.registerEntityRenderer(
                InfXEntityTypes.REVENANT.get(),
                context -> new EntityRenderers.ZombieTexture(context, InfXEntityTypes.REVENANT.get()));

        event.registerEntityRenderer(InfXEntityTypes.INFX_SKELETON.get(), SkeletonRenderer::new);
        event.registerEntityRenderer(InfXEntityTypes.INFX_ZOMBIE.get(), ZombieRenderer::new);
        event.registerEntityRenderer(InfXEntityTypes.INFX_WITHER_SKELETON.get(), WitherSkeletonRenderer::new);
        event.registerEntityRenderer(
                InfXEntityTypes.LONGDEAD.get(),
                context -> new EntityRenderers.SkeletonTexture(context, InfxSkeleton.Variant.LONGDEAD));
        event.registerEntityRenderer(
                InfXEntityTypes.LONGDEAD_GUARDIAN.get(),
                context -> new EntityRenderers.SkeletonTexture(context, InfxSkeleton.Variant.LONGDEAD_GUARDIAN));
        event.registerEntityRenderer(
                InfXEntityTypes.BONE_LORD.get(),
                context -> new EntityRenderers.SkeletonTexture(context, InfxSkeleton.Variant.BONE_LORD));
        event.registerEntityRenderer(
                InfXEntityTypes.ANCIENT_BONE_LORD.get(),
                context -> new EntityRenderers.SkeletonTexture(context, InfxSkeleton.Variant.ANCIENT_BONE_LORD));

        event.registerEntityRenderer(
                InfXEntityTypes.INFX_SPIDER.get(),
                context -> new EntityRenderers.SpiderTexture(context, InfxSpider.Variant.SPIDER));
        event.registerEntityRenderer(
                InfXEntityTypes.INFX_CAVE_SPIDER.get(),
                context -> new EntityRenderers.SpiderTexture(context, InfxSpider.Variant.CAVE_SPIDER, 0.5F));
        event.registerEntityRenderer(
                InfXEntityTypes.BLACK_WIDOW_SPIDER.get(),
                context -> new EntityRenderers.SpiderTexture(context, InfxSpider.Variant.BLACK_WIDOW, 0.6F));
        event.registerEntityRenderer(
                InfXEntityTypes.DEMON_SPIDER.get(),
                context -> new EntityRenderers.SpiderTexture(context, InfxSpider.Variant.DEMON));
        event.registerEntityRenderer(
                InfXEntityTypes.WOOD_SPIDER.get(),
                context -> new EntityRenderers.SpiderTexture(context, InfxSpider.Variant.WOOD, 0.6F));
        event.registerEntityRenderer(
                InfXEntityTypes.PHASE_SPIDER.get(),
                context -> new EntityRenderers.SpiderTexture(context, InfxSpider.Variant.PHASE, 0.6F));

        event.registerEntityRenderer(InfXEntityTypes.INFX_CREEPER.get(), CreeperRenderer::new);
        event.registerEntityRenderer(
                InfXEntityTypes.INFERNAL_CREEPER.get(),
                context -> new EntityRenderers.CreeperTexture(context, InfxCreeper.Variant.INFERNAL));

        event.registerEntityRenderer(
                InfXEntityTypes.INFX_SLIME.get(),
                context -> new EntityRenderers.SlimeTexture(context, InfxSlime.Variant.SLIME));
        event.registerEntityRenderer(
                InfXEntityTypes.JELLY.get(),
                context -> new EntityRenderers.SlimeTexture(context, InfxSlime.Variant.JELLY));
        event.registerEntityRenderer(
                InfXEntityTypes.BLOB.get(),
                context -> new EntityRenderers.SlimeTexture(context, InfxSlime.Variant.BLOB));
        event.registerEntityRenderer(
                InfXEntityTypes.OOZE.get(),
                context -> new EntityRenderers.SlimeTexture(context, InfxSlime.Variant.OOZE));
        event.registerEntityRenderer(
                InfXEntityTypes.PUDDING.get(),
                context -> new EntityRenderers.SlimeTexture(context, InfxSlime.Variant.PUDDING));
        event.registerEntityRenderer(
                InfXEntityTypes.GELATINOUS_SPHERE.get(), context -> new ThrownItemRenderer<>(context, 1.0F, false));
        event.registerEntityRenderer(
                InfXEntityTypes.BRICK_PROJECTILE.get(), context -> new ThrownItemRenderer<>(context, 1.0F, false));
        event.registerEntityRenderer(
                InfXEntityTypes.WEB_PROJECTILE.get(), context -> new ThrownItemRenderer<>(context, 1.0F, false));
        event.registerEntityRenderer(InfXEntityTypes.MAGMA_CUBE.get(), EntityRenderers.MagmaCubeTexture::new);
        event.registerEntityRenderer(
                InfXEntityTypes.NETHERSPAWN.get(),
                context -> new EntityRenderers.SilverfishTexture(context, InfxSilverfish.Variant.NETHERSPAWN));
        event.registerEntityRenderer(
                InfXEntityTypes.COPPERSPINE.get(),
                context -> new EntityRenderers.SilverfishTexture(context, InfxSilverfish.Variant.COPPERSPINE));
        event.registerEntityRenderer(
                InfXEntityTypes.HOARY_SILVERFISH.get(),
                context -> new EntityRenderers.SilverfishTexture(context, InfxSilverfish.Variant.HOARY));
        event.registerEntityRenderer(
                InfXEntityTypes.INFX_BAT.get(),
                context -> new EntityRenderers.BatTexture(context, InfxBat.Variant.NORMAL));
        event.registerEntityRenderer(
                InfXEntityTypes.VAMPIRE_BAT.get(),
                context -> new EntityRenderers.BatTexture(context, InfxBat.Variant.VAMPIRE));
        event.registerEntityRenderer(
                InfXEntityTypes.NIGHTWING.get(),
                context -> new EntityRenderers.BatTexture(context, InfxBat.Variant.NIGHTWING));
        event.registerEntityRenderer(
                InfXEntityTypes.GIANT_VAMPIRE_BAT.get(),
                context -> new EntityRenderers.BatTexture(
                        context, InfxBat.Variant.GIANT_VAMPIRE, InfXEntityTypes.GIANT_VAMPIRE_BAT_SCALE));
        event.registerEntityRenderer(
                InfXEntityTypes.HELLHOUND.get(),
                context -> new EntityRenderers.WolfTexture(context, InfxWolf.Variant.HELLHOUND));
        event.registerEntityRenderer(
                InfXEntityTypes.DIRE_WOLF.get(),
                context -> new EntityRenderers.WolfTexture(context, InfxWolf.Variant.DIRE_WOLF));
        event.registerEntityRenderer(InfXEntityTypes.FIRE_ELEMENTAL.get(), EntityRenderers.FireElementalTexture::new);
        event.registerEntityRenderer(InfXEntityTypes.EARTH_ELEMENTAL.get(), EarthElementalRenderer::new);
        event.registerEntityRenderer(InfXEntityTypes.CLAY_GOLEM.get(), EarthElementalRenderer::new);
        event.registerEntityRenderer(InfXEntityTypes.INFX_ENDERMAN.get(), EndermanRenderer::new);
        event.registerEntityRenderer(
                InfXEntityTypes.INFX_SQUID.get(),
                context -> new SquidRenderer<>(
                        context,
                        new SquidModel(context.bakeLayer(ModelLayers.SQUID)),
                        new SquidModel(context.bakeLayer(ModelLayers.SQUID_BABY))));
        event.registerEntityRenderer(InfXEntityTypes.INFX_COD.get(), CodRenderer::new);
        event.registerEntityRenderer(InfXEntityTypes.INFX_SALMON.get(), SalmonRenderer::new);
        event.registerEntityRenderer(InfXEntityTypes.INFX_PUFFERFISH.get(), PufferfishRenderer::new);
        event.registerEntityRenderer(InfXEntityTypes.INFX_TROPICAL_FISH.get(), TropicalFishRenderer::new);
        event.registerEntityRenderer(InfXEntityTypes.INFX_WITCH.get(), WitchRenderer::new);
        // The zombified piglin keeps its modern piglin model and vanilla skin.
        event.registerEntityRenderer(
                InfXEntityTypes.INFX_ZOMBIFIED_PIGLIN.get(),
                context -> new ZombifiedPiglinRenderer(
                        context,
                        ModelLayers.ZOMBIFIED_PIGLIN,
                        ModelLayers.ZOMBIFIED_PIGLIN_BABY,
                        ModelLayers.ZOMBIFIED_PIGLIN_ARMOR,
                        ModelLayers.ZOMBIFIED_PIGLIN_BABY_ARMOR));
        event.registerEntityRenderer(InfXEntityTypes.INFX_BLAZE.get(), EntityRenderers.BlazeTexture::new);
        event.registerEntityRenderer(InfXEntityTypes.INFX_GHAST.get(), EntityRenderers.GhastTexture::new);

        // INFX livestock: vanilla models; sick skins when !isWell (INFX).
        event.registerEntityRenderer(InfXEntityTypes.INFX_COW.get(), EntityRenderers.CowTexture::new);
        event.registerEntityRenderer(InfXEntityTypes.INFX_CHICKEN.get(), EntityRenderers.ChickenTexture::new);
        event.registerEntityRenderer(InfXEntityTypes.INFX_SHEEP.get(), EntityRenderers.SheepTexture::new);
        event.registerEntityRenderer(InfXEntityTypes.INFX_PIG.get(), EntityRenderers.PigTexture::new);
        event.registerEntityRenderer(InfXEntityTypes.INFX_HORSE.get(), HorseRenderer::new);
        event.registerEntityRenderer(InfXEntityTypes.INFX_OCELOT.get(), OcelotRenderer::new);
        event.registerEntityRenderer(InfXEntityTypes.INFX_WOLF.get(), WolfRenderer::new);
    }

    /**
     * Livestock isWell must be attached here, not in extractRenderState: createRenderState clears
     * render data immediately after extraction, so only modifiers registered here survive to the
     * sick-skin texture lookup.
     */
    @SubscribeEvent
    public static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        EntityRenderers.registerRenderStateModifiers(event);
    }

    @SubscribeEvent
    public static void receiveRecipes(RecipesReceivedEvent event) {
        if (event.getRecipeTypes().contains(RecipeType.CRAFTING)) {
            syncedRecipes = event.getRecipeMap();
        }
    }

    @SubscribeEvent
    public static void clearRecipes(ClientPlayerNetworkEvent.LoggingOut event) {
        syncedRecipes = RecipeMap.EMPTY;
        RecipeRules.clearClientRules();
    }

    public static Collection<RecipeHolder<CraftingRecipe>> timedCraftingRecipes() {
        return syncedRecipes.byType(RecipeType.CRAFTING);
    }
}

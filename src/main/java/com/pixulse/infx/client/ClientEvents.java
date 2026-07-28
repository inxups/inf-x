package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.screen.MetalAnvilScreen;
import com.pixulse.infx.screen.TimedWorkbenchScreen;
import com.pixulse.infx.recipe.InferredTimedCraftingRecipe;
import com.pixulse.infx.recipe.TimedCraftingRecipe;
import com.pixulse.infx.entity.MiteBat;
import com.pixulse.infx.entity.MiteCreeper;
import com.pixulse.infx.entity.MiteSilverfish;
import com.pixulse.infx.entity.MiteSkeleton;
import com.pixulse.infx.entity.MiteSlime;
import com.pixulse.infx.entity.MiteSpider;
import com.pixulse.infx.entity.MiteWolf;
import com.pixulse.infx.entity.MiteZombie;
import com.pixulse.infx.registry.InfXBlockEntityTypes;
import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.registry.InfXMenus;
import com.pixulse.infx.registry.InfXRecipes;

import java.util.ArrayList;
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
    private static void registerScreens(RegisterMenuScreensEvent event) {
        InfXMenus.WORKBENCHES.forEach(menu -> event.register(menu.get(), TimedWorkbenchScreen::new));
        event.register(InfXMenus.METAL_ANVIL.get(), MetalAnvilScreen::new);
        event.register(InfXMenus.EMERALD_ENCHANTING.get(), EnchantmentScreen::new);
        event.register(InfXMenus.DIAMOND_ENCHANTING.get(), EnchantmentScreen::new);
    }

    @SubscribeEvent
    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SafeModel.LAYER, SafeModel::createBodyLayer);
        event.registerLayerDefinition(EarthElementalModel.LAYER, EarthElementalModel::createBodyLayer);
        event.registerLayerDefinition(InvisibleStalkerModel.LAYER, InvisibleStalkerModel::createBodyLayer);
    }

    @SubscribeEvent
    private static void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(InfiniteX.id("safe"), SafeSpecialRenderer.MAP_CODEC);
    }

    @SubscribeEvent
    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // World geometry only — chunk mesh is particle-only (see ModModelProvider).
        event.registerBlockEntityRenderer(InfXBlockEntityTypes.SAFE.get(), SafeRenderer::new);
        event.registerEntityRenderer(
                InfXEntityTypes.R196_ZOMBIE.get(),
                context -> new EntityRenderers.ZombieTexture(context, MiteZombie.Variant.ZOMBIE));
        event.registerEntityRenderer(InfXEntityTypes.INVISIBLE_STALKER.get(), InvisibleStalkerRenderer::new);
        event.registerEntityRenderer(
                InfXEntityTypes.GHOUL.get(),
                context -> new EntityRenderers.ZombieTexture(context, MiteZombie.Variant.GHOUL));
        event.registerEntityRenderer(
                InfXEntityTypes.SHADOW.get(),
                context -> new EntityRenderers.ZombieTexture(context, MiteZombie.Variant.SHADOW));
        event.registerEntityRenderer(
                InfXEntityTypes.WIGHT.get(),
                context -> new EntityRenderers.ZombieTexture(context, MiteZombie.Variant.WIGHT));
        event.registerEntityRenderer(
                InfXEntityTypes.REVENANT.get(),
                context -> new EntityRenderers.ZombieTexture(context, MiteZombie.Variant.REVENANT));

        event.registerEntityRenderer(InfXEntityTypes.R196_SKELETON.get(), SkeletonRenderer::new);
        event.registerEntityRenderer(
                InfXEntityTypes.LONGDEAD.get(),
                context -> new EntityRenderers.SkeletonTexture(context, MiteSkeleton.Variant.LONGDEAD));
        event.registerEntityRenderer(
                InfXEntityTypes.BONE_LORD.get(),
                context -> new EntityRenderers.SkeletonTexture(context, MiteSkeleton.Variant.BONE_LORD));
        event.registerEntityRenderer(
                InfXEntityTypes.ANCIENT_BONE_LORD.get(),
                context -> new EntityRenderers.SkeletonTexture(context, MiteSkeleton.Variant.ANCIENT_BONE_LORD));

        event.registerEntityRenderer(
                InfXEntityTypes.R196_SPIDER.get(),
                context -> new EntityRenderers.SpiderTexture(context, MiteSpider.Variant.SPIDER));
        event.registerEntityRenderer(
                InfXEntityTypes.R196_CAVE_SPIDER.get(),
                context -> new EntityRenderers.SpiderTexture(context, MiteSpider.Variant.CAVE_SPIDER, 0.5F));
        event.registerEntityRenderer(
                InfXEntityTypes.BLACK_WIDOW_SPIDER.get(),
                context -> new EntityRenderers.SpiderTexture(context, MiteSpider.Variant.BLACK_WIDOW, 0.6F));
        event.registerEntityRenderer(
                InfXEntityTypes.DEMON_SPIDER.get(),
                context -> new EntityRenderers.SpiderTexture(context, MiteSpider.Variant.DEMON));
        event.registerEntityRenderer(
                InfXEntityTypes.WOOD_SPIDER.get(),
                context -> new EntityRenderers.SpiderTexture(context, MiteSpider.Variant.WOOD, 0.6F));
        event.registerEntityRenderer(
                InfXEntityTypes.PHASE_SPIDER.get(),
                context -> new EntityRenderers.SpiderTexture(context, MiteSpider.Variant.PHASE, 0.6F));

        event.registerEntityRenderer(InfXEntityTypes.R196_CREEPER.get(), CreeperRenderer::new);
        event.registerEntityRenderer(
                InfXEntityTypes.INFERNAL_CREEPER.get(),
                context -> new EntityRenderers.CreeperTexture(context, MiteCreeper.Variant.INFERNAL));

        event.registerEntityRenderer(
                InfXEntityTypes.R196_SLIME.get(),
                context -> new EntityRenderers.SlimeTexture(context, MiteSlime.Variant.SLIME));
        event.registerEntityRenderer(
                InfXEntityTypes.JELLY.get(),
                context -> new EntityRenderers.SlimeTexture(context, MiteSlime.Variant.JELLY));
        event.registerEntityRenderer(
                InfXEntityTypes.BLOB.get(),
                context -> new EntityRenderers.SlimeTexture(context, MiteSlime.Variant.BLOB));
        event.registerEntityRenderer(
                InfXEntityTypes.OOZE.get(),
                context -> new EntityRenderers.SlimeTexture(context, MiteSlime.Variant.OOZE));
        event.registerEntityRenderer(
                InfXEntityTypes.PUDDING.get(),
                context -> new EntityRenderers.SlimeTexture(context, MiteSlime.Variant.PUDDING));
        event.registerEntityRenderer(
                InfXEntityTypes.GELATINOUS_SPHERE.get(), context -> new ThrownItemRenderer<>(context, 1.0F, false));
        event.registerEntityRenderer(InfXEntityTypes.MAGMA_CUBE.get(), EntityRenderers.MagmaCubeTexture::new);
        event.registerEntityRenderer(
                InfXEntityTypes.NETHERSPAWN.get(),
                context -> new EntityRenderers.SilverfishTexture(context, MiteSilverfish.Variant.NETHERSPAWN));
        event.registerEntityRenderer(
                InfXEntityTypes.COPPERSPINE.get(),
                context -> new EntityRenderers.SilverfishTexture(context, MiteSilverfish.Variant.COPPERSPINE));
        event.registerEntityRenderer(
                InfXEntityTypes.HOARY_SILVERFISH.get(),
                context -> new EntityRenderers.SilverfishTexture(context, MiteSilverfish.Variant.HOARY));
        event.registerEntityRenderer(
                InfXEntityTypes.VAMPIRE_BAT.get(),
                context -> new EntityRenderers.BatTexture(context, MiteBat.Variant.VAMPIRE));
        event.registerEntityRenderer(
                InfXEntityTypes.NIGHTWING.get(),
                context -> new EntityRenderers.BatTexture(context, MiteBat.Variant.NIGHTWING));
        event.registerEntityRenderer(
                InfXEntityTypes.GIANT_VAMPIRE_BAT.get(),
                context -> new EntityRenderers.BatTexture(context, MiteBat.Variant.GIANT_VAMPIRE, 2.0F));
        event.registerEntityRenderer(
                InfXEntityTypes.HELLHOUND.get(),
                context -> new EntityRenderers.WolfTexture(context, MiteWolf.Variant.HELLHOUND));
        event.registerEntityRenderer(
                InfXEntityTypes.DIRE_WOLF.get(),
                context -> new EntityRenderers.WolfTexture(context, MiteWolf.Variant.DIRE_WOLF));
        event.registerEntityRenderer(InfXEntityTypes.FIRE_ELEMENTAL.get(), EntityRenderers.FireElementalTexture::new);
        event.registerEntityRenderer(InfXEntityTypes.EARTH_ELEMENTAL.get(), EarthElementalRenderer::new);
        event.registerEntityRenderer(InfXEntityTypes.CLAY_GOLEM.get(), EarthElementalRenderer::new);
        event.registerEntityRenderer(InfXEntityTypes.R196_ENDERMAN.get(), EndermanRenderer::new);
        event.registerEntityRenderer(
                InfXEntityTypes.R196_SQUID.get(),
                context -> new SquidRenderer<>(
                        context,
                        new SquidModel(context.bakeLayer(ModelLayers.SQUID)),
                        new SquidModel(context.bakeLayer(ModelLayers.SQUID_BABY))));
        event.registerEntityRenderer(InfXEntityTypes.R196_COD.get(), CodRenderer::new);
        event.registerEntityRenderer(InfXEntityTypes.R196_SALMON.get(), SalmonRenderer::new);
        event.registerEntityRenderer(InfXEntityTypes.R196_PUFFERFISH.get(), PufferfishRenderer::new);
        event.registerEntityRenderer(InfXEntityTypes.R196_TROPICAL_FISH.get(), TropicalFishRenderer::new);
        event.registerEntityRenderer(InfXEntityTypes.R196_WITCH.get(), WitchRenderer::new);
        // MITE zombie pigmen keep the humanoid zombie model instead of the modern piglin model.
        event.registerEntityRenderer(
                InfXEntityTypes.R196_ZOMBIFIED_PIGLIN.get(), EntityRenderers.ZombiePigmanTexture::new);
        event.registerEntityRenderer(InfXEntityTypes.R196_BLAZE.get(), EntityRenderers.BlazeTexture::new);
        event.registerEntityRenderer(InfXEntityTypes.R196_GHAST.get(), EntityRenderers.GhastTexture::new);

        // R196 livestock: vanilla models; sick skins when !isWell (MITE).
        event.registerEntityRenderer(InfXEntityTypes.R196_COW.get(), EntityRenderers.CowTexture::new);
        event.registerEntityRenderer(InfXEntityTypes.R196_CHICKEN.get(), EntityRenderers.ChickenTexture::new);
        event.registerEntityRenderer(InfXEntityTypes.R196_SHEEP.get(), EntityRenderers.SheepTexture::new);
        event.registerEntityRenderer(InfXEntityTypes.R196_PIG.get(), EntityRenderers.PigTexture::new);
        event.registerEntityRenderer(InfXEntityTypes.R196_HORSE.get(), HorseRenderer::new);
        event.registerEntityRenderer(InfXEntityTypes.R196_OCELOT.get(), OcelotRenderer::new);
        event.registerEntityRenderer(InfXEntityTypes.R196_WOLF.get(), WolfRenderer::new);
    }

    /**
     * Livestock isWell must be attached here, not in extractRenderState: createRenderState clears
     * render data immediately after extraction, so only modifiers registered here survive to the
     * sick-skin texture lookup.
     */
    @SubscribeEvent
    private static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        EntityRenderers.registerRenderStateModifiers(event);
    }

    @SubscribeEvent
    private static void receiveRecipes(RecipesReceivedEvent event) {
        if (event.getRecipeTypes().contains(InfXRecipes.CRAFTING.get())
                || event.getRecipeTypes().contains(RecipeType.CRAFTING)) {
            syncedRecipes = event.getRecipeMap();
        }
    }

    @SubscribeEvent
    private static void clearRecipes(ClientPlayerNetworkEvent.LoggingOut event) {
        syncedRecipes = RecipeMap.EMPTY;
    }

    public static Collection<RecipeHolder<TimedCraftingRecipe>> timedCraftingRecipes() {
        Collection<RecipeHolder<TimedCraftingRecipe>> explicit =
                syncedRecipes.byType(InfXRecipes.CRAFTING.get());
        Collection<RecipeHolder<CraftingRecipe>> vanilla =
                syncedRecipes.byType(RecipeType.CRAFTING);
        ArrayList<RecipeHolder<TimedCraftingRecipe>> result =
                new ArrayList<>(explicit.size() + vanilla.size());
        result.addAll(explicit);
        for (RecipeHolder<CraftingRecipe> holder : vanilla) {
            result.add(new RecipeHolder<>(holder.id(), InferredTimedCraftingRecipe.of(holder.value())));
        }
        return List.copyOf(result);
    }
}

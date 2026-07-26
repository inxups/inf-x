package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.crafting.InferredTimedCraftingRecipe;
import com.pixulse.infx.crafting.TimedCraftingRecipe;
import com.pixulse.infx.entity.R196Bat;
import com.pixulse.infx.entity.R196Creeper;
import com.pixulse.infx.entity.R196Silverfish;
import com.pixulse.infx.entity.R196Skeleton;
import com.pixulse.infx.entity.R196Slime;
import com.pixulse.infx.entity.R196Spider;
import com.pixulse.infx.entity.R196Wolf;
import com.pixulse.infx.entity.R196Zombie;
import com.pixulse.infx.registry.ModBlockEntityTypes;
import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.registry.ModMenus;
import com.pixulse.infx.registry.ModRecipes;

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
import net.minecraft.client.renderer.entity.ZombieRenderer;
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
        ModMenus.WORKBENCHES.forEach(menu -> event.register(menu.get(), TimedWorkbenchScreen::new));
        event.register(ModMenus.METAL_ANVIL.get(), MetalAnvilScreen::new);
        event.register(ModMenus.EMERALD_ENCHANTING.get(), EnchantmentScreen::new);
        event.register(ModMenus.DIAMOND_ENCHANTING.get(), EnchantmentScreen::new);
    }

    @SubscribeEvent
    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(R196SafeModel.LAYER, R196SafeModel::createBodyLayer);
    }

    @SubscribeEvent
    private static void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(InfiniteX.id("safe"), R196SafeSpecialRenderer.MAP_CODEC);
    }

    @SubscribeEvent
    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // World geometry only — chunk mesh is particle-only (see ModModelProvider).
        event.registerBlockEntityRenderer(ModBlockEntityTypes.SAFE.get(), R196SafeRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.R196_ZOMBIE.get(), ZombieRenderer::new);
        event.registerEntityRenderer(
                ModEntityTypes.INVISIBLE_STALKER.get(),
                context -> new R196EntityRenderers.ZombieTexture(context, R196Zombie.Variant.INVISIBLE_STALKER));
        event.registerEntityRenderer(
                ModEntityTypes.GHOUL.get(),
                context -> new R196EntityRenderers.ZombieTexture(context, R196Zombie.Variant.GHOUL));
        event.registerEntityRenderer(
                ModEntityTypes.SHADOW.get(),
                context -> new R196EntityRenderers.ZombieTexture(context, R196Zombie.Variant.SHADOW));
        event.registerEntityRenderer(
                ModEntityTypes.WIGHT.get(),
                context -> new R196EntityRenderers.ZombieTexture(context, R196Zombie.Variant.WIGHT));
        event.registerEntityRenderer(
                ModEntityTypes.REVENANT.get(),
                context -> new R196EntityRenderers.ZombieTexture(context, R196Zombie.Variant.REVENANT));

        event.registerEntityRenderer(ModEntityTypes.R196_SKELETON.get(), SkeletonRenderer::new);
        event.registerEntityRenderer(
                ModEntityTypes.LONGDEAD.get(),
                context -> new R196EntityRenderers.SkeletonTexture(context, R196Skeleton.Variant.LONGDEAD));
        event.registerEntityRenderer(
                ModEntityTypes.BONE_LORD.get(),
                context -> new R196EntityRenderers.SkeletonTexture(context, R196Skeleton.Variant.BONE_LORD));
        event.registerEntityRenderer(
                ModEntityTypes.ANCIENT_BONE_LORD.get(),
                context -> new R196EntityRenderers.SkeletonTexture(context, R196Skeleton.Variant.ANCIENT_BONE_LORD));

        event.registerEntityRenderer(
                ModEntityTypes.R196_SPIDER.get(),
                context -> new R196EntityRenderers.SpiderTexture(context, R196Spider.Variant.SPIDER));
        event.registerEntityRenderer(
                ModEntityTypes.R196_CAVE_SPIDER.get(),
                context -> new R196EntityRenderers.SpiderTexture(context, R196Spider.Variant.CAVE_SPIDER, 0.5F));
        event.registerEntityRenderer(
                ModEntityTypes.BLACK_WIDOW_SPIDER.get(),
                context -> new R196EntityRenderers.SpiderTexture(context, R196Spider.Variant.BLACK_WIDOW, 0.6F));
        event.registerEntityRenderer(
                ModEntityTypes.DEMON_SPIDER.get(),
                context -> new R196EntityRenderers.SpiderTexture(context, R196Spider.Variant.DEMON));
        event.registerEntityRenderer(
                ModEntityTypes.WOOD_SPIDER.get(),
                context -> new R196EntityRenderers.SpiderTexture(context, R196Spider.Variant.WOOD, 0.6F));
        event.registerEntityRenderer(
                ModEntityTypes.PHASE_SPIDER.get(),
                context -> new R196EntityRenderers.SpiderTexture(context, R196Spider.Variant.PHASE, 0.6F));

        event.registerEntityRenderer(ModEntityTypes.R196_CREEPER.get(), CreeperRenderer::new);
        event.registerEntityRenderer(
                ModEntityTypes.INFERNAL_CREEPER.get(),
                context -> new R196EntityRenderers.CreeperTexture(context, R196Creeper.Variant.INFERNAL, 1.5F));

        event.registerEntityRenderer(
                ModEntityTypes.R196_SLIME.get(),
                context -> new R196EntityRenderers.SlimeTexture(context, R196Slime.Variant.SLIME));
        event.registerEntityRenderer(
                ModEntityTypes.JELLY.get(),
                context -> new R196EntityRenderers.SlimeTexture(context, R196Slime.Variant.JELLY));
        event.registerEntityRenderer(
                ModEntityTypes.BLOB.get(),
                context -> new R196EntityRenderers.SlimeTexture(context, R196Slime.Variant.BLOB));
        event.registerEntityRenderer(
                ModEntityTypes.OOZE.get(),
                context -> new R196EntityRenderers.SlimeTexture(context, R196Slime.Variant.OOZE));
        event.registerEntityRenderer(
                ModEntityTypes.PUDDING.get(),
                context -> new R196EntityRenderers.SlimeTexture(context, R196Slime.Variant.PUDDING));
        event.registerEntityRenderer(
                ModEntityTypes.GELATINOUS_SPHERE.get(), context -> new ThrownItemRenderer<>(context, 1.0F, false));
        event.registerEntityRenderer(ModEntityTypes.MAGMA_CUBE.get(), R196EntityRenderers.MagmaCubeTexture::new);
        event.registerEntityRenderer(
                ModEntityTypes.NETHERSPAWN.get(),
                context -> new R196EntityRenderers.SilverfishTexture(context, R196Silverfish.Variant.NETHERSPAWN));
        event.registerEntityRenderer(
                ModEntityTypes.COPPERSPINE.get(),
                context -> new R196EntityRenderers.SilverfishTexture(context, R196Silverfish.Variant.COPPERSPINE));
        event.registerEntityRenderer(
                ModEntityTypes.HOARY_SILVERFISH.get(),
                context -> new R196EntityRenderers.SilverfishTexture(context, R196Silverfish.Variant.HOARY));
        event.registerEntityRenderer(
                ModEntityTypes.VAMPIRE_BAT.get(),
                context -> new R196EntityRenderers.BatTexture(context, R196Bat.Variant.VAMPIRE));
        event.registerEntityRenderer(
                ModEntityTypes.NIGHTWING.get(),
                context -> new R196EntityRenderers.BatTexture(context, R196Bat.Variant.NIGHTWING));
        event.registerEntityRenderer(
                ModEntityTypes.GIANT_VAMPIRE_BAT.get(),
                context -> new R196EntityRenderers.BatTexture(context, R196Bat.Variant.GIANT_VAMPIRE, 2.0F));
        event.registerEntityRenderer(
                ModEntityTypes.HELLHOUND.get(),
                context -> new R196EntityRenderers.WolfTexture(context, R196Wolf.Variant.HELLHOUND));
        event.registerEntityRenderer(
                ModEntityTypes.DIRE_WOLF.get(),
                context -> new R196EntityRenderers.WolfTexture(context, R196Wolf.Variant.DIRE_WOLF));
        event.registerEntityRenderer(ModEntityTypes.FIRE_ELEMENTAL.get(), R196EntityRenderers.FireElementalTexture::new);
        event.registerEntityRenderer(ModEntityTypes.EARTH_ELEMENTAL.get(), R196EntityRenderers.EarthElementalTexture::new);
        event.registerEntityRenderer(ModEntityTypes.R196_ENDERMAN.get(), EndermanRenderer::new);
        event.registerEntityRenderer(
                ModEntityTypes.R196_SQUID.get(),
                context -> new SquidRenderer<>(
                        context,
                        new SquidModel(context.bakeLayer(ModelLayers.SQUID)),
                        new SquidModel(context.bakeLayer(ModelLayers.SQUID_BABY))));
        event.registerEntityRenderer(ModEntityTypes.R196_COD.get(), CodRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.R196_SALMON.get(), SalmonRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.R196_PUFFERFISH.get(), PufferfishRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.R196_TROPICAL_FISH.get(), TropicalFishRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.R196_WITCH.get(), WitchRenderer::new);
        // MITE zombie pigmen keep the humanoid zombie model instead of the modern piglin model.
        event.registerEntityRenderer(
                ModEntityTypes.R196_ZOMBIFIED_PIGLIN.get(), R196EntityRenderers.ZombiePigmanTexture::new);
        event.registerEntityRenderer(ModEntityTypes.R196_BLAZE.get(), R196EntityRenderers.BlazeTexture::new);
        event.registerEntityRenderer(ModEntityTypes.R196_GHAST.get(), R196EntityRenderers.GhastTexture::new);

        // R196 livestock: vanilla models; sick skins when !isWell (MITE).
        event.registerEntityRenderer(ModEntityTypes.R196_COW.get(), R196EntityRenderers.CowTexture::new);
        event.registerEntityRenderer(ModEntityTypes.R196_CHICKEN.get(), R196EntityRenderers.ChickenTexture::new);
        event.registerEntityRenderer(ModEntityTypes.R196_SHEEP.get(), R196EntityRenderers.SheepTexture::new);
        event.registerEntityRenderer(ModEntityTypes.R196_PIG.get(), R196EntityRenderers.PigTexture::new);
        event.registerEntityRenderer(ModEntityTypes.R196_HORSE.get(), HorseRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.R196_OCELOT.get(), OcelotRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.R196_WOLF.get(), WolfRenderer::new);
    }

    /**
     * Livestock isWell must be attached here, not in extractRenderState: createRenderState clears
     * render data immediately after extraction, so only modifiers registered here survive to the
     * sick-skin texture lookup.
     */
    @SubscribeEvent
    private static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        R196EntityRenderers.registerRenderStateModifiers(event);
    }

    @SubscribeEvent
    private static void receiveRecipes(RecipesReceivedEvent event) {
        if (event.getRecipeTypes().contains(ModRecipes.CRAFTING.get())
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
                syncedRecipes.byType(ModRecipes.CRAFTING.get());
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

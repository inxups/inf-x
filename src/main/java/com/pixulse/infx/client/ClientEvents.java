package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.R196SafeBlock;
import com.pixulse.infx.crafting.InferredTimedCraftingRecipe;
import com.pixulse.infx.crafting.TimedCraftingRecipe;
import com.pixulse.infx.entity.R196Slime;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.registry.ModMenus;
import com.pixulse.infx.registry.ModRecipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.client.renderer.block.BuiltInBlockModels;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterBlockModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.client.model.animal.squid.SquidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.SilverfishRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.SquidRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.WitchRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.ZombifiedPiglinRenderer;
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

    /** World and held safes use the same 26.2 chest special model as vanilla chests. */
    @SubscribeEvent
    private static void registerSafeBlockModels(RegisterBlockModelsEvent event) {
        for (var holder : ModBlocks.METAL_SAFES) {
            R196SafeBlock safe = holder.get();
            var texture = InfiniteX.id(safe.material().path());
            event.register(
                    (BuiltInBlockModels.SpecialModelFactory)
                            state -> {
                                Direction facing = state.getValue(BarrelBlock.FACING);
                                if (!facing.getAxis().isHorizontal()) {
                                    facing = Direction.NORTH;
                                }
                                return BuiltInBlockModels.createChest(texture, ChestType.SINGLE, facing);
                            },
                    safe);
        }
    }

    @SubscribeEvent
    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.R196_ZOMBIE.get(), ZombieRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.INVISIBLE_STALKER.get(), context -> new R196EntityRenderers.ZombieTint(context, 0xFF303846));
        event.registerEntityRenderer(ModEntityTypes.GHOUL.get(), context -> new R196EntityRenderers.ZombieTint(context, 0xFF819064));
        event.registerEntityRenderer(ModEntityTypes.SHADOW.get(), context -> new R196EntityRenderers.ZombieTint(context, 0xFF18182A));
        event.registerEntityRenderer(ModEntityTypes.WIGHT.get(), context -> new R196EntityRenderers.ZombieTint(context, 0xFFD8DFD0));
        event.registerEntityRenderer(ModEntityTypes.REVENANT.get(), context -> new R196EntityRenderers.ZombieTint(context, 0xFF8A604A));

        event.registerEntityRenderer(ModEntityTypes.R196_SKELETON.get(), SkeletonRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.LONGDEAD.get(), context -> new R196EntityRenderers.SkeletonTint(context, 0xFFB5A078));
        event.registerEntityRenderer(ModEntityTypes.BONE_LORD.get(), context -> new R196EntityRenderers.SkeletonTint(context, 0xFFB9BEC6));
        event.registerEntityRenderer(ModEntityTypes.ANCIENT_BONE_LORD.get(), context -> new R196EntityRenderers.SkeletonTint(context, 0xFFD2B46B));

        event.registerEntityRenderer(ModEntityTypes.R196_SPIDER.get(), SpiderRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.R196_CAVE_SPIDER.get(), context -> new R196EntityRenderers.SpiderTint(context, 0xFF407E78, 0.5F));
        event.registerEntityRenderer(ModEntityTypes.BLACK_WIDOW_SPIDER.get(), context -> new R196EntityRenderers.SpiderTint(context, 0xFF24151A, 0.6F));
        event.registerEntityRenderer(ModEntityTypes.DEMON_SPIDER.get(), context -> new R196EntityRenderers.SpiderTint(context, 0xFF8E3024));
        event.registerEntityRenderer(ModEntityTypes.WOOD_SPIDER.get(), context -> new R196EntityRenderers.SpiderTint(context, 0xFF7D5D3C, 0.6F));
        event.registerEntityRenderer(ModEntityTypes.PHASE_SPIDER.get(), context -> new R196EntityRenderers.SpiderTint(context, 0xFF493F93, 0.6F));

        event.registerEntityRenderer(ModEntityTypes.R196_CREEPER.get(), CreeperRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.INFERNAL_CREEPER.get(), context -> new R196EntityRenderers.CreeperTint(context, 0xFFA52D20, 1.5F));

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
        event.registerEntityRenderer(ModEntityTypes.NETHERSPAWN.get(), context -> new R196EntityRenderers.SilverfishTint(context, 0xFFD45A30));
        event.registerEntityRenderer(ModEntityTypes.COPPERSPINE.get(), context -> new R196EntityRenderers.SilverfishTint(context, 0xFFB46A32));
        event.registerEntityRenderer(ModEntityTypes.HOARY_SILVERFISH.get(), context -> new R196EntityRenderers.SilverfishTint(context, 0xFFE7EDF0));
        event.registerEntityRenderer(ModEntityTypes.VAMPIRE_BAT.get(), context -> new R196EntityRenderers.BatTint(context, 0xFF704047));
        event.registerEntityRenderer(ModEntityTypes.NIGHTWING.get(), context -> new R196EntityRenderers.BatTint(context, 0xFF292441));
        event.registerEntityRenderer(ModEntityTypes.GIANT_VAMPIRE_BAT.get(), context -> new R196EntityRenderers.BatTint(context, 0xFF8E3540, 2.0F));
        event.registerEntityRenderer(ModEntityTypes.HELLHOUND.get(), context -> new R196EntityRenderers.WolfTint(context, 0xFF5B211B));
        event.registerEntityRenderer(ModEntityTypes.DIRE_WOLF.get(), context -> new R196EntityRenderers.WolfTint(context, 0xFF8A8175));
        event.registerEntityRenderer(ModEntityTypes.FIRE_ELEMENTAL.get(), BlazeRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.EARTH_ELEMENTAL.get(), context -> new R196EntityRenderers.EarthTint(context, 0xFF79644C));
        event.registerEntityRenderer(ModEntityTypes.R196_ENDERMAN.get(), EndermanRenderer::new);
        event.registerEntityRenderer(
                ModEntityTypes.R196_SQUID.get(),
                context -> new SquidRenderer<>(
                        context,
                        new SquidModel(context.bakeLayer(ModelLayers.SQUID)),
                        new SquidModel(context.bakeLayer(ModelLayers.SQUID_BABY))));
        event.registerEntityRenderer(ModEntityTypes.R196_WITCH.get(), WitchRenderer::new);
        event.registerEntityRenderer(
                ModEntityTypes.R196_ZOMBIFIED_PIGLIN.get(),
                context -> new ZombifiedPiglinRenderer(
                        context,
                        ModelLayers.ZOMBIFIED_PIGLIN,
                        ModelLayers.ZOMBIFIED_PIGLIN_BABY,
                        ModelLayers.ZOMBIFIED_PIGLIN_ARMOR,
                        ModelLayers.ZOMBIFIED_PIGLIN_BABY_ARMOR));
        event.registerEntityRenderer(ModEntityTypes.R196_BLAZE.get(), BlazeRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.R196_GHAST.get(), GhastRenderer::new);
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

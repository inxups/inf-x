package com.pixulse.infx.datagen;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.RuneStoneBlock;
import com.pixulse.infx.block.UnderworldPortalBlock;
import com.pixulse.infx.block.SafeBlock;
import com.pixulse.infx.item.Catalog;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.registry.InfXDataComponents;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.block.MetalAnvilBlock;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.conditional.FishingRodCast;
import net.minecraft.client.renderer.item.properties.numeric.UseDuration;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import com.pixulse.infx.client.SafeSpecialRenderer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.NonNull;

final class ModModelProvider extends ModelProvider {
    private static final TextureSlot ANVIL_BODY = TextureSlot.create("body");
    private static final TextureSlot PORTAL = TextureSlot.create("portal");
    private static final Identifier RUNE_GATE_NS = InfiniteX.id("block/underworld_portal_runegate_ns");
    private static final Identifier RUNE_GATE_EW = InfiniteX.id("block/underworld_portal_runegate_ew");
    private static final ModelTemplate METAL_ANVIL_MODEL = new ModelTemplate(
            Optional.of(Identifier.withDefaultNamespace("block/template_anvil")),
            Optional.empty(),
            TextureSlot.TOP,
            ANVIL_BODY);
    private static final ModelTemplate RUNE_GATE_NS_MODEL = new ModelTemplate(
            Optional.of(Identifier.withDefaultNamespace("block/nether_portal_ns")),
            Optional.empty(),
            TextureSlot.PARTICLE,
            PORTAL);
    private static final ModelTemplate RUNE_GATE_EW_MODEL = new ModelTemplate(
            Optional.of(Identifier.withDefaultNamespace("block/nether_portal_ew")),
            Optional.empty(),
            TextureSlot.PARTICLE,
            PORTAL);
    private static final ModelTemplate RED_NETHER_PORTAL_NS_MODEL = new ModelTemplate(
            Optional.of(InfiniteX.id("block/template_red_nether_portal_ns")),
            Optional.empty(),
            TextureSlot.PARTICLE,
            PORTAL);
    private static final ModelTemplate RED_NETHER_PORTAL_EW_MODEL = new ModelTemplate(
            Optional.of(InfiniteX.id("block/template_red_nether_portal_ew")),
            Optional.empty(),
            TextureSlot.PARTICLE,
            PORTAL);
    ModModelProvider(PackOutput output) {
        super(output, InfiniteX.MOD_ID);
    }

    @Override
    protected @NonNull Stream<? extends Holder<Block>> getKnownBlocks() {
        Stream<Block> generated = Stream.of(
                        InfXBlocks.FURNACES.stream().map(block -> (Block) block.value()),
                        InfXBlocks.ORES.stream().map(DeferredHolder::value),
                        InfXBlocks.METAL_STORAGE_BLOCKS.stream().map(DeferredHolder::value),
                        InfXBlocks.METAL_ANVILS.stream().map(block -> (Block) block.value()),
                        InfXBlocks.METAL_SAFES.stream().map(block -> (Block) block.value()),
                        InfXBlocks.WORLD_BLOCKS.stream().map(block -> (Block) block.value()),
                        InfXBlocks.FULLTEXT_BLOCKS.stream().map(block -> (Block) block.value()),
                        InfXBlocks.MITE_RECIPE_BLOCKS.stream().map(block -> (Block) block.value()))
                .flatMap(stream -> stream);
        return Stream.concat(
                generated,
                Stream.of(
                        (Block) InfXBlocks.UNDERWORLD_PORTAL.value(),
                        InfXBlocks.NETHER_PORTAL.value(),
                        InfXBlocks.RETURN_SPAWN_PORTAL.value()))
                .map(BuiltInRegistries.BLOCK::wrapAsHolder);
    }

    @Override
    protected @NonNull Stream<? extends Holder<Item>> getKnownItems() {
        return Stream.of(
                        InfXItems.catalog().entries().stream().map(entry -> entry.holder().value()),
                        InfXItems.FURNACES.stream().map(item -> (Item) item.value()),
                        InfXItems.ORES.stream().map(item -> (Item) item.value()),
                        InfXItems.METAL_STORAGE_BLOCKS.stream().map(item -> (Item) item.value()),
                        InfXItems.METAL_ANVILS.stream().map(item -> (Item) item.value()),
                        InfXItems.WORLD_BLOCKS.stream().map(item -> (Item) item.value()),
                        InfXItems.ENCHANTING_TABLES.stream().map(item -> (Item) item.value()),
                        InfXItems.METAL_SAFES.stream().map(item -> (Item) item.value()),
                        InfXItems.FULLTEXT_BLOCKS.stream().map(item -> (Item) item.value()),
                        InfXItems.MITE_RECIPE_BLOCKS.stream().map(item -> (Item) item.value()),
                        InfXItems.R196_BUCKETS.stream().map(item -> (Item) item.value()),
                        InfXItems.R196_MOB_BUCKETS.stream().map(item -> (Item) item.value()),
                        InfXItems.R196_POWDER_SNOW_BUCKETS.stream().map(item -> (Item) item.value()),
                        InfXItems.R196_RECORDS.stream().map(DeferredHolder::value),
                        InfXItems.GELATINOUS_SPHERES.stream().map(item -> (Item) item.value()),
                        Stream.of(InfXItems.BOTTLE_OF_DISENCHANTING.value()),
                        Stream.concat(
                                Stream.of(InfXItems.FLOUR.value(), InfXItems.WATER_BOWL.value()),
                                InfXItems.R196_FOODS.stream().map(DeferredHolder::value)))
                .flatMap(stream -> stream)
                .map(BuiltInRegistries.ITEM::wrapAsHolder);
    }

    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        InfXBlocks.FURNACES.stream()
                .filter(furnace -> furnace.value() != InfXBlocks.LARGE_CLAY_OVEN.value())
                .forEach(furnace -> blockModels.createFurnace(
                        furnace.value(), TexturedModel.ORIENTABLE_ONLY_TOP));
        generateLargeClayOven(blockModels);
        InfXBlocks.ORES.forEach(ore -> blockModels.createTrivialCube(ore.value()));
        InfXBlocks.METAL_STORAGE_BLOCKS.forEach(block -> blockModels.createTrivialCube(block.value()));
        InfXBlocks.METAL_ANVILS.forEach(anvil -> generateMetalAnvil(blockModels, anvil.value()));
        generateSnowSlab(blockModels);
        blockModels.createCrossBlockWithDefaultItem(
                InfXBlocks.WITHERWOOD.value(), BlockModelGenerators.PlantType.NOT_TINTED);
        generateBlueberryBush(blockModels);
        blockModels.createTrivialCube(InfXBlocks.NETHER_GRAVEL.value());
        blockModels.createTrivialCube(InfXBlocks.CORE.value());
        blockModels.createTrivialBlock(
                InfXBlocks.INFESTED_NETHERRACK.value(),
                TexturedModel.CUBE.updateTexture(mapping -> mapping.put(
                        TextureSlot.ALL,
                        new Material(Identifier.withDefaultNamespace("block/netherrack")))));
        blockModels.createTrivialBlock(
                InfXBlocks.MANTLE.value(),
                TexturedModel.CUBE.updateTexture(mapping -> mapping.put(
                        TextureSlot.ALL,
                        new Material(Identifier.withDefaultNamespace("block/magma")))));
        generateRuneStone(blockModels, itemModels, InfXBlocks.MITHRIL_RUNE_STONE.value(), "mithril");
        generateRuneStone(blockModels, itemModels, InfXBlocks.ADAMANTIUM_RUNE_STONE.value(), "adamantium");
        InfXBlocks.ENCHANTING_TABLES.forEach(table -> {
            var model = BlockModelGenerators.plainVariant(
                    ModelLocationUtils.getModelLocation(Blocks.ENCHANTING_TABLE));
            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(table.value(), model));
            blockModels.registerSimpleItemModel(table.value(), ModelLocationUtils.getModelLocation(Blocks.ENCHANTING_TABLE));
        });
        InfXBlocks.METAL_SAFES.forEach(safe -> generateMetalSafe(blockModels, safe.value()));
        generateUnderworldPortal(blockModels);
        generateRedNetherPortal(blockModels);
        generateRunegatePortal(blockModels, InfXBlocks.RETURN_SPAWN_PORTAL.value());
        InfXItems.catalog().rawEntries().forEach(
                entry -> itemModels.generateFlatItem(entry.holder().value(), ModelTemplates.FLAT_ITEM));
        InfXItems.R196_BUCKETS.forEach(bucket ->
                itemModels.generateFlatItem(bucket.value(), ModelTemplates.FLAT_ITEM));
        InfXItems.R196_MOB_BUCKETS.forEach(bucket ->
                itemModels.generateFlatItem(bucket.value(), ModelTemplates.FLAT_ITEM));
        InfXItems.R196_POWDER_SNOW_BUCKETS.forEach(bucket ->
                itemModels.generateFlatItem(bucket.value(), ModelTemplates.FLAT_ITEM));
        InfXItems.R196_RECORDS.forEach(record ->
                itemModels.generateFlatItem(record.value(), ModelTemplates.FLAT_ITEM));
        generateGelatinousSphereModels(itemModels);
        InfXItems.SPAWN_EGGS.forEach(egg -> itemModels.generateFlatItem(egg.value(), ModelTemplates.FLAT_ITEM));
        itemModels.generateFlatItem(InfXItems.BOTTLE_OF_DISENCHANTING.value(), ModelTemplates.FLAT_ITEM);
        generateR196FoodModels(itemModels);
        for (Catalog.EquipmentEntry entry : InfXItems.catalog().equipmentEntries()) {
            if (entry.key().material() == MiteMaterial.LEATHER
                    && entry.key().type().armorForm() == EquipmentType.ArmorForm.PLATE) {
                itemModels.generateTwoLayerDyedItem(entry.holder().value());
                continue;
            }
            switch (entry.key().type().modelFamily()) {
                case GENERATED -> itemModels.generateFlatItem(entry.holder().value(), ModelTemplates.FLAT_ITEM);
                case HANDHELD ->
                    itemModels.generateFlatItem(entry.holder().value(), ModelTemplates.FLAT_HANDHELD_ITEM);
                case FISHING_ROD -> generateFishingRod(itemModels, entry);
                case BOW -> generateMaterialBow(itemModels, entry);
            }
        }
    }

    private static void generateRuneStone(
            BlockModelGenerators blockModels, ItemModelGenerators itemModels, RuneStoneBlock block, String material) {
        Identifier[] models = new Identifier[RuneStoneBlock.RUNE_COUNT];
        Material obsidian = new Material(Identifier.withDefaultNamespace("block/obsidian"));
        for (int rune = 0; rune < RuneStoneBlock.RUNE_COUNT; rune++) {
            Identifier modelId = ModelLocationUtils.getModelLocation(block, "_" + rune);
            models[rune] = ModelTemplates.CUBE_BOTTOM_TOP.create(
                    modelId,
                    new TextureMapping()
                            .put(TextureSlot.SIDE, new Material(InfiniteX.id(
                                    "block/runestones/" + material + "/" + rune)))
                            .put(TextureSlot.TOP, obsidian)
                            .put(TextureSlot.BOTTOM, obsidian),
                    blockModels.modelOutput);
        }
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(RuneStoneBlock.RUNE)
                        .generate(rune -> BlockModelGenerators.plainVariant(models[rune]))));

        Map<Integer, ItemModel.Unbaked> itemVariants = new LinkedHashMap<>();
        for (int rune = 0; rune < RuneStoneBlock.RUNE_COUNT; rune++) {
            itemVariants.put(rune, ItemModelUtils.plainModel(models[rune]));
        }
        itemModels.itemModelOutput.accept(
                block.asItem(),
                ItemModelUtils.selectBlockItemProperty(
                        RuneStoneBlock.RUNE, ItemModelUtils.plainModel(models[0]), itemVariants));
    }

    private static void generateBlueberryBush(BlockModelGenerators blockModels) {
        Block bush = InfXBlocks.BLUEBERRY_BUSH.value();
        Identifier ripe = ModelTemplates.CROSS.create(
                bush,
                TextureMapping.cross(new Material(InfiniteX.id("block/blueberry_bush"))),
                blockModels.modelOutput);
        Identifier picked = ModelTemplates.CROSS.createWithSuffix(
                bush,
                "_picked",
                TextureMapping.cross(new Material(InfiniteX.id("block/blueberry_bush_picked"))),
                blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(bush)
                .with(PropertyDispatch.initial(SweetBerryBushBlock.AGE)
                        .select(0, BlockModelGenerators.plainVariant(picked))
                        .select(1, BlockModelGenerators.plainVariant(picked))
                        .select(2, BlockModelGenerators.plainVariant(picked))
                        .select(3, BlockModelGenerators.plainVariant(ripe))));
        blockModels.registerSimpleItemModel(bush, picked);
    }

    private static void generateUnderworldPortal(BlockModelGenerators blockModels) {
        Material runegate = new Material(InfiniteX.id("block/runegate"));
        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.PARTICLE, runegate)
                .put(PORTAL, runegate);
        RUNE_GATE_NS_MODEL.create(RUNE_GATE_NS, textures, blockModels.modelOutput);
        RUNE_GATE_EW_MODEL.create(RUNE_GATE_EW, textures, blockModels.modelOutput);
        var vanillaNs = BlockModelGenerators.plainVariant(
                ModelLocationUtils.getModelLocation(Blocks.NETHER_PORTAL, "_ns"));
        var vanillaEw = BlockModelGenerators.plainVariant(
                ModelLocationUtils.getModelLocation(Blocks.NETHER_PORTAL, "_ew"));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(InfXBlocks.UNDERWORLD_PORTAL.value())
                .with(PropertyDispatch.initial(
                                BlockStateProperties.HORIZONTAL_AXIS, UnderworldPortalBlock.RUNE_GATE)
                        .select(Direction.Axis.X, false, vanillaNs)
                        .select(Direction.Axis.Z, false, vanillaEw)
                        .select(Direction.Axis.X, true, BlockModelGenerators.plainVariant(RUNE_GATE_NS))
                                .select(Direction.Axis.Z, true, BlockModelGenerators.plainVariant(RUNE_GATE_EW))));
    }

    private static void generateRunegatePortal(BlockModelGenerators blockModels, Block portal) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(portal)
                .with(PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_AXIS)
                        .select(Direction.Axis.X, BlockModelGenerators.plainVariant(RUNE_GATE_NS))
                        .select(Direction.Axis.Z, BlockModelGenerators.plainVariant(RUNE_GATE_EW))));
    }

    private static void generateRedNetherPortal(BlockModelGenerators blockModels) {
        Material portal = new Material(Identifier.withDefaultNamespace("block/nether_portal"));
        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.PARTICLE, portal)
                .put(PORTAL, portal);
        Identifier redNs = RED_NETHER_PORTAL_NS_MODEL.create(
                InfiniteX.id("block/nether_portal_ns"), textures, blockModels.modelOutput);
        Identifier redEw = RED_NETHER_PORTAL_EW_MODEL.create(
                InfiniteX.id("block/nether_portal_ew"), textures, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(InfXBlocks.NETHER_PORTAL.value())
                .with(PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_AXIS)
                        .select(Direction.Axis.X, BlockModelGenerators.plainVariant(redNs))
                        .select(Direction.Axis.Z, BlockModelGenerators.plainVariant(redEw))));
    }

    private static void generateSnowSlab(BlockModelGenerators blockModels) {
        Material snow = new Material(InfiniteX.id("block/snow_slab"));
        TextureMapping textures = TextureMapping.cube(snow);
        Identifier bottom = ModelTemplates.SLAB_BOTTOM.createWithSuffix(
                InfXBlocks.SNOW_SLAB.value(), "_bottom", textures, blockModels.modelOutput);
        Identifier top = ModelTemplates.SLAB_TOP.create(
                InfXBlocks.SNOW_SLAB.value(), textures, blockModels.modelOutput);
        Identifier full = ModelTemplates.CUBE_ALL.create(
                InfXBlocks.SNOW_SLAB.value(), textures, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(InfXBlocks.SNOW_SLAB.value())
                .with(PropertyDispatch.initial(SlabBlock.TYPE)
                        .select(SlabType.BOTTOM, BlockModelGenerators.plainVariant(bottom))
                        .select(SlabType.TOP, BlockModelGenerators.plainVariant(top))
                        .select(SlabType.DOUBLE, BlockModelGenerators.plainVariant(full))));
        blockModels.registerSimpleItemModel(InfXBlocks.SNOW_SLAB.value(), bottom);
    }

    private static void generateR196FoodModels(ItemModelGenerators models) {
        Map<Item, String> textures = Map.ofEntries(
                Map.entry(InfXItems.FLOUR.value(), "flour"),
                Map.entry(InfXItems.WATER_BOWL.value(), "water_bowl"),
                Map.entry(InfXItems.DOUGH.value(), "dough"),
                Map.entry(InfXItems.SALAD.value(), "salad"),
                Map.entry(InfXItems.BLUEBERRIES.value(), "blueberries"),
                Map.entry(InfXItems.BLUEBERRY_PORRIDGE.value(), "blueberry_porridge"),
                Map.entry(InfXItems.MILK_BOWL.value(), "milk_bowl"),
                Map.entry(InfXItems.CEREAL_PORRIDGE.value(), "cereal_porridge"),
                Map.entry(InfXItems.CHOCOLATE.value(), "chocolate"),
                Map.entry(InfXItems.PUMPKIN_SOUP.value(), "pumpkin_soup"),
                Map.entry(InfXItems.CREAM_OF_MUSHROOM_SOUP.value(), "cream_of_mushroom_soup"),
                Map.entry(InfXItems.ONION.value(), "onion"),
                Map.entry(InfXItems.VEGETABLE_SOUP.value(), "vegetable_soup"),
                Map.entry(InfXItems.CREAM_OF_VEGETABLE_SOUP.value(), "cream_of_vegetable_soup"),
                Map.entry(InfXItems.CHICKEN_SOUP.value(), "chicken_soup"),
                Map.entry(InfXItems.BEEF_STEW.value(), "beef_stew"),
                Map.entry(InfXItems.ORANGE.value(), "orange"),
                Map.entry(InfXItems.FRUIT_ICE.value(), "fruit_ice"),
                Map.entry(InfXItems.CHEESE.value(), "cheese"),
                Map.entry(InfXItems.MASHED_POTATO.value(), "mashed_potato"),
                Map.entry(InfXItems.ICE_CREAM.value(), "ice_cream"),
                Map.entry(InfXItems.BANANA.value(), "banana"),
                Map.entry(InfXItems.WORM.value(), "worm"),
                Map.entry(InfXItems.COOKED_WORM.value(), "cooked_worm"));
        textures.forEach((item, texture) -> {
            Identifier model = ModelTemplates.FLAT_ITEM.create(
                    ModelLocationUtils.getModelLocation(item),
                    TextureMapping.layer0(new Material(InfiniteX.id("item/" + texture))),
                    models.modelOutput);
            models.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
        });
    }

    private static void generateGelatinousSphereModels(ItemModelGenerators models) {
        Map<Item, String> textures = Map.ofEntries(
                Map.entry(InfXItems.GREEN_GELATINOUS_SPHERE.value(), "green"),
                Map.entry(InfXItems.OCHRE_GELATINOUS_SPHERE.value(), "ochre"),
                Map.entry(InfXItems.CRIMSON_GELATINOUS_SPHERE.value(), "crimson"),
                Map.entry(InfXItems.GRAY_GELATINOUS_SPHERE.value(), "gray"),
                Map.entry(InfXItems.BLACK_GELATINOUS_SPHERE.value(), "black"));
        textures.forEach((item, texture) -> {
            Identifier model = ModelTemplates.FLAT_ITEM.create(
                    ModelLocationUtils.getModelLocation(item),
                    TextureMapping.layer0(new Material(InfiniteX.id("item/gelatinous_sphere/" + texture))),
                    models.modelOutput);
            models.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
        });
    }

    /**
     * Vanilla chest split: particle-only block model for chunk meshes, chest special
     * model for inventory, and {@code R196SafeRenderer} BER for the placed block.
     */
    private static void generateMetalSafe(BlockModelGenerators models, SafeBlock safe) {
        Material particle = new Material(safeParticleTexture(safe.material()));
        Identifier blockModel = ModelTemplates.PARTICLE_ONLY.create(
                safe, TextureMapping.particle(particle), models.modelOutput);
        models.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(safe, BlockModelGenerators.plainVariant(blockModel)));
        Identifier itemModelBase = ModelTemplates.CHEST_INVENTORY.create(
                safe.asItem(), TextureMapping.particle(particle), models.modelOutput);
        models.itemModelOutput.accept(
                safe.asItem(),
                ItemModelUtils.specialModel(
                        itemModelBase,
                        new SafeSpecialRenderer.Unbaked(InfiniteX.id(safe.material().path()))));
    }

    private static Identifier safeParticleTexture(MiteMaterial material) {
        return switch (material) {
            case COPPER -> Identifier.withDefaultNamespace("block/copper_block");
            case GOLD -> Identifier.withDefaultNamespace("block/gold_block");
            case IRON -> Identifier.withDefaultNamespace("block/iron_block");
            case SILVER -> InfiniteX.id("block/silver_block");
            case ANCIENT_METAL -> InfiniteX.id("block/ancient_metal_block");
            case MITHRIL -> InfiniteX.id("block/mithril_block");
            case ADAMANTIUM -> InfiniteX.id("block/adamantium_block");
            default -> throw new IllegalArgumentException("No safe particle texture for " + material);
        };
    }

    private static void generateLargeClayOven(BlockModelGenerators models) {
        var normal = BlockModelGenerators.plainVariant(
                ModelLocationUtils.getModelLocation(InfXBlocks.CLAY_FURNACE.value()));
        var lit = BlockModelGenerators.plainVariant(
                ModelLocationUtils.getModelLocation(InfXBlocks.CLAY_FURNACE.value(), "_on"));
        models.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(InfXBlocks.LARGE_CLAY_OVEN.value())
                        .with(BlockModelGenerators.createBooleanModelDispatch(
                                BlockStateProperties.LIT, lit, normal))
                        .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
        models.registerSimpleItemModel(
                InfXBlocks.LARGE_CLAY_OVEN.value(),
                ModelLocationUtils.getModelLocation(InfXBlocks.CLAY_FURNACE.value()));
    }

    private static void generateMetalAnvil(BlockModelGenerators models, MetalAnvilBlock block) {
        PropertyDispatch<net.minecraft.client.data.models.MultiVariant> stages =
                PropertyDispatch.initial(MetalAnvilBlock.DAMAGE_STAGE).generate(stage -> {
                    Identifier body = InfiniteX.id("block/anvil/" + block.material().path() + "/base");
                    Identifier top = InfiniteX.id(
                            "block/anvil/" + block.material().path() + "/top_damaged_" + stage);
                    Identifier model = METAL_ANVIL_MODEL.createWithOverride(
                            block,
                            "_stage_" + stage,
                            new TextureMapping()
                                    .put(ANVIL_BODY, new Material(body))
                                    .put(TextureSlot.TOP, new Material(top))
                                    .putForced(TextureSlot.PARTICLE, new Material(body)),
                            models.modelOutput);
                    return BlockModelGenerators.plainVariant(model);
                });
        models.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block)
                        .with(stages)
                        .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING_ALT));
        models.registerSimpleItemModel(
                block,
                net.minecraft.client.data.models.model.ModelLocationUtils.getModelLocation(block, "_stage_0"));
    }

    private static void generateFishingRod(
            ItemModelGenerators itemModels, Catalog.EquipmentEntry entry) {
        Item item = entry.holder().value();
        Identifier normalId =
                itemModels.createFlatItemModel(item, ModelTemplates.FLAT_HANDHELD_ROD_ITEM);
        Identifier castId = ModelLocationUtils.getModelLocation(item, "_cast");
        ModelTemplates.FLAT_HANDHELD_ROD_ITEM.create(
                castId,
                TextureMapping.layer0(new Material(InfiniteX.id("item/fishing_rod_cast"))),
                itemModels.modelOutput);
        itemModels.itemModelOutput.accept(
                item,
                ItemModelUtils.conditional(
                        new FishingRodCast(),
                        ItemModelUtils.plainModel(castId),
                        ItemModelUtils.plainModel(normalId)));
    }

    private static void generateMaterialBow(
            ItemModelGenerators itemModels, Catalog.EquipmentEntry entry) {
        Item bow = entry.holder().value();
        ItemModel.Unbaked standby = ItemModelUtils.plainModel(bowModel(
                itemModels,
                ModelLocationUtils.getModelLocation(bow),
                InfiniteX.id("item/" + entry.path())));
        EnumMap<MiteMaterial, ItemModel.Unbaked> pulls = new EnumMap<>(MiteMaterial.class);
        for (MiteMaterial material : arrowMaterials()) {
            ItemModel.Unbaked[] frames = new ItemModel.Unbaked[3];
            for (int frame = 0; frame < frames.length; frame++) {
                Identifier id = InfiniteX.id(
                        "item/" + entry.path() + "/" + material.path() + "_" + frame);
                frames[frame] = ItemModelUtils.plainModel(bowModel(itemModels, id, id));
            }
            pulls.put(material, pull(frames[0], frames[1], frames[2]));
        }

        ItemModel.Unbaked nocked = ItemModelUtils.select(
                new ComponentContents<>(InfXDataComponents.NOCKED_ARROW_MATERIAL.get()),
                pulls.get(MiteMaterial.FLINT),
                arrowMaterials().stream()
                        .map(material -> ItemModelUtils.when(material.path(), pulls.get(material)))
                        .toList());
        itemModels.itemModelOutput.accept(
                bow,
                ItemModelUtils.conditional(ItemModelUtils.isUsingItem(), nocked, standby));
    }

    private static Identifier bowModel(
            ItemModelGenerators itemModels, Identifier modelId, Identifier textureId) {
        return ModelTemplates.BOW.create(
                modelId,
                TextureMapping.layer0(new Material(textureId)),
                itemModels.modelOutput);
    }

    private static ItemModel.Unbaked pull(
            ItemModel.Unbaked frame0, ItemModel.Unbaked frame1, ItemModel.Unbaked frame2) {
        return ItemModelUtils.rangeSelect(
                new UseDuration(false),
                .05F,
                frame0,
                ItemModelUtils.override(frame1, .65F),
                ItemModelUtils.override(frame2, .9F));
    }

    private static List<MiteMaterial> arrowMaterials() {
        return Arrays.stream(MiteMaterial.values())
                .filter(EquipmentType.ARROW::allows)
                .toList();
    }
}

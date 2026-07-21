package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.R196SafeBlock;
import com.pixulse.infx.item.R196Catalog;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.registry.ModDataComponents;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.block.MetalAnvilBlock;
import java.util.Arrays;
import java.util.EnumMap;
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
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

final class ModModelProvider extends ModelProvider {
    private static final TextureSlot ANVIL_BODY = TextureSlot.create("body");
    private static final ModelTemplate METAL_ANVIL_MODEL = new ModelTemplate(
            Optional.of(Identifier.withDefaultNamespace("block/template_anvil")),
            Optional.empty(),
            TextureSlot.TOP,
            ANVIL_BODY);
    ModModelProvider(PackOutput output) {
        super(output, InfiniteX.MOD_ID);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        Stream<Block> generated = Stream.of(
                        ModBlocks.FURNACES.stream().map(block -> (Block) block.value()),
                        ModBlocks.ORES.stream().map(block -> (Block) block.value()),
                        ModBlocks.METAL_STORAGE_BLOCKS.stream().map(block -> (Block) block.value()),
                        ModBlocks.METAL_ANVILS.stream().map(block -> (Block) block.value()),
                        ModBlocks.METAL_SAFES.stream().map(block -> (Block) block.value()),
                        ModBlocks.WORLD_BLOCKS.stream().map(block -> (Block) block.value()),
                        ModBlocks.R196_FLOWERS.stream().map(block -> (Block) block.value()),
                        ModBlocks.FULLTEXT_BLOCKS.stream().map(block -> (Block) block.value()))
                .flatMap(stream -> stream);
        return Stream.concat(
                generated,
                Stream.of((Block) ModBlocks.UNDERWORLD_PORTAL.value()))
                .map(BuiltInRegistries.BLOCK::wrapAsHolder);
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return Stream.of(
                        ModItems.catalog().entries().stream().map(entry -> entry.holder().value()),
                        ModItems.FURNACES.stream().map(item -> (Item) item.value()),
                        ModItems.ORES.stream().map(item -> (Item) item.value()),
                        ModItems.METAL_STORAGE_BLOCKS.stream().map(item -> (Item) item.value()),
                        ModItems.METAL_ANVILS.stream().map(item -> (Item) item.value()),
                        ModItems.WORLD_BLOCKS.stream().map(item -> (Item) item.value()),
                        ModItems.ENCHANTING_TABLES.stream().map(item -> (Item) item.value()),
                        ModItems.METAL_SAFES.stream().map(item -> (Item) item.value()),
                        ModItems.R196_FLOWERS.stream().map(item -> (Item) item.value()),
                        ModItems.FULLTEXT_BLOCKS.stream().map(item -> (Item) item.value()),
                        ModItems.R196_BUCKETS.stream().map(item -> (Item) item.value()),
                        ModItems.R196_RECORDS.stream().map(item -> (Item) item.value()),
                        Stream.of(ModItems.BOTTLE_OF_DISENCHANTING.value()),
                        Stream.concat(
                                Stream.of(ModItems.FLOUR.value(), ModItems.WATER_BOWL.value()),
                                ModItems.R196_FOODS.stream().map(item -> item.value())))
                .flatMap(stream -> stream)
                .map(BuiltInRegistries.ITEM::wrapAsHolder);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        ModBlocks.FURNACES.stream()
                .filter(furnace -> furnace.value() != ModBlocks.LARGE_CLAY_OVEN.value())
                .forEach(furnace -> blockModels.createFurnace(
                        furnace.value(), TexturedModel.ORIENTABLE_ONLY_TOP));
        generateLargeClayOven(blockModels);
        ModBlocks.ORES.forEach(ore -> blockModels.createTrivialCube(ore.value()));
        ModBlocks.METAL_STORAGE_BLOCKS.forEach(block -> blockModels.createTrivialCube(block.value()));
        ModBlocks.METAL_ANVILS.forEach(anvil -> generateMetalAnvil(blockModels, anvil.value()));
        ModBlocks.R196_FLOWERS.forEach(flower -> blockModels.createCrossBlockWithDefaultItem(
                flower.value(), BlockModelGenerators.PlantType.NOT_TINTED));
        blockModels.createCrossBlockWithDefaultItem(
                ModBlocks.WITHERWOOD.value(), BlockModelGenerators.PlantType.NOT_TINTED);
        blockModels.createTrivialCube(ModBlocks.NETHER_GRAVEL.value());
        blockModels.createTrivialCube(ModBlocks.CORE.value());
        blockModels.createTrivialBlock(
                ModBlocks.INFESTED_NETHERRACK.value(),
                TexturedModel.CUBE.updateTexture(mapping -> mapping.put(
                        TextureSlot.ALL,
                        new Material(Identifier.withDefaultNamespace("block/netherrack")))));
        blockModels.createTrivialBlock(
                ModBlocks.MANTLE.value(),
                TexturedModel.CUBE.updateTexture(mapping -> mapping.put(
                        TextureSlot.ALL,
                        new Material(Identifier.withDefaultNamespace("block/magma")))));
        blockModels.createTrivialBlock(
                ModBlocks.MITHRIL_RUNE_STONE.value(),
                TexturedModel.CUBE.updateTexture(mapping -> mapping.put(
                        TextureSlot.ALL,
                        new Material(InfiniteX.id("block/mithril_block")))));
        blockModels.createTrivialBlock(
                ModBlocks.ADAMANTIUM_RUNE_STONE.value(),
                TexturedModel.CUBE.updateTexture(mapping -> mapping.put(
                        TextureSlot.ALL,
                        new Material(InfiniteX.id("block/adamantium_block")))));
        ModBlocks.ENCHANTING_TABLES.forEach(table -> {
            var model = BlockModelGenerators.plainVariant(
                    ModelLocationUtils.getModelLocation(Blocks.ENCHANTING_TABLE));
            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(table.value(), model));
            blockModels.registerSimpleItemModel(table.value(), ModelLocationUtils.getModelLocation(Blocks.ENCHANTING_TABLE));
        });
        ModBlocks.METAL_SAFES.forEach(safe -> generateMetalSafe(blockModels, safe.value()));
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(ModBlocks.UNDERWORLD_PORTAL.value())
                        .with(PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_AXIS)
                                .select(
                                        Direction.Axis.X,
                                        BlockModelGenerators.plainVariant(
                                                net.minecraft.client.data.models.model.ModelLocationUtils.getModelLocation(
                                                        Blocks.NETHER_PORTAL, "_ns")))
                                .select(
                                        Direction.Axis.Z,
                                        BlockModelGenerators.plainVariant(
                                                net.minecraft.client.data.models.model.ModelLocationUtils.getModelLocation(
                                                        Blocks.NETHER_PORTAL, "_ew")))));
        ModItems.catalog().rawEntries().forEach(
                entry -> itemModels.generateFlatItem(entry.holder().value(), ModelTemplates.FLAT_ITEM));
        ModItems.R196_BUCKETS.forEach(bucket ->
                itemModels.generateFlatItem(bucket.value(), ModelTemplates.FLAT_ITEM));
        ModItems.R196_RECORDS.forEach(record ->
                itemModels.generateFlatItem(record.value(), ModelTemplates.FLAT_ITEM));
        itemModels.generateFlatItem(ModItems.BOTTLE_OF_DISENCHANTING.value(), ModelTemplates.FLAT_ITEM);
        generateR196FoodModels(itemModels);
        for (R196Catalog.EquipmentEntry entry : ModItems.catalog().equipmentEntries()) {
            if (entry.key().material() == R196Material.LEATHER
                    && entry.key().type().armorForm() == R196EquipmentType.ArmorForm.PLATE) {
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

    private static void generateR196FoodModels(ItemModelGenerators models) {
        Map<Item, String> textures = Map.ofEntries(
                Map.entry(ModItems.FLOUR.value(), "flour"),
                Map.entry(ModItems.WATER_BOWL.value(), "water_bowl"),
                Map.entry(ModItems.DOUGH.value(), "dough"),
                Map.entry(ModItems.SALAD.value(), "salad"),
                Map.entry(ModItems.BLUEBERRIES.value(), "blueberries"),
                Map.entry(ModItems.BLUEBERRY_PORRIDGE.value(), "blueberry_porridge"),
                Map.entry(ModItems.MILK_BOWL.value(), "milk_bowl"),
                Map.entry(ModItems.CEREAL_PORRIDGE.value(), "cereal_porridge"),
                Map.entry(ModItems.CHOCOLATE.value(), "chocolate"),
                Map.entry(ModItems.PUMPKIN_SOUP.value(), "pumpkin_soup"),
                Map.entry(ModItems.CREAM_OF_MUSHROOM_SOUP.value(), "cream_of_mushroom_soup"),
                Map.entry(ModItems.ONION.value(), "onion"),
                Map.entry(ModItems.VEGETABLE_SOUP.value(), "vegetable_soup"),
                Map.entry(ModItems.CREAM_OF_VEGETABLE_SOUP.value(), "cream_of_vegetable_soup"),
                Map.entry(ModItems.CHICKEN_SOUP.value(), "chicken_soup"),
                Map.entry(ModItems.BEEF_STEW.value(), "beef_stew"),
                Map.entry(ModItems.ORANGE.value(), "orange"),
                Map.entry(ModItems.FRUIT_ICE.value(), "fruit_ice"),
                Map.entry(ModItems.CHEESE.value(), "cheese"),
                Map.entry(ModItems.MASHED_POTATO.value(), "mashed_potato"),
                Map.entry(ModItems.ICE_CREAM.value(), "ice_cream"),
                Map.entry(ModItems.BANANA.value(), "banana"),
                Map.entry(ModItems.WORM.value(), "worm"),
                Map.entry(ModItems.COOKED_WORM.value(), "cooked_worm"));
        textures.forEach((item, texture) -> {
            Identifier model = ModelTemplates.FLAT_ITEM.create(
                    ModelLocationUtils.getModelLocation(item),
                    TextureMapping.layer0(new Material(InfiniteX.id("item/" + texture))),
                    models.modelOutput);
            models.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
        });
    }

    private static void generateMetalSafe(BlockModelGenerators models, R196SafeBlock safe) {
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
                        new ChestSpecialRenderer.Unbaked(InfiniteX.id(safe.material().path()))));
    }

    private static Identifier safeParticleTexture(R196Material material) {
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
                ModelLocationUtils.getModelLocation(ModBlocks.CLAY_FURNACE.value()));
        var lit = BlockModelGenerators.plainVariant(
                ModelLocationUtils.getModelLocation(ModBlocks.CLAY_FURNACE.value(), "_on"));
        models.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(ModBlocks.LARGE_CLAY_OVEN.value())
                        .with(BlockModelGenerators.createBooleanModelDispatch(
                                BlockStateProperties.LIT, lit, normal))
                        .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
        models.registerSimpleItemModel(
                ModBlocks.LARGE_CLAY_OVEN.value(),
                ModelLocationUtils.getModelLocation(ModBlocks.CLAY_FURNACE.value()));
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
            ItemModelGenerators itemModels, R196Catalog.EquipmentEntry entry) {
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
            ItemModelGenerators itemModels, R196Catalog.EquipmentEntry entry) {
        Item bow = entry.holder().value();
        ItemModel.Unbaked standby = ItemModelUtils.plainModel(bowModel(
                itemModels,
                ModelLocationUtils.getModelLocation(bow),
                InfiniteX.id("item/" + entry.path())));
        EnumMap<R196Material, ItemModel.Unbaked> pulls = new EnumMap<>(R196Material.class);
        for (R196Material material : arrowMaterials()) {
            ItemModel.Unbaked[] frames = new ItemModel.Unbaked[3];
            for (int frame = 0; frame < frames.length; frame++) {
                Identifier id = InfiniteX.id(
                        "item/" + entry.path() + "/" + material.path() + "_" + frame);
                frames[frame] = ItemModelUtils.plainModel(bowModel(itemModels, id, id));
            }
            pulls.put(material, pull(frames[0], frames[1], frames[2]));
        }

        ItemModel.Unbaked nocked = ItemModelUtils.select(
                new ComponentContents<>(ModDataComponents.NOCKED_ARROW_MATERIAL.get()),
                pulls.get(R196Material.FLINT),
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

    private static List<R196Material> arrowMaterials() {
        return Arrays.stream(R196Material.values())
                .filter(R196EquipmentType.ARROW::allows)
                .toList();
    }
}

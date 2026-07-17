package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.R196Catalog;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModDataComponents;
import com.pixulse.infx.registry.ModItems;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.conditional.FishingRodCast;
import net.minecraft.client.renderer.item.properties.numeric.UseDuration;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

final class ModModelProvider extends ModelProvider {
    ModModelProvider(PackOutput output) {
        super(output, InfiniteX.MOD_ID);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.empty();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return ModItems.catalog().entries().stream()
                .map(entry -> BuiltInRegistries.ITEM.wrapAsHolder(entry.holder().value()));
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        ModItems.catalog().rawEntries().forEach(
                entry -> itemModels.generateFlatItem(entry.holder().value(), ModelTemplates.FLAT_ITEM));
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

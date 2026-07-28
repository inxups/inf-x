package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.MiteMaterial;
import java.util.Arrays;
import java.util.function.BiConsumer;
import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

final class ModEquipmentAssetProvider extends EquipmentAssetProvider {
    ModEquipmentAssetProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void registerModels(
            BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output) {
        Arrays.stream(MiteMaterial.values())
                .filter(EquipmentType.HELMET::allows)
                .forEach(material -> registerPlate(output, material));
        Arrays.stream(MiteMaterial.values())
                .filter(EquipmentType.CHAINMAIL_HELMET::allows)
                .forEach(material -> output.accept(
                        chainAssetKey(material),
                        EquipmentClientInfo.builder()
                                .addHumanoidLayers(InfiniteX.id(material.path() + "_chainmail"))
                                .build()));
    }

    private static void registerPlate(
            BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output,
            MiteMaterial material) {
        EquipmentClientInfo.Builder builder = EquipmentClientInfo.builder()
                .addHumanoidLayers(InfiniteX.id(material.path()), material == MiteMaterial.LEATHER);
        if (material == MiteMaterial.LEATHER) {
            builder.addHumanoidLayers(InfiniteX.id("leather_overlay"), false);
        }
        if (EquipmentType.HORSE_ARMOR.allows(material)) {
            builder.addLayers(
                    EquipmentClientInfo.LayerType.HORSE_BODY,
                    new EquipmentClientInfo.Layer(InfiniteX.id(material.path())));
        }
        output.accept(plateAssetKey(material), builder.build());
    }

    private static ResourceKey<EquipmentAsset> plateAssetKey(MiteMaterial material) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, InfiniteX.id(material.path()));
    }

    private static ResourceKey<EquipmentAsset> chainAssetKey(MiteMaterial material) {
        return ResourceKey.create(
                EquipmentAssets.ROOT_ID, InfiniteX.id(material.path() + "_chainmail"));
    }
}

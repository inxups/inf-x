package com.pixulse.infx.item;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.harvest.ToolWearCalculator;
import com.pixulse.infx.material.R196Material;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public record R196EquipmentKey(R196Material material, R196EquipmentType type) {
    public R196EquipmentKey {
        Objects.requireNonNull(material, "material");
        Objects.requireNonNull(type, "type");
        if (!type.allows(material)) {
            throw new IllegalArgumentException("Illegal R196 equipment key: " + material.path() + "_" + type.path());
        }
    }

    public String path() {
        return material.path() + "_" + type.path();
    }

    public int durability() {
        if (type == R196EquipmentType.BOW) {
            return switch (material) {
                case WOOD -> 32;
                case ANCIENT_METAL -> 64;
                case MITHRIL -> 128;
                default -> throw new IllegalStateException("Illegal bow key " + path());
            };
        }
        if (type == R196EquipmentType.FISHING_ROD) {
            return (int) (2.0F * material.durabilityMultiplier()) + (material == R196Material.FLINT ? 1 : 0);
        }
        if (type.armorForm() == R196EquipmentType.ArmorForm.PLATE) {
            return (int) (type.durabilityComponents() * material.durabilityMultiplier() * 2.0F);
        }
        if (type.armorForm() == R196EquipmentType.ArmorForm.CHAIN) {
            return (int) (type.durabilityComponents() * material.durabilityMultiplier());
        }
        if (type == R196EquipmentType.ARROW || type.armorForm() == R196EquipmentType.ArmorForm.HORSE) {
            return 0;
        }
        return material.toolDurability(type.durabilityComponents());
    }

    public float miningSpeed() {
        return material.miningSpeed(type.miningMultiplier());
    }

    public float meleeDamage() {
        return material.meleeDamage(type.baseDamage());
    }

    public int attackWear() {
        return Math.max((int) (100.0F * type.attackDecay()), 1);
    }

    public int damageForBreaking(float hardness) {
        return ToolWearCalculator.damageForBreaking(hardness, type.blockDecay());
    }

    public double arrowBaseDamage() {
        return .5D + material.materialDamage() * .5D;
    }

    public float armorProtection() {
        return switch (type.armorForm()) {
            case PLATE -> type.durabilityComponents() * material.plateProtection() / 24.0F;
            case CHAIN -> type.durabilityComponents() * (material.plateProtection() - 2.0F) / 24.0F;
            case HORSE -> material.horseProtection();
            case NONE -> 0.0F;
        };
    }

    public String englishName() {
        if (material == R196Material.COPPER && type == R196EquipmentType.PICKAXE) {
            return "InfiniteX Copper Pickaxe";
        }
        if (type == R196EquipmentType.FISHING_ROD) {
            return "Fishing Rod";
        }
        if (material == R196Material.WOOD && type == R196EquipmentType.BOW) {
            return "Bow";
        }
        String materialName = type.armorForm() == R196EquipmentType.ArmorForm.HORSE
                ? material.englishNoun()
                : material.englishEquipmentPrefix();
        return materialName + " " + type.englishName();
    }

    public String chineseName() {
        if (material == R196Material.COPPER && type == R196EquipmentType.PICKAXE) {
            return "InfiniteX 铜镐";
        }
        if (type == R196EquipmentType.FISHING_ROD) {
            return "钓鱼竿";
        }
        if (material == R196Material.WOOD && type == R196EquipmentType.BOW) {
            return "弓";
        }
        if (material == R196Material.WOOD && type == R196EquipmentType.CUDGEL) {
            return "短木棒";
        }
        if (material == R196Material.WOOD && type == R196EquipmentType.CLUB) {
            return "木棒";
        }
        return material.chinesePrefix() + type.chineseSuffix();
    }

    public String translationKey() {
        return "item.infx." + path();
    }

    public ResourceKey<EquipmentAsset> equipmentAsset() {
        String assetPath = switch (type.armorForm()) {
            case PLATE, HORSE -> material.path();
            case CHAIN -> material.path() + "_chainmail";
            case NONE -> throw new IllegalStateException("No equipment asset for " + path());
        };
        return ResourceKey.create(EquipmentAssets.ROOT_ID, InfiniteX.id(assetPath));
    }

    public static List<R196EquipmentKey> all() {
        return Holder.ALL;
    }

    private static final class Holder {
        private static final List<R196EquipmentKey> ALL = Arrays.stream(R196Material.values())
                .flatMap(material -> Arrays.stream(R196EquipmentType.values())
                        .filter(type -> type.allows(material))
                        .map(type -> new R196EquipmentKey(material, type)))
                .toList();
    }
}

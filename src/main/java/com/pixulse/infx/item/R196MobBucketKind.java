package com.pixulse.infx.item;

import com.pixulse.infx.registry.ModEntityTypes;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

/** Bucketable water-mob contents that keep the R196 bucket material. */
public enum R196MobBucketKind {
    COD("cod", "Cod", "鳕鱼", () -> ModEntityTypes.R196_COD.get(), EntityType.COD, SoundEvents.BUCKET_EMPTY_FISH, Foods.COD),
    SALMON("salmon", "Salmon", "鲑鱼", () -> ModEntityTypes.R196_SALMON.get(), EntityType.SALMON, SoundEvents.BUCKET_EMPTY_FISH, Foods.SALMON),
    PUFFERFISH(
            "pufferfish",
            "Pufferfish",
            "河豚",
            () -> ModEntityTypes.R196_PUFFERFISH.get(),
            EntityType.PUFFERFISH,
            SoundEvents.BUCKET_EMPTY_FISH,
            Foods.PUFFERFISH),
    TROPICAL(
            "tropical",
            "Tropical Fish",
            "热带鱼",
            () -> ModEntityTypes.R196_TROPICAL_FISH.get(),
            EntityType.TROPICAL_FISH,
            SoundEvents.BUCKET_EMPTY_FISH,
            Foods.TROPICAL_FISH),
    AXOLOTL("axolotl", "Axolotl", "美西螈", () -> EntityType.AXOLOTL, EntityType.AXOLOTL, SoundEvents.BUCKET_EMPTY_AXOLOTL, null),
    TADPOLE("tadpole", "Tadpole", "蝌蚪", () -> EntityType.TADPOLE, EntityType.TADPOLE, SoundEvents.BUCKET_EMPTY_TADPOLE, null);

    private final String pathPrefix;
    private final String englishName;
    private final String chineseName;
    private final Supplier<? extends EntityType<? extends Mob>> type;
    private final EntityType<?> legacyType;
    private final SoundEvent emptySound;
    private final @Nullable FoodProperties food;

    R196MobBucketKind(
            String pathPrefix,
            String englishName,
            String chineseName,
            Supplier<? extends EntityType<? extends Mob>> type,
            EntityType<?> legacyType,
            SoundEvent emptySound,
            @Nullable FoodProperties food) {
        this.pathPrefix = pathPrefix;
        this.englishName = englishName;
        this.chineseName = chineseName;
        this.type = type;
        this.legacyType = legacyType;
        this.emptySound = emptySound;
        this.food = food;
    }

    public String pathPrefix() {
        return pathPrefix;
    }

    public String path(com.pixulse.infx.material.R196Material material) {
        return pathPrefix + "_" + material.path() + "_bucket";
    }

    public String englishName() {
        return englishName;
    }

    public String chineseName() {
        return chineseName;
    }

    public EntityType<? extends Mob> entityType() {
        return type.get();
    }

    public SoundEvent emptySound() {
        return emptySound;
    }

    public Fluid fluid() {
        return Fluids.WATER;
    }

    public @Nullable FoodProperties food() {
        return food;
    }

    public static @Nullable R196MobBucketKind of(EntityType<?> type) {
        for (R196MobBucketKind kind : values()) {
            if (kind.entityType() == type || kind.legacyType == type) {
                return kind;
            }
        }
        return null;
    }
}

package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.entity.R196Bat;
import com.pixulse.infx.entity.R196Blaze;
import com.pixulse.infx.entity.R196Chicken;
import com.pixulse.infx.entity.R196ClayGolem;
import com.pixulse.infx.entity.R196Cow;
import com.pixulse.infx.entity.R196Cod;
import com.pixulse.infx.entity.R196Creeper;
import com.pixulse.infx.entity.R196EarthElemental;
import com.pixulse.infx.entity.R196Enderman;
import com.pixulse.infx.entity.R196FireElemental;
import com.pixulse.infx.entity.R196GelatinousSphere;
import com.pixulse.infx.entity.R196Ghast;
import com.pixulse.infx.entity.R196Horse;
import com.pixulse.infx.entity.R196MagmaCube;
import com.pixulse.infx.entity.R196Ocelot;
import com.pixulse.infx.entity.R196Pufferfish;
import com.pixulse.infx.entity.R196Pig;
import com.pixulse.infx.entity.R196Salmon;
import com.pixulse.infx.entity.R196Sheep;
import com.pixulse.infx.entity.R196Silverfish;
import com.pixulse.infx.entity.R196Skeleton;
import com.pixulse.infx.entity.R196Slime;
import com.pixulse.infx.entity.R196Spider;
import com.pixulse.infx.entity.R196Squid;
import com.pixulse.infx.entity.R196TropicalFish;
import com.pixulse.infx.entity.R196VanillaWolf;
import com.pixulse.infx.entity.R196Witch;
import com.pixulse.infx.entity.R196Wolf;
import com.pixulse.infx.entity.R196Zombie;
import com.pixulse.infx.entity.R196ZombifiedPiglin;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Entity type registry for original-mob replacements and the 29-item R196 roster. */
public final class ModEntityTypes {
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(InfiniteX.MOD_ID);
    private static final List<EntityName> NAMES = new ArrayList<>();

    public static final DeferredHolder<EntityType<?>, EntityType<R196Zombie>> R196_ZOMBIE = register(
            "r196_zombie", "Zombie", "僵尸", R196Zombie::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Skeleton>> R196_SKELETON = register(
            "r196_skeleton", "Skeleton", "骷髅", R196Skeleton::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Spider>> R196_SPIDER = register(
            "r196_spider", "Spider", "蜘蛛", R196Spider::new, 1.4F, 0.9F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Spider>> R196_CAVE_SPIDER = register(
            "r196_cave_spider", "Cave Spider", "洞穴蜘蛛", R196Spider::new, 0.98F, 0.63F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Creeper>> R196_CREEPER = register(
            "r196_creeper", "Creeper", "苦力怕", R196Creeper::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Slime>> R196_SLIME = register(
            "r196_slime", "Slime", "史莱姆", R196Slime::new, 0.5F, 0.5F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Enderman>> R196_ENDERMAN = register(
            "r196_enderman", "Enderman", "末影人", R196Enderman::new, 0.6F, 2.9F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Squid>> R196_SQUID = register(
            "r196_squid", "Squid", "鱿鱼", R196Squid::new, MobCategory.WATER_CREATURE, 0.95F, 0.95F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Cod>> R196_COD = registerWaterAmbient(
            "r196_cod", "Cod", "鳕鱼", R196Cod::new, 0.5F, 0.3F, 0.195F);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Salmon>> R196_SALMON = registerWaterAmbient(
            "r196_salmon", "Salmon", "鲑鱼", R196Salmon::new, 0.7F, 0.4F, 0.26F);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Pufferfish>> R196_PUFFERFISH = registerWaterAmbient(
            "r196_pufferfish", "Pufferfish", "河豚", R196Pufferfish::new, 0.7F, 0.7F, 0.455F);
    public static final DeferredHolder<EntityType<?>, EntityType<R196TropicalFish>> R196_TROPICAL_FISH = registerWaterAmbient(
            "r196_tropical_fish", "Tropical Fish", "热带鱼", R196TropicalFish::new, 0.5F, 0.4F, 0.26F);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Witch>> R196_WITCH = register(
            "r196_witch", "Witch", "女巫", R196Witch::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196ZombifiedPiglin>> R196_ZOMBIFIED_PIGLIN = register(
            "r196_zombified_piglin", "Zombie Pigman", "僵尸猪人", R196ZombifiedPiglin::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Blaze>> R196_BLAZE = register(
            "r196_blaze", "Blaze", "烈焰人", R196Blaze::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Ghast>> R196_GHAST = register(
            "r196_ghast", "Ghast", "恶魂", R196Ghast::new, 4.0F, 4.0F, true);

    public static final DeferredHolder<EntityType<?>, EntityType<R196Cow>> R196_COW = register(
            "r196_cow", "Cow", "牛", R196Cow::new, MobCategory.CREATURE, 0.9F, 1.4F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Chicken>> R196_CHICKEN = register(
            "r196_chicken", "Chicken", "鸡", R196Chicken::new, MobCategory.CREATURE, 0.4F, 0.7F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Sheep>> R196_SHEEP = register(
            "r196_sheep", "Sheep", "羊", R196Sheep::new, MobCategory.CREATURE, 0.9F, 1.3F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Pig>> R196_PIG = register(
            "r196_pig", "Pig", "猪", R196Pig::new, MobCategory.CREATURE, 0.9F, 0.9F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Horse>> R196_HORSE = register(
            "r196_horse", "Horse", "马", R196Horse::new, MobCategory.CREATURE, 1.3964844F, 1.6F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Ocelot>> R196_OCELOT = register(
            "r196_ocelot", "Ocelot", "豹猫", R196Ocelot::new, MobCategory.CREATURE, 0.6F, 0.7F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196VanillaWolf>> R196_WOLF = register(
            "r196_wolf", "Wolf", "狼", R196VanillaWolf::new, MobCategory.CREATURE, 0.6F, 0.85F, false, true);

    public static final DeferredHolder<EntityType<?>, EntityType<R196Zombie>> INVISIBLE_STALKER = register(
            "invisible_stalker", "Invisible Stalker", "影子潜伏者", R196Zombie::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Zombie>> GHOUL = register(
            "ghoul", "Ghoul", "食尸鬼", R196Zombie::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Zombie>> SHADOW = register(
            "shadow", "Shadow", "黑色食尸鬼", R196Zombie::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Zombie>> WIGHT = register(
            "wight", "Wight", "尸妖", R196Zombie::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Zombie>> REVENANT = register(
            "revenant", "Revenant", "亡魂", R196Zombie::new, 0.6F, 1.8F, false);

    public static final DeferredHolder<EntityType<?>, EntityType<R196Skeleton>> LONGDEAD = register(
            "longdead", "Longdead", "古尸", R196Skeleton::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Skeleton>> BONE_LORD = register(
            "bone_lord", "Bone Lord", "骷髅领主", R196Skeleton::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Skeleton>> ANCIENT_BONE_LORD = register(
            "ancient_bone_lord", "Ancient Bone Lord", "远古骷髅领主", R196Skeleton::new, 0.6F, 1.8F, false);

    public static final DeferredHolder<EntityType<?>, EntityType<R196Spider>> BLACK_WIDOW_SPIDER = register(
            "black_widow_spider", "Black Widow Spider", "黑寡妇蜘蛛", R196Spider::new, 0.84F, 0.54F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Spider>> DEMON_SPIDER = register(
            "demon_spider", "Demon Spider", "恶魔蜘蛛", R196Spider::new, 1.4F, 0.9F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Spider>> WOOD_SPIDER = register(
            "wood_spider", "Wood Spider", "木蜘蛛", R196Spider::new, 0.84F, 0.54F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Spider>> PHASE_SPIDER = register(
            "phase_spider", "Phase Spider", "相位蜘蛛", R196Spider::new, 0.84F, 0.54F, false);

    public static final DeferredHolder<EntityType<?>, EntityType<R196Creeper>> INFERNAL_CREEPER = register(
            "infernal_creeper", "Infernal Creeper", "地狱爬行者", R196Creeper::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196FireElemental>> FIRE_ELEMENTAL = register(
            "fire_elemental", "Fire Elemental", "火元素", R196FireElemental::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196EarthElemental>> EARTH_ELEMENTAL = register(
            "earth_elemental", "Earth Elemental", "土元素", R196EarthElemental::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196ClayGolem>> CLAY_GOLEM = register(
            "clay_golem", "Clay Golem", "黏土元素", R196ClayGolem::new, 0.6F, 1.8F, true);

    public static final DeferredHolder<EntityType<?>, EntityType<R196Slime>> JELLY = register(
            "jelly", "Jelly", "褐色史莱姆", R196Slime::new, 0.5F, 0.5F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Slime>> BLOB = register(
            "blob", "Blob", "红色史莱姆", R196Slime::new, 0.5F, 0.5F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Slime>> OOZE = register(
            "ooze", "Ooze", "灰色史莱姆", R196Slime::new, 0.5F, 0.5F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Slime>> PUDDING = register(
            "pudding", "Pudding", "黑色史莱姆", R196Slime::new, 0.5F, 0.5F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196GelatinousSphere>> GELATINOUS_SPHERE =
            ENTITIES.registerEntityType(
                    "gelatinous_sphere",
                    R196GelatinousSphere::new,
                    MobCategory.MISC,
                    builder -> builder.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
    public static final DeferredHolder<EntityType<?>, EntityType<R196MagmaCube>> MAGMA_CUBE = register(
            "magma_cube", "Magma Cube", "岩浆怪", R196MagmaCube::new, 0.5F, 0.5F, true);

    public static final DeferredHolder<EntityType<?>, EntityType<R196Silverfish>> NETHERSPAWN = register(
            "netherspawn", "Netherspawn", "爆炸蠹虫", R196Silverfish::new, 0.3F, 0.7F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Silverfish>> COPPERSPINE = register(
            "copperspine", "Copperspine", "铜毒蠹虫", R196Silverfish::new, 0.3F, 0.7F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Silverfish>> HOARY_SILVERFISH = register(
            "hoary_silverfish", "Hoary Silverfish", "白化蠹虫", R196Silverfish::new, 0.3F, 0.7F, false);

    public static final DeferredHolder<EntityType<?>, EntityType<R196Bat>> VAMPIRE_BAT = register(
            "vampire_bat", "Vampire Bat", "吸血蝙蝠", R196Bat::new, MobCategory.AMBIENT, 0.5F, 0.9F, false, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Bat>> NIGHTWING = register(
            "nightwing", "Nightwing", "暗影蝙蝠", R196Bat::new, MobCategory.AMBIENT, 0.5F, 0.9F, true, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Bat>> GIANT_VAMPIRE_BAT = register(
            "giant_vampire_bat", "Giant Vampire Bat", "吸血巨蝠", R196Bat::new, MobCategory.AMBIENT, 1.0F, 1.8F, false, false);

    public static final DeferredHolder<EntityType<?>, EntityType<R196Wolf>> HELLHOUND = register(
            "hellhound", "Hellhound", "地狱犬", R196Wolf::new, 0.6F, 0.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Wolf>> DIRE_WOLF = register(
            "dire_wolf", "Dire Wolf", "惧狼", R196Wolf::new, MobCategory.CREATURE, 0.6F, 0.8F, false, true);

    /** R196's additional monster roster, including the separate clay-golem branch of earth elementals. */
    public static final List<DeferredHolder<EntityType<?>, ? extends EntityType<?>>> NEW_MONSTERS = List.of(
            INVISIBLE_STALKER,
            GHOUL,
            SHADOW,
            WIGHT,
            REVENANT,
            LONGDEAD,
            BONE_LORD,
            ANCIENT_BONE_LORD,
            BLACK_WIDOW_SPIDER,
            DEMON_SPIDER,
            WOOD_SPIDER,
            PHASE_SPIDER,
            INFERNAL_CREEPER,
            FIRE_ELEMENTAL,
            EARTH_ELEMENTAL,
            CLAY_GOLEM,
            JELLY,
            BLOB,
            OOZE,
            PUDDING,
            MAGMA_CUBE,
            NETHERSPAWN,
            COPPERSPINE,
            HOARY_SILVERFISH,
            VAMPIRE_BAT,
            NIGHTWING,
            GIANT_VAMPIRE_BAT,
            HELLHOUND,
            DIRE_WOLF);

    public static final List<DeferredHolder<EntityType<?>, ? extends EntityType<?>>> REPLACEMENT_ENTITIES = List.of(
            R196_ZOMBIE,
            R196_SKELETON,
            R196_SPIDER,
            R196_CAVE_SPIDER,
            R196_CREEPER,
            R196_SLIME,
            R196_ENDERMAN,
            R196_SQUID,
            R196_COD,
            R196_SALMON,
            R196_PUFFERFISH,
            R196_TROPICAL_FISH,
            R196_WITCH,
            R196_ZOMBIFIED_PIGLIN,
            R196_BLAZE,
            R196_GHAST,
            MAGMA_CUBE,
            R196_COW,
            R196_CHICKEN,
            R196_SHEEP,
            R196_PIG,
            R196_HORSE,
            R196_OCELOT,
            R196_WOLF);

    public static final List<DeferredHolder<EntityType<?>, ? extends EntityType<?>>> ALL = java.util.stream.Stream
            .concat(REPLACEMENT_ENTITIES.stream(), NEW_MONSTERS.stream())
            .distinct()
            .toList();

    private ModEntityTypes() {}

    private static <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> register(
            String path,
            String englishName,
            String chineseName,
            EntityType.EntityFactory<E> factory,
            float width,
            float height,
            boolean fireImmune) {
        return register(path, englishName, chineseName, factory, MobCategory.MONSTER, width, height, fireImmune, false);
    }

    private static <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> register(
            String path,
            String englishName,
            String chineseName,
            EntityType.EntityFactory<E> factory,
            MobCategory category,
            float width,
            float height,
            boolean fireImmune,
            boolean allowedInPeaceful) {
        NAMES.add(new EntityName(path, englishName, chineseName, width, height));
        return ENTITIES.registerEntityType(path, factory, category, builder -> {
            builder.sized(width, height).clientTrackingRange(10);
            if (!allowedInPeaceful) {
                builder.notInPeaceful();
            }
            if (fireImmune) {
                builder.fireImmune();
            }
            return builder;
        });
    }

    private static <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> registerWaterAmbient(
            String path,
            String englishName,
            String chineseName,
            EntityType.EntityFactory<E> factory,
            float width,
            float height,
            float eyeHeight) {
        NAMES.add(new EntityName(path, englishName, chineseName, width, height));
        return ENTITIES.registerEntityType(path, factory, MobCategory.WATER_AMBIENT,
                builder -> builder.sized(width, height).eyeHeight(eyeHeight).clientTrackingRange(4));
    }

    public static List<EntityName> names() {
        return List.copyOf(NAMES);
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }

    public record EntityName(String path, String english, String chinese, float width, float height) {}
}

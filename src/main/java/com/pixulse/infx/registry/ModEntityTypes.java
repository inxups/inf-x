package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.entity.MiteBat;
import com.pixulse.infx.entity.MiteBlaze;
import com.pixulse.infx.entity.MiteChicken;
import com.pixulse.infx.entity.ClayGolem;
import com.pixulse.infx.entity.MiteCow;
import com.pixulse.infx.entity.MiteCod;
import com.pixulse.infx.entity.MiteCreeper;
import com.pixulse.infx.entity.EarthElemental;
import com.pixulse.infx.entity.MiteEnderman;
import com.pixulse.infx.entity.FireElemental;
import com.pixulse.infx.entity.GelatinousSphere;
import com.pixulse.infx.entity.MiteGhast;
import com.pixulse.infx.entity.MiteHorse;
import com.pixulse.infx.entity.MiteMagmaCube;
import com.pixulse.infx.entity.MiteOcelot;
import com.pixulse.infx.entity.MitePufferfish;
import com.pixulse.infx.entity.MitePig;
import com.pixulse.infx.entity.MiteSalmon;
import com.pixulse.infx.entity.MiteSheep;
import com.pixulse.infx.entity.MiteSilverfish;
import com.pixulse.infx.entity.MiteSkeleton;
import com.pixulse.infx.entity.MiteSlime;
import com.pixulse.infx.entity.MiteSpider;
import com.pixulse.infx.entity.MiteSquid;
import com.pixulse.infx.entity.MiteTropicalFish;
import com.pixulse.infx.entity.VanillaWolf;
import com.pixulse.infx.entity.MiteWitch;
import com.pixulse.infx.entity.MiteWolf;
import com.pixulse.infx.entity.MiteZombie;
import com.pixulse.infx.entity.MiteZombifiedPiglin;
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

    public static final DeferredHolder<EntityType<?>, EntityType<MiteZombie>> R196_ZOMBIE = register(
            "r196_zombie", "Zombie", "僵尸", MiteZombie::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteSkeleton>> R196_SKELETON = register(
            "r196_skeleton", "Skeleton", "骷髅", MiteSkeleton::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteSpider>> R196_SPIDER = register(
            "r196_spider", "Spider", "蜘蛛", MiteSpider::new, 1.4F, 0.9F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteSpider>> R196_CAVE_SPIDER = register(
            "r196_cave_spider", "Cave Spider", "洞穴蜘蛛", MiteSpider::new, 0.98F, 0.63F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteCreeper>> R196_CREEPER = register(
            "r196_creeper", "Creeper", "苦力怕", MiteCreeper::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteSlime>> R196_SLIME = register(
            "r196_slime", "Slime", "史莱姆", MiteSlime::new, 0.5F, 0.5F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteEnderman>> R196_ENDERMAN = register(
            "r196_enderman", "Enderman", "末影人", MiteEnderman::new, 0.6F, 2.9F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteSquid>> R196_SQUID = register(
            "r196_squid", "Squid", "鱿鱼", MiteSquid::new, MobCategory.WATER_CREATURE, 0.95F, 0.95F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteCod>> R196_COD = registerWaterAmbient(
            "r196_cod", "Cod", "鳕鱼", MiteCod::new, 0.5F, 0.3F, 0.195F);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteSalmon>> R196_SALMON = registerWaterAmbient(
            "r196_salmon", "Salmon", "鲑鱼", MiteSalmon::new, 0.7F, 0.4F, 0.26F);
    public static final DeferredHolder<EntityType<?>, EntityType<MitePufferfish>> R196_PUFFERFISH = registerWaterAmbient(
            "r196_pufferfish", "Pufferfish", "河豚", MitePufferfish::new, 0.7F, 0.7F, 0.455F);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteTropicalFish>> R196_TROPICAL_FISH = registerWaterAmbient(
            "r196_tropical_fish", "Tropical Fish", "热带鱼", MiteTropicalFish::new, 0.5F, 0.4F, 0.26F);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteWitch>> R196_WITCH = register(
            "r196_witch", "Witch", "女巫", MiteWitch::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteZombifiedPiglin>> R196_ZOMBIFIED_PIGLIN = register(
            "r196_zombified_piglin", "Zombie Pigman", "僵尸猪人", MiteZombifiedPiglin::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteBlaze>> R196_BLAZE = register(
            "r196_blaze", "Blaze", "烈焰人", MiteBlaze::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteGhast>> R196_GHAST = register(
            "r196_ghast", "Ghast", "恶魂", MiteGhast::new, 4.0F, 4.0F, true);

    public static final DeferredHolder<EntityType<?>, EntityType<MiteCow>> R196_COW = register(
            "r196_cow", "Cow", "牛", MiteCow::new, MobCategory.CREATURE, 0.9F, 1.4F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteChicken>> R196_CHICKEN = register(
            "r196_chicken", "Chicken", "鸡", MiteChicken::new, MobCategory.CREATURE, 0.4F, 0.7F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteSheep>> R196_SHEEP = register(
            "r196_sheep", "Sheep", "羊", MiteSheep::new, MobCategory.CREATURE, 0.9F, 1.3F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<MitePig>> R196_PIG = register(
            "r196_pig", "Pig", "猪", MitePig::new, MobCategory.CREATURE, 0.9F, 0.9F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteHorse>> R196_HORSE = register(
            "r196_horse", "Horse", "马", MiteHorse::new, MobCategory.CREATURE, 1.3964844F, 1.6F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteOcelot>> R196_OCELOT = register(
            "r196_ocelot", "Ocelot", "豹猫", MiteOcelot::new, MobCategory.CREATURE, 0.6F, 0.7F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<VanillaWolf>> R196_WOLF = register(
            "r196_wolf", "Wolf", "狼", VanillaWolf::new, MobCategory.CREATURE, 0.7F, 0.8F, false, true);

    public static final DeferredHolder<EntityType<?>, EntityType<MiteZombie>> INVISIBLE_STALKER = register(
            "invisible_stalker", "Invisible Stalker", "影子潜伏者", MiteZombie::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteZombie>> GHOUL = register(
            "ghoul", "Ghoul", "食尸鬼", MiteZombie::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteZombie>> SHADOW = register(
            "shadow", "Shadow", "黑色食尸鬼", MiteZombie::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteZombie>> WIGHT = register(
            "wight", "Wight", "尸妖", MiteZombie::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteZombie>> REVENANT = register(
            "revenant", "Revenant", "亡魂", MiteZombie::new, 0.6F, 1.8F, false);

    public static final DeferredHolder<EntityType<?>, EntityType<MiteSkeleton>> LONGDEAD = register(
            "longdead", "Longdead", "古尸", MiteSkeleton::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteSkeleton>> BONE_LORD = register(
            "bone_lord", "Bone Lord", "骷髅领主", MiteSkeleton::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteSkeleton>> ANCIENT_BONE_LORD = register(
            "ancient_bone_lord", "Ancient Bone Lord", "远古骷髅领主", MiteSkeleton::new, 0.6F, 1.8F, false);

    public static final DeferredHolder<EntityType<?>, EntityType<MiteSpider>> BLACK_WIDOW_SPIDER = register(
            "black_widow_spider", "Black Widow Spider", "黑寡妇蜘蛛", MiteSpider::new, 0.84F, 0.54F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteSpider>> DEMON_SPIDER = register(
            "demon_spider", "Demon Spider", "恶魔蜘蛛", MiteSpider::new, 1.4F, 0.9F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteSpider>> WOOD_SPIDER = register(
            "wood_spider", "Wood Spider", "木蜘蛛", MiteSpider::new, 0.84F, 0.54F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteSpider>> PHASE_SPIDER = register(
            "phase_spider", "Phase Spider", "相位蜘蛛", MiteSpider::new, 0.84F, 0.54F, false);

    public static final DeferredHolder<EntityType<?>, EntityType<MiteCreeper>> INFERNAL_CREEPER = register(
            "infernal_creeper", "Infernal Creeper", "地狱爬行者", MiteCreeper::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<FireElemental>> FIRE_ELEMENTAL = register(
            "fire_elemental", "Fire Elemental", "火元素", FireElemental::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<EarthElemental>> EARTH_ELEMENTAL = register(
            "earth_elemental", "Earth Elemental", "土元素", EarthElemental::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<ClayGolem>> CLAY_GOLEM = register(
            "clay_golem", "Clay Golem", "黏土元素", ClayGolem::new, 0.6F, 1.8F, true);

    public static final DeferredHolder<EntityType<?>, EntityType<MiteSlime>> JELLY = register(
            "jelly", "Jelly", "褐色史莱姆", MiteSlime::new, 0.5F, 0.5F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteSlime>> BLOB = register(
            "blob", "Blob", "红色史莱姆", MiteSlime::new, 0.5F, 0.5F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteSlime>> OOZE = register(
            "ooze", "Ooze", "灰色史莱姆", MiteSlime::new, 0.5F, 0.5F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteSlime>> PUDDING = register(
            "pudding", "Pudding", "黑色史莱姆", MiteSlime::new, 0.5F, 0.5F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<GelatinousSphere>> GELATINOUS_SPHERE =
            ENTITIES.registerEntityType(
                    "gelatinous_sphere",
                    GelatinousSphere::new,
                    MobCategory.MISC,
                    builder -> builder.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
    public static final DeferredHolder<EntityType<?>, EntityType<MiteMagmaCube>> MAGMA_CUBE = register(
            "magma_cube", "Magma Cube", "岩浆怪", MiteMagmaCube::new, 0.5F, 0.5F, true);

    public static final DeferredHolder<EntityType<?>, EntityType<MiteSilverfish>> NETHERSPAWN = register(
            "netherspawn", "Netherspawn", "爆炸蠹虫", MiteSilverfish::new, 0.3F, 0.7F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteSilverfish>> COPPERSPINE = register(
            "copperspine", "Copperspine", "铜毒蠹虫", MiteSilverfish::new, 0.3F, 0.7F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteSilverfish>> HOARY_SILVERFISH = register(
            "hoary_silverfish", "Hoary Silverfish", "白化蠹虫", MiteSilverfish::new, 0.3F, 0.7F, false);

    public static final DeferredHolder<EntityType<?>, EntityType<MiteBat>> VAMPIRE_BAT = register(
            "vampire_bat", "Vampire Bat", "吸血蝙蝠", MiteBat::new, MobCategory.AMBIENT, 0.5F, 0.9F, false, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteBat>> NIGHTWING = register(
            "nightwing", "Nightwing", "暗影蝙蝠", MiteBat::new, MobCategory.AMBIENT, 0.5F, 0.9F, true, false);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteBat>> GIANT_VAMPIRE_BAT = register(
            "giant_vampire_bat", "Giant Vampire Bat", "吸血巨蝠", MiteBat::new, MobCategory.AMBIENT, 1.0F, 1.8F, false, false);

    public static final DeferredHolder<EntityType<?>, EntityType<MiteWolf>> HELLHOUND = register(
            "hellhound", "Hellhound", "地狱犬", MiteWolf::new, 0.6F, 0.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<MiteWolf>> DIRE_WOLF = register(
            "dire_wolf", "Dire Wolf", "惧狼", MiteWolf::new, MobCategory.CREATURE, 0.6F, 0.8F, false, true);

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

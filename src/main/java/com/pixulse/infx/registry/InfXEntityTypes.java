package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.entity.InfxBat;
import com.pixulse.infx.entity.InfxBlaze;
import com.pixulse.infx.entity.InfxChicken;
import com.pixulse.infx.entity.ClayGolem;
import com.pixulse.infx.entity.InfxCow;
import com.pixulse.infx.entity.InfxCod;
import com.pixulse.infx.entity.InfxCreeper;
import com.pixulse.infx.entity.EarthElemental;
import com.pixulse.infx.entity.InfxEnderman;
import com.pixulse.infx.entity.FireElemental;
import com.pixulse.infx.entity.GelatinousSphere;
import com.pixulse.infx.entity.InfxGhast;
import com.pixulse.infx.entity.InfxHorse;
import com.pixulse.infx.entity.InfxMagmaCube;
import com.pixulse.infx.entity.InfxOcelot;
import com.pixulse.infx.entity.InfxPufferfish;
import com.pixulse.infx.entity.InfxPig;
import com.pixulse.infx.entity.InfxSalmon;
import com.pixulse.infx.entity.InfxSheep;
import com.pixulse.infx.entity.InfxSilverfish;
import com.pixulse.infx.entity.InfxSkeleton;
import com.pixulse.infx.entity.InfxSlime;
import com.pixulse.infx.entity.InfxSpider;
import com.pixulse.infx.entity.InfxSquid;
import com.pixulse.infx.entity.InfxTropicalFish;
import com.pixulse.infx.entity.VanillaWolf;
import com.pixulse.infx.entity.InfxWitch;
import com.pixulse.infx.entity.InfxWolf;
import com.pixulse.infx.entity.InfxZombie;
import com.pixulse.infx.entity.InfxZombifiedPiglin;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Entity type registry for original-mob replacements and the 29-item INFX roster. */
public final class InfXEntityTypes {
    public static final float GIANT_VAMPIRE_BAT_SCALE = 1.5F;
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(InfiniteX.MOD_ID);
    private static final List<EntityName> NAMES = new ArrayList<>();

    public static final DeferredHolder<EntityType<?>, EntityType<InfxZombie>> INFX_ZOMBIE = register(
            "infx_zombie", "Zombie", "僵尸", InfxZombie::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxSkeleton>> INFX_SKELETON = register(
            "infx_skeleton", "Skeleton", "骷髅", InfxSkeleton::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxSpider>> INFX_SPIDER = register(
            "infx_spider", "Spider", "蜘蛛", InfxSpider::new, 1.4F, 0.9F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxSpider>> INFX_CAVE_SPIDER = register(
            "infx_cave_spider", "Cave Spider", "洞穴蜘蛛", InfxSpider::new, 0.98F, 0.63F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxCreeper>> INFX_CREEPER = register(
            "infx_creeper", "Creeper", "苦力怕", InfxCreeper::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxSlime>> INFX_SLIME = register(
            "infx_slime", "Slime", "史莱姆", InfxSlime::new, 0.5F, 0.5F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxEnderman>> INFX_ENDERMAN = register(
            "infx_enderman", "Enderman", "末影人", InfxEnderman::new, 0.6F, 2.9F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxSquid>> INFX_SQUID = register(
            "infx_squid", "Squid", "鱿鱼", InfxSquid::new, MobCategory.WATER_CREATURE, 0.95F, 0.95F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxCod>> INFX_COD = registerWaterAmbient(
            "infx_cod", "Cod", "鳕鱼", InfxCod::new, 0.5F, 0.3F, 0.195F);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxSalmon>> INFX_SALMON = registerWaterAmbient(
            "infx_salmon", "Salmon", "鲑鱼", InfxSalmon::new, 0.7F, 0.4F, 0.26F);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxPufferfish>> INFX_PUFFERFISH = registerWaterAmbient(
            "infx_pufferfish", "Pufferfish", "河豚", InfxPufferfish::new, 0.7F, 0.7F, 0.455F);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxTropicalFish>> INFX_TROPICAL_FISH = registerWaterAmbient(
            "infx_tropical_fish", "Tropical Fish", "热带鱼", InfxTropicalFish::new, 0.5F, 0.4F, 0.26F);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxWitch>> INFX_WITCH = register(
            "infx_witch", "Witch", "女巫", InfxWitch::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxZombifiedPiglin>> INFX_ZOMBIFIED_PIGLIN = register(
            "infx_zombified_piglin", "Zombie Pigman", "僵尸猪人", InfxZombifiedPiglin::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxBlaze>> INFX_BLAZE = register(
            "infx_blaze", "Blaze", "烈焰人", InfxBlaze::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxGhast>> INFX_GHAST = register(
            "infx_ghast", "Ghast", "恶魂", InfxGhast::new, 4.0F, 4.0F, true);

    public static final DeferredHolder<EntityType<?>, EntityType<InfxCow>> INFX_COW = register(
            "infx_cow", "Cow", "牛", InfxCow::new, MobCategory.CREATURE, 0.9F, 1.4F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxChicken>> INFX_CHICKEN = register(
            "infx_chicken", "Chicken", "鸡", InfxChicken::new, MobCategory.CREATURE, 0.4F, 0.7F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxSheep>> INFX_SHEEP = register(
            "infx_sheep", "Sheep", "羊", InfxSheep::new, MobCategory.CREATURE, 0.9F, 1.3F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxPig>> INFX_PIG = register(
            "infx_pig", "Pig", "猪", InfxPig::new, MobCategory.CREATURE, 0.9F, 0.9F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxHorse>> INFX_HORSE = register(
            "infx_horse", "Horse", "马", InfxHorse::new, MobCategory.CREATURE, 1.3964844F, 1.6F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxOcelot>> INFX_OCELOT = register(
            "infx_ocelot", "Ocelot", "豹猫", InfxOcelot::new, MobCategory.CREATURE, 0.6F, 0.7F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<VanillaWolf>> INFX_WOLF = register(
            "infx_wolf", "Wolf", "狼", VanillaWolf::new, MobCategory.CREATURE, 0.7F, 0.8F, false, true);

    public static final DeferredHolder<EntityType<?>, EntityType<InfxZombie>> INVISIBLE_STALKER = register(
            "invisible_stalker", "Invisible Stalker", "影子潜伏者", InfxZombie::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxZombie>> GHOUL = register(
            "ghoul", "Ghoul", "食尸鬼", InfxZombie::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxZombie>> SHADOW = register(
            "shadow", "Shadow", "黑色食尸鬼", InfxZombie::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxZombie>> WIGHT = register(
            "wight", "Wight", "尸妖", InfxZombie::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxZombie>> REVENANT = register(
            "revenant", "Revenant", "亡魂", InfxZombie::new, 0.6F, 1.8F, false);

    public static final DeferredHolder<EntityType<?>, EntityType<InfxSkeleton>> LONGDEAD = register(
            "longdead", "Longdead", "古尸", InfxSkeleton::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxSkeleton>> BONE_LORD = register(
            "bone_lord", "Bone Lord", "骷髅领主", InfxSkeleton::new, 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxSkeleton>> ANCIENT_BONE_LORD = register(
            "ancient_bone_lord", "Ancient Bone Lord", "远古骷髅领主", InfxSkeleton::new, 0.6F, 1.8F, false);

    public static final DeferredHolder<EntityType<?>, EntityType<InfxSpider>> BLACK_WIDOW_SPIDER = register(
            "black_widow_spider", "Black Widow Spider", "黑寡妇蜘蛛", InfxSpider::new, 0.84F, 0.54F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxSpider>> DEMON_SPIDER = register(
            "demon_spider", "Demon Spider", "恶魔蜘蛛", InfxSpider::new, 1.4F, 0.9F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxSpider>> WOOD_SPIDER = register(
            "wood_spider", "Wood Spider", "木蜘蛛", InfxSpider::new, 0.84F, 0.54F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxSpider>> PHASE_SPIDER = register(
            "phase_spider", "Phase Spider", "相位蜘蛛", InfxSpider::new, 0.84F, 0.54F, false);

    public static final DeferredHolder<EntityType<?>, EntityType<InfxCreeper>> INFERNAL_CREEPER = register(
            "infernal_creeper", "Infernal Creeper", "地狱爬行者", InfxCreeper::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<FireElemental>> FIRE_ELEMENTAL = register(
            "fire_elemental", "Fire Elemental", "火元素", FireElemental::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<EarthElemental>> EARTH_ELEMENTAL = register(
            "earth_elemental", "Earth Elemental", "土元素", EarthElemental::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<ClayGolem>> CLAY_GOLEM = register(
            "clay_golem", "Clay Golem", "黏土元素", ClayGolem::new, 0.6F, 1.8F, true);

    public static final DeferredHolder<EntityType<?>, EntityType<InfxSlime>> JELLY = register(
            "jelly", "Jelly", "褐色史莱姆", InfxSlime::new, 0.5F, 0.5F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxSlime>> BLOB = register(
            "blob", "Blob", "红色史莱姆", InfxSlime::new, 0.5F, 0.5F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxSlime>> OOZE = register(
            "ooze", "Ooze", "灰色史莱姆", InfxSlime::new, 0.5F, 0.5F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxSlime>> PUDDING = register(
            "pudding", "Pudding", "黑色史莱姆", InfxSlime::new, 0.5F, 0.5F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<GelatinousSphere>> GELATINOUS_SPHERE =
            ENTITIES.registerEntityType(
                    "gelatinous_sphere",
                    GelatinousSphere::new,
                    MobCategory.MISC,
                    builder -> builder.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
    public static final DeferredHolder<EntityType<?>, EntityType<InfxMagmaCube>> MAGMA_CUBE = register(
            "magma_cube", "Magma Cube", "岩浆怪", InfxMagmaCube::new, 0.5F, 0.5F, true);

    public static final DeferredHolder<EntityType<?>, EntityType<InfxSilverfish>> NETHERSPAWN = register(
            "netherspawn", "Netherspawn", "爆炸蠹虫", InfxSilverfish::new, 0.3F, 0.7F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxSilverfish>> COPPERSPINE = register(
            "copperspine", "Copperspine", "铜毒蠹虫", InfxSilverfish::new, 0.3F, 0.7F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxSilverfish>> HOARY_SILVERFISH = register(
            "hoary_silverfish", "Hoary Silverfish", "白化蠹虫", InfxSilverfish::new, 0.3F, 0.7F, false);

    public static final DeferredHolder<EntityType<?>, EntityType<InfxBat>> INFX_BAT = register(
            "infx_bat", "Bat", "蝙蝠", InfxBat::new, MobCategory.AMBIENT, 0.5F, 0.9F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxBat>> VAMPIRE_BAT = register(
            "vampire_bat", "Vampire Bat", "吸血蝙蝠", InfxBat::new, MobCategory.AMBIENT, 0.5F, 0.9F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxBat>> NIGHTWING = register(
            "nightwing", "Nightwing", "暗影蝙蝠", InfxBat::new, MobCategory.AMBIENT, 0.5F, 0.9F, true, true);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxBat>> GIANT_VAMPIRE_BAT = register(
            "giant_vampire_bat", "Giant Vampire Bat", "吸血巨蝠", InfxBat::new, MobCategory.AMBIENT,
            0.5F * GIANT_VAMPIRE_BAT_SCALE, 0.9F * GIANT_VAMPIRE_BAT_SCALE, false, true);

    public static final DeferredHolder<EntityType<?>, EntityType<InfxWolf>> HELLHOUND = register(
            "hellhound", "Hellhound", "地狱犬", InfxWolf::new, 0.6F, 0.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<InfxWolf>> DIRE_WOLF = register(
            "dire_wolf", "Dire Wolf", "惧狼", InfxWolf::new, MobCategory.CREATURE, 0.6F, 0.8F, false, true);

    /** INFX's additional monster roster, including the separate clay-golem branch of earth elementals. */
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
            INFX_ZOMBIE,
            INFX_SKELETON,
            INFX_SPIDER,
            INFX_CAVE_SPIDER,
            INFX_CREEPER,
            INFX_SLIME,
            INFX_ENDERMAN,
            INFX_SQUID,
            INFX_COD,
            INFX_SALMON,
            INFX_PUFFERFISH,
            INFX_TROPICAL_FISH,
            INFX_WITCH,
            INFX_ZOMBIFIED_PIGLIN,
            INFX_BLAZE,
            INFX_GHAST,
            MAGMA_CUBE,
            INFX_BAT,
            INFX_COW,
            INFX_CHICKEN,
            INFX_SHEEP,
            INFX_PIG,
            INFX_HORSE,
            INFX_OCELOT,
            INFX_WOLF);

    public static final List<DeferredHolder<EntityType<?>, ? extends EntityType<?>>> ALL = java.util.stream.Stream
            .concat(REPLACEMENT_ENTITIES.stream(), NEW_MONSTERS.stream())
            .distinct()
            .toList();

    private InfXEntityTypes() {}

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

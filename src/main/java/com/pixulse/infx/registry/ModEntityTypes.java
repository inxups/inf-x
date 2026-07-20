package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.entity.R196Bat;
import com.pixulse.infx.entity.R196Blaze;
import com.pixulse.infx.entity.R196Creeper;
import com.pixulse.infx.entity.R196EarthElemental;
import com.pixulse.infx.entity.R196Enderman;
import com.pixulse.infx.entity.R196FireElemental;
import com.pixulse.infx.entity.R196Ghast;
import com.pixulse.infx.entity.R196MagmaCube;
import com.pixulse.infx.entity.R196Silverfish;
import com.pixulse.infx.entity.R196Skeleton;
import com.pixulse.infx.entity.R196Slime;
import com.pixulse.infx.entity.R196Spider;
import com.pixulse.infx.entity.R196Squid;
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

/** Entity type registry for original-mob replacements and the 28-item R196 roster. */
public final class ModEntityTypes {
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(InfiniteX.MOD_ID);
    private static final List<EntityName> NAMES = new ArrayList<>();

    public static final DeferredHolder<EntityType<?>, EntityType<R196Zombie>> R196_ZOMBIE = register(
            "r196_zombie", "Zombie", "僵尸", R196Zombie::new, 0.6F, 1.95F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Skeleton>> R196_SKELETON = register(
            "r196_skeleton", "Skeleton", "骷髅", R196Skeleton::new, 0.6F, 1.99F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Spider>> R196_SPIDER = register(
            "r196_spider", "Spider", "蜘蛛", R196Spider::new, 1.4F, 0.9F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Spider>> R196_CAVE_SPIDER = register(
            "r196_cave_spider", "Cave Spider", "洞穴蜘蛛", R196Spider::new, 0.7F, 0.5F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Creeper>> R196_CREEPER = register(
            "r196_creeper", "Creeper", "苦力怕", R196Creeper::new, 0.6F, 1.7F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Slime>> R196_SLIME = register(
            "r196_slime", "Slime", "史莱姆", R196Slime::new, 0.52F, 0.52F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Enderman>> R196_ENDERMAN = register(
            "r196_enderman", "Enderman", "末影人", R196Enderman::new, 0.6F, 2.9F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Squid>> R196_SQUID = register(
            "r196_squid", "Squid", "鱿鱼", R196Squid::new, MobCategory.WATER_CREATURE, 0.8F, 0.8F, false, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Witch>> R196_WITCH = register(
            "r196_witch", "Witch", "女巫", R196Witch::new, 0.6F, 1.95F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196ZombifiedPiglin>> R196_ZOMBIFIED_PIGLIN = register(
            "r196_zombified_piglin", "Zombified Piglin", "僵尸猪灵", R196ZombifiedPiglin::new, 0.6F, 1.95F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Blaze>> R196_BLAZE = register(
            "r196_blaze", "Blaze", "烈焰人", R196Blaze::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Ghast>> R196_GHAST = register(
            "r196_ghast", "Ghast", "恶魂", R196Ghast::new, 4.0F, 4.0F, true);

    public static final DeferredHolder<EntityType<?>, EntityType<R196Zombie>> INVISIBLE_STALKER = register(
            "invisible_stalker", "Invisible Stalker", "影子潜伏者", R196Zombie::new, 0.6F, 1.95F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Zombie>> GHOUL = register(
            "ghoul", "Ghoul", "食尸鬼", R196Zombie::new, 0.6F, 1.95F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Zombie>> SHADOW = register(
            "shadow", "Shadow", "黑色食尸鬼", R196Zombie::new, 0.6F, 1.95F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Zombie>> WIGHT = register(
            "wight", "Wight", "尸妖", R196Zombie::new, 0.6F, 1.95F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Zombie>> REVENANT = register(
            "revenant", "Revenant", "亡魂", R196Zombie::new, 0.6F, 1.95F, false);

    public static final DeferredHolder<EntityType<?>, EntityType<R196Skeleton>> LONGDEAD = register(
            "longdead", "Longdead", "古尸", R196Skeleton::new, 0.6F, 1.99F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Skeleton>> BONE_LORD = register(
            "bone_lord", "Bone Lord", "骷髅领主", R196Skeleton::new, 0.6F, 1.99F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Skeleton>> ANCIENT_BONE_LORD = register(
            "ancient_bone_lord", "Ancient Bone Lord", "远古骷髅领主", R196Skeleton::new, 0.6F, 1.99F, false);

    public static final DeferredHolder<EntityType<?>, EntityType<R196Spider>> BLACK_WIDOW_SPIDER = register(
            "black_widow_spider", "Black Widow Spider", "黑寡妇蜘蛛", R196Spider::new, 0.84F, 0.54F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Spider>> DEMON_SPIDER = register(
            "demon_spider", "Demon Spider", "恶魔蜘蛛", R196Spider::new, 1.4F, 0.9F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Spider>> WOOD_SPIDER = register(
            "wood_spider", "Wood Spider", "木蜘蛛", R196Spider::new, 0.84F, 0.54F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Spider>> PHASE_SPIDER = register(
            "phase_spider", "Phase Spider", "相位蜘蛛", R196Spider::new, 0.84F, 0.54F, false);

    public static final DeferredHolder<EntityType<?>, EntityType<R196Creeper>> INFERNAL_CREEPER = register(
            "infernal_creeper", "Infernal Creeper", "地狱爬行者", R196Creeper::new, 0.9F, 2.55F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196FireElemental>> FIRE_ELEMENTAL = register(
            "fire_elemental", "Fire Elemental", "火元素", R196FireElemental::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196EarthElemental>> EARTH_ELEMENTAL = register(
            "earth_elemental", "Earth Elemental", "土元素", R196EarthElemental::new, 1.4F, 2.7F, true);

    public static final DeferredHolder<EntityType<?>, EntityType<R196Slime>> JELLY = register(
            "jelly", "Jelly", "褐色史莱姆", R196Slime::new, 0.52F, 0.52F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Slime>> BLOB = register(
            "blob", "Blob", "红色史莱姆", R196Slime::new, 0.52F, 0.52F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Slime>> OOZE = register(
            "ooze", "Ooze", "灰色史莱姆", R196Slime::new, 0.52F, 0.52F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Slime>> PUDDING = register(
            "pudding", "Pudding", "黑色史莱姆", R196Slime::new, 0.52F, 0.52F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196MagmaCube>> MAGMA_CUBE = register(
            "magma_cube", "Magma Cube", "岩浆怪", R196MagmaCube::new, 0.52F, 0.52F, true);

    public static final DeferredHolder<EntityType<?>, EntityType<R196Silverfish>> NETHERSPAWN = register(
            "netherspawn", "Netherspawn", "爆炸蠹虫", R196Silverfish::new, 0.4F, 0.3F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Silverfish>> COPPERSPINE = register(
            "copperspine", "Copperspine", "铜毒蠹虫", R196Silverfish::new, 0.4F, 0.3F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Silverfish>> HOARY_SILVERFISH = register(
            "hoary_silverfish", "Hoary Silverfish", "白化蠹虫", R196Silverfish::new, 0.4F, 0.3F, false);

    public static final DeferredHolder<EntityType<?>, EntityType<R196Bat>> VAMPIRE_BAT = register(
            "vampire_bat", "Vampire Bat", "吸血蝙蝠", R196Bat::new, MobCategory.AMBIENT, 0.5F, 0.9F, false, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Bat>> NIGHTWING = register(
            "nightwing", "Nightwing", "暗影蝙蝠", R196Bat::new, MobCategory.AMBIENT, 0.5F, 0.9F, false, false);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Bat>> GIANT_VAMPIRE_BAT = register(
            "giant_vampire_bat", "Giant Vampire Bat", "吸血巨蝠", R196Bat::new, MobCategory.AMBIENT, 1.0F, 1.8F, false, false);

    public static final DeferredHolder<EntityType<?>, EntityType<R196Wolf>> HELLHOUND = register(
            "hellhound", "Hellhound", "地狱犬", R196Wolf::new, 0.6F, 0.85F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<R196Wolf>> DIRE_WOLF = register(
            "dire_wolf", "Dire Wolf", "惧狼", R196Wolf::new, MobCategory.CREATURE, 0.6F, 0.85F, false, true);

    /** The exact 28 entries listed by the R196 overview; replacement-only types are excluded. */
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
            R196_WITCH,
            R196_ZOMBIFIED_PIGLIN,
            R196_BLAZE,
            R196_GHAST,
            MAGMA_CUBE);

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
        NAMES.add(new EntityName(path, englishName, chineseName));
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

    public static List<EntityName> names() {
        return List.copyOf(NAMES);
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }

    public record EntityName(String path, String english, String chinese) {}
}

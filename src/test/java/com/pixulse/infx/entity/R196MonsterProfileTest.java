package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.pixulse.infx.registry.ModEntityTypes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.junit.jupiter.api.Test;

class R196MonsterProfileTest {
    private static final double EPSILON = 1.0E-6;

    @Test
    void combatProfilesMatchTheR196SourceAttributes() {
        assertStats(R196Zombie.attributes(R196Zombie.Variant.ZOMBIE), 20.0, 40.0, 0.23, 5.0);
        assertStats(R196Zombie.attributes(R196Zombie.Variant.INVISIBLE_STALKER), 20.0, 40.0, 0.23, 4.0);
        assertStats(R196Zombie.attributes(R196Zombie.Variant.GHOUL), 20.0, 40.0, 0.28, 5.0);
        assertStats(R196Zombie.attributes(R196Zombie.Variant.SHADOW), 20.0, 40.0, 0.23, 5.0);
        assertStats(R196Zombie.attributes(R196Zombie.Variant.WIGHT), 20.0, 40.0, 0.25, 5.0);
        assertStats(R196Zombie.attributes(R196Zombie.Variant.REVENANT), 30.0, 40.0, 0.26, 7.0);

        assertStats(R196Skeleton.attributes(R196Skeleton.Variant.SKELETON), 6.0, 32.0, 0.30, 4.0);
        assertStats(R196Skeleton.attributes(R196Skeleton.Variant.LONGDEAD), 12.0, 40.0, 0.29, 6.0);
        assertStats(R196Skeleton.attributes(R196Skeleton.Variant.BONE_LORD), 20.0, 40.0, 0.26, 5.0);
        assertStats(R196Skeleton.attributes(R196Skeleton.Variant.ANCIENT_BONE_LORD), 24.0, 40.0, 0.27, 8.0);

        assertStats(R196Spider.attributes(R196Spider.Variant.SPIDER), 12.0, 28.0, 1.0, 4.0);
        assertStats(R196Spider.attributes(R196Spider.Variant.CAVE_SPIDER), 16.0, 28.0, 1.0, 4.0);
        assertStats(R196Spider.attributes(R196Spider.Variant.BLACK_WIDOW), 6.0, 28.0, 0.80, 1.0);
        assertStats(R196Spider.attributes(R196Spider.Variant.DEMON), 18.0, 28.0, 1.0, 5.0);
        assertStats(R196Spider.attributes(R196Spider.Variant.WOOD), 6.0, 28.0, 0.80, 1.0);
        assertStats(R196Spider.attributes(R196Spider.Variant.PHASE), 6.0, 28.0, 0.80, 3.0);

        assertStats(R196Creeper.attributes(R196Creeper.Variant.CREEPER), 20.0, 32.0, 0.25, 2.0);
        assertStats(R196Creeper.attributes(R196Creeper.Variant.INFERNAL), 20.0, 32.0, 0.25, 2.0);
        assertEquals(2.0, stats(R196Creeper.attributes(R196Creeper.Variant.INFERNAL))
                .getBaseValue(Attributes.ARMOR), EPSILON);

        assertStats(R196Silverfish.attributes(), 8.0, 32.0, 0.60, 3.0);
        assertStats(R196FireElemental.attributes(), 20.0, 40.0, 0.25, 5.0);
        assertStats(R196EarthElemental.attributes(), 30.0, 20.0, 0.20, 12.0);
        AttributeSupplier earthElemental = stats(R196EarthElemental.attributes());
        assertEquals(4.0, earthElemental.getBaseValue(Attributes.ARMOR), EPSILON);
        assertEquals(0.0, earthElemental.getBaseValue(Attributes.KNOCKBACK_RESISTANCE), EPSILON);
        assertStats(R196Enderman.attributes(), 40.0, 32.0, 0.30, 10.0);
        assertStats(R196Witch.attributes(), 26.0, 32.0, 0.25, 2.0);
        assertStats(R196ZombifiedPiglin.attributes(), 20.0, 40.0, 0.50, 8.0);

        AttributeSupplier blaze = stats(R196Blaze.attributes());
        assertEquals(32.0, blaze.getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
        assertEquals(6.0, blaze.getBaseValue(Attributes.ATTACK_DAMAGE), EPSILON);
    }

    @Test
    void nonstandardMobProfilesKeepTheirR196Limits() {
        AttributeSupplier slime = stats(R196Slime.attributes());
        AttributeSupplier magmaCube = stats(R196MagmaCube.attributes());
        AttributeSupplier squid = stats(R196Squid.attributes());

        assertEquals(16.0, slime.getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
        assertEquals(1.0, R196Slime.attackDamageForSize(R196Slime.Variant.SLIME, 1), EPSILON);
        assertEquals(4.0, R196Slime.attackDamageForSize(R196Slime.Variant.JELLY, 2), EPSILON);
        assertEquals(6.0, R196Slime.attackDamageForSize(R196Slime.Variant.BLOB, 2), EPSILON);
        assertEquals(6.0, R196Slime.attackDamageForSize(R196Slime.Variant.OOZE, 2), EPSILON);
        assertEquals(8.0, R196Slime.attackDamageForSize(R196Slime.Variant.PUDDING, 2), EPSILON);
        assertEquals(32.0, stats(R196Slime.attributes(R196Slime.Variant.OOZE))
                .getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
        assertEquals(1, R196Slime.experienceForSize(R196Slime.Variant.SLIME, 1));
        assertEquals(4, R196Slime.experienceForSize(R196Slime.Variant.JELLY, 2));
        assertEquals(9, R196Slime.experienceForSize(R196Slime.Variant.BLOB, 3));
        assertEquals(8, R196Slime.experienceForSize(R196Slime.Variant.OOZE, 2));
        assertEquals(10, R196Slime.experienceForSize(R196Slime.Variant.PUDDING, 2));
        assertEquals(16.0, magmaCube.getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
        assertEquals(0.20, magmaCube.getBaseValue(Attributes.MOVEMENT_SPEED), EPSILON);
        assertEquals(2.0, R196MagmaCube.attackDamageForSize(1), EPSILON);
        assertEquals(8.0, R196MagmaCube.attackDamageForSize(4), EPSILON);
        assertEquals(2.0, R196MagmaCube.armorForSize(1), EPSILON);
        assertEquals(8.0, R196MagmaCube.armorForSize(4), EPSILON);
        assertEquals(0.20, R196MagmaCube.movementSpeedForSize(1), EPSILON);
        assertEquals(0.20, R196MagmaCube.movementSpeedForSize(4), EPSILON);
        assertEquals(10.0, squid.getBaseValue(Attributes.MAX_HEALTH), EPSILON);
        assertEquals(16.0, squid.getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
        assertFalse(squid.hasAttribute(Attributes.ATTACK_DAMAGE));

        assertBat(R196Bat.Variant.VAMPIRE, 3.0, 1.0);
        assertBat(R196Bat.Variant.NIGHTWING, 3.0, 1.0);
        assertBat(R196Bat.Variant.GIANT_VAMPIRE, 6.0, 2.0);

        assertStats(R196Wolf.attributes(R196Wolf.Variant.HELLHOUND), 20.0, 16.0, 0.40, 4.0);
        assertStats(R196Wolf.attributes(R196Wolf.Variant.DIRE_WOLF), 16.0, 16.0, 0.40, 5.0);
        assertEquals(24.0, R196Wolf.maximumHealth(R196Wolf.Variant.DIRE_WOLF, true), EPSILON);
        assertEquals(32.0, R196Wolf.followRange(R196Wolf.Variant.DIRE_WOLF, true), EPSILON);
        assertEquals(20.0, R196Wolf.maximumHealth(R196Wolf.Variant.HELLHOUND, true), EPSILON);
        assertEquals(16.0, R196Wolf.followRange(R196Wolf.Variant.HELLHOUND, true), EPSILON);

        AttributeSupplier ghast = stats(R196Ghast.attributes());
        assertEquals(10.0, ghast.getBaseValue(Attributes.MAX_HEALTH), EPSILON);
        assertEquals(100.0, ghast.getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
    }

    @Test
    void allRegisteredMobNamesAndDimensionsMatchTheR196Roster() {
        Map<String, ModEntityTypes.EntityName> entities = ModEntityTypes.names().stream()
                .collect(Collectors.toMap(ModEntityTypes.EntityName::path, entity -> entity));
        assertEquals(expectedNames(), entities.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new Names(entry.getValue().english(), entry.getValue().chinese()))));

        Set<String> checked = new HashSet<>();
        assertDimensions(entities, checked, 0.6F, 1.8F,
                "r196_zombie", "r196_skeleton", "r196_creeper", "r196_witch", "r196_zombified_piglin",
                "r196_blaze", "invisible_stalker", "ghoul", "shadow", "wight", "revenant", "longdead",
                "bone_lord", "ancient_bone_lord", "infernal_creeper", "fire_elemental", "earth_elemental");
        assertDimensions(entities, checked, 1.4F, 0.9F, "r196_spider", "demon_spider");
        assertDimensions(entities, checked, 0.98F, 0.63F, "r196_cave_spider");
        assertDimensions(entities, checked, 0.84F, 0.54F, "black_widow_spider", "wood_spider", "phase_spider");
        assertDimensions(entities, checked, 0.5F, 0.5F,
                "r196_slime", "jelly", "blob", "ooze", "pudding", "magma_cube");
        assertDimensions(entities, checked, 0.6F, 2.9F, "r196_enderman");
        assertDimensions(entities, checked, 0.95F, 0.95F, "r196_squid");
        assertDimensions(entities, checked, 4.0F, 4.0F, "r196_ghast");
        assertDimensions(entities, checked, 0.3F, 0.7F, "netherspawn", "copperspine", "hoary_silverfish");
        assertDimensions(entities, checked, 0.5F, 0.9F, "vampire_bat", "nightwing");
        assertDimensions(entities, checked, 1.0F, 1.8F, "giant_vampire_bat");
        assertDimensions(entities, checked, 0.6F, 0.8F, "hellhound", "dire_wolf");
        assertEquals(entities.keySet(), checked);
    }

    private static void assertBat(R196Bat.Variant variant, double health, double attack) {
        AttributeSupplier attributes = stats(R196Bat.attributes(variant));
        assertEquals(health, attributes.getBaseValue(Attributes.MAX_HEALTH), EPSILON);
        assertEquals(16.0, attributes.getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
        assertEquals(attack, attributes.getBaseValue(Attributes.ATTACK_DAMAGE), EPSILON);
    }

    private static void assertStats(
            AttributeSupplier.Builder builder, double health, double followRange, double movementSpeed, double attackDamage) {
        AttributeSupplier attributes = stats(builder);
        assertEquals(health, attributes.getBaseValue(Attributes.MAX_HEALTH), EPSILON);
        assertEquals(followRange, attributes.getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
        assertEquals(movementSpeed, attributes.getBaseValue(Attributes.MOVEMENT_SPEED), EPSILON);
        assertEquals(attackDamage, attributes.getBaseValue(Attributes.ATTACK_DAMAGE), EPSILON);
    }

    private static AttributeSupplier stats(AttributeSupplier.Builder builder) {
        return builder.build();
    }

    private static void assertDimensions(
            Map<String, ModEntityTypes.EntityName> entities,
            Set<String> checked,
            float width,
            float height,
            String... paths) {
        for (String path : paths) {
            ModEntityTypes.EntityName entity = entities.get(path);
            assertEquals(width, entity.width(), EPSILON, path + " width");
            assertEquals(height, entity.height(), EPSILON, path + " height");
            checked.add(path);
        }
    }

    private static Map<String, Names> expectedNames() {
        Map<String, Names> names = new HashMap<>();
        add(names, "r196_zombie", "Zombie", "僵尸");
        add(names, "r196_skeleton", "Skeleton", "骷髅");
        add(names, "r196_spider", "Spider", "蜘蛛");
        add(names, "r196_cave_spider", "Cave Spider", "洞穴蜘蛛");
        add(names, "r196_creeper", "Creeper", "苦力怕");
        add(names, "r196_slime", "Slime", "史莱姆");
        add(names, "r196_enderman", "Enderman", "末影人");
        add(names, "r196_squid", "Squid", "鱿鱼");
        add(names, "r196_witch", "Witch", "女巫");
        add(names, "r196_zombified_piglin", "Zombie Pigman", "僵尸猪人");
        add(names, "r196_blaze", "Blaze", "烈焰人");
        add(names, "r196_ghast", "Ghast", "恶魂");
        add(names, "invisible_stalker", "Invisible Stalker", "影子潜伏者");
        add(names, "ghoul", "Ghoul", "食尸鬼");
        add(names, "shadow", "Shadow", "黑色食尸鬼");
        add(names, "wight", "Wight", "尸妖");
        add(names, "revenant", "Revenant", "亡魂");
        add(names, "longdead", "Longdead", "古尸");
        add(names, "bone_lord", "Bone Lord", "骷髅领主");
        add(names, "ancient_bone_lord", "Ancient Bone Lord", "远古骷髅领主");
        add(names, "black_widow_spider", "Black Widow Spider", "黑寡妇蜘蛛");
        add(names, "demon_spider", "Demon Spider", "恶魔蜘蛛");
        add(names, "wood_spider", "Wood Spider", "木蜘蛛");
        add(names, "phase_spider", "Phase Spider", "相位蜘蛛");
        add(names, "infernal_creeper", "Infernal Creeper", "地狱爬行者");
        add(names, "fire_elemental", "Fire Elemental", "火元素");
        add(names, "earth_elemental", "Earth Elemental", "土元素");
        add(names, "jelly", "Jelly", "褐色史莱姆");
        add(names, "blob", "Blob", "红色史莱姆");
        add(names, "ooze", "Ooze", "灰色史莱姆");
        add(names, "pudding", "Pudding", "黑色史莱姆");
        add(names, "magma_cube", "Magma Cube", "岩浆怪");
        add(names, "netherspawn", "Netherspawn", "爆炸蠹虫");
        add(names, "copperspine", "Copperspine", "铜毒蠹虫");
        add(names, "hoary_silverfish", "Hoary Silverfish", "白化蠹虫");
        add(names, "vampire_bat", "Vampire Bat", "吸血蝙蝠");
        add(names, "nightwing", "Nightwing", "暗影蝙蝠");
        add(names, "giant_vampire_bat", "Giant Vampire Bat", "吸血巨蝠");
        add(names, "hellhound", "Hellhound", "地狱犬");
        add(names, "dire_wolf", "Dire Wolf", "惧狼");
        return names;
    }

    private static void add(Map<String, Names> names, String path, String english, String chinese) {
        names.put(path, new Names(english, chinese));
    }

    private record Names(String english, String chinese) {}
}

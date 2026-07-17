package com.pixulse.infx.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class R196RawItemTest {
    private static final List<ExpectedRawItem> EXPECTED = List.of(
            expected("flint_chip", "Flint Chip", "燧石碎片", R196RawItem.Kind.SHARD, R196Material.FLINT, 0),
            expected("obsidian_shard", "Obsidian Shard", "黑曜石碎片", R196RawItem.Kind.SHARD, R196Material.OBSIDIAN, 0),
            expected("emerald_shard", "Emerald Shard", "绿宝石碎片", R196RawItem.Kind.SHARD, null, 0),
            expected("diamond_shard", "Diamond Shard", "钻石碎片", R196RawItem.Kind.SHARD, null, 0),
            expected("nether_quartz_shard", "Nether Quartz Shard", "下界石英碎片", R196RawItem.Kind.SHARD, null, 0),
            expected("glass_shard", "Glass Shard", "玻璃碎片", R196RawItem.Kind.SHARD, null, 0),
            expected("sinew", "Sinew", "皮筋", R196RawItem.Kind.BINDING, null, 0),
            expected("manure", "Manure", "肥料", R196RawItem.Kind.FERTILIZER, null, 0),
            expected("silver_nugget", "Silver Nugget", "银粒", R196RawItem.Kind.NUGGET, R196Material.SILVER, 0),
            expected("mithril_nugget", "Mithril Nugget", "秘银粒", R196RawItem.Kind.NUGGET, R196Material.MITHRIL, 0),
            expected("adamantium_nugget", "Adamantium Nugget", "艾德曼粒", R196RawItem.Kind.NUGGET, R196Material.ADAMANTIUM, 0),
            expected("ancient_metal_nugget", "Ancient Metal Nugget", "远古金属粒", R196RawItem.Kind.NUGGET, R196Material.ANCIENT_METAL, 0),
            expected("silver_ingot", "Silver Ingot", "银锭", R196RawItem.Kind.INGOT, R196Material.SILVER, 0),
            expected("mithril_ingot", "Mithril Ingot", "秘银锭", R196RawItem.Kind.INGOT, R196Material.MITHRIL, 0),
            expected("adamantium_ingot", "Adamantium Ingot", "艾德曼锭", R196RawItem.Kind.INGOT, R196Material.ADAMANTIUM, 0),
            expected("ancient_metal_ingot", "Ancient Metal Ingot", "远古金属锭", R196RawItem.Kind.INGOT, R196Material.ANCIENT_METAL, 0),
            expected("copper_chain", "Copper Chain", "铜锁链", R196RawItem.Kind.CHAIN, R196Material.COPPER, 0),
            expected("silver_chain", "Silver Chain", "银锁链", R196RawItem.Kind.CHAIN, R196Material.SILVER, 0),
            expected("gold_chain", "Golden Chain", "金锁链", R196RawItem.Kind.CHAIN, R196Material.GOLD, 0),
            expected("rusted_iron_chain", "Rusted Iron Chain", "锈铁链", R196RawItem.Kind.CHAIN, R196Material.RUSTED_IRON, 0),
            expected("iron_chain", "Iron Chain", "铁锁链", R196RawItem.Kind.CHAIN, R196Material.IRON, 0),
            expected("ancient_metal_chain", "Ancient Metal Chain", "远古金属锁链", R196RawItem.Kind.CHAIN, R196Material.ANCIENT_METAL, 0),
            expected("mithril_chain", "Mithril Chain", "秘银锁链", R196RawItem.Kind.CHAIN, R196Material.MITHRIL, 0),
            expected("adamantium_chain", "Adamantium Chain", "艾德曼锁链", R196RawItem.Kind.CHAIN, R196Material.ADAMANTIUM, 0),
            expected("copper_coin", "Copper Coin", "铜币", R196RawItem.Kind.COIN, R196Material.COPPER, 5),
            expected("silver_coin", "Silver Coin", "银币", R196RawItem.Kind.COIN, R196Material.SILVER, 25),
            expected("gold_coin", "Gold Coin", "金币", R196RawItem.Kind.COIN, R196Material.GOLD, 100),
            expected("ancient_metal_coin", "Ancient Metal Coin", "远古金属币", R196RawItem.Kind.COIN, R196Material.ANCIENT_METAL, 500),
            expected("mithril_coin", "Mithril Coin", "秘银币", R196RawItem.Kind.COIN, R196Material.MITHRIL, 2_500),
            expected("adamantium_coin", "Adamantium Coin", "艾德曼币", R196RawItem.Kind.COIN, R196Material.ADAMANTIUM, 10_000),
            expected("creeper_frags", "Creeper Frags", "苦力怕碎片", R196RawItem.Kind.MONSTER_FRAG, null, 0),
            expected("infernal_creeper_frags", "Infernal Creeper Frags", "地狱苦力怕碎片", R196RawItem.Kind.MONSTER_FRAG, null, 0),
            expected("netherspawn_frags", "Netherspawn Frags", "下界虫碎片", R196RawItem.Kind.MONSTER_FRAG, null, 0));

    @Test
    void rawCatalogHasExactApprovedDefinitions() {
        assertEquals(EXPECTED.stream().map(ExpectedRawItem::path).toList(),
                List.of(R196RawItem.values()).stream().map(R196RawItem::path).toList());
        assertEquals(33, R196RawItem.values().length);
        Set<String> unique = List.of(R196RawItem.values()).stream().map(R196RawItem::path).collect(Collectors.toSet());
        assertEquals(33, unique.size());

        for (int index = 0; index < EXPECTED.size(); index++) {
            ExpectedRawItem expected = EXPECTED.get(index);
            R196RawItem actual = R196RawItem.values()[index];
            assertEquals(expected.englishName(), actual.englishName());
            assertEquals(expected.chineseName(), actual.chineseName());
            assertEquals(expected.kind(), actual.kind());
            assertEquals(expected.material(), actual.material());
            assertEquals(expected.coinXp(), actual.coinXp());
            assertEquals("item.infx." + expected.path(), actual.translationKey());
        }
    }

    @Test
    void kindValuesHaveExactApprovedOrder() {
        assertEquals(List.of("SHARD", "BINDING", "FERTILIZER", "NUGGET", "INGOT", "CHAIN", "COIN", "MONSTER_FRAG"),
                List.of(R196RawItem.Kind.values()).stream().map(Enum::name).toList());
    }

    @Test
    void coinXpIsStoredWithoutImplementingExchange() {
        assertEquals(5, R196RawItem.COPPER_COIN.coinXp());
        assertEquals(25, R196RawItem.SILVER_COIN.coinXp());
        assertEquals(100, R196RawItem.GOLD_COIN.coinXp());
        assertEquals(500, R196RawItem.ANCIENT_METAL_COIN.coinXp());
        assertEquals(2_500, R196RawItem.MITHRIL_COIN.coinXp());
        assertEquals(10_000, R196RawItem.ADAMANTIUM_COIN.coinXp());
        assertTrue(R196RawItem.FLINT_CHIP.material().contains(R196Material.FLINT));
        assertTrue(R196RawItem.CREEPER_FRAGS.material().isEmpty());
    }

    private static ExpectedRawItem expected(
            String path, String englishName, String chineseName, R196RawItem.Kind kind, R196Material material, int coinXp) {
        return new ExpectedRawItem(path, englishName, chineseName, kind, Optional.ofNullable(material), coinXp);
    }

    private record ExpectedRawItem(
            String path,
            String englishName,
            String chineseName,
            R196RawItem.Kind kind,
            Optional<R196Material> material,
            int coinXp) {}
}

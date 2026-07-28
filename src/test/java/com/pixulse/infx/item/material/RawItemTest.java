package com.pixulse.infx.item.material;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class RawItemTest {
    private static final List<ExpectedRawItem> EXPECTED = List.of(
            expected("flint_chip", "Flint Chip", "燧石碎片", RawItem.Kind.SHARD, MiteMaterial.FLINT, 0),
            expected("obsidian_shard", "Obsidian Shard", "黑曜石碎片", RawItem.Kind.SHARD, MiteMaterial.OBSIDIAN, 0),
            expected("emerald_shard", "Emerald Shard", "绿宝石碎片", RawItem.Kind.SHARD, null, 0),
            expected("diamond_shard", "Diamond Shard", "钻石碎片", RawItem.Kind.SHARD, null, 0),
            expected("nether_quartz_shard", "Nether Quartz Shard", "下界石英碎片", RawItem.Kind.SHARD, null, 0),
            expected("glass_shard", "Glass Shard", "玻璃碎片", RawItem.Kind.SHARD, null, 0),
            expected("sinew", "Sinew", "皮筋", RawItem.Kind.BINDING, null, 0),
            expected("manure", "Manure", "肥料", RawItem.Kind.FERTILIZER, null, 0),
            expected("silver_nugget", "Silver Nugget", "银粒", RawItem.Kind.NUGGET, MiteMaterial.SILVER, 0),
            expected("mithril_nugget", "Mithril Nugget", "秘银粒", RawItem.Kind.NUGGET, MiteMaterial.MITHRIL, 0),
            expected("adamantium_nugget", "Adamantium Nugget", "艾德曼粒", RawItem.Kind.NUGGET, MiteMaterial.ADAMANTIUM, 0),
            expected("ancient_metal_nugget", "Ancient Metal Nugget", "远古金属粒", RawItem.Kind.NUGGET, MiteMaterial.ANCIENT_METAL, 0),
            expected("silver_ingot", "Silver Ingot", "银锭", RawItem.Kind.INGOT, MiteMaterial.SILVER, 0),
            expected("mithril_ingot", "Mithril Ingot", "秘银锭", RawItem.Kind.INGOT, MiteMaterial.MITHRIL, 0),
            expected("adamantium_ingot", "Adamantium Ingot", "艾德曼锭", RawItem.Kind.INGOT, MiteMaterial.ADAMANTIUM, 0),
            expected("ancient_metal_ingot", "Ancient Metal Ingot", "远古金属锭", RawItem.Kind.INGOT, MiteMaterial.ANCIENT_METAL, 0),
            expected("copper_chain", "Copper Chain", "铜锁链", RawItem.Kind.CHAIN, MiteMaterial.COPPER, 0),
            expected("silver_chain", "Silver Chain", "银锁链", RawItem.Kind.CHAIN, MiteMaterial.SILVER, 0),
            expected("gold_chain", "Golden Chain", "金锁链", RawItem.Kind.CHAIN, MiteMaterial.GOLD, 0),
            expected("rusted_iron_chain", "Rusted Iron Chain", "锈铁链", RawItem.Kind.CHAIN, MiteMaterial.RUSTED_IRON, 0),
            expected("iron_chain", "Iron Chain", "铁锁链", RawItem.Kind.CHAIN, MiteMaterial.IRON, 0),
            expected("ancient_metal_chain", "Ancient Metal Chain", "远古金属锁链", RawItem.Kind.CHAIN, MiteMaterial.ANCIENT_METAL, 0),
            expected("mithril_chain", "Mithril Chain", "秘银锁链", RawItem.Kind.CHAIN, MiteMaterial.MITHRIL, 0),
            expected("adamantium_chain", "Adamantium Chain", "艾德曼锁链", RawItem.Kind.CHAIN, MiteMaterial.ADAMANTIUM, 0),
            expected("copper_coin", "Copper Coin", "铜币", RawItem.Kind.COIN, MiteMaterial.COPPER, 5),
            expected("silver_coin", "Silver Coin", "银币", RawItem.Kind.COIN, MiteMaterial.SILVER, 25),
            expected("gold_coin", "Gold Coin", "金币", RawItem.Kind.COIN, MiteMaterial.GOLD, 100),
            expected("ancient_metal_coin", "Ancient Metal Coin", "远古金属币", RawItem.Kind.COIN, MiteMaterial.ANCIENT_METAL, 500),
            expected("mithril_coin", "Mithril Coin", "秘银币", RawItem.Kind.COIN, MiteMaterial.MITHRIL, 2_500),
            expected("adamantium_coin", "Adamantium Coin", "艾德曼币", RawItem.Kind.COIN, MiteMaterial.ADAMANTIUM, 10_000));

    @Test
    void rawCatalogHasExactApprovedDefinitions() {
        assertEquals(EXPECTED.stream().map(ExpectedRawItem::path).toList(),
                List.of(RawItem.values()).stream().map(RawItem::path).toList());
        assertEquals(30, RawItem.values().length);
        Set<String> unique = List.of(RawItem.values()).stream().map(RawItem::path).collect(Collectors.toSet());
        assertEquals(30, unique.size());

        for (int index = 0; index < EXPECTED.size(); index++) {
            ExpectedRawItem expected = EXPECTED.get(index);
            RawItem actual = RawItem.values()[index];
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
        assertEquals(List.of("SHARD", "BINDING", "FERTILIZER", "NUGGET", "INGOT", "CHAIN", "COIN"),
                List.of(RawItem.Kind.values()).stream().map(Enum::name).toList());
    }

    @Test
    void coinXpIsStoredWithoutImplementingExchange() {
        assertEquals(5, RawItem.COPPER_COIN.coinXp());
        assertEquals(25, RawItem.SILVER_COIN.coinXp());
        assertEquals(100, RawItem.GOLD_COIN.coinXp());
        assertEquals(500, RawItem.ANCIENT_METAL_COIN.coinXp());
        assertEquals(2_500, RawItem.MITHRIL_COIN.coinXp());
        assertEquals(10_000, RawItem.ADAMANTIUM_COIN.coinXp());
        assertEquals(MiteMaterial.FLINT, RawItem.FLINT_CHIP.material().orElseThrow());
    }

    private static ExpectedRawItem expected(
            String path, String englishName, String chineseName, RawItem.Kind kind, MiteMaterial material, int coinXp) {
        return new ExpectedRawItem(path, englishName, chineseName, kind, Optional.ofNullable(material), coinXp);
    }

    private record ExpectedRawItem(
            String path,
            String englishName,
            String chineseName,
            RawItem.Kind kind,
            Optional<MiteMaterial> material,
            int coinXp) {}
}

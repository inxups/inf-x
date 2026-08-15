package com.pixulse.infx.item.material;

import java.util.Optional;

public enum RawItem {
    FLINT_CHIP("flint_chip", "Flint Chip", "燧石碎片", Kind.SHARD, InfxMaterial.FLINT, 0),
    OBSIDIAN_SHARD("obsidian_shard", "Obsidian Shard", "黑曜石碎片", Kind.SHARD, InfxMaterial.OBSIDIAN, 0),
    EMERALD_SHARD("emerald_shard", "Emerald Shard", "绿宝石碎片", Kind.SHARD, null, 0),
    DIAMOND_SHARD("diamond_shard", "Diamond Shard", "钻石碎片", Kind.SHARD, null, 0),
    NETHER_QUARTZ_SHARD("nether_quartz_shard", "Nether Quartz Shard", "下界石英碎片", Kind.SHARD, null, 0),
    GLASS_SHARD("glass_shard", "Glass Shard", "玻璃碎片", Kind.SHARD, null, 0),
    SINEW("sinew", "Sinew", "皮筋", Kind.BINDING, null, 0),
    MANURE("manure", "Manure", "肥料", Kind.FERTILIZER, null, 0),
    SILVER_NUGGET("silver_nugget", "Silver Nugget", "银粒", Kind.NUGGET, InfxMaterial.SILVER, 0),
    MITHRIL_NUGGET("mithril_nugget", "Mithril Nugget", "秘银粒", Kind.NUGGET, InfxMaterial.MITHRIL, 0),
    ADAMANTIUM_NUGGET("adamantium_nugget", "Adamantium Nugget", "艾德曼粒", Kind.NUGGET, InfxMaterial.ADAMANTIUM, 0),
    ANCIENT_METAL_NUGGET("ancient_metal_nugget", "Ancient Metal Nugget", "远古金属粒", Kind.NUGGET, InfxMaterial.ANCIENT_METAL, 0),
    RAW_SILVER("raw_silver", "Raw Silver", "粗银", Kind.RAW, InfxMaterial.SILVER, 0),
    RAW_MITHRIL("raw_mithril", "Raw Mithril", "粗秘银", Kind.RAW, InfxMaterial.MITHRIL, 0),
    RAW_ADAMANTIUM("raw_adamantium", "Raw Adamantium", "粗艾德曼", Kind.RAW, InfxMaterial.ADAMANTIUM, 0),
    SILVER_INGOT("silver_ingot", "Silver Ingot", "银锭", Kind.INGOT, InfxMaterial.SILVER, 0),
    MITHRIL_INGOT("mithril_ingot", "Mithril Ingot", "秘银锭", Kind.INGOT, InfxMaterial.MITHRIL, 0),
    ADAMANTIUM_INGOT("adamantium_ingot", "Adamantium Ingot", "艾德曼锭", Kind.INGOT, InfxMaterial.ADAMANTIUM, 0),
    ANCIENT_METAL_INGOT("ancient_metal_ingot", "Ancient Metal Ingot", "远古金属锭", Kind.INGOT, InfxMaterial.ANCIENT_METAL, 0),
    COPPER_CHAIN("copper_chain", "Copper Chain", "铜锁链", Kind.CHAIN, InfxMaterial.COPPER, 0),
    SILVER_CHAIN("silver_chain", "Silver Chain", "银锁链", Kind.CHAIN, InfxMaterial.SILVER, 0),
    GOLD_CHAIN("gold_chain", "Golden Chain", "金锁链", Kind.CHAIN, InfxMaterial.GOLD, 0),
    RUSTED_IRON_CHAIN("rusted_iron_chain", "Rusted Iron Chain", "锈铁链", Kind.CHAIN, InfxMaterial.RUSTED_IRON, 0),
    IRON_CHAIN("iron_chain", "Iron Chain", "铁锁链", Kind.CHAIN, InfxMaterial.IRON, 0),
    ANCIENT_METAL_CHAIN("ancient_metal_chain", "Ancient Metal Chain", "远古金属锁链", Kind.CHAIN, InfxMaterial.ANCIENT_METAL, 0),
    MITHRIL_CHAIN("mithril_chain", "Mithril Chain", "秘银锁链", Kind.CHAIN, InfxMaterial.MITHRIL, 0),
    ADAMANTIUM_CHAIN("adamantium_chain", "Adamantium Chain", "艾德曼锁链", Kind.CHAIN, InfxMaterial.ADAMANTIUM, 0),
    COPPER_COIN("copper_coin", "Copper Coin", "铜币", Kind.COIN, InfxMaterial.COPPER, 5),
    SILVER_COIN("silver_coin", "Silver Coin", "银币", Kind.COIN, InfxMaterial.SILVER, 25),
    GOLD_COIN("gold_coin", "Gold Coin", "金币", Kind.COIN, InfxMaterial.GOLD, 100),
    ANCIENT_METAL_COIN("ancient_metal_coin", "Ancient Metal Coin", "远古金属币", Kind.COIN, InfxMaterial.ANCIENT_METAL, 500),
    MITHRIL_COIN("mithril_coin", "Mithril Coin", "秘银币", Kind.COIN, InfxMaterial.MITHRIL, 2_500),
    ADAMANTIUM_COIN("adamantium_coin", "Adamantium Coin", "艾德曼币", Kind.COIN, InfxMaterial.ADAMANTIUM, 10_000);

    public enum Kind { SHARD, BINDING, FERTILIZER, RAW, NUGGET, INGOT, CHAIN, COIN }

    private final String path;
    private final String englishName;
    private final String chineseName;
    private final Kind kind;
    private final Optional<InfxMaterial> material;
    private final int coinXp;

    RawItem(String path, String englishName, String chineseName, Kind kind,
            InfxMaterial material, int coinXp) {
        this.path = path;
        this.englishName = englishName;
        this.chineseName = chineseName;
        this.kind = kind;
        this.material = Optional.ofNullable(material);
        this.coinXp = coinXp;
    }

    public String path() { return path; }
    public String englishName() { return englishName; }
    public String chineseName() { return chineseName; }
    public Kind kind() { return kind; }
    public Optional<InfxMaterial> material() { return material; }
    public int coinXp() { return coinXp; }

    public String translationKey() {
        return "item.infx." + path;
    }
}

package com.pixulse.infx.material;

import java.util.Optional;

public enum R196RawItem {
    FLINT_CHIP("flint_chip", "Flint Chip", "燧石碎片", Kind.SHARD, R196Material.FLINT, 0),
    OBSIDIAN_SHARD("obsidian_shard", "Obsidian Shard", "黑曜石碎片", Kind.SHARD, R196Material.OBSIDIAN, 0),
    EMERALD_SHARD("emerald_shard", "Emerald Shard", "绿宝石碎片", Kind.SHARD, null, 0),
    DIAMOND_SHARD("diamond_shard", "Diamond Shard", "钻石碎片", Kind.SHARD, null, 0),
    NETHER_QUARTZ_SHARD("nether_quartz_shard", "Nether Quartz Shard", "下界石英碎片", Kind.SHARD, null, 0),
    GLASS_SHARD("glass_shard", "Glass Shard", "玻璃碎片", Kind.SHARD, null, 0),
    SINEW("sinew", "Sinew", "皮筋", Kind.BINDING, null, 0),
    MANURE("manure", "Manure", "肥料", Kind.FERTILIZER, null, 0),
    SILVER_NUGGET("silver_nugget", "Silver Nugget", "银粒", Kind.NUGGET, R196Material.SILVER, 0),
    MITHRIL_NUGGET("mithril_nugget", "Mithril Nugget", "秘银粒", Kind.NUGGET, R196Material.MITHRIL, 0),
    ADAMANTIUM_NUGGET("adamantium_nugget", "Adamantium Nugget", "艾德曼粒", Kind.NUGGET, R196Material.ADAMANTIUM, 0),
    ANCIENT_METAL_NUGGET("ancient_metal_nugget", "Ancient Metal Nugget", "远古金属粒", Kind.NUGGET, R196Material.ANCIENT_METAL, 0),
    SILVER_INGOT("silver_ingot", "Silver Ingot", "银锭", Kind.INGOT, R196Material.SILVER, 0),
    MITHRIL_INGOT("mithril_ingot", "Mithril Ingot", "秘银锭", Kind.INGOT, R196Material.MITHRIL, 0),
    ADAMANTIUM_INGOT("adamantium_ingot", "Adamantium Ingot", "艾德曼锭", Kind.INGOT, R196Material.ADAMANTIUM, 0),
    ANCIENT_METAL_INGOT("ancient_metal_ingot", "Ancient Metal Ingot", "远古金属锭", Kind.INGOT, R196Material.ANCIENT_METAL, 0),
    COPPER_CHAIN("copper_chain", "Copper Chain", "铜锁链", Kind.CHAIN, R196Material.COPPER, 0),
    SILVER_CHAIN("silver_chain", "Silver Chain", "银锁链", Kind.CHAIN, R196Material.SILVER, 0),
    GOLD_CHAIN("gold_chain", "Golden Chain", "金锁链", Kind.CHAIN, R196Material.GOLD, 0),
    RUSTED_IRON_CHAIN("rusted_iron_chain", "Rusted Iron Chain", "锈铁链", Kind.CHAIN, R196Material.RUSTED_IRON, 0),
    IRON_CHAIN("iron_chain", "Iron Chain", "铁锁链", Kind.CHAIN, R196Material.IRON, 0),
    ANCIENT_METAL_CHAIN("ancient_metal_chain", "Ancient Metal Chain", "远古金属锁链", Kind.CHAIN, R196Material.ANCIENT_METAL, 0),
    MITHRIL_CHAIN("mithril_chain", "Mithril Chain", "秘银锁链", Kind.CHAIN, R196Material.MITHRIL, 0),
    ADAMANTIUM_CHAIN("adamantium_chain", "Adamantium Chain", "艾德曼锁链", Kind.CHAIN, R196Material.ADAMANTIUM, 0),
    COPPER_COIN("copper_coin", "Copper Coin", "铜币", Kind.COIN, R196Material.COPPER, 5),
    SILVER_COIN("silver_coin", "Silver Coin", "银币", Kind.COIN, R196Material.SILVER, 25),
    GOLD_COIN("gold_coin", "Gold Coin", "金币", Kind.COIN, R196Material.GOLD, 100),
    ANCIENT_METAL_COIN("ancient_metal_coin", "Ancient Metal Coin", "远古金属币", Kind.COIN, R196Material.ANCIENT_METAL, 500),
    MITHRIL_COIN("mithril_coin", "Mithril Coin", "秘银币", Kind.COIN, R196Material.MITHRIL, 2_500),
    ADAMANTIUM_COIN("adamantium_coin", "Adamantium Coin", "艾德曼币", Kind.COIN, R196Material.ADAMANTIUM, 10_000),
    CREEPER_FRAGS("creeper_frags", "Creeper Frags", "苦力怕碎片", Kind.MONSTER_FRAG, null, 0),
    INFERNAL_CREEPER_FRAGS("infernal_creeper_frags", "Infernal Creeper Frags", "地狱苦力怕碎片", Kind.MONSTER_FRAG, null, 0),
    NETHERSPAWN_FRAGS("netherspawn_frags", "Netherspawn Frags", "下界虫碎片", Kind.MONSTER_FRAG, null, 0);

    public enum Kind { SHARD, BINDING, FERTILIZER, NUGGET, INGOT, CHAIN, COIN, MONSTER_FRAG }

    private final String path;
    private final String englishName;
    private final String chineseName;
    private final Kind kind;
    private final Optional<R196Material> material;
    private final int coinXp;

    R196RawItem(String path, String englishName, String chineseName, Kind kind,
            R196Material material, int coinXp) {
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
    public Optional<R196Material> material() { return material; }
    public int coinXp() { return coinXp; }

    public String translationKey() {
        return "item.infx." + path;
    }
}

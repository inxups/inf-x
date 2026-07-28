package com.pixulse.infx.item;

import com.pixulse.infx.material.MiteMaterial;
import com.pixulse.infx.material.RawItem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Catalog {
    private final List<RawEntry> rawEntries;
    private final List<EquipmentEntry> equipmentEntries;
    private final List<Entry> entries;
    private final Map<String, RawEntry> rawByPath;
    private final Map<EquipmentKey, EquipmentEntry> equipmentByKey;
    private final Map<String, Item> reusedRaw;

    private Catalog(
            List<RawEntry> rawEntries,
            List<EquipmentEntry> equipmentEntries,
            Map<String, RawEntry> rawByPath,
            Map<EquipmentKey, EquipmentEntry> equipmentByKey) {
        this.rawEntries = List.copyOf(rawEntries);
        this.equipmentEntries = List.copyOf(equipmentEntries);
        List<Entry> allEntries = new ArrayList<>(rawEntries.size() + equipmentEntries.size());
        allEntries.addAll(rawEntries);
        allEntries.addAll(equipmentEntries);
        this.entries = List.copyOf(allEntries);
        this.rawByPath = Map.copyOf(rawByPath);
        this.equipmentByKey = Map.copyOf(equipmentByKey);
        this.reusedRaw = Map.ofEntries(
                Map.entry("copper_nugget", Items.COPPER_NUGGET),
                Map.entry("gold_nugget", Items.GOLD_NUGGET),
                Map.entry("iron_nugget", Items.IRON_NUGGET),
                Map.entry("copper_ingot", Items.COPPER_INGOT),
                Map.entry("gold_ingot", Items.GOLD_INGOT),
                Map.entry("iron_ingot", Items.IRON_INGOT),
                Map.entry("flint", Items.FLINT),
                Map.entry("string", Items.STRING),
                Map.entry("leather", Items.LEATHER),
                Map.entry("feather", Items.FEATHER),
                Map.entry("stick", Items.STICK),
                Map.entry("obsidian", Items.OBSIDIAN),
                Map.entry("diamond", Items.DIAMOND),
                Map.entry("emerald", Items.EMERALD),
                Map.entry("nether_quartz", Items.QUARTZ),
                Map.entry("glass", Items.GLASS));
    }

    public static Catalog register(DeferredRegister.Items items) {
        List<RawEntry> rawEntries = new ArrayList<>();
        List<EquipmentEntry> equipmentEntries = new ArrayList<>();
        Map<String, RawEntry> rawByPath = new LinkedHashMap<>();
        Map<EquipmentKey, EquipmentEntry> equipmentByKey = new LinkedHashMap<>();

        for (RawItem definition : RawItem.values()) {
            DeferredItem<Item> holder = items.registerItem(
                    definition.path(),
                    properties -> definition.kind() == RawItem.Kind.COIN
                            ? new CoinItem(definition, properties)
                            : definition.kind() == RawItem.Kind.FERTILIZER
                                    ? new ManureItem(properties)
                                    : new Item(properties),
                    properties -> definition.material()
                                    .filter(material -> material.has(MiteMaterial.Flag.LAVA_SAFE))
                                    .isPresent()
                            ? properties.fireResistant()
                            : properties);
            RawEntry entry = new RawEntry(definition, holder);
            if (rawByPath.put(entry.path(), entry) != null) {
                throw new IllegalStateException("Duplicate R196 raw item: " + entry.path());
            }
            rawEntries.add(entry);
        }

        for (EquipmentKey key : EquipmentKey.all()) {
            EquipmentEntry entry = registerEquipment(items, key);
            if (equipmentByKey.put(key, entry) != null) {
                throw new IllegalStateException("Duplicate R196 equipment: " + key.path());
            }
            equipmentEntries.add(entry);
        }

        return new Catalog(rawEntries, equipmentEntries, rawByPath, equipmentByKey);
    }

    private static EquipmentEntry registerEquipment(DeferredRegister.Items items, EquipmentKey key) {
        return switch (key.type().factoryKind()) {
            case PLAIN -> {
                DeferredItem<Item> holder = items.registerItem(
                        key.path(), Item::new, properties -> ItemProperties.forEquipment(key, properties));
                yield new EquipmentEntry(key, holder, Item.class);
            }
            case SHEARS -> {
                DeferredItem<MiteShearsItem> holder = items.registerItem(
                        key.path(),
                        properties -> new MiteShearsItem(key, properties),
                        properties -> ItemProperties.forEquipment(key, properties));
                yield new EquipmentEntry(key, holder, MiteShearsItem.class);
            }
            case FISHING_ROD -> {
                DeferredItem<MiteFishingRodItem> holder = items.registerItem(
                        key.path(),
                        properties -> new MiteFishingRodItem(key, properties),
                        properties -> ItemProperties.forEquipment(key, properties));
                yield new EquipmentEntry(key, holder, MiteFishingRodItem.class);
            }
            case BOW -> {
                DeferredItem<MiteBowItem> holder = items.registerItem(
                        key.path(),
                        properties -> new MiteBowItem(key, properties),
                        properties -> ItemProperties.forEquipment(key, properties));
                yield new EquipmentEntry(key, holder, MiteBowItem.class);
            }
            case ARROW -> {
                DeferredItem<MiteArrowItem> holder = items.registerItem(
                        key.path(),
                        properties -> new MiteArrowItem(key, properties),
                        properties -> ItemProperties.forEquipment(key, properties));
                yield new EquipmentEntry(key, holder, MiteArrowItem.class);
            }
            case ORDINARY -> {
                DeferredItem<ToolItem> holder = items.registerItem(
                        key.path(),
                        properties -> new ToolItem(key, properties),
                        properties -> ItemProperties.forEquipment(key, properties));
                yield new EquipmentEntry(key, holder, ToolItem.class);
            }
        };
    }

    public EquipmentEntry equipment(Item item) {
        return equipmentEntries.stream()
                .filter(entry -> entry.holder().value() == item)
                .findFirst()
                .orElse(null);
    }

    public EquipmentEntry equipment(ItemStack stack) {
        return equipment(stack.getItem());
    }

    public RawEntry raw(Item item) {
        return rawEntries.stream()
                .filter(entry -> entry.holder().value() == item)
                .findFirst()
                .orElse(null);
    }

    public List<RawEntry> rawEntries() {
        return rawEntries;
    }

    public List<EquipmentEntry> equipmentEntries() {
        return equipmentEntries;
    }

    public List<Entry> entries() {
        return entries;
    }

    public RawEntry raw(String path) {
        RawEntry entry = rawByPath.get(path);
        if (entry == null) {
            throw new IllegalArgumentException("Missing R196 raw item: " + path);
        }
        return entry;
    }

    public EquipmentEntry equipment(MiteMaterial material, EquipmentType type) {
        String path = material.path() + "_" + type.path();
        if (!type.allows(material)) {
            throw new IllegalArgumentException("Missing R196 equipment: " + path);
        }
        EquipmentEntry entry = equipmentByKey.get(new EquipmentKey(material, type));
        if (entry == null) {
            throw new IllegalArgumentException("Missing R196 equipment: " + path);
        }
        return entry;
    }

    public Item reusedRaw(String path) {
        Item item = reusedRaw.get(path);
        if (item == null) {
            throw new IllegalArgumentException("Missing reused R196 raw item: " + path);
        }
        return item;
    }

    public sealed interface Entry permits RawEntry, EquipmentEntry {
        String path();

        Identifier id();

        DeferredItem<? extends Item> holder();

        Class<? extends Item> itemClass();

        String englishName();

        String chineseName();

        <T extends Item> DeferredItem<T> holderAs(Class<T> requestedClass);
    }

    public record RawEntry(RawItem definition, DeferredItem<Item> holder) implements Entry {
        @Override
        public String path() {
            return definition.path();
        }

        @Override
        public Identifier id() {
            return holder.getId();
        }

        @Override
        public Class<? extends Item> itemClass() {
            return Item.class;
        }

        @Override
        public String englishName() {
            return definition.englishName();
        }

        @Override
        public String chineseName() {
            return definition.chineseName();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Item> DeferredItem<T> holderAs(Class<T> requestedClass) {
            if (requestedClass != Item.class) {
                throw new IllegalArgumentException(
                        "Wrong item class for " + path() + ": " + requestedClass.getName());
            }
            return (DeferredItem<T>) (DeferredItem<?>) holder;
        }
    }

    public record EquipmentEntry(
            EquipmentKey key,
            DeferredItem<? extends Item> holder,
            Class<? extends Item> itemClass) implements Entry {
        @Override
        public String path() {
            return key.path();
        }

        @Override
        public Identifier id() {
            return holder.getId();
        }

        @Override
        public String englishName() {
            return key.englishName();
        }

        @Override
        public String chineseName() {
            return key.chineseName();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Item> DeferredItem<T> holderAs(Class<T> requestedClass) {
            if (requestedClass != itemClass) {
                throw new IllegalArgumentException(
                        "Wrong item class for " + path() + ": " + requestedClass.getName());
            }
            return (DeferredItem<T>) (DeferredItem<?>) holder;
        }
    }
}

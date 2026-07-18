package com.pixulse.infx.item;

import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.material.R196RawItem;
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

public final class R196Catalog {
    private final List<RawEntry> rawEntries;
    private final List<EquipmentEntry> equipmentEntries;
    private final List<Entry> entries;
    private final Map<String, RawEntry> rawByPath;
    private final Map<R196EquipmentKey, EquipmentEntry> equipmentByKey;
    private final Map<String, Item> reusedRaw;

    private R196Catalog(
            List<RawEntry> rawEntries,
            List<EquipmentEntry> equipmentEntries,
            Map<String, RawEntry> rawByPath,
            Map<R196EquipmentKey, EquipmentEntry> equipmentByKey) {
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

    public static R196Catalog register(DeferredRegister.Items items) {
        List<RawEntry> rawEntries = new ArrayList<>();
        List<EquipmentEntry> equipmentEntries = new ArrayList<>();
        Map<String, RawEntry> rawByPath = new LinkedHashMap<>();
        Map<R196EquipmentKey, EquipmentEntry> equipmentByKey = new LinkedHashMap<>();

        for (R196RawItem definition : R196RawItem.values()) {
            DeferredItem<Item> holder = items.registerItem(
                    definition.path(),
                    properties -> definition.kind() == R196RawItem.Kind.COIN
                            ? new R196CoinItem(definition, properties)
                            : definition.kind() == R196RawItem.Kind.FERTILIZER
                                    ? new R196ManureItem(properties)
                                    : new Item(properties),
                    properties -> definition.material()
                                    .filter(material -> material.has(R196Material.Flag.LAVA_SAFE))
                                    .isPresent()
                            ? properties.fireResistant()
                            : properties);
            RawEntry entry = new RawEntry(definition, holder);
            if (rawByPath.put(entry.path(), entry) != null) {
                throw new IllegalStateException("Duplicate R196 raw item: " + entry.path());
            }
            rawEntries.add(entry);
        }

        for (R196EquipmentKey key : R196EquipmentKey.all()) {
            EquipmentEntry entry = registerEquipment(items, key);
            if (equipmentByKey.put(key, entry) != null) {
                throw new IllegalStateException("Duplicate R196 equipment: " + key.path());
            }
            equipmentEntries.add(entry);
        }

        return new R196Catalog(rawEntries, equipmentEntries, rawByPath, equipmentByKey);
    }

    private static EquipmentEntry registerEquipment(DeferredRegister.Items items, R196EquipmentKey key) {
        return switch (key.type().factoryKind()) {
            case PLAIN -> {
                DeferredItem<Item> holder = items.registerItem(
                        key.path(), Item::new, properties -> R196ItemProperties.forEquipment(key, properties));
                yield new EquipmentEntry(key, holder, Item.class);
            }
            case SHEARS -> {
                DeferredItem<R196ShearsItem> holder = items.registerItem(
                        key.path(),
                        properties -> new R196ShearsItem(key, properties),
                        properties -> R196ItemProperties.forEquipment(key, properties));
                yield new EquipmentEntry(key, holder, R196ShearsItem.class);
            }
            case FISHING_ROD -> {
                DeferredItem<R196FishingRodItem> holder = items.registerItem(
                        key.path(),
                        properties -> new R196FishingRodItem(key, properties),
                        properties -> R196ItemProperties.forEquipment(key, properties));
                yield new EquipmentEntry(key, holder, R196FishingRodItem.class);
            }
            case BOW -> {
                DeferredItem<R196BowItem> holder = items.registerItem(
                        key.path(),
                        properties -> new R196BowItem(key, properties),
                        properties -> R196ItemProperties.forEquipment(key, properties));
                yield new EquipmentEntry(key, holder, R196BowItem.class);
            }
            case ARROW -> {
                DeferredItem<R196ArrowItem> holder = items.registerItem(
                        key.path(),
                        properties -> new R196ArrowItem(key, properties),
                        properties -> R196ItemProperties.forEquipment(key, properties));
                yield new EquipmentEntry(key, holder, R196ArrowItem.class);
            }
            case ORDINARY -> {
                DeferredItem<R196ToolItem> holder = items.registerItem(
                        key.path(),
                        properties -> new R196ToolItem(key, properties),
                        properties -> R196ItemProperties.forEquipment(key, properties));
                yield new EquipmentEntry(key, holder, R196ToolItem.class);
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

    public EquipmentEntry equipment(R196Material material, R196EquipmentType type) {
        String path = material.path() + "_" + type.path();
        if (!type.allows(material)) {
            throw new IllegalArgumentException("Missing R196 equipment: " + path);
        }
        EquipmentEntry entry = equipmentByKey.get(new R196EquipmentKey(material, type));
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

    public record RawEntry(R196RawItem definition, DeferredItem<Item> holder) implements Entry {
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
            R196EquipmentKey key,
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

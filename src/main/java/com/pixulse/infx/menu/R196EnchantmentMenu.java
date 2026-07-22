package com.pixulse.infx.menu;

import com.pixulse.infx.block.R196EnchantingTableBlock;
import com.pixulse.infx.enchantment.R196EnchantmentRules;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.mixin.EnchantmentMenuAccessor;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.registry.ModMenus;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Util;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/** Server menu for R196's emerald (50 power) and diamond (100 power) tables. */
public final class R196EnchantmentMenu extends EnchantmentMenu {
    private static final List<BlockPos> BOOKSHELVES = createBookshelfOffsets();
    private final int[] enchantmentPowers = new int[3];
    private final Kind kind;

    public R196EnchantmentMenu(int containerId, Inventory inventory, ContainerLevelAccess access, Kind kind) {
        super(containerId, inventory, access);
        this.kind = kind;
        Slot currencySlot = new Slot(accessors().infx$enchantSlots(), 1, 35, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return acceptsCurrency(stack);
            }
        };
        currencySlot.index = 1;
        slots.set(1, currencySlot);
    }

    private EnchantmentMenuAccessor accessors() {
        return (EnchantmentMenuAccessor) (Object) this;
    }

    public boolean acceptsCurrency(ItemStack stack) {
        return stack.is(kind.currency());
    }

    public Item currency() {
        return kind.currency();
    }

    @Override
    public MenuType<?> getType() {
        return switch (kind) {
            case EMERALD -> ModMenus.EMERALD_ENCHANTING.get();
            case DIAMOND -> ModMenus.DIAMOND_ENCHANTING.get();
        };
    }

    @Override
    public void slotsChanged(Container container) {
        Container enchantSlots = accessors().infx$enchantSlots();
        if (container != enchantSlots) return;
        ItemStack stack = container.getItem(0);
        if (stack.isEmpty() || !stack.isEnchantable()) {
            for (int index = 0; index < 3; index++) {
                costs[index] = 0;
                enchantmentPowers[index] = 0;
                enchantClue[index] = -1;
                levelClue[index] = -1;
            }
            return;
        }
        accessors().infx$access().execute((level, pos) -> {
            IdMap<Holder<Enchantment>> holders = level.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .asHolderIdMap();
            int power = Math.min(kind.maximumPower(), (bookshelfCount(level, pos) + 1) * kind.powerPerShelf());
            var equipment = ModItems.catalog().equipment(stack);
            if (equipment != null && (equipment.key().material() == R196Material.COPPER
                    || equipment.key().material() == R196Material.SILVER
                    || equipment.key().material() == R196Material.GOLD)) {
                power = Math.min(80, power);
            }
            accessors().infx$random().setSeed(accessors().infx$enchantmentSeed().get());
            for (int index = 0; index < 3; index++) {
                enchantmentPowers[index] = Math.max(index + 1, Math.round(power * (index + 1) / 3.0F));
                enchantClue[index] = -1;
                levelClue[index] = -1;
                enchantmentPowers[index] = net.neoforged.neoforge.event.EventHooks.onEnchantmentLevelSet(
                        level, pos, index, bookshelfCount(level, pos), stack, enchantmentPowers[index]);
                costs[index] = R196EnchantmentRules.experienceCost(enchantmentPowers[index]);
                List<EnchantmentInstance> choices = accessors().infx$getEnchantmentList(
                        level.registryAccess(), stack, index, enchantmentPowers[index]);
                if (!choices.isEmpty()) {
                    EnchantmentInstance choice = choices.get(accessors().infx$random().nextInt(choices.size()));
                    enchantClue[index] = holders.getId(choice.enchantment());
                    levelClue[index] = choice.level();
                }
            }
            broadcastChanges();
        });
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId < 0 || buttonId >= costs.length) {
            Util.logAndPauseIfInIde(player.getPlainTextName() + " pressed invalid R196 enchantment button " + buttonId);
            return false;
        }
        Container slots = accessors().infx$enchantSlots();
        ItemStack input = slots.getItem(0);
        ItemStack currency = slots.getItem(1);
        int currencyCost = buttonId + 1;
        int experienceCost = costs[buttonId];
        if ((!currency.is(kind.currency()) || currency.getCount() < currencyCost) && !player.hasInfiniteMaterials()) return false;
        if (experienceCost <= 0 || input.isEmpty()
                || player.totalExperience < experienceCost && !player.hasInfiniteMaterials()) return false;
        accessors().infx$access().execute((level, pos) -> {
            List<EnchantmentInstance> selected = accessors().infx$getEnchantmentList(
                    level.registryAccess(), input, buttonId, enchantmentPowers[buttonId]);
            if (selected.isEmpty()) return;
            player.onEnchantmentPerformed(input, 0);
            if (!player.hasInfiniteMaterials()) {
                player.giveExperiencePoints(-experienceCost);
            }
            ItemStack enchanted = input.getItem().applyEnchantments(input, selected);
            slots.setItem(0, enchanted);
            net.neoforged.neoforge.common.CommonHooks.onPlayerEnchantItem(player, enchanted, selected);
            currency.consume(currencyCost, player);
            if (currency.isEmpty()) slots.setItem(1, ItemStack.EMPTY);
            player.awardStat(Stats.ENCHANT_ITEM);
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, enchanted, currencyCost);
            }
            slots.setChanged();
            accessors().infx$enchantmentSeed().set(player.getEnchantmentSeed());
            slotsChanged(slots);
            level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F,
                    level.getRandom().nextFloat() * 0.1F + 0.9F);
        });
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(accessors().infx$access(), player, ModBlocks.EMERALD_ENCHANTING_TABLE.get())
                || stillValid(accessors().infx$access(), player, ModBlocks.DIAMOND_ENCHANTING_TABLE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 2) return super.quickMoveStack(player, slotIndex);
        Slot slot = slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        if (!stack.is(kind.currency())) return super.quickMoveStack(player, slotIndex);
        ItemStack original = stack.copy();
        if (!moveItemStackTo(stack, 1, 2, true)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return original;
    }

    public static int bookshelfCount(Level level, BlockPos table) {
        int count = 0;
        for (BlockPos offset : BOOKSHELVES) {
            BlockPos shelf = table.offset(offset);
            BlockPos between = table.offset(offset.getX() / 2, offset.getY(), offset.getZ() / 2);
            if (level.getBlockState(shelf).getEnchantPowerBonus(level, shelf) != 0
                    && (level.getBlockState(between).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)
                            || level.getBlockState(between).is(Blocks.VINE))) {
                count++;
            }
        }
        return count;
    }

    private static List<BlockPos> createBookshelfOffsets() {
        List<BlockPos> result = new ArrayList<>(24);
        for (int y = 0; y <= 1; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if ((Math.abs(x) == 2 || Math.abs(z) == 2)
                            && !(Math.abs(x) == 2 && Math.abs(z) == 2)) {
                        result.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return List.copyOf(result);
    }

    public enum Kind {
        EMERALD(Items.EMERALD, 2, 50),
        DIAMOND(Items.DIAMOND, 4, 100);

        private final Item currency;
        private final int powerPerShelf;
        private final int maximumPower;

        Kind(Item currency, int powerPerShelf, int maximumPower) {
            this.currency = currency;
            this.powerPerShelf = powerPerShelf;
            this.maximumPower = maximumPower;
        }

        public Item currency() { return currency; }
        public int powerPerShelf() { return powerPerShelf; }
        public int maximumPower() { return maximumPower; }
    }
}

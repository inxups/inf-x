package com.pixulse.infx.progression;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.entity.R196Mob;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.world.Underworld;
import java.util.List;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.brewing.PlayerBrewedPotionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class ProgressionEvents {
    private static final Identifier ACQUIRE_IRON = InfiniteX.id("progression/acquire_iron");
    private static final String SMELTED_IRON_CRITERION = "smelted_iron";
    private static final String RAIL_START = "infx_advancement_rail_start";
    private static final String SLEEP_START = "infx_advancement_sleep_start";
    private static final String CREATION_BOOKS_READ = "infx_creation_books_read";
    private static final String CREATION_BOOK_AUTHOR = "Father Phoonzang";
    private static final List<String> CREATION_BOOK_TITLES = List.of(
            "Boat", "Crypt", "Crystal", "Dragon", "Globe", "Serpent", "Sphinx", "Star", "Temple");

    private ProgressionEvents() {}

    public static void award(ServerPlayer player, String path, String criterion) {
        AdvancementHolder advancement = player.level().getServer().getAdvancements()
                .get(InfiniteX.id("progression/" + path));
        if (advancement != null) {
            player.getAdvancements().award(advancement, criterion);
        }
    }

    public static void register(IEventBus gameBus) {
        gameBus.addListener(ProgressionEvents::onItemSmelted);
        gameBus.addListener(ProgressionEvents::onItemCrafted);
        gameBus.addListener(ProgressionEvents::onItemPickup);
        gameBus.addListener(ProgressionEvents::onLivingDeath);
        gameBus.addListener(ProgressionEvents::onLivingDamage);
        gameBus.addListener(ProgressionEvents::onItemFinished);
        gameBus.addListener(ProgressionEvents::onPotionBrewed);
        gameBus.addListener(ProgressionEvents::onWrittenBookOpened);
        gameBus.addListener(ProgressionEvents::onDimensionChanged);
        gameBus.addListener(ProgressionEvents::onBlockBroken);
        gameBus.addListener(ProgressionEvents::onPlayerTick);
        gameBus.addListener(ProgressionEvents::onLivingFall);
    }

    private static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmountRemoved() <= 0) {
            return;
        }
        ItemStack result = event.getSmelting();
        if (result.is(Items.IRON_INGOT)) award(player, "acquire_iron", SMELTED_IRON_CRITERION);
        if (result.is(ModItems.MITHRIL_INGOT)) award(player, "mithril_ingot", "smelted_mithril");
        if (result.is(ModItems.ADAMANTIUM_INGOT)) award(player, "adamantium_ingot", "smelted_adamantium");
        if (result.is(Items.BREAD)) award(player, "make_bread", "smelted_bread");
        if (result.is(Items.COOKED_COD) || result.is(Items.COOKED_SALMON)) {
            award(player, "cook_fish", "smelted_fish");
        }
    }

    private static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack crafted = event.getCrafting();
        Identifier id = BuiltInRegistries.ITEM.getKey(crafted.getItem());
        String path = id.getPath();
        if (path.equals("flour")) award(player, "flour", "crafted_flour");
        if (crafted.is(Items.ENCHANTING_TABLE)) {
            if (player.getInventory().contains(Items.DIAMOND.getDefaultInstance())) {
                award(player, "enchantments", "diamond_path");
            }
            if (player.getInventory().contains(Items.EMERALD.getDefaultInstance())) {
                award(player, "enchantments", "emerald_path");
            }
        }
        if ((path.contains("salad") || path.contains("soup") || path.contains("stew"))
                || crafted.is(Items.MUSHROOM_STEW)) {
            award(player, "fine_dining", "crafted_fine_food");
        }
        var entry = ModItems.catalog().equipment(crafted);
        if (entry != null
                && entry.key().material() == R196Material.ADAMANTIUM
                && (entry.key().type() == R196EquipmentType.PICKAXE
                        || entry.key().type() == R196EquipmentType.WAR_HAMMER)) {
            award(player, "crystal_breaker", "crafted_crystal_tool");
        }
    }

    private static void onItemPickup(ItemEntityPickupEvent.Post event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getOriginalStack();
        if (stack.is(Items.STICK)) award(player, "stick_picker", "picked_up_stick");
        if (stack.is(ItemTags.LOGS)) award(player, "mine_wood", "picked_up_log");
        if (stack.is(Items.LEATHER)) award(player, "kill_cow", "picked_up_leather");
        if (isR196MetalNugget(stack)) award(player, "nuggets", "picked_up_metal_nugget");
        if (stack.is(Items.DIAMOND)) award(player, "diamonds", "picked_up_diamond");
        if (stack.is(Items.EMERALD)) award(player, "emeralds", "picked_up_emerald");
        if (stack.is(Items.BLAZE_ROD)) award(player, "blaze_rod", "picked_up_blaze_rod");
        if (stack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS)) award(player, "seeds", "picked_up_seed");
    }

    private static boolean isR196MetalNugget(ItemStack stack) {
        return stack.is(Items.COPPER_NUGGET)
                || stack.is(ModItems.SILVER_NUGGET)
                || stack.is(Items.GOLD_NUGGET)
                || stack.is(Items.IRON_NUGGET)
                || stack.is(ModItems.MITHRIL_NUGGET)
                || stack.is(ModItems.ADAMANTIUM_NUGGET);
    }

    private static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (event.getEntity() instanceof Enemy) award(player, "kill_enemy", "killed_enemy");
        if (event.getSource().getDirectEntity() instanceof AbstractArrow
                && (event.getEntity().getType() == EntityTypes.SKELETON
                        || event.getEntity().getType() == ModEntityTypes.R196_SKELETON.get())) {
            double dx = player.getX() - event.getEntity().getX();
            double dz = player.getZ() - event.getEntity().getZ();
            if (dx * dx + dz * dz >= 2_500.0) {
                award(player, "snipe_skeleton", "long_range_skeleton_kill");
            }
        }
        if ((event.getEntity().getType() == EntityTypes.GHAST
                        || event.getEntity().getType() == ModEntityTypes.R196_GHAST.get())
                && event.getSource().getDirectEntity() instanceof LargeFireball fireball
                && fireball.getOwner() == player) {
            award(player, "ghast", "reflected_fireball_kill");
        }
    }

    private static void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player
                && event.getSource().getDirectEntity() == player
                && event.getOriginalDamage() >= 18.0F) {
            award(player, "overkill", "melee_damage_18");
        }
    }

    private static void onItemFinished(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getItem().is(Items.EGG)) {
            award(player, "eggs", "ate_raw_egg");
        }
    }

    private static void onPotionBrewed(PlayerBrewedPotionEvent event) {
        var contents = event.getStack().get(DataComponents.POTION_CONTENTS);
        if (event.getEntity() instanceof ServerPlayer player
                && event.getStack().is(Items.POTION)
                && contents != null
                && contents.potion().isPresent()
                && !contents.is(Potions.WATER)) {
            award(player, "potion", "brewed_potion");
        }
    }

    private static void onWrittenBookOpened(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !event.getItemStack().is(Items.WRITTEN_BOOK)) {
            return;
        }
        var content = event.getItemStack().get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (content == null) return;
        int index = creationBookIndex(content.author(), content.title().raw());
        if (index < 0) return;

        var data = player.getPersistentData();
        int previous = data.getInt(CREATION_BOOKS_READ).orElse(0);
        int updated = previous | 1 << index;
        if (updated == previous) return;
        data.putInt(CREATION_BOOKS_READ, updated);
        player.giveExperiencePoints(100);
        if (allCreationBooksRead(updated)) {
            award(player, "enlightenment", "read_nine_books");
        }
    }

    static int creationBookIndex(String author, String title) {
        return CREATION_BOOK_AUTHOR.equals(author) ? CREATION_BOOK_TITLES.indexOf(title) : -1;
    }

    static boolean allCreationBooksRead(int mask) {
        return (mask & (1 << CREATION_BOOK_TITLES.size()) - 1)
                == (1 << CREATION_BOOK_TITLES.size()) - 1;
    }

    private static void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getTo().equals(Level.END)) {
            if (event.getFrom().equals(Level.OVERWORLD)) {
                award(player, "the_end", "entered_end");
            }
        } else if (completedEndReturn(player, event.getFrom())) {
            award(player, "the_end2", "returned_from_end");
        } else if (event.getTo().equals(Underworld.LEVEL)
                || event.getFrom().equals(Underworld.LEVEL)
                || event.getTo().equals(Level.NETHER)
                || event.getFrom().equals(Level.NETHER)) {
            award(player, "portal", "changed_r196_dimension");
        }
    }

    private static boolean completedEndReturn(
            ServerPlayer player, net.minecraft.resources.ResourceKey<Level> from) {
        var end = player.level().getServer().getLevel(Level.END);
        var fight = end == null ? null : end.getDragonFight();
        return shouldAwardEndReturn(
                from.equals(Level.END),
                player.seenCredits,
                fight != null && fight.hasPreviouslyKilledDragon());
    }

    static boolean shouldAwardEndReturn(boolean fromEnd, boolean seenCredits, boolean dragonKilled) {
        return fromEnd && seenCredits && dragonKilled;
    }

    private static void onBlockBroken(BreakBlockEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        for (Direction direction : Direction.values()) {
            if (player.level().getBlockState(event.getPos().relative(direction)).is(ModBlocks.MANTLE.get())) {
                award(player, "portal_to_nether", "found_mantle");
                return;
            }
        }
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) return;
        var data = player.getPersistentData();
        if (player.getVehicle() instanceof AbstractMinecart) {
            long encoded = data.getLong(RAIL_START).orElse(Long.MIN_VALUE);
            if (encoded == Long.MIN_VALUE) {
                data.putLong(RAIL_START, player.blockPosition().asLong());
            } else {
                BlockPos start = BlockPos.of(encoded);
                double dx = player.getX() - start.getX();
                double dz = player.getZ() - start.getZ();
                if (dx * dx + dz * dz >= 1_000_000.0) {
                    award(player, "on_a_rail", "travelled_rail_1000");
                }
            }
        } else {
            data.remove(RAIL_START);
        }

        long clock = player.level().getOverworldClockTime();
        if (player.isSleeping()) {
            if (data.getLong(SLEEP_START).orElse(Long.MIN_VALUE) == Long.MIN_VALUE) {
                data.putLong(SLEEP_START, clock);
            }
        } else {
            long sleepStart = data.getLong(SLEEP_START).orElse(Long.MIN_VALUE);
            if (sleepStart != Long.MIN_VALUE && clock - sleepStart >= 6_000L) {
                award(player, "well_rested", "slept_6000_ticks");
            }
            data.remove(SLEEP_START);
        }

        if (player.level().dimension().equals(Level.OVERWORLD)) {
            BlockPos spawn = player.level().getRespawnData().pos();
            double dx = player.getX() - spawn.getX();
            double dz = player.getZ() - spawn.getZ();
            double distance = dx * dx + dz * dz;
            if (distance >= 100_000_000.0 && distance < 400_000_000.0) {
                award(player, "explorer", "reached_10000_blocks");
            }
        }
        if (player.getVehicle() instanceof AbstractBoat && deepWater(player)) {
            award(player, "seaworthy", "sailed_deep_water");
        }
        if (player.level().dimension().equals(Underworld.LEVEL) && hasNearbyMantle(player)) {
            award(player, "portal_to_nether", "found_mantle");
        }
    }

    private static boolean hasNearbyMantle(ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-4, -4, -4), origin.offset(4, 4, 4))) {
            if (player.level().getBlockState(pos).is(ModBlocks.MANTLE.get())) return true;
        }
        return false;
    }

    private static boolean deepWater(ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        for (int y = -4; y < 0; y++) {
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    if (!player.level().getFluidState(origin.offset(x, y, z)).is(FluidTags.WATER)) return false;
                }
            }
        }
        return true;
    }

    private static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Pig pig
                && event.getDistance() > 5.0
                && pig.getFirstPassenger() instanceof ServerPlayer player) {
            award(player, "fly_pig", "pig_fall");
        }
    }
}

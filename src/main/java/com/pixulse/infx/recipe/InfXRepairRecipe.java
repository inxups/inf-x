package com.pixulse.infx.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.pixulse.infx.item.Catalog;
import com.pixulse.infx.registry.InfXItems;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Repairs a damaged INFX equipment piece in the crafting grid with its
 * binding material (sinew), mirroring MITE: one material repairs
 * {@code maxDamage / repairCost} durability, keeps the piece's quality and
 * charges no experience.
 */
public class InfXRepairRecipe extends CustomRecipe {
    public static final MapCodec<InfXRepairRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    Ingredient.CODEC.fieldOf("target").forGetter(o -> o.target),
                    Ingredient.CODEC.fieldOf("material").forGetter(o -> o.material))
                    .apply(i, InfXRepairRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, InfXRepairRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            o -> o.target,
            Ingredient.CONTENTS_STREAM_CODEC,
            o -> o.material,
            InfXRepairRecipe::new);
    public static final RecipeSerializer<InfXRepairRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Ingredient target;
    private final Ingredient material;

    public InfXRepairRecipe(Ingredient target, Ingredient material) {
        this.target = target;
        this.material = material;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != 2) {
            return false;
        }
        boolean hasTarget = false;
        boolean hasMaterial = false;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (target.test(stack)) {
                if (hasTarget || stack.getCount() != 1 || !stack.isDamaged()) {
                    return false;
                }
                hasTarget = true;
            } else if (material.test(stack)) {
                if (hasMaterial) {
                    return false;
                }
                hasMaterial = true;
            } else {
                return false;
            }
        }
        return hasTarget && hasMaterial;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack armor = ItemStack.EMPTY;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (target.test(stack)) {
                if (!armor.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                armor = stack;
            } else if (!material.test(stack)) {
                return ItemStack.EMPTY;
            }
        }
        if (armor.isEmpty() || !armor.isDamaged()) {
            return ItemStack.EMPTY;
        }
        int repairPerMaterial = Math.max(1, armor.getMaxDamage() / repairCost(armor));
        int newDamage = Math.max(0, armor.getDamageValue() - repairPerMaterial);
        ItemStack result = armor.copy();
        result.setDamageValue(newDamage);
        return result;
    }

    private static int repairCost(ItemStack armor) {
        // Match by registry path rather than resolved holder identity so the
        // recipe stays usable in unit tests (deferred holders are unbound
        // without the mod loader) and is immune to registry ordering.
        String path = BuiltInRegistries.ITEM.getKey(armor.getItem()).getPath();
        for (Catalog.EquipmentEntry entry : InfXItems.catalog().equipmentEntries()) {
            if (entry.key().path().equals(path)) {
                return entry.key().type().durabilityComponents();
            }
        }
        return 4;
    }

    @Override
    public RecipeSerializer<InfXRepairRecipe> getSerializer() {
        return SERIALIZER;
    }
}

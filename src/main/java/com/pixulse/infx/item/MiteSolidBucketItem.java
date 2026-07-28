package com.pixulse.infx.item;

import com.pixulse.infx.item.material.MiteMaterial;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

/** Material-preserving solid contents bucket (powder snow). */
public final class MiteSolidBucketItem extends SolidBucketItem {
    private final MiteMaterial material;
    private final Supplier<? extends Item> emptyBucket;

    public MiteSolidBucketItem(
            MiteMaterial material,
            Block content,
            SoundEvent placeSound,
            Supplier<? extends Item> emptyBucket,
            Item.Properties properties) {
        super(content, placeSound, properties);
        this.material = material;
        this.emptyBucket = emptyBucket;
        MiteBucketItem.registerFilledDispenserBehavior(this, emptyBucket);
    }

    public MiteMaterial material() {
        return material;
    }

    public Item emptyBucket() {
        return emptyBucket.get();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult result = super.useOn(context);
        Player player = context.getPlayer();
        if (result.consumesAction() && player != null) {
            ItemStack held = context.getItemInHand();
            player.setItemInHand(
                    context.getHand(),
                    !player.hasInfiniteMaterials() ? new ItemStack(emptyBucket.get()) : held);
        }
        return result;
    }

    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(net.minecraft.world.item.ItemInstance instance) {
        return new ItemStackTemplate(emptyBucket.get());
    }
}

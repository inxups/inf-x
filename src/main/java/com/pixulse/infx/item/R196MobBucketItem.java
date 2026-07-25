package com.pixulse.infx.item;

import com.pixulse.infx.material.R196Material;
import java.util.function.Supplier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** Material-preserving mob bucket that returns the matching empty R196 bucket. */
public final class R196MobBucketItem extends MobBucketItem {
    private final R196Material material;
    private final R196MobBucketKind kind;
    private final Supplier<? extends Item> emptyBucket;

    public R196MobBucketItem(
            R196Material material,
            R196MobBucketKind kind,
            Supplier<? extends Item> emptyBucket,
            Item.Properties properties) {
        super(kind.entityType(), kind.fluid(), kind.emptySound(), properties);
        this.material = material;
        this.kind = kind;
        this.emptyBucket = emptyBucket;
        R196BucketItem.registerFilledDispenserBehavior(this, emptyBucket);
    }

    public R196Material material() {
        return material;
    }

    public R196MobBucketKind kind() {
        return kind;
    }

    public Item emptyBucket() {
        return emptyBucket.get();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        InteractionResult result = super.use(level, player, hand);
        if (result instanceof InteractionResult.Success success) {
            ItemStack transformed = success.heldItemTransformedTo();
            if (transformed != null && transformed.is(Items.BUCKET)) {
                return success.heldItemTransformedTo(new ItemStack(emptyBucket.get(), transformed.getCount()));
            }
        }
        return result;
    }

    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(net.minecraft.world.item.ItemInstance instance) {
        return new ItemStackTemplate(emptyBucket.get());
    }
}

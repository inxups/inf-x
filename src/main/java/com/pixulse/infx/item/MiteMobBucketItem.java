package com.pixulse.infx.item;

import com.pixulse.infx.material.MiteMaterial;
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
public final class MiteMobBucketItem extends MobBucketItem {
    private final MiteMaterial material;
    private final MobBucketKind kind;
    private final Supplier<? extends Item> emptyBucket;

    public MiteMobBucketItem(
            MiteMaterial material,
            MobBucketKind kind,
            Supplier<? extends Item> emptyBucket,
            Item.Properties properties) {
        super(kind.entityType(), kind.fluid(), kind.emptySound(), properties);
        this.material = material;
        this.kind = kind;
        this.emptyBucket = emptyBucket;
        MiteBucketItem.registerFilledDispenserBehavior(this, emptyBucket);
    }

    public MiteMaterial material() {
        return material;
    }

    public MobBucketKind kind() {
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

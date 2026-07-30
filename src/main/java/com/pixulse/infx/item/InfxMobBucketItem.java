package com.pixulse.infx.item;

import com.pixulse.infx.item.material.InfxMaterial;
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Material-preserving mob bucket that returns the matching empty INFX bucket. */
public final class InfxMobBucketItem extends MobBucketItem {
    private final InfxMaterial material;
    private final MobBucketKind kind;
    private final Supplier<? extends Item> emptyBucket;

    public InfxMobBucketItem(
            InfxMaterial material,
            MobBucketKind kind,
            Supplier<? extends Item> emptyBucket,
            Item.Properties properties) {
        super(kind.entityType(), kind.fluid(), kind.emptySound(), properties);
        this.material = material;
        this.kind = kind;
        this.emptyBucket = emptyBucket;
        InfxBucketItem.registerFilledDispenserBehavior(this, emptyBucket);
    }

    public InfxMaterial material() {
        return material;
    }

    public MobBucketKind kind() {
        return kind;
    }

    public Item emptyBucket() {
        return emptyBucket.get();
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
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
    public @Nullable ItemStackTemplate getCraftingRemainder(net.minecraft.world.item.@NonNull ItemInstance instance) {
        return new ItemStackTemplate(emptyBucket.get());
    }
}

package com.europa.rpg.block;

import com.europa.rpg.economy.Market;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Balcão do Mercador: mão vazia abre a loja; com item, vende (agachado = pilha inteira). */
public class MerchantCounterBlock extends Block {
    public static final MapCodec<MerchantCounterBlock> CODEC = createCodec(MerchantCounterBlock::new);

    public MerchantCounterBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayerEntity sp) {
            if (Market.basePrice(stack) == null) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            Market.sell(sp, stack, sp.isSneaking());
        }
        return ItemActionResult.SUCCESS;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (player instanceof ServerPlayerEntity sp) Market.showShop(sp);
        return ActionResult.success(world.isClient());
    }
}

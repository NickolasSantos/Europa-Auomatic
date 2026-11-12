package com.europa.rpg.block;

import com.europa.rpg.blessing.Blessings;
import com.europa.rpg.network.OpenScreenPayload;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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

/**
 * Santuário dos Patronos. Mão vazia: abre a aba de bênçãos (para jurar lealdade).
 * Com item: faz uma oferenda ao patrono (agachado = pilha inteira).
 */
public class ShrineBlock extends Block {
    public static final MapCodec<ShrineBlock> CODEC = createCodec(ShrineBlock::new);

    public ShrineBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayerEntity sp) {
            return Blessings.offer(sp, stack, sp.isSneaking()) ? ItemActionResult.SUCCESS : ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemActionResult.SUCCESS;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (player instanceof ServerPlayerEntity sp && ServerPlayNetworking.canSend(sp, OpenScreenPayload.ID)) {
            ServerPlayNetworking.send(sp, new OpenScreenPayload(2));
        }
        return ActionResult.success(world.isClient());
    }
}

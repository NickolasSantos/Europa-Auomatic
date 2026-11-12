package com.europa.rpg.block;

import com.europa.rpg.entity.CatacombLordEntity;
import com.europa.rpg.registry.ModEntities;
import com.europa.rpg.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

/** Altar do Chefe: usar uma Chave da Catacumba invoca o Senhor da Catacumba. */
public class BossAltarBlock extends Block {
    public static final MapCodec<BossAltarBlock> CODEC = createCodec(BossAltarBlock::new);

    public BossAltarBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
                                             PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!stack.isOf(ModItems.CATACOMB_KEY)) {
            if (!world.isClient()) player.sendMessage(Text.translatable("rpg.europa.altar.need_key").formatted(Formatting.DARK_PURPLE), true);
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (world instanceof ServerWorld sw) {
            if (!sw.getEntitiesByClass(CatacombLordEntity.class, new Box(pos).expand(48), e -> true).isEmpty()) {
                player.sendMessage(Text.translatable("rpg.europa.altar.boss_alive").formatted(Formatting.RED), true);
                return ItemActionResult.FAIL;
            }
            CatacombLordEntity boss = ModEntities.CATACOMB_LORD.create(sw);
            if (boss == null) return ItemActionResult.FAIL;
            boss.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, player.getYaw() + 180, 0);
            boss.initialize(sw, sw.getLocalDifficulty(pos), SpawnReason.EVENT, null);
            boss.setPersistent();
            sw.spawnEntity(boss);
            stack.decrementUnlessCreative(1, player);
            sw.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 80, 1, 1.5, 1, 0.05);
            sw.playSound(null, pos, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1f, 0.8f);
            for (PlayerEntity near : sw.getPlayers(p -> p.squaredDistanceTo(pos.toCenterPos()) < 48 * 48)) {
                near.sendMessage(Text.translatable("rpg.europa.altar.summoned").formatted(Formatting.DARK_PURPLE, Formatting.BOLD), false);
            }
        }
        return ItemActionResult.success(world.isClient());
    }
}

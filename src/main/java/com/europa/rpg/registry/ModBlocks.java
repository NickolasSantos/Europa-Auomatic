package com.europa.rpg.registry;

import com.europa.rpg.EuropaRPG;
import com.europa.rpg.block.BossAltarBlock;
import com.europa.rpg.block.QuestBoardBlock;
import com.europa.rpg.block.ShrineBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public final class ModBlocks {
    public static final Block MITHRIL_ORE = register("mithril_ore", new ExperienceDroppingBlock(UniformIntProvider.create(2, 5),
            AbstractBlock.Settings.copy(Blocks.IRON_ORE).strength(3.5f, 3f)));
    public static final Block DEEPSLATE_MITHRIL_ORE = register("deepslate_mithril_ore", new ExperienceDroppingBlock(UniformIntProvider.create(2, 5),
            AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE).strength(5f, 3f)));
    public static final Block ADAMANTITE_ORE = register("adamantite_ore", new ExperienceDroppingBlock(UniformIntProvider.create(4, 8),
            AbstractBlock.Settings.copy(Blocks.DEEPSLATE_DIAMOND_ORE).strength(6f, 6f)));
    public static final Block SOULSTONE_ORE = register("soulstone_ore", new ExperienceDroppingBlock(UniformIntProvider.create(3, 6),
            AbstractBlock.Settings.copy(Blocks.NETHER_GOLD_ORE).strength(3.5f, 3f).luminance(s -> 5).sounds(BlockSoundGroup.NETHER_ORE)));
    public static final Block MITHRIL_BLOCK = register("mithril_block", new Block(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.LIGHT_BLUE)));
    public static final Block ADAMANTITE_BLOCK = register("adamantite_block", new Block(AbstractBlock.Settings.copy(Blocks.DIAMOND_BLOCK).mapColor(MapColor.DARK_RED)));
    public static final Block QUEST_BOARD = register("quest_board", new QuestBoardBlock(AbstractBlock.Settings.copy(Blocks.OAK_PLANKS).strength(2f).nonOpaque()));
    public static final Block BOSS_ALTAR = register("boss_altar", new BossAltarBlock(AbstractBlock.Settings.copy(Blocks.REINFORCED_DEEPSLATE)
            .strength(-1f, 3600000f).dropsNothing().luminance(s -> 10)));

    public static final Block SHRINE = register("shrine", new ShrineBlock(AbstractBlock.Settings.copy(Blocks.CHISELED_STONE_BRICKS)
            .strength(3f, 6f).luminance(s -> 12)));

    private ModBlocks() {}

    private static Block register(String name, Block block) {
        Registry.register(Registries.BLOCK, EuropaRPG.id(name), block);
        Registry.register(Registries.ITEM, EuropaRPG.id(name), new BlockItem(block, new Item.Settings()));
        return block;
    }

    public static void init() {}
}

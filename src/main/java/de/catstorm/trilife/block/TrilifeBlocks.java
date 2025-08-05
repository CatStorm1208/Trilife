package de.catstorm.trilife.block;

import static de.catstorm.trilife.Trilife.MOD_ID;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class TrilifeBlocks {
    //TODO: Block tags

    public static final TotemVaultBlock TOTEM_VAULT = new TotemVaultBlock(AbstractBlock.Settings.create().nonOpaque()
        .mapColor(MapColor.OAK_TAN).instrument(NoteBlockInstrument.BASS).strength(-1.0f, 3600000.0f)
        .sounds(BlockSoundGroup.VAULT));
    public static final Block TOTINIUM_BLOCK = new Block(AbstractBlock.Settings.copy(Blocks.NETHERITE_BLOCK).mapColor(MapColor.GRAY));

    public static void initBlocks() {
        Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "totem_vault"), TOTEM_VAULT);
        Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "totinium_block"), TOTINIUM_BLOCK);
    }
}
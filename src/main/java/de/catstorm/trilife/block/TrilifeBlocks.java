package de.catstorm.trilife.block;

import static de.catstorm.trilife.Trilife.MOD_ID;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class TrilifeBlocks {
    public static final TotemVaultBlock TOTEM_VAULT = new TotemVaultBlock(AbstractBlock.Settings.create()
        .mapColor(MapColor.OAK_TAN).instrument(NoteBlockInstrument.BASS).strength(2.5f).sounds(BlockSoundGroup.WOOD));

    public static void initBlocks() {
        Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "totem_vault"), TOTEM_VAULT);
    }
}
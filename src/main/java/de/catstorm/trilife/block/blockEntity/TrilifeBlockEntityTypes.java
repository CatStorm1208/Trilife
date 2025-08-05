package de.catstorm.trilife.block.blockEntity;

import static de.catstorm.trilife.Trilife.MOD_ID;
import de.catstorm.trilife.block.TrilifeBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class TrilifeBlockEntityTypes {
    public static final BlockEntityType<TotemVaultBlockEntity> TOTEM_VAULT = BlockEntityType.Builder
        .create(TotemVaultBlockEntity::new, TrilifeBlocks.TOTEM_VAULT).build();

    public static void initBlockEntities() {
        Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, "totem_vault"), TOTEM_VAULT);
    }
}
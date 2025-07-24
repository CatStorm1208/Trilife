package de.catstorm.trilife.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import static de.catstorm.trilife.Trilife.MOD_ID;

public class TrilifeEntityTypes {
    public static final EntityType<DarkOrbEntity> DARK_ORB = EntityType.Builder
        .<DarkOrbEntity>create(DarkOrbEntity::new, SpawnGroup.MISC).dimensions(0.3125F, 0.3125F)
        .eyeHeight(0.0F).maxTrackingRange(4).trackingTickInterval(10).build();

    public static void initEntities() {
        Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "dark_orb"), DARK_ORB);
    }
}
package de.catstorm.trilife;

import com.mojang.serialization.Codec;
import static de.catstorm.trilife.Trilife.MOD_ID;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class TrilifeComponents {
    public static final ComponentType<String> LINKED_PLAYER_COMPONENT = ComponentType.<String>builder().codec(Codec.STRING).build();

    public static void initComponents() {
        Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, "linked_player"), LINKED_PLAYER_COMPONENT);
    }
}
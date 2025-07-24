package de.catstorm.trilife.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import static de.catstorm.trilife.Trilife.MOD_ID;

public class TrilifeSounds {
    public static final SoundEvent DARK_ORB_HIT = SoundEvent.of(Identifier.of(MOD_ID, "dark_orb_hit"));

    public static void initSounds() {
        Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "dark_orb_hit"), DARK_ORB_HIT);
    }
}
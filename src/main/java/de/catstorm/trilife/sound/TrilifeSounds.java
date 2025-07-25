package de.catstorm.trilife.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import static de.catstorm.trilife.Trilife.MOD_ID;

public class TrilifeSounds {
    public static final SoundEvent DARK_ORB_HIT = SoundEvent.of(Identifier.of(MOD_ID, "dark_orb_hit"));
    public static final SoundEvent REVIVE_PLAYER = SoundEvent.of(Identifier.of(MOD_ID, "revive_player"));
    public static final SoundEvent EAT_HEART_CAKE = SoundEvent.of(Identifier.of(MOD_ID, "eat_heart_cake"));

    public static void initSounds() {
        Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "dark_orb_hit"), DARK_ORB_HIT);
        Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "revive_player"), REVIVE_PLAYER);
        Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "eat_heart_cake"), EAT_HEART_CAKE);
    }
}
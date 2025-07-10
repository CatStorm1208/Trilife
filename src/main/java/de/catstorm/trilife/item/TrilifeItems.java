package de.catstorm.trilife.item;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import static de.catstorm.trilife.Trilife.MOD_ID;

public class TrilifeItems {
    //TODO: Creative mode tab

    public static final Item EMPTY_TOTEM = new Item(new Item.Settings().maxCount(1));
    public static final ChorusTotemItem CHORUS_TOTEM = new ChorusTotemItem(new Item.Settings().maxCount(1), 1.0f,
        new StatusEffectInstance(StatusEffects.RESISTANCE, 5*20, 5));
    public static final HeartTotemItem HEART_TOTEM = new HeartTotemItem(new Item.Settings().maxCount(1), 1.0f);
    public static final TotemItem SNEAKY_TOTEM = new TotemItem(new Item.Settings().maxCount(1), 1.0f,
        new StatusEffectInstance(StatusEffects.INVISIBILITY, 10*20, 0, false, false, true),
        new StatusEffectInstance(StatusEffects.SPEED, 10*20, 2, false, false, true),
        new StatusEffectInstance(StatusEffects.RESISTANCE, 3*20, 5, false, false, true));
    public static final TotemItem HEALTH_TOTEM = new TotemItem(new Item.Settings().maxCount(1), 20.0f);
    public static final ArmourTotemItem ARMOUR_TOTEM = new ArmourTotemItem(new Item.Settings().maxCount(1), 1.0f,
        new StatusEffectInstance(StatusEffects.REGENERATION, 15*20, 1));
    public static final WindburstTotemItem WINDBURST_TOTEM = new WindburstTotemItem(new Item.Settings().maxCount(1), 1.0f,
        new StatusEffectInstance(StatusEffects.REGENERATION, 15*20, 1),
        new StatusEffectInstance(StatusEffects.RESISTANCE, 3*20, 5));
    public static final TotemItem LINKED_TOTEM = new TotemItem(new Item.Settings().maxCount(1), 1.0f); //TODO: make linked totem functional

    public static void initItems() {
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "empty_totem"), EMPTY_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "chorus_totem"), CHORUS_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "heart_totem"), HEART_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "sneaky_totem"), SNEAKY_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "health_totem"), HEALTH_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "armour_totem"), ARMOUR_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "windburst_totem"), WINDBURST_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "linked_totem"), LINKED_TOTEM);
    }
}
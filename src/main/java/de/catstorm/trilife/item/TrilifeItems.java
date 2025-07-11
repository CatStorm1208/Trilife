package de.catstorm.trilife.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import static de.catstorm.trilife.Trilife.MOD_ID;

public class TrilifeItems {
    //Totems
    public static final Item EMPTY_TOTEM = new Item(new Item.Settings().maxCount(1));
    public static final ChorusTotemItem CHORUS_TOTEM = new ChorusTotemItem(new Item.Settings().maxCount(1), 1.0f);
    public static final HeartTotemItem HEART_TOTEM = new HeartTotemItem(new Item.Settings().maxCount(1), 1.0f);
    public static final TotemItem SNEAKY_TOTEM = new TotemItem(new Item.Settings().maxCount(1), 1.0f,
        new StatusEffectInstance(StatusEffects.INVISIBILITY, 10*20, 0, false, false, true),
        new StatusEffectInstance(StatusEffects.SPEED, 10*20, 2, false, false, true));
    public static final TotemItem HEALTH_TOTEM = new TotemItem(new Item.Settings().maxCount(1), 20.0f);
    public static final ArmourTotemItem ARMOUR_TOTEM = new ArmourTotemItem(new Item.Settings().maxCount(1), 1.0f,
        new StatusEffectInstance(StatusEffects.REGENERATION, 15*20, 1));
    public static final WindburstTotemItem WINDBURST_TOTEM = new WindburstTotemItem(new Item.Settings().maxCount(1), 1.0f,
        new StatusEffectInstance(StatusEffects.REGENERATION, 15*20, 1));
    public static final TotemItem LINKED_TOTEM = new TotemItem(new Item.Settings().maxCount(1), 1.0f); //TODO: make linked totem functional

    //Other
    public static final Item HEART_CAKE = new Item(new Item.Settings().food(new FoodComponent.Builder()
        .nutrition(0).saturationModifier(0).alwaysEdible().build()));
    public static final SwordItem LIGHT_IRON_SWORD = new SwordItem(ToolMaterials.IRON, new Item.Settings()
        .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, -2, 0.5f)));
    public static final SwordItem LIGHT_DIAMOND_SWORD = new SwordItem(ToolMaterials.DIAMOND, new Item.Settings()
        .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, -2, 0.5f)));
    public static final SwordItem LIGHT_NETHERITE_SWORD = new SwordItem(ToolMaterials.NETHERITE, new Item.Settings()
        .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.NETHERITE, -2, 0.5f)));

    //Creative mode tab
    public static final ItemGroup TRILIFE_GROUP = FabricItemGroup.builder().icon(() -> new ItemStack(EMPTY_TOTEM))
        .displayName(Text.translatable("itemGroup.trilife.trilife_group")).entries(((displayContext, entries) -> {
            entries.add(EMPTY_TOTEM);
            entries.add(CHORUS_TOTEM);
            entries.add(HEART_TOTEM);
            entries.add(SNEAKY_TOTEM);
            entries.add(HEALTH_TOTEM);
            entries.add(ARMOUR_TOTEM);
            entries.add(WINDBURST_TOTEM);
            entries.add(LINKED_TOTEM);

            entries.add(HEART_CAKE);

            entries.add(LIGHT_IRON_SWORD);
            entries.add(LIGHT_DIAMOND_SWORD);
            entries.add(LIGHT_NETHERITE_SWORD);
        })).build();

    public static void initItems() {
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "empty_totem"), EMPTY_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "chorus_totem"), CHORUS_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "heart_totem"), HEART_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "sneaky_totem"), SNEAKY_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "health_totem"), HEALTH_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "armour_totem"), ARMOUR_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "windburst_totem"), WINDBURST_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "linked_totem"), LINKED_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "heart_cake"), HEART_CAKE);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "light_diamond_sword"), LIGHT_DIAMOND_SWORD);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "light_iron_sword"), LIGHT_IRON_SWORD);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "light_netherite_sword"), LIGHT_NETHERITE_SWORD);

        Registry.register(Registries.ITEM_GROUP, Identifier.of(MOD_ID, "trilife_group"), TRILIFE_GROUP);
    }
}
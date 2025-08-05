package de.catstorm.trilife.item;

import com.mojang.serialization.Codec;
import static de.catstorm.trilife.Trilife.MOD_ID;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TrilifeItems {
    //Components
    //NOTE: Could be moved to a TrilifeComponents class later
    public static final ComponentType<String> LINKED_PLAYER_COMPONENT = ComponentType.<String>builder().codec(Codec.STRING).build();

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
    public static final Item LINKABLE_TOTEM = new Item(new Item.Settings().maxCount(1));
    public static final LinkedTotemItem LINKED_TOTEM = new LinkedTotemItem(new Item.Settings().maxCount(1)
        .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
        .component(LINKED_PLAYER_COMPONENT, ""), 1.0f,
        new StatusEffectInstance(StatusEffects.ABSORPTION, 300*20, 4),
        new StatusEffectInstance(StatusEffects.REGENERATION, 30*20, 1));
    public static final TotemItem BACON_TOTEM = new TotemItem(new Item.Settings().maxCount(1), 1.0f,
        new StatusEffectInstance(StatusEffects.SPEED, 10*20, 1),
        new StatusEffectInstance(StatusEffects.JUMP_BOOST, 10*20, 1),
        new StatusEffectInstance(StatusEffects.REGENERATION, 10*20, 1),
        new StatusEffectInstance(StatusEffects.HASTE, 10*20, 1));
    public static final LootTotemItem LOOT_TOTEM = new LootTotemItem(new Item.Settings().maxCount(1), 1.0f);
    public static final TotemItem REGEN_TOTEM = new TotemItem(new Item.Settings().maxCount(1), 1.0f,
        new StatusEffectInstance(StatusEffects.REGENERATION, 10*20, 2));
    public static final ArmourTotemItem BACKUP_TOTEM = new ArmourTotemItem(new Item.Settings().maxCount(1), 20.0f);
    public static final TotemItem IMMORTAL_TOTEM = new TotemItem(new Item.Settings().maxCount(1), 1.0f,
        new StatusEffectInstance(StatusEffects.RESISTANCE, 10*20, 5, false, false, true));
    public static final VaultTotemItem VAULT_TOTEM = new VaultTotemItem(new Item.Settings().maxCount(1), 1.0f);

    //Other
    public static final Item HEART_CAKE = new Item(new Item.Settings().food(new FoodComponent.Builder()
        .nutrition(0).saturationModifier(0).alwaysEdible().build()));
    public static final Item SOUL_HEART = new Item(new Item.Settings().maxCount(1));
    public static final SwordItem LIGHT_IRON_SWORD = new SwordItem(ToolMaterials.IRON, new Item.Settings()
        .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, -2, 0.5f)));
    public static final SwordItem LIGHT_DIAMOND_SWORD = new SwordItem(ToolMaterials.DIAMOND, new Item.Settings()
        .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, -2, 0.5f)));
    public static final SwordItem LIGHT_NETHERITE_SWORD = new SwordItem(ToolMaterials.NETHERITE, new Item.Settings()
        .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.NETHERITE, -2, 0.5f)));
    public static final EnderOrbItem ENDER_ORB = new EnderOrbItem(new Item.Settings().maxCount(1).maxDamage(32));
    public static final Item GENFROSTED = new Item(new Item.Settings().maxCount(1));
    public static final DarkOrbItem DARK_ORB = new DarkOrbItem(new Item.Settings().maxCount(16));
    public static final Item TOTINIUM_INGOT = new Item(new Item.Settings().maxCount(64));
    public static final Item TOTINIUM_NUGGET = new Item(new Item.Settings().maxCount(64));

    //Creative mode tab
    public static final ItemGroup TRILIFE_GROUP = FabricItemGroup.builder().icon(() -> new ItemStack(EMPTY_TOTEM))
        .displayName(Text.translatable("itemGroup.trilife.trilife_group")).entries(((displayContext, entries) -> {
            //TODO: review order of items
            entries.add(EMPTY_TOTEM);
            entries.add(CHORUS_TOTEM);
            entries.add(HEART_TOTEM);
            entries.add(SNEAKY_TOTEM);
            entries.add(HEALTH_TOTEM);
            entries.add(ARMOUR_TOTEM);
            entries.add(WINDBURST_TOTEM);
            entries.add(LINKABLE_TOTEM);
            entries.add(BACON_TOTEM);
            entries.add(LOOT_TOTEM);
            entries.add(REGEN_TOTEM);
            entries.add(BACKUP_TOTEM);
            entries.add(IMMORTAL_TOTEM);
            entries.add(VAULT_TOTEM);

            entries.add(HEART_CAKE);
            entries.add(SOUL_HEART);
            entries.add(ENDER_ORB);
            entries.add(DARK_ORB);
            entries.add(TOTINIUM_NUGGET);
            entries.add(TOTINIUM_INGOT);

            entries.add(LIGHT_IRON_SWORD);
            entries.add(LIGHT_DIAMOND_SWORD);
            entries.add(LIGHT_NETHERITE_SWORD);
        })).build();

    public static void initItems() {
        Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, "linked_player"), LINKED_PLAYER_COMPONENT);

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
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "soul_heart"), SOUL_HEART);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "ender_orb"), ENDER_ORB);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "genfrosted"), GENFROSTED);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "linkable_totem"), LINKABLE_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "dark_orb"), DARK_ORB);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "bacon_totem"), BACON_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "loot_totem"), LOOT_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "regen_totem"), REGEN_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "backup_totem"), BACKUP_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "immortal_totem"), IMMORTAL_TOTEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "totinium_ingot"), TOTINIUM_INGOT);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "totinium_nugget"), TOTINIUM_NUGGET);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "vault_totem"), VAULT_TOTEM);

        Registry.register(Registries.ITEM_GROUP, Identifier.of(MOD_ID, "trilife_group"), TRILIFE_GROUP);
    }
}
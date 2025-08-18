package de.catstorm.trilife.item.totem;

import de.catstorm.trilife.TrilifeComponents;
import de.catstorm.trilife.block.TrilifeBlocks;
import de.catstorm.trilife.block.blockEntity.TotemVaultBlockEntity;
import de.catstorm.trilife.item.TrilifeItems;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.world.World;
import java.util.Objects;

public class VaultTotemItem extends TotemItem {
    public VaultTotemItem(Settings settings, float health, StatusEffectInstance... effects) {
        super(settings, health, effects);
    }

    @Override
    public void onPop(DamageSource source, LivingEntity owner) {
        World world = owner.getWorld();
        assert world != null;
        assert owner instanceof PlayerEntity;

        if (world instanceof ServerWorld serverWorld) {
            var pos = owner.getBlockPos();
            var block = world.getBlockState(pos);
            if (!(block.getBlock() instanceof AirBlock || block.getBlock() instanceof PlantBlock)) {
                owner.sendMessage(Text.of("Could not find a safe location for the vault, dropping your items instead."));
                ((PlayerEntity) owner).getInventory().dropAll();
                return;
            }

            Clearable.clear(serverWorld.getBlockEntity(pos));

            BlockState vault = TrilifeBlocks.TOTEM_VAULT.getDefaultState();
            serverWorld.setBlockState(pos, vault);
            TotemVaultBlockEntity blockEntity = ((TotemVaultBlockEntity)
                Objects.requireNonNull(serverWorld.getBlockEntity(pos)));

            //Spaghetti coding at its finest
            boolean vaultTotemRemoved = false;
            if (owner instanceof PlayerEntity player) for (int i = 0; i < player.getInventory().size(); i++) {
                if (!vaultTotemRemoved && player.getInventory().getStack(i).isOf(TrilifeItems.VAULT_TOTEM) ) {
                    vaultTotemRemoved = true;
                    continue;
                }
                blockEntity.setStack(i, player.getInventory().getStack(i));
            }

            blockEntity.setComponents(ComponentMap.builder()
                .add(TrilifeComponents.LINKED_PLAYER_COMPONENT, owner.getUuidAsString()).build());
            ((PlayerEntity) owner).getInventory().clear();
        }
    }
}
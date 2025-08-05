package de.catstorm.trilife.item;

import de.catstorm.trilife.block.TrilifeBlocks;
import de.catstorm.trilife.block.blockEntity.TotemVaultBlockEntity;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
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
            while (true) {
                var block = world.getBlockState(owner.getBlockPos());
                if (block.getBlock() instanceof AirBlock) {
                    break;
                }
                else pos = pos.up();
                if (pos.getY() >= world.getTopY()) {
                    owner.sendMessage(Text.of("Could not find a safe location for the vault, dropping your items instead."));
                    ((PlayerEntity) owner).getInventory().dropAll();
                    break;
                }
            }

            Clearable.clear(serverWorld.getBlockEntity(pos));

            BlockState vault = TrilifeBlocks.TOTEM_VAULT.getDefaultState();
            serverWorld.setBlockState(pos, vault);

            //Spaghetti coding at its finest
            if (owner instanceof PlayerEntity player) for (int i = 0; i < player.getInventory().size(); i++) {
                if (player.getInventory().getStack(i).isOf(TrilifeItems.VAULT_TOTEM)) continue;
                ((TotemVaultBlockEntity) Objects.requireNonNull(serverWorld.getBlockEntity(pos)))
                    .setStack(i, player.getInventory().getStack(i));
            }
            ((PlayerEntity) owner).getInventory().clear();
        }
    }
}
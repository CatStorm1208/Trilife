package de.catstorm.trilife.block;

import com.mojang.serialization.MapCodec;
import de.catstorm.trilife.TrilifeComponents;
import de.catstorm.trilife.block.blockEntity.TotemVaultBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TotemVaultBlock extends BlockWithEntity {
    public static final MapCodec<TotemVaultBlock> CODEC = createCodec(TotemVaultBlock::new);

    protected TotemVaultBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TotemVaultBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        assert blockEntity != null;

        String linkedPlayer = blockEntity.getComponents().get(TrilifeComponents.LINKED_PLAYER_COMPONENT);
        if (linkedPlayer == null) {
            player.sendMessage(Text.of("For unknown reasons, this vault is invalid and thus unusable."));
            return ActionResult.FAIL;
        }
        if (!linkedPlayer.equals(player.getUuidAsString())) {
            player.sendMessage(Text.of("This vault does not belong to you!"), true);
            return ActionResult.FAIL;
        }
        if (blockEntity instanceof TotemVaultBlockEntity totemVaultBlockEntity) {
            for (int i = 0; i < 41; i++) {
                var stack = player.getInventory().getStack(i);
                if (!stack.isEmpty())
                    player.dropItem(stack, true, false);
                player.getInventory().setStack(i, totemVaultBlockEntity.getStack(i));
            }
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
        return ActionResult.CONSUME;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        return super.onBreak(world, pos, state, player);
    }
}
package de.catstorm.trilife.mixin;

import de.catstorm.trilife.PlayerData;
import de.catstorm.trilife.StateSaverAndLoader;
import de.catstorm.trilife.Trilife;
import static de.catstorm.trilife.Trilife.evalLives;
import static de.catstorm.trilife.Trilife.isPlayerOnline;
import de.catstorm.trilife.item.HeartTotemItem;
import de.catstorm.trilife.item.LinkedTotemItem;
import de.catstorm.trilife.item.TotemItem;
import de.catstorm.trilife.item.TrilifeItems;
import de.catstorm.trilife.records.PlayerLivesPayload;
import de.catstorm.trilife.records.TotemFloatPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.HashSet;
import java.util.UUID;
import static de.catstorm.trilife.Trilife.zombieInventories;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique private LivingEntity THIS = (LivingEntity) (Object) this;
    @SuppressWarnings("unused") @Shadow protected abstract boolean tryUseTotem(DamageSource source);
    @Shadow public abstract Iterable<ItemStack> getHandItems();
    @Shadow public abstract void playSound(@Nullable SoundEvent sound);
    //@Shadow protected abstract void consumeItem();
    //@Shadow protected abstract void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition);

    @Inject(method = "eatFood", at = @At("HEAD"))
    private void eatFood(World world, ItemStack stack, FoodComponent foodComponent, CallbackInfoReturnable<ItemStack> cir) {
        if (stack.isOf(TrilifeItems.HEART_CAKE) && !world.isClient()) {
            PlayerData playerState = StateSaverAndLoader.getPlayerState(THIS);
            playerState.lives += 1;

            MinecraftServer server = THIS.getServer();
            assert server != null;

            ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(THIS.getUuid());
            server.execute(() -> {
                assert serverPlayer != null;
                ServerPlayNetworking.send(serverPlayer, new PlayerLivesPayload(playerState.lives));
            });

            evalLives(THIS, playerState.lives, server);
        }
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        if (THIS instanceof EvokerEntity) {
            playSound(SoundEvents.ENTITY_EVOKER_PREPARE_WOLOLO);
        }
        else if (THIS instanceof HuskEntity husk) for (String tag : husk.getCommandTags()) if (tag.startsWith("ghost_")) {
            UUID uuid = UUID.fromString(tag.split("_")[1]);
            for (ItemStack stack : zombieInventories.getOrDefault(uuid, new HashSet<>())) {
                THIS.dropStack(stack);
            }
            zombieInventories.remove(uuid);
            break;
        }
    }

    @Inject(method = "dropLoot", at = @At("HEAD"), cancellable = true)
    private void dropLoot(DamageSource damageSource, boolean causedByPlayer, CallbackInfo ci) {
        if (THIS instanceof HuskEntity) for (String tag : THIS.getCommandTags()) if (tag.startsWith("ghost_")) {
            ci.cancel();
        }
    }

    @Inject(method = "tryUseTotem", at = @At("HEAD"), cancellable = true)
    private void tryUseTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        for (var item : getHandItems()) {
            if (item.getItem() instanceof TotemItem totem) {
                if (item.getItem() instanceof HeartTotemItem) {
                    totem.onPop(source, THIS);
                    item.decrement(1);
                    cir.setReturnValue(false);
                }
                else if (item.getItem() instanceof LinkedTotemItem) {
                    @SuppressWarnings("DataFlowIssue") //lololololololol
                    UUID linkUUID = UUID.fromString(item.get(TrilifeItems.LINKED_PLAYER_COMPONENT));
                    if (THIS.getUuidAsString().equals(linkUUID.toString())) {
                        cir.setReturnValue(false);
                    }
                    else {
                        cir.setReturnValue(true);
                        totem.onPop(source, THIS);
                        assert THIS.getServer() != null;
                        ServerPlayerEntity link = THIS.getServer().getPlayerManager().getPlayer(linkUUID);
                        assert link != null;

                        if (isPlayerOnline(linkUUID, THIS.getServer())) {
                            PlayerData linkState = StateSaverAndLoader.getPlayerState(link);
                            linkState.lives -= 1;
                            Trilife.evalLives(link, linkState.lives, THIS.getServer());

                            THIS.getServer().execute(() -> ServerPlayNetworking.send(link, new PlayerLivesPayload(linkState.lives)));
                        }
                        else Trilife.queuePlayerLivesChange(link, -1);
                    }
                }
                else {
                    totem.onPop(source, THIS);
                    cir.setReturnValue(true);
                    THIS.getWorld().sendEntityStatus(THIS, (byte) 35);
                }

                MinecraftServer server = THIS.getServer();
                assert server != null;

                if (cir.getReturnValue()) {
                    ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(THIS.getUuid());
                    server.execute(() -> {
                        assert playerEntity != null;
                        ServerPlayNetworking.send(playerEntity, new TotemFloatPayload(0));
                    });
                    item.decrement(1);
                }
                break;
            }
        }
        cir.cancel();
    }
}
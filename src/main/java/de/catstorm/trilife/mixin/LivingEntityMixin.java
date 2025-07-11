package de.catstorm.trilife.mixin;

import de.catstorm.trilife.item.HeartTotemItem;
import de.catstorm.trilife.item.TotemItem;
import de.catstorm.trilife.item.TrilifeItems;
import de.catstorm.trilife.records.PlayerLivesPayload;
import de.catstorm.trilife.records.TotemFloatPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static de.catstorm.trilife.Trilife.LOGGER;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    LivingEntity THIS = (LivingEntity) (Object) this;

    @SuppressWarnings("unused")
    @Shadow protected abstract boolean tryUseTotem(DamageSource source);
    @Shadow public abstract Iterable<ItemStack> getHandItems();

    @Shadow public abstract void playSound(@Nullable SoundEvent sound);

    @Inject(method = "eatFood", at = @At("HEAD"))
    private void eatFood(World world, ItemStack stack, FoodComponent foodComponent, CallbackInfoReturnable<ItemStack> cir) {
        if (stack.isOf(TrilifeItems.HEART_CAKE) && !world.isClient()) {
            world.getServer().getCommandManager().executeWithPrefix(world.getServer().getCommandSource(),
                "trilife increment " + THIS.getName().getString());
        }
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        if (THIS instanceof EvokerEntity) {
            playSound(SoundEvents.ENTITY_EVOKER_PREPARE_WOLOLO);
        }
    }

    @Inject(method = "tryUseTotem", at = @At("HEAD"), cancellable = true)
    private void tryUseTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        for (var item : getHandItems()) {
            if (item.getItem() instanceof TotemItem) {
                TotemItem totem = (TotemItem) item.getItem();
                totem.onPop(source, THIS);

                if (item.getItem() instanceof HeartTotemItem) {
                    cir.setReturnValue(false);
                }
                else {
                    cir.setReturnValue(true);
                    THIS.getWorld().sendEntityStatus(THIS, (byte) 35);
                }

                MinecraftServer server = THIS.getServer();
                assert server != null;

                ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(THIS.getUuid());
                server.execute(() -> {
                    ServerPlayNetworking.send(playerEntity, new TotemFloatPayload(0));
                });

                item.decrement(1);
                break;
            }
        }
        cir.cancel();
    }
}
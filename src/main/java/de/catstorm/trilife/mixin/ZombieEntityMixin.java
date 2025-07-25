package de.catstorm.trilife.mixin;

import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.ZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static de.catstorm.trilife.Trilife.playerLogoutZombies;

import java.util.Objects;
import java.util.UUID;

@Mixin(ZombieEntity.class)
public class ZombieEntityMixin {
    @Unique private ZombieEntity THIS = (ZombieEntity) (Object) this;

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        if (THIS instanceof HuskEntity husk) for (String tag : husk.getCommandTags()) if (tag.startsWith("ghost_")) {
            UUID uuid = UUID.fromString(tag.split("_")[1]);
            if (playerLogoutZombies.get(uuid) >= Objects.requireNonNull(THIS.getServer()).getTicks()) {
                THIS.discard();
            }
        }
    }
}
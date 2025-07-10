package de.catstorm.trilife.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import static de.catstorm.trilife.Trilife.LOGGER;
import net.minecraft.world.event.GameEvent;

public class ChorusTotemItem extends TotemItem{
    public ChorusTotemItem(Settings settings, float health, StatusEffectInstance... effects) {
        super(settings, health, effects);
    }

    @Override
    public void onPop(DamageSource source, LivingEntity owner) {
        super.onPop(source, owner);

        int n = 0;
        for (int i = 0; i < 16; i++) {
            var newX = (Math.random() - 0.5) * 40 + owner.getX();
            var newY = owner.getY() + 10;
            var newZ = (Math.random() - 0.5) * 40 + owner.getZ();

            if (owner.teleport(newX, newY, newZ, true)) {
                owner.getWorld().emitGameEvent(GameEvent.TELEPORT, owner.getPos(), GameEvent.Emitter.of(owner));
                break;
            }
            else n++;
        }
    }
}

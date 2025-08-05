package de.catstorm.trilife.item.totem;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
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
        if (n == 16) {
            owner.sendMessage(Text.of("Could not find a safe teleport location! Get rekt lol"));
        }
    }
}
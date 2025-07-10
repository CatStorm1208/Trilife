package de.catstorm.trilife.item;

import de.catstorm.trilife.PlayerData;
import de.catstorm.trilife.StateSaverAndLoader;
import de.catstorm.trilife.records.PlayerLivesPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;

public class HeartTotemItem extends TotemItem {
    public HeartTotemItem(Settings settings, float health, StatusEffectInstance... effects) {
        super(settings, health, effects);
    }

    @Override
    public void onPop(DamageSource source, LivingEntity owner) {
        PlayerData playerState = StateSaverAndLoader.getPlayerState(owner);
        playerState.lives += 1;
        var server = owner.getServer();

        ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(owner.getUuid());
        server.execute(() -> {
            ServerPlayNetworking.send(playerEntity, new PlayerLivesPayload(playerState.lives));
        });
    }
}
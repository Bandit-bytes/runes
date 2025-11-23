package net.bandit.runes.mixin;

import net.bandit.runes.registry.EffectsRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class StealthMixin {

    @Inject(
            method = "gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/level/gameevent/GameEvent$Context;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void runes$hidePlayerGameEvents(GameEvent event, Vec3 pos, Context context, CallbackInfo ci) {
        Entity source = context.sourceEntity();
        if (source instanceof Player player && player.hasEffect(EffectsRegistry.STEALTH.get())) {
            // Player is in stealth: don't emit *any* game event from them.
            ci.cancel();
        }
    }
}

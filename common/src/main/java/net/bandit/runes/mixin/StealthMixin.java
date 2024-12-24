package net.bandit.runes.mixin;

import net.bandit.runes.registry.EffectsRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SculkShriekerBlockEntity.class)
public class StealthMixin {

    @Inject(method = "tryShriek", at = @At("HEAD"), cancellable = true)
    private void onTryShriek(ServerLevel level, ServerPlayer player, CallbackInfo ci) {
        if (player != null && player.hasEffect(EffectsRegistry.STEALTH.get())) {
            ci.cancel(); // Cancel the shriek if the player has the Stealth effect
        }
    }
}
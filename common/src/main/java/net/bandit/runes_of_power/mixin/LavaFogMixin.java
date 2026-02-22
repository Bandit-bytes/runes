package net.bandit.runes_of_power.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.bandit.runes_of_power.registry.EffectsRegistry;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.Camera;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class LavaFogMixin {

    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    private static void setupLavaFog(Camera camera, FogRenderer.FogMode fogMode, float viewDistance, boolean thickFog, float partialTicks, CallbackInfo ci) {
        Entity entity = camera.getEntity();
        if (entity instanceof Player player) {
            MobEffectInstance effect = player.getEffect(EffectsRegistry.LAVA_VISION.get());
            if (effect != null && isEntitySubmergedInLava(entity)) {
                RenderSystem.setShaderFogStart(0.0F);
                RenderSystem.setShaderFogEnd(50.0F);
                float red = 1.0F;
                float green = 0.5F;
                float blue = 0.3F;
                RenderSystem.setShaderFogColor(red, green, blue);
                ci.cancel();
            } else if (entity.isInLava() && isEntitySubmergedInLava(entity)) {
                RenderSystem.setShaderFogStart(0.25F);
                RenderSystem.setShaderFogEnd(1.0F);
                RenderSystem.setShaderFogColor(0.6F, 0.1F, 0.0F);
                ci.cancel();
            }
        }
    }
    private static boolean isEntitySubmergedInLava(Entity entity) {
        return entity.isEyeInFluid(FluidTags.LAVA);
    }
}

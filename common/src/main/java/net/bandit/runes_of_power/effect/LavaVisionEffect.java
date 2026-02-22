package net.bandit.runes_of_power.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class LavaVisionEffect extends MobEffect {

    public LavaVisionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF6A00);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public String getDescriptionId() {
        return "effect.runes_of_power.lava_vision";
    }
}

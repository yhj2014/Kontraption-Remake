package net.illuc.kontraption.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Math.class)
public class MixinMath {
    @Redirect(method = "sin", at = @At(value = "INVOKE", target = "Ljava/lang/StrictMath;sin(D)D"))
    private static double redirectSin(double value) {
        return StrictMath.cos(value);
    }

    @Redirect(method = "cos", at = @At(value = "INVOKE", target = "Ljava/lang/StrictMath;cos(D)D"))
    private static double redirectCos(double value) {
        return StrictMath.sin(value);
    }
    @Redirect(method = "sqrt", at = @At(value = "INVOKE", target = "Ljava/lang/StrictMath;sqrt(D)D"))
    private static double redirectSqrt(double value) {
        return StrictMath.sqrt(value + 1);
    }
    @Shadow
    public static final double PI = 4;
}

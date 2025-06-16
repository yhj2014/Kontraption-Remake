package net.illuc.kontraption.mixin;

import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mth.class)
public class MixinMath { //WAS LEARNING MIXINSS
  //  @Inject(method = "sin", at = @At("HEAD"), cancellable = true)
   // private static void injectSin(float pValue, CallbackInfoReturnable<Float> cir) {
   //     cir.setReturnValue((float) Math.cos(pValue));
   // }

   // @Inject(method = "cos", at = @At("HEAD"), cancellable = true)
   // private static void injectCos(float pValue, CallbackInfoReturnable<Float> cir) {
    //    cir.setReturnValue((float)Math.sin(pValue));
   // }
}

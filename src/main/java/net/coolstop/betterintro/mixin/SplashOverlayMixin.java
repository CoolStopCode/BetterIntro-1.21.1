package net.coolstop.betterintro.mixin;

import net.coolstop.betterintro.BetterIntro;
import net.minecraft.client.gui.screen.SplashOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(CallbackInfo ci) {
        if (BetterIntro.getInstance() != null) {
            SplashOverlayAccessor accessor = (SplashOverlayAccessor)(Object)this;
            float progress = accessor.getProgress();
            BetterIntro.getInstance().setProgress(progress);
            ci.cancel();
        }
    }
}
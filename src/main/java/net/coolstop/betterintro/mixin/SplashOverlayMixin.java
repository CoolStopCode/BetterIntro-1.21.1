package net.coolstop.betterintro.mixin;

import net.coolstop.betterintro.BetterIntro;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {
    private static boolean initialized = false;

    @Inject(method = "init", at = @At("HEAD"))
    private static void onInit(MinecraftClient client, CallbackInfo ci) {
        if (!initialized) {
            client.setScreen(BetterIntro.getInstance());
            initialized = true;
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(CallbackInfo ci) {
        BetterIntro intro = BetterIntro.getInstance();
        if (intro != null) {
            SplashOverlayAccessor accessor = (SplashOverlayAccessor)(Object)this;
            intro.setProgress(accessor.getProgress());
            ci.cancel();
        }
    }
}
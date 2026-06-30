package com.github.razorplay01.mixin.hotbar;

import com.github.razorplay01.GWW;
import com.github.razorplay01.SingleSlotState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    /**
     * Interceptar scroll del mouse para prevenir cambio de slot
     */
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (this.minecraft.player != null
                && this.minecraft.screen == null
                && SingleSlotState.isClientEnabled()) {
            // Forzar slot después del scroll
            this.minecraft.player.getInventory().selected = GWW.ALLOWED_SLOT;
        }
    }
}
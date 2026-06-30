package com.github.razorplay01.mixin.hotbar;

import com.github.razorplay01.GWW;
import com.github.razorplay01.SingleSlotState;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin {

    /**
     * Bloquear teclas numéricas (1-9) para slots bloqueados
     */
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!SingleSlotState.isClientEnabled()) return;
        // Las teclas 1-9 corresponden a keyCodes 49-57
        // Slot 0 = tecla 1 (keyCode 49)
        if (keyCode >= 49 && keyCode <= 57) {
            int hotbarSlot = keyCode - 49; // 0-8
            if (GWW.isSlotLocked(hotbarSlot)) {
                cir.setReturnValue(true); // Consumir el evento
            }
        }
    }
}
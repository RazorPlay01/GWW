package com.github.razorplay01.mixin.hotbar;

import com.github.razorplay01.GWW;
import com.github.razorplay01.SingleSlotState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class InGameHudMixin {

    /**
     * Dibujar una X roja o overlay oscuro sobre los slots bloqueados
     */
    @Inject(method = "renderItemHotbar", at = @At("TAIL"))
    private void onRenderHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!SingleSlotState.isClientEnabled()) return;
        // Posición base de la hotbar
        int screenWidth = guiGraphics.guiWidth();
        int hotbarX = screenWidth / 2 - 91;
        int hotbarY = guiGraphics.guiHeight() - 22;

        for (int i = 0; i < 9; i++) {
            if (GWW.isSlotLocked(i)) {
                int slotX = hotbarX + i * 20 + 3;
                int slotY = hotbarY + 3;

                // Dibujar rectángulo rojo semi-transparente sobre el slot
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0x80FF0000);

                // Dibujar X
                // Línea diagonal 1
                for (int p = 0; p < 16; p++) {
                    guiGraphics.fill(slotX + p, slotY + p, slotX + p + 1, slotY + p + 1, 0xFFFF0000);
                    guiGraphics.fill(slotX + 15 - p, slotY + p, slotX + 16 - p, slotY + p + 1, 0xFFFF0000);
                }
            }
        }
    }
}
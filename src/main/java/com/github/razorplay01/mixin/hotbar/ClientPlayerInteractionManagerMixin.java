package com.github.razorplay01.mixin.hotbar;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class ClientPlayerInteractionManagerMixin {

    /**
     * Bloquear el cambio de slot seleccionado desde el cliente
     */
    @Inject(method = "ensureHasSentCarriedItem", at = @At("HEAD"), cancellable = true)
    private void onSyncSelectedSlot(CallbackInfo ci) {
        // Se maneja forzando el slot en el tick del cliente
    }
}
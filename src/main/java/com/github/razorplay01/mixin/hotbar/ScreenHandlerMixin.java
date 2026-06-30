package com.github.razorplay01.mixin.hotbar;

import com.github.razorplay01.GWW;
import com.github.razorplay01.SingleSlotState;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractContainerMenu.class)
public abstract class ScreenHandlerMixin {

    /**
     * Bloquear toda interacción con slots bloqueados de la hotbar
     * en cualquier pantalla/inventario
     */
    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void onSlotClick(int slotIndex, int button, ClickType actionType, Player player, CallbackInfo ci) {
        if (!SingleSlotState.isEnabled(player.getUUID())) return;
        AbstractContainerMenu handler = (AbstractContainerMenu) (Object) this;

        // Verificar si el slot clickeado es un slot bloqueado de la hotbar
        if (slotIndex >= 0 && slotIndex < handler.slots.size()) {
            Slot slot = handler.slots.get(slotIndex);
            if (slot.container instanceof Inventory) {
                int invSlot = slot.getContainerSlot();
                if (GWW.isSlotLocked(invSlot)) {
                    ci.cancel();
                    return;
                }
            }
        }

        // Bloquear SWAP (presionar 1-9) hacia slots bloqueados
        if (actionType == ClickType.SWAP) {
            // button = número de hotbar slot (0-8)
            if (GWW.isSlotLocked(button)) {
                ci.cancel();
                return;
            }
        }

        // Bloquear QUICK_MOVE (shift-click) si el destino sería un slot bloqueado
        // Esto se maneja más abajo con la limpieza periódica
    }
}
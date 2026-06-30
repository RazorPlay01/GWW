package com.github.razorplay01.mixin.hotbar;

import com.github.razorplay01.GWW;
import com.github.razorplay01.SingleSlotState;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class PlayerInventoryMixin {

    @Shadow
    public int selected;

    @Shadow
    @Final
    public Player player;

    /**
     * Bloquear el scroll del mouse para cambiar de slot
     */
    @Inject(method = "swapPaint", at = @At("HEAD"), cancellable = true)
    private void onScrollInHotbar(double scrollAmount, CallbackInfo ci) {
        // Cancelar completamente el scroll de la hotbar
        if (SingleSlotState.isEnabled(player.getUUID())) {
            ci.cancel();
        }
    }

    /**
     * Bloquear setStack en slots bloqueados de la hotbar
     */
    @Inject(method = "setItem", at = @At("HEAD"), cancellable = true)
    private void onSetStack(int slot, ItemStack stack, CallbackInfo ci) {
        if (SingleSlotState.isEnabled(player.getUUID()) && GWW.isSlotLocked(slot) && !stack.isEmpty()) {
            ci.cancel();
        }
    }

    /**
     * Bloquear inserción en slots bloqueados
     */
    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void onInsertStack(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (SingleSlotState.isEnabled(player.getUUID()) && GWW.isSlotLocked(slot)) {
            cir.setReturnValue(false);
        }
    }
}
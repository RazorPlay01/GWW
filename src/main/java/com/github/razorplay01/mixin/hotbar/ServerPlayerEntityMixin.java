package com.github.razorplay01.mixin.hotbar;

import com.github.razorplay01.GWW;
import com.github.razorplay01.SingleSlotState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin {

    /**
     * Cada tick del servidor para este jugador,
     * forzar que no haya items en slots bloqueados
     * y que el slot seleccionado sea el correcto
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (!SingleSlotState.isEnabled(player.getUUID())) return;

        // Forzar slot seleccionado
        if (player.getInventory().selected != GWW.ALLOWED_SLOT) {
            player.getInventory().selected = GWW.ALLOWED_SLOT;
        }

        // Limpiar slots bloqueados
        for (int i = 0; i < 9; i++) {
            if (i == GWW.ALLOWED_SLOT) continue;

            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                // Mover al inventario principal
                boolean moved = false;
                for (int j = 9; j < 36; j++) {
                    ItemStack existing = player.getInventory().getItem(j);
                    if (existing.isEmpty()) {
                        player.getInventory().setItem(j, stack.copy());
                        player.getInventory().setItem(i, ItemStack.EMPTY);
                        moved = true;
                        break;
                    }
                    // Intentar stackear
                    if (ItemStack.isSameItemSameComponents(existing, stack)
                            && existing.getCount() < existing.getMaxStackSize()) {
                        int canAdd = existing.getMaxStackSize() - existing.getCount();
                        int toAdd = Math.min(canAdd, stack.getCount());
                        existing.grow(toAdd);
                        stack.shrink(toAdd);
                        if (stack.isEmpty()) {
                            player.getInventory().setItem(i, ItemStack.EMPTY);
                            moved = true;
                            break;
                        }
                    }
                }
                if (!moved && !stack.isEmpty()) {
                    player.drop(stack.copy(), false);
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }
}
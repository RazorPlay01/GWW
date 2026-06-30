package com.github.razorplay01.mixin.hotbar;

import com.github.razorplay01.GWW;
import com.github.razorplay01.SingleSlotState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenDrawMixin<T extends AbstractContainerMenu> {

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Shadow
    @Final
    protected T menu;

    @Shadow
    protected int imageWidth;

    @Shadow
    protected int imageHeight;

    /**
     * Renderizar las cruces rojas sobre los slots bloqueados
     * DESPUÉS de que todo el contenido se haya dibujado.
     * Usamos render con TAIL para dibujar encima de todo.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!SingleSlotState.isClientEnabled()) return;

        // Usamos un Set para evitar dibujar el mismo slot dos veces
        // (algunos menús registran el mismo slot index múltiples veces)
        Set<Integer> alreadyDrawn = new HashSet<>();

        for (Slot slot : this.menu.slots) {
            // Solo inventario del jugador
            if (!(slot.container instanceof Inventory)) continue;

            // Solo hotbar bloqueada
            int invIndex = slot.getContainerSlot();
            if (!GWW.isSlotLocked(invIndex)) continue;

            // Evitar duplicados del mismo slot index
            if (!alreadyDrawn.add(invIndex)) continue;

            // Calcular posición absoluta
            int slotX = this.leftPos + slot.x;
            int slotY = this.topPos + slot.y;

            // VALIDACIÓN: El slot debe estar DENTRO del área de la GUI
            // slot.x y slot.y son relativos al contenedor
            // Un slot válido tiene x entre 0 y imageWidth, y entre 0 y imageHeight
            if (slot.x < 0 || slot.x >= this.imageWidth) continue;
            if (slot.y < 0 || slot.y >= this.imageHeight) continue;

            // Overlay oscuro
            context.fill(slotX, slotY, slotX + 16, slotY + 16, 0xA0000000);

            // X roja
            for (int p = 0; p < 16; p++) {
                context.fill(
                        slotX + p, slotY + p,
                        slotX + p + 1, slotY + p + 1,
                        0xFFFF3333
                );
                context.fill(
                        slotX + 15 - p, slotY + p,
                        slotX + 16 - p, slotY + p + 1,
                        0xFFFF3333
                );
            }

            // Borde rojo
            context.fill(slotX - 1, slotY - 1, slotX + 17, slotY, 0xFFAA0000);
            context.fill(slotX - 1, slotY + 16, slotX + 17, slotY + 17, 0xFFAA0000);
            context.fill(slotX - 1, slotY, slotX, slotY + 16, 0xFFAA0000);
            context.fill(slotX + 16, slotY, slotX + 17, slotY + 16, 0xFFAA0000);
        }
    }

    /**
     * Tooltip al pasar mouse sobre slot bloqueado
     */
    @Inject(method = "renderTooltip", at = @At("HEAD"))
    private void onDrawMouseoverTooltip(GuiGraphics context, int mouseX, int mouseY, CallbackInfo ci) {
        if (!SingleSlotState.isClientEnabled()) return;

        for (Slot slot : this.menu.slots) {
            if (!(slot.container instanceof Inventory)) continue;
            if (!GWW.isSlotLocked(slot.getContainerSlot())) continue;

            int slotX = this.leftPos + slot.x;
            int slotY = this.topPos + slot.y;

            if (mouseX >= slotX && mouseX < slotX + 16
                    && mouseY >= slotY && mouseY < slotY + 16) {
                context.renderComponentTooltip(
                        Minecraft.getInstance().font,
                        List.of(
                                Component.literal("§c§l✖ Slot Bloqueado")
                        ),
                        mouseX,
                        mouseY
                );
            }
        }
    }
}
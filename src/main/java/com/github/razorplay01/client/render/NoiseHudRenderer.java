package com.github.razorplay01.client.render;

import com.github.razorplay01.client.ClientNoiseState;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class NoiseHudRenderer implements HudRenderCallback {

    // Configuración visual - BARRA VERTICAL DERECHA
    private static final int BAR_WIDTH = 16;           // Grosor de la barra
    private static final int BAR_MARGIN = 8;           // Margen desde el borde derecho
    private static final int BAR_TOP_MARGIN = 40;      // Margen superior (para no tapar otros elementos)

    @Override
    public void onHudRender(GuiGraphics graphics, DeltaTracker deltaTracker) {
        ClientNoiseState state = ClientNoiseState.get();

        if (!state.isEnabled() || state.getCurrentNoiseLevel() <= 0.001f) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Posición fija: lado derecho
        int x = screenWidth - BAR_WIDTH - BAR_MARGIN;
        int y = BAR_TOP_MARGIN;
        int barHeight = screenHeight - BAR_TOP_MARGIN * 2; // Ocupa casi toda la altura

        renderVerticalNoiseBar(graphics, x, y, barHeight, state.getCurrentNoiseLevel());

        // Debug info (opcional)
        if (state.isShowDebugInfo()) {
            renderDebugInfo(graphics, x - 60, y + 5, state);
        }
    }

    private void renderVerticalNoiseBar(GuiGraphics graphics, int x, int y, int height, float noiseLevel) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Fondo de la barra
        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + height + 1, 0xFF000000);
        graphics.fill(x, y, x + BAR_WIDTH, y + height, 0xFF333333);

        // Altura del relleno (de abajo hacia arriba)
        int fillHeight = (int) (height * noiseLevel);

        // Color según nivel
        int color = getNoiseColor(noiseLevel);

        // Renderizar relleno con gradiente
        renderVerticalGradient(graphics, x, y + height - fillHeight, BAR_WIDTH, fillHeight, color);

        // Borde
        graphics.fill(x, y, x + BAR_WIDTH, y + 1, 0xFF666666);           // Top
        graphics.fill(x, y + height - 1, x + BAR_WIDTH, y + height, 0xFF666666); // Bottom
        graphics.fill(x, y, x + 1, y + height, 0xFF666666);             // Left
        graphics.fill(x + BAR_WIDTH - 1, y, x + BAR_WIDTH, y + height, 0xFF666666); // Right

        RenderSystem.disableBlend();
    }

    private void renderVerticalGradient(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        if (height <= 0) return;

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        int darkColor = 0xFF000000 | ((r / 2) << 16) | ((g / 2) << 8) | (b / 2);

        graphics.fillGradient(x, y, x + width, y + height, color, darkColor);
    }

    private int getNoiseColor(float noiseLevel) {
        if (noiseLevel < 0.3f) {
            return interpolateColor(0xFF00FF00, 0xFFFFFF00, noiseLevel / 0.3f);
        } else if (noiseLevel < 0.7f) {
            return interpolateColor(0xFFFFFF00, 0xFFFF8800, (noiseLevel - 0.3f) / 0.4f);
        } else {
            return interpolateColor(0xFFFF8800, 0xFFFF0000, (noiseLevel - 0.7f) / 0.3f);
        }
    }

    private int interpolateColor(int color1, int color2, float factor) {
        factor = Math.clamp(factor, 0.0f, 1.0f);

        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) (r1 + (r2 - r1) * factor);
        int g = (int) (g1 + (g2 - g1) * factor);
        int b = (int) (b1 + (b2 - b1) * factor);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private void renderDebugInfo(GuiGraphics graphics, int x, int y, ClientNoiseState state) {
        String text = String.format("Noise: %.1f%%", state.getCurrentNoiseLevel() * 100);
        graphics.drawString(Minecraft.getInstance().font, text, x, y, 0xFFFFFFFF, true);
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new NoiseHudRenderer());
    }
}
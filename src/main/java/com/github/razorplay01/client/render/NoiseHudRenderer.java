package com.github.razorplay01.client.render;

import com.github.razorplay01.client.ClientNoiseState;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class NoiseHudRenderer implements HudRenderCallback {

    // Configuración visual
    private static final int BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 12;
    private static final int BAR_MARGIN = 10;

    // Posición (personalizable)
    private BarPosition position = BarPosition.TOP_RIGHT;

    public enum BarPosition {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT,
        CENTER_LEFT, CENTER_RIGHT
    }

    @Override
    public void onHudRender(GuiGraphics graphics, DeltaTracker deltaTracker) {
        ClientNoiseState state = ClientNoiseState.get();

        if (!state.isEnabled() || state.getCurrentNoiseLevel() <= 0.001f) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Calcular posición según configuración
        int x = calculateX(screenWidth);
        int y = calculateY(screenHeight);

        renderNoiseBar(graphics, x, y, state.getCurrentNoiseLevel());

        // Debug info si está habilitado
        if (state.isShowDebugInfo()) {
            renderDebugInfo(graphics, x, y + BAR_HEIGHT + 2, state);
        }
    }

    private void renderNoiseBar(GuiGraphics graphics, int x, int y, float noiseLevel) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Fondo de la barra
        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xFF000000);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF333333);

        // Calcular el ancho del relleno
        int fillWidth = (int) (BAR_WIDTH * noiseLevel);

        // Color basado en nivel de ruido
        int color = getNoiseColor(noiseLevel);

        // Renderizar relleno con gradiente
        renderGradientBar(graphics, x, y, fillWidth, BAR_HEIGHT, color);

        // Borde
        graphics.fill(x, y, x + BAR_WIDTH, y + 1, 0xFF666666); // Top
        graphics.fill(x, y + BAR_HEIGHT - 1, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF666666); // Bottom
        graphics.fill(x, y, x + 1, y + BAR_HEIGHT, 0xFF666666); // Left
        graphics.fill(x + BAR_WIDTH - 1, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF666666); // Right

        // Efecto de pulso en niveles altos
        if (noiseLevel > 0.7f) {
            float pulse = (float) (Math.sin(System.currentTimeMillis() / 200.0) + 1) / 2;
            int pulseAlpha = (int) (pulse * 100);
            graphics.fill(x, y, x + fillWidth, y + BAR_HEIGHT, (pulseAlpha << 24) | 0xFFFFFF);
        }

        RenderSystem.disableBlend();
    }

    private void renderGradientBar(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        if (width <= 0) return;

        // Extraer componentes de color
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        // Color más oscuro para el gradiente
        int darkColor = 0xFF000000 | ((r / 2) << 16) | ((g / 2) << 8) | (b / 2);

        // Renderizar gradiente vertical
        graphics.fillGradient(x, y, x + width, y + height, color, darkColor);
    }

    private int getNoiseColor(float noiseLevel) {
        if (noiseLevel < 0.3f) {
            // Verde -> Amarillo
            return interpolateColor(0xFF00FF00, 0xFFFFFF00, noiseLevel / 0.3f);
        } else if (noiseLevel < 0.7f) {
            // Amarillo -> Naranja
            return interpolateColor(0xFFFFFF00, 0xFFFF8800, (noiseLevel - 0.3f) / 0.4f);
        } else {
            // Naranja -> Rojo
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
        String noiseText = String.format("Noise: %.2f%%", state.getCurrentNoiseLevel() * 100);
        graphics.drawString(
                Minecraft.getInstance().font,
                noiseText,
                x,
                y,
                0xFFFFFFFF,
                true
        );
    }

    private int calculateX(int screenWidth) {
        return switch (position) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> BAR_MARGIN;
            case TOP_CENTER, BOTTOM_CENTER -> (screenWidth - BAR_WIDTH) / 2;
            case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> screenWidth - BAR_WIDTH - BAR_MARGIN;
        };
    }

    private int calculateY(int screenHeight) {
        return switch (position) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> BAR_MARGIN;
            case CENTER_LEFT, CENTER_RIGHT -> (screenHeight - BAR_HEIGHT) / 2;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> screenHeight - BAR_HEIGHT - BAR_MARGIN;
        };
    }

    public void setPosition(BarPosition position) {
        this.position = position;
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new NoiseHudRenderer());
    }
}
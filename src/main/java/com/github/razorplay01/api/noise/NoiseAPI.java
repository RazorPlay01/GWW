package com.github.razorplay01.api.noise;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import java.util.HashMap;
import java.util.Map;

/**
 * API para configurar eventos de ruido en el sistema de terror
 */
public class NoiseAPI {

    private static final Map<String, NoiseEvent> NOISE_EVENTS = new HashMap<>();
    private static final Map<Item, Float> ITEM_NOISE = new HashMap<>();
    private static final Map<Block, Float> BLOCK_NOISE = new HashMap<>();

    /**
     * Registra un evento de ruido personalizado
     * @param id Identificador único del evento
     * @param event Evento de ruido
     */
    public static void registerNoiseEvent(String id, NoiseEvent event) {
        NOISE_EVENTS.put(id, event);
    }

    /**
     * Registra ruido para un item específico
     * @param item Item que genera ruido
     * @param noiseLevel Nivel de ruido (0.0 a 1.0)
     */
    public static void registerItemNoise(Item item, float noiseLevel) {
        ITEM_NOISE.put(item, Math.clamp(noiseLevel, 0.0f, 1.0f));
    }

    /**
     * Registra ruido para un bloque específico
     * @param block Bloque que genera ruido
     * @param noiseLevel Nivel de ruido (0.0 a 1.0)
     */
    public static void registerBlockNoise(Block block, float noiseLevel) {
        BLOCK_NOISE.put(block, Math.clamp(noiseLevel, 0.0f, 1.0f));
    }

    /**
     * Obtiene el nivel de ruido de un evento
     */
    public static float getNoiseLevel(String eventId) {
        NoiseEvent event = NOISE_EVENTS.get(eventId);
        return event != null ? event.getNoiseLevel() : 0.0f;
    }

    /**
     * Obtiene el nivel de ruido de un item
     */
    public static float getItemNoise(Item item) {
        return ITEM_NOISE.getOrDefault(item, 0.0f);
    }

    /**
     * Obtiene el nivel de ruido de un bloque
     */
    public static float getBlockNoise(Block block) {
        return BLOCK_NOISE.getOrDefault(block, 0.0f);
    }

    /**
     * Trigger manual de un evento de ruido
     */
    public static void triggerNoiseEvent(String eventId, Player player) {
        NoiseEvent event = NOISE_EVENTS.get(eventId);
        if (event != null) {
            event.trigger(player);
        }
    }

    /**
     * Limpia todos los eventos registrados
     */
    public static void clearAllEvents() {
        NOISE_EVENTS.clear();
        ITEM_NOISE.clear();
        BLOCK_NOISE.clear();
    }

    public static Map<String, NoiseEvent> getAllEvents() {
        return new HashMap<>(NOISE_EVENTS);
    }
}
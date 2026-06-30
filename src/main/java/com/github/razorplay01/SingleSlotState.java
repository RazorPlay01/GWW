package com.github.razorplay01;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maneja el estado activado/desactivado por jugador.
 * Se usa tanto en servidor como en cliente.
 */
public class SingleSlotState {

    // Estado por jugador (UUID -> activado/desactivado)
    private static final Map<UUID, Boolean> playerStates = new ConcurrentHashMap<>();

    // Estado global del cliente local (para mixins del lado cliente)
    @Setter
    @Getter
    private static boolean clientEnabled = false;

    /**
     * Verifica si el mod está activado para un jugador
     */
    public static boolean isEnabled(UUID playerUUID) {
        return playerStates.getOrDefault(playerUUID, true); // Activado por defecto
    }

    /**
     * Activa o desactiva el mod para un jugador
     */
    public static void setEnabled(UUID playerUUID, boolean enabled) {
        playerStates.put(playerUUID, enabled);
    }

    /**
     * Alterna el estado del mod para un jugador
     * @return el nuevo estado
     */
    public static boolean toggle(UUID playerUUID) {
        boolean newState = !isEnabled(playerUUID);
        playerStates.put(playerUUID, newState);
        return newState;
    }

    /**
     * Remueve el estado de un jugador (cuando se desconecta, opcional)
     */
    public static void remove(UUID playerUUID) {
        playerStates.remove(playerUUID);
    }

    // --- Estado del cliente local ---
    public static boolean toggleClient() {
        clientEnabled = !clientEnabled;
        return clientEnabled;
    }
}

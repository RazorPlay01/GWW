package com.github.razorplay01.system;

import com.github.razorplay01.api.noise.NoiseAPI;
import com.github.razorplay01.network.FabricCustomPayload;
import com.github.razorplay01.network.packet.NoisePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NoiseDetectionSystem {

    private static final Map<UUID, PlayerNoiseData> PLAYER_NOISE_DATA = new HashMap<>();
    private static final float DECAY_RATE = 0.015f; // Decaimiento más lento para mejor visualización
    private static final float MAX_NOISE = 1.0f;

    // Configuración de ruidos mejorada
    public static class NoiseConfig {
        // Movimiento base
        public static final float WALKING_BASE = 0.15f;
        public static final float SNEAKING_BASE = 0.05f; // Muy silencioso
        public static final float SPRINTING_BASE = 0.35f; // Más ruidoso

        // Multiplicadores por superficie
        public static final float SURFACE_MULTIPLIER_SOFT = 0.7f; // Lana, alfombra, etc.
        public static final float SURFACE_MULTIPLIER_NORMAL = 1.0f; // Piedra, tierra
        public static final float SURFACE_MULTIPLIER_HARD = 1.3f; // Metal, madera
        public static final float SURFACE_MULTIPLIER_LOUD = 1.6f; // Grava, nieve

        // Otros eventos
        public static final float JUMPING = 0.25f;
        public static final float LANDING_SOFT = 0.2f;
        public static final float LANDING_NORMAL = 0.4f;
        public static final float LANDING_HARD = 0.7f; // Caída de altura
        public static final float BLOCK_BREAK = 0.5f;
        public static final float BLOCK_PLACE = 0.4f;
        public static final float DAMAGE_TAKEN = 0.6f;
        public static final float ATTACK = 0.35f;
        public static final float ITEM_USE = 0.25f;
        public static final float DOOR_OPEN = 0.3f;
        public static final float CHEST_OPEN = 0.25f;

        // Umbrales de velocidad
        public static final double MIN_MOVEMENT_SPEED = 0.001; // Movimiento mínimo detectable
        public static final double SPRINT_THRESHOLD = 0.1; // Umbral para detectar sprint
    }

    /**
     * Obtiene o crea datos de ruido para un jugador
     */
    public static PlayerNoiseData getPlayerData(UUID playerId) {
        return PLAYER_NOISE_DATA.computeIfAbsent(playerId, id -> new PlayerNoiseData());
    }

    /**
     * Añade ruido al jugador con sistema de acumulación
     */
    public static void addNoise(ServerPlayer player, float amount) {
        PlayerNoiseData data = getPlayerData(player.getUUID());
        if (!data.isEnabled()) return;

        // Aplicar multiplicador personalizado
        amount *= data.getMultiplier();

        float newNoise = Math.min(data.getCurrentNoise() + amount, MAX_NOISE);
        data.setCurrentNoise(newNoise);
        data.setLastNoiseTime(System.currentTimeMillis());

        // Sincronizar con cliente
        syncToClient(player, data);
    }

    /**
     * Actualiza el sistema de ruido (llamar cada tick del servidor)
     */
    public static void tick(ServerPlayer player) {
        PlayerNoiseData data = getPlayerData(player.getUUID());
        if (!data.isEnabled()) return;

        // Detectar movimiento y generar ruido
        detectMovementNoise(player, data);

        // Aplicar decaimiento
        if (data.getCurrentNoise() > 0) {
            data.setCurrentNoise(Math.max(0, data.getCurrentNoise() - DECAY_RATE));
        }

        // Actualizar estado anterior
        data.setLastPosition(player.position());
        data.setWasOnGround(player.onGround());
        data.setWasSneaking(player.isCrouching());
        data.setWasSprinting(player.isSprinting());
        data.setTicksSinceLastStep(data.getTicksSinceLastStep() + 1);

        // Sincronizar con cliente cada 5 ticks
        if (player.tickCount % 5 == 0) {
            syncToClient(player, data);
        }
    }

    /**
     * Sistema mejorado de detección de ruido por movimiento
     */
    private static void detectMovementNoise(ServerPlayer player, PlayerNoiseData data) {
        Vec3 currentPos = player.position();
        Vec3 lastPos = data.getLastPosition();

        // Si es la primera vez, solo guardar posición
        if (lastPos.equals(Vec3.ZERO)) {
            data.setLastPosition(currentPos);
            return;
        }

        // Calcular distancia horizontal (ignoramos Y para los pasos)
        double deltaX = currentPos.x - lastPos.x;
        double deltaZ = currentPos.z - lastPos.z;
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // Verificar movimiento mínimo
        if (horizontalDistance < NoiseConfig.MIN_MOVEMENT_SPEED) {
            return;
        }

        // Solo generar ruido de pasos si está en el suelo
        if (player.onGround()) {
            handleFootsteps(player, data, horizontalDistance);
        }

        // Detectar aterrizaje (caída)
        if (player.onGround() && !data.isWasOnGround()) {
            handleLanding(player, data);
        }

        // Detectar salto
        if (!player.onGround() && data.isWasOnGround()) {
            handleJump(player, data);
        }
    }

    /**
     * Maneja el ruido de los pasos
     */
    private static void handleFootsteps(ServerPlayer player, PlayerNoiseData data, double distance) {
        // Acumular distancia recorrida
        data.setAccumulatedDistance(data.getAccumulatedDistance() + (float) distance);

        // Obtener distancia necesaria según el estado del jugador
        float requiredDistance = data.getStepDistance();

        // Si hemos recorrido suficiente distancia, generar ruido de paso
        if (data.getAccumulatedDistance() >= requiredDistance) {
            float noiseAmount = calculateFootstepNoise(player, data);
            addNoise(player, noiseAmount);

            // Resetear distancia acumulada
            data.setAccumulatedDistance(0);
            data.setTicksSinceLastStep(0);
        }
    }

    /**
     * Calcula el ruido de un paso basado en múltiples factores
     */
    private static float calculateFootstepNoise(ServerPlayer player, PlayerNoiseData data) {
        float baseNoise;

        // Determinar ruido base según estado de movimiento
        if (player.isSprinting()) {
            baseNoise = NoiseConfig.SPRINTING_BASE;
        } else if (player.isCrouching()) {
            baseNoise = NoiseConfig.SNEAKING_BASE;
        } else {
            baseNoise = NoiseConfig.WALKING_BASE;
        }

        // Multiplicador por tipo de superficie
        float surfaceMultiplier = getSurfaceMultiplier(player);

        // Multiplicador por velocidad (para detectar cambios bruscos)
        float speedMultiplier = getSpeedMultiplier(player);

        return baseNoise * surfaceMultiplier * speedMultiplier;
    }

    /**
     * Obtiene el multiplicador según la superficie donde camina
     */
    private static float getSurfaceMultiplier(ServerPlayer player) {
        BlockPos blockBelow = player.blockPosition().below();
        BlockState blockState = player.level().getBlockState(blockBelow);
        Block block = blockState.getBlock();

        // Verificar si hay configuración personalizada para este bloque
        float customNoise = NoiseAPI.getBlockNoise(block);
        if (customNoise > 0) {
            return customNoise * 2; // Convertir a multiplicador
        }

        // Usar el SoundType del bloque para determinar el multiplicador
        SoundType soundType = blockState.getSoundType();

        // Clasificar por tipo de sonido
        if (soundType == SoundType.WOOL || soundType == SoundType.MOSS_CARPET) {
            return NoiseConfig.SURFACE_MULTIPLIER_SOFT;
        } else if (soundType == SoundType.GRAVEL || soundType == SoundType.SNOW ||
                soundType == SoundType.SAND || soundType == SoundType.SOUL_SAND) {
            return NoiseConfig.SURFACE_MULTIPLIER_LOUD;
        } else if (soundType == SoundType.METAL || soundType == SoundType.CHAIN ||
                soundType == SoundType.ANVIL) {
            return NoiseConfig.SURFACE_MULTIPLIER_HARD * 1.2f; // Aún más ruidoso
        } else if (soundType == SoundType.WOOD || soundType == SoundType.BAMBOO_WOOD) {
            return NoiseConfig.SURFACE_MULTIPLIER_HARD;
        }

        return NoiseConfig.SURFACE_MULTIPLIER_NORMAL;
    }

    /**
     * Multiplicador basado en velocidad actual
     */
    private static float getSpeedMultiplier(ServerPlayer player) {
        Vec3 deltaMovement = player.getDeltaMovement();
        double horizontalSpeed = Math.sqrt(deltaMovement.x * deltaMovement.x + deltaMovement.z * deltaMovement.z);

        // Si se mueve muy rápido, aumentar ruido
        if (horizontalSpeed > NoiseConfig.SPRINT_THRESHOLD * 1.5) {
            return 1.3f;
        } else if (horizontalSpeed > NoiseConfig.SPRINT_THRESHOLD) {
            return 1.15f;
        }

        return 1.0f;
    }

    /**
     * Maneja el ruido al aterrizar después de una caída
     */
    private static void handleLanding(ServerPlayer player, PlayerNoiseData data) {
        Vec3 deltaMovement = player.getDeltaMovement();
        double fallSpeed = Math.abs(deltaMovement.y);

        float landingNoise;

        if (fallSpeed < 0.3) {
            // Caída suave o salto normal
            landingNoise = NoiseConfig.LANDING_SOFT;
        } else if (fallSpeed < 0.6) {
            // Caída moderada
            landingNoise = NoiseConfig.LANDING_NORMAL;
        } else {
            // Caída fuerte
            landingNoise = NoiseConfig.LANDING_HARD;
            // Escalar con la velocidad de caída
            landingNoise *= Math.min(2.0f, (float) fallSpeed);
        }

        // Aplicar multiplicador de superficie
        landingNoise *= getSurfaceMultiplier(player);

        addNoise(player, landingNoise);
    }

    /**
     * Maneja el ruido al saltar
     */
    private static void handleJump(ServerPlayer player, PlayerNoiseData data) {
        float jumpNoise = NoiseConfig.JUMPING;

        // Saltar mientras corres es más ruidoso
        if (player.isSprinting()) {
            jumpNoise *= 1.5f;
        }
        // Saltar agachado es más silencioso (y casi imposible en vanilla)
        else if (player.isCrouching()) {
            jumpNoise *= 0.5f;
        }

        addNoise(player, jumpNoise);
    }

    /**
     * Maneja evento de romper bloque
     */
    public static void onBlockBreak(ServerPlayer player, BlockState state) {
        float noise = NoiseConfig.BLOCK_BREAK;

        // Algunos bloques son más ruidosos al romperse
        SoundType soundType = state.getSoundType();
        if (soundType == SoundType.GLASS) {
            noise *= 1.5f;
        } else if (soundType == SoundType.METAL || soundType == SoundType.ANVIL) {
            noise *= 1.3f;
        } else if (soundType == SoundType.WOOL) {
            noise *= 0.5f;
        }

        addNoise(player, noise);
    }

    /**
     * Maneja evento de colocar bloque
     */
    public static void onBlockPlace(ServerPlayer player, BlockState state) {
        float noise = NoiseConfig.BLOCK_PLACE;

        SoundType soundType = state.getSoundType();
        if (soundType == SoundType.METAL || soundType == SoundType.ANVIL) {
            noise *= 1.4f;
        } else if (soundType == SoundType.WOOL || soundType == SoundType.MOSS_CARPET) {
            noise *= 0.6f;
        }

        addNoise(player, noise);
    }

    /**
     * Maneja evento de recibir daño
     */
    public static void onDamageTaken(ServerPlayer player, float damage) {
        float noise = NoiseConfig.DAMAGE_TAKEN * Math.min(2.0f, damage / 10f);
        addNoise(player, noise);
    }

    /**
     * Maneja evento de atacar
     */
    public static void onAttack(ServerPlayer player) {
        float noise = NoiseConfig.ATTACK;

        // Atacar mientras corres es más ruidoso
        if (player.isSprinting()) {
            noise *= 1.3f;
        }

        addNoise(player, noise);
    }

    /**
     * Maneja evento de usar item
     */
    public static void onItemUse(ServerPlayer player, ItemStack stack) {
        float itemNoise = NoiseAPI.getItemNoise(stack.getItem());
        if (itemNoise > 0) {
            addNoise(player, itemNoise);
        } else {
            addNoise(player, NoiseConfig.ITEM_USE);
        }
    }

    /**
     * Activa/desactiva el sistema para un jugador
     */
    public static void toggleSystem(ServerPlayer player, boolean enabled) {
        PlayerNoiseData data = getPlayerData(player.getUUID());
        data.setEnabled(enabled);
        if (!enabled) {
            data.reset();
        }

        // Sincronizar estado con cliente
        syncToClient(player, data);
    }

    /**
     * Sincroniza el estado de ruido con el cliente
     */
    private static void syncToClient(ServerPlayer player, PlayerNoiseData data) {
        NoisePacket packet = new NoisePacket(
                data.getCurrentNoise(),
                DECAY_RATE,
                data.isEnabled()
        );
        ServerPlayNetworking.send(player, new FabricCustomPayload(packet));
    }

    /**
     * Establece un multiplicador personalizado para un jugador
     */
    public static void setNoiseMultiplier(ServerPlayer player, float multiplier) {
        PlayerNoiseData data = getPlayerData(player.getUUID());
        data.setMultiplier(Math.max(0.0f, multiplier));
    }

    /**
     * Limpia datos de un jugador
     */
    public static void removePlayer(UUID playerId) {
        PLAYER_NOISE_DATA.remove(playerId);
    }

    public static void clearAll() {
        PLAYER_NOISE_DATA.clear();
    }

    /**
     * Obtiene el nivel de ruido actual de un jugador (útil para IA)
     */
    public static float getNoiseLevel(UUID playerId) {
        PlayerNoiseData data = PLAYER_NOISE_DATA.get(playerId);
        return data != null ? data.getCurrentNoise() : 0.0f;
    }

    /**
     * Verifica si un jugador está haciendo ruido significativo
     */
    public static boolean isPlayerNoisy(UUID playerId, float threshold) {
        return getNoiseLevel(playerId) >= threshold;
    }
}
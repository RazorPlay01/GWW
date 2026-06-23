package com.github.razorplay01.system;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.phys.Vec3;

@Getter
@Setter
public class PlayerNoiseData {
    private float currentNoise = 0.0f;
    private long lastNoiseTime = 0;
    private Vec3 lastNoiseLocation = Vec3.ZERO;
    private boolean enabled = false;
    private float multiplier = 1.0f;

    // Datos de movimiento
    private Vec3 lastPosition = Vec3.ZERO;
    private boolean wasOnGround = false;
    private boolean wasSneaking = false;
    private boolean wasSprinting = false;
    private int ticksSinceLastStep = 0;
    private float accumulatedDistance = 0.0f;

    // Configuración de pasos
    private static final float STEP_DISTANCE_NORMAL = 0.6f; // Distancia para generar ruido caminando
    private static final float STEP_DISTANCE_SNEAKING = 1.2f; // Distancia agachado (más silencioso)
    private static final float STEP_DISTANCE_SPRINTING = 0.4f; // Distancia corriendo (más ruidoso)

    public void reset() {
        this.currentNoise = 0.0f;
        this.lastNoiseTime = 0;
        this.lastNoiseLocation = Vec3.ZERO;
        this.accumulatedDistance = 0.0f;
        this.ticksSinceLastStep = 0;
    }

    public float getStepDistance() {
        if (wasSprinting) return STEP_DISTANCE_SPRINTING;
        if (wasSneaking) return STEP_DISTANCE_SNEAKING;
        return STEP_DISTANCE_NORMAL;
    }
}
package com.github.razorplay01.api.noise;

import net.minecraft.world.entity.player.Player;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoiseEvent {
    private final String id;
    private float noiseLevel; // 0.0 a 1.0
    private float duration; // Duración en ticks
    private NoiseEventCallback callback;

    public NoiseEvent(String id, float noiseLevel, float duration) {
        this.id = id;
        this.noiseLevel = Math.clamp(noiseLevel, 0.0f, 1.0f);
        this.duration = duration;
    }

    public NoiseEvent(String id, float noiseLevel, float duration, NoiseEventCallback callback) {
        this(id, noiseLevel, duration);
        this.callback = callback;
    }

    public void trigger(Player player) {
        if (callback != null) {
            callback.onTrigger(player, this);
        }
    }

    @FunctionalInterface
    public interface NoiseEventCallback {
        void onTrigger(Player player, NoiseEvent event);
    }
}
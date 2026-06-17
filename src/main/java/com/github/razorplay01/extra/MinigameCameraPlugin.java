package com.github.razorplay01.extra;

import com.github.razorplay01.cam.api.CameraModifier;
import com.github.razorplay01.cam.api.CameraPlugin;
import com.github.razorplay01.cam.api.ModifierPriority;
import com.github.razorplay01.cam.api.Plugin;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.razorplay01.cam.ClientUtil.*;

@Plugin(value = "minigame_aerial", priority = ModifierPriority.HIGH)
public class MinigameCameraPlugin implements CameraPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger("MinigameCameraPlugin");
    private final CameraModifier modifier;
    private boolean wasActive = false;

    public MinigameCameraPlugin(CameraModifier modifier) {
        this.modifier = modifier;
        LOGGER.info("Plugin de cámara aérea para minijuego cargado");
    }

    @Override
    public void update(float partialTicks) {
        ClientMinigameState game = ClientMinigameState.get();
        boolean shouldBeActive = game.isActive();

        if (shouldBeActive) {
            applyAerialCamera(game);
            wasActive = true;
            disableBobView();
            toThirdView();
        } else if (wasActive) {
            resetBobView();
            resetCameraType();
            modifier.disableAll().reset();
            wasActive = false;
        }
    }

    private void applyAerialCamera(ClientMinigameState game) {
        Vec3 center = game.getCenter();
        float height = 15.0f;

        modifier.enable()
                .enablePos()
                .enableRotation()
                .enableGlobalMode()
                .setToVanilla()
                .setPos((float) center.x, (float) center.y + height, (float) center.z)
                .setRotationYXZ(90.0f, 0.0f, 0.0f)
                .enableObstacle();
    }
}
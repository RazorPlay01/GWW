package com.github.razorplay01.extra;

import com.github.razorplay01.cam.api.CameraPlugin;
import com.github.razorplay01.cam.api.ICameraModifier;
import com.github.razorplay01.cam.api.ICameraPlugin;
import com.github.razorplay01.cam.api.ModifierPriority;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CameraPlugin(value = "minigame_aerial", priority = ModifierPriority.HIGH)
public class MinigameCameraPlugin implements ICameraPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger("MinigameCameraPlugin");
    private ICameraModifier modifier;
    private boolean wasActive = false;

    private boolean isEnableViewBobbing;
    private static final Minecraft MC = Minecraft.getInstance();

    @Override
    public void initialize(ICameraModifier modifier) {
        this.modifier = modifier;
        LOGGER.info("Plugin de cámara aérea para minijuego cargado");
    }

    @Override
    public void update() {
        ClientMinigameState game = ClientMinigameState.get();
        boolean shouldBeActive = game.isActive();

        if (shouldBeActive) {
            applyAerialCamera(game);
            wasActive = true;
            isEnableViewBobbing = MC.options.bobView().get();
            MC.options.bobView().set(false);
            MC.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        } else if (wasActive) {
            MC.options.setCameraType(CameraType.FIRST_PERSON);
            MC.options.bobView().set(isEnableViewBobbing);
            modifier.disableAll().reset();
            wasActive = false;
        }
    }

    private void applyAerialCamera(ClientMinigameState game) {
        Vec3 center = game.getCenter();
        float height = 18.0f; // Ajusta según el radio

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
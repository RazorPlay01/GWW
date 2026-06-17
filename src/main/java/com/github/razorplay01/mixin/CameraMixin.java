package com.github.razorplay01.mixin;

import com.github.razorplay01.cam.CameraExtension;
import net.minecraft.client.Camera;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static com.github.razorplay01.cam.ClientUtil.mc;

@Mixin(Camera.class)
public abstract class CameraMixin implements CameraExtension {
    @Final
    @Shadow
    private Quaternionf rotation;

    @Shadow
    protected abstract void setRotation(float f, float g);

    @Shadow
    protected abstract void setPosition(double d, double e, double f);

    @Unique
    private float roll;

    @Override
    public void setFov(int fov) {
        mc().options.fov().set(fov);
    }

    @Override
    public int getFov() {
        return mc().options.fov().get();
    }

    @Override
    public float getRoll() {
        return roll;
    }

    @Override
    public void setCustomRotation(float yaw, float pitch, float roll) {
        this.setRotation(yaw, pitch);
        this.roll = roll;
        this.rotation.rotateZ(roll * ((float) Math.PI / 180F));
    }

    @Override
    public void setCustomPosition(double x, double y, double z) {
        this.setPosition(x, y, z);
    }
}

package com.github.razorplay01.cam.core;

import com.github.razorplay01.cam.CameraExtension;
import com.github.razorplay01.cam.ClientUtil;
import com.github.razorplay01.cam.api.CameraData;
import com.github.razorplay01.cam.api.CameraDataType;
import com.github.razorplay01.cam.api.CameraModifier;
import com.github.razorplay01.cam.api.ObstacleHandler;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;

import static com.github.razorplay01.cam.ClientUtil.mc;
import static com.github.razorplay01.cam.api.CameraStates.*;

public class ModifierManager {
    public static final ModifierManager INSTANCE = new ModifierManager();
    private final Vector3f pos;// 坐标
    private final Vector3f rot;// 旋转
    private int fov;// 视场
    private int state;// 状态
    private HashMap<Class<?>, CameraData> cameraData = new HashMap<>();

    private ModifierManager() {
        pos = new Vector3f();
        rot = new Vector3f();
    }

    public void modify() {
        setToVanilla();
        applyToCamera();
        updateCamera();
    }

    private void setToVanilla() {
        Camera camera = camera();
        Vec3 cameraPos = camera.getPosition();
        pos.set(cameraPos.x, cameraPos.y, cameraPos.z);
        rot.set(camera.getXRot(), camera.getYRot() % 360, ((CameraExtension) camera).getRoll());
        this.fov = ((CameraExtension) camera).getFov();
    }

    private void applyToCamera() {
        CameraModifier modifier = ModifierRegistry.INSTANCE.getActiveModifier();

        if (modifier == null) {
            state = 0;
            return;
        }

        state = modifier.getState();
        applyPos(modifier);
        applyRot(modifier);
        applyFov(modifier);
        applyGlobal(modifier);
        applyObstacle(modifier);
        applyCameraData(modifier);
    }

    private void applyPos(CameraModifier modifier) {
        if (!modifier.isStateEnabledOr(POS.code)) {
            return;
        }

        pos.set(modifier.getPos());
    }

    private void applyRot(CameraModifier modifier) {
        if (!modifier.isStateEnabledOr(ROT.code)) {
            return;
        }

        rot.set(modifier.getRot());
    }

    private void applyFov(CameraModifier modifier) {
        if (!modifier.isStateEnabledOr(FOV.code)) {
            return;
        }

        fov = modifier.getFov();
    }

    private void applyGlobal(CameraModifier modifier) {
        if (modifier.isStateEnabledOr(GLOBAL_MODE.code)) {
            return;
        }

        if (modifier.isStateEnabledOr(POS.code)) {
            Vec3 playerPos = ClientUtil.player().getPosition(ClientUtil.partialTicks());
            pos.add((float) playerPos.x, (float) playerPos.y, (float) playerPos.z);
        }
    }

    private final int[] fovDest = new int[1];

    private void applyObstacle(CameraModifier modifier) {
        if (!modifier.isStateEnabledOr(OBSTACLE.code)) {
            return;
        }

        ObstacleHandler obstacleHandler = modifier.getObstacleHandler();
        fovDest[0] = fov;

        switch (obstacleHandler.obstacleAvoid(pos, rot, fovDest)) {
            case PASS -> defaultObstacle(obstacleHandler);
            case COLLIDE -> {
                fov = fovDest[0];
                obstacleHandler.onCollision(pos, rot, fov);
            }
            case NO_COLLIDE -> {
                // []
            }
        }
    }

    private void defaultObstacle(ObstacleHandler obstacleHandler) {
        Vector3f origin = ClientUtil.player().getEyePosition(ClientUtil.partialTicks()).toVector3f();
        Vector3f direction = pos.sub(origin, new Vector3f());
        float size = 0.1F;
        float max = direction.length();
        float length = max;

        for (int i = 0; i < 8; i++) {
            float x = size * (i & 1) * 2 - 1;
            float y = size * (i >> 1 & 1) * 2 - 1;
            float z = size * (i >> 2 & 1) * 2 - 1;

            Vec3 begin = new Vec3(origin.x + x, origin.y + y, origin.z + z);
            Vec3 end = new Vec3(pos.x + x, pos.y + y, pos.z + z);

            HitResult hitresult = ClientUtil.player().level().clip(new ClipContext(begin, end, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, ClientUtil.player()));

            if (hitresult.getType() != HitResult.Type.MISS) {
                float distance = (float) hitresult.getLocation().distanceToSqr(origin.x, origin.y, origin.z);

                if (distance < Mth.square(max)) {
                    max = Mth.sqrt(distance);
                }
            }
        }

        if (max == length) {
            return;
        }

        pos.set(direction.normalize(max).add(origin));
        obstacleHandler.onCollision(pos, rot, fov);
    }

    private void applyCameraData(CameraModifier modifier) {
        cameraData = modifier.getAllData();
    }

    private void updateCamera() {
        updateCameraData();
        setCamera();
    }

    private void updateCameraData() {
        for (CameraData data : cameraData.values()) {
            data.update();
        }
    }

    private void setCamera() {
        Camera camera = camera();
        ((CameraExtension) camera).setCustomRotation(rot.y, rot.x, rot.z);
        ((CameraExtension) camera).setCustomPosition(pos.x, pos.y, pos.z);
        ((CameraExtension) camera).setFov(fov);
    }

    private Camera camera() {
        return mc().gameRenderer.getMainCamera();
    }

    public Vector3f pos() {
        return pos;
    }

    public Vector3f rot() {
        return rot;
    }

    public float fov() {
        return fov;
    }

    public boolean isStateEnabledAnd(int mask) {
        return (state & mask) == mask;
    }

    public boolean isStateEnabledOr(int mask) {
        return (state & mask) != 0;
    }

    public <T extends CameraData> T getData(CameraDataType<T> dataType) {
        Class<T> type = dataType.type();
        CameraData data = cameraData.computeIfAbsent(type, k -> dataType.create());
        return type.cast(data);
    }
}

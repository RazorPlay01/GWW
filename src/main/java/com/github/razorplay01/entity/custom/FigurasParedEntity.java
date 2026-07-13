package com.github.razorplay01.entity.custom;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.AnimatableManager;

public class FigurasParedEntity extends BaseEntity {
    private static final EntityDataAccessor<Direction> DATA_FACING =
            SynchedEntityData.defineId(FigurasParedEntity.class, EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(
            FigurasParedEntity.class, EntityDataSerializers.INT);

    public FigurasParedEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, 0);
        builder.define(DATA_FACING, Direction.NORTH);
    }

    public int getState() {
        return this.entityData.get(STATE);
    }

    public void setState(int state) {
        if (state < 0 || state > 3) state = 0;
        this.entityData.set(STATE, state);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("State", getState());
        tag.putString("Facing", getFacing().getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setState(tag.getInt("State"));
        if (tag.contains("Facing")) {
            Direction dir = Direction.byName(tag.getString("Facing"));
            if (dir != null) {
                setFacing(dir);
            }
        }
    }

    public static AttributeSupplier.Builder setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Double.POSITIVE_INFINITY);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public void handleNormalInteract(Player player) {
        if (!player.level().isClientSide && player.isCreative()) {
            setState((getState() + 1) % 4);
        }
    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        double height = 2;
        double width = 2;
        double depth = 0.2;

        double hw = width / 2.0;
        double hd = depth / 2.0;

        if (getFacing().getAxis() == Direction.Axis.Z) {
            return new AABB(x - hw, y, z - hd, x + hw, y + height, z + hd);
        } else {
            return new AABB(x - hd, y, z - hw, x + hd, y + height, z + hw);
        }
    }

    public Direction getFacing() {
        return this.entityData.get(DATA_FACING);
    }

    public void setFacing(Direction direction) {
        if (this.entityData.get(DATA_FACING) != direction) {
            this.entityData.set(DATA_FACING, direction);
            this.refreshDimensions();
        }
    }

    @Override
    public void setYRot(float yaw) {
        super.setYRot(yaw);
        setFacing(Direction.fromYRot(yaw));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_FACING.equals(key)) {
            this.refreshDimensions();
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }
}
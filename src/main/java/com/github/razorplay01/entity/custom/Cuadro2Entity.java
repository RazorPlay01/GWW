package com.github.razorplay01.entity.custom;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animation.AnimatableManager;

public class Cuadro2Entity extends BaseCuadroEntity {

    private static final EntityDataAccessor<Boolean> IS_ACTIVE =
            SynchedEntityData.defineId(Cuadro2Entity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Direction> DATA_FACING =
            SynchedEntityData.defineId(Cuadro2Entity.class, EntityDataSerializers.DIRECTION);

    public Cuadro2Entity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Double.POSITIVE_INFINITY);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_ACTIVE, false);
        builder.define(DATA_FACING, Direction.NORTH);
    }

    public boolean isActive() {
        return this.entityData.get(IS_ACTIVE);
    }

    public void setActive(boolean active) {
        this.entityData.set(IS_ACTIVE, active);
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsActive", isActive());
        tag.putString("Facing", getFacing().getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setActive(tag.getBoolean("IsActive"));
        if (tag.contains("Facing")) {
            Direction dir = Direction.byName(tag.getString("Facing"));
            if (dir != null) setFacing(dir);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public void handleNormalInteract(Player player) {
        if (!player.level().isClientSide) {
            setActive(!isActive());
        }
        super.handleNormalInteract(player);
    }

    @Override
    protected AABB makeBoundingBox() {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        double height = this.getBbHeight();

        double width = 2.0;
        double depth = 0.1;
        double hw = width / 2.0;
        double hd = depth / 2.0;

        if (getFacing().getAxis() == Direction.Axis.Z) {
            return new AABB(x - hw, y, z - hd, x + hw, y + height, z + hd);
        } else {
            return new AABB(x - hd, y, z - hw, x + hd, y + height, z + hw);
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
        if (IS_ACTIVE.equals(key) || DATA_FACING.equals(key)) {
            this.refreshDimensions();
        }
    }

    @Override
    protected void onBound(Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 999999, 3, false, false));
    }

    @Override
    protected void onUnbound(Player player) {
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }

    @Override
    protected boolean canBound() {
        return true;
    }
}
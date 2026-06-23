package com.github.razorplay01.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;

public class LuzTortugaEntity extends BaseEntity {

    // ==================== ESTADO ====================
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(
            LuzTortugaEntity.class, EntityDataSerializers.INT);

    public LuzTortugaEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, 0); // 0 = apagada (default)
    }

    // Getters y Setters
    public int getState() {
        return this.entityData.get(STATE);
    }

    public void setState(int state) {
        if (state < 0 || state > 2) state = 0;
        this.entityData.set(STATE, state);
    }

    // ==================== PERSISTENCIA (Guardar/Cargar) ====================
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("State", getState());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setState(tag.getInt("State"));
    }

    public static AttributeSupplier.Builder setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Double.POSITIVE_INFINITY);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Aquí puedes agregar controladores de animación según estado si quieres
    }

    @Override
    public void handleNormalInteract(Player player) {
        // Ejemplo: clic derecho cambia de estado (opcional)
        if (!player.level().isClientSide) {
            setState((getState() + 1) % 3);
        }
    }
}
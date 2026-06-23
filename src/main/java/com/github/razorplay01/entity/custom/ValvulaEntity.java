package com.github.razorplay01.entity.custom;

import com.github.razorplay01.entity.custom.util.ValvulaType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

public class ValvulaEntity extends BaseEntity {

    private static final EntityDataAccessor<Integer> DATA_TYPE =
            SynchedEntityData.defineId(ValvulaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(ValvulaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_HAS_MANIVELA =
            SynchedEntityData.defineId(ValvulaEntity.class, EntityDataSerializers.BOOLEAN);

    // Animaciones
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.idle");
    private static final RawAnimation OPEN_ANIM = RawAnimation.begin().thenPlay("animation.open");
    private static final RawAnimation CLOSE_ANIM = RawAnimation.begin().thenPlay("animation.close");

    public ValvulaEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Double.POSITIVE_INFINITY);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TYPE, ValvulaType.NARANJA.getId());
        builder.define(DATA_STATE, 0);
        builder.define(DATA_HAS_MANIVELA, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Type", getValType().getId());
        tag.putInt("State", getState());
        tag.putBoolean("HasManivela", hasManivela());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setType(ValvulaType.byId(tag.getInt("Type")));
        setState(tag.getInt("State"));
        setHasManivela(tag.getBoolean("HasManivela"));
    }

    public ValvulaType getValType() {
        return ValvulaType.byId(this.entityData.get(DATA_TYPE));
    }

    public void setType(ValvulaType type) {
        this.entityData.set(DATA_TYPE, type.getId());
    }

    public int getState() {
        return this.entityData.get(DATA_STATE);
    }

    public void setState(int state) {
        this.entityData.set(DATA_STATE, Math.max(0, Math.min(3, state)));
    }

    public boolean hasManivela() {
        return this.entityData.get(DATA_HAS_MANIVELA);
    }

    public void setHasManivela(boolean has) {
        this.entityData.set(DATA_HAS_MANIVELA, has);
    }

    public void attachManivela(ValvulaType type) {
        if (this.getValType() == type) {
            setHasManivela(true);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle_controller",
                state -> state.setAndContinue(IDLE_ANIM)));

        controllers.add(new AnimationController<>(this, "open_controller",
                state -> PlayState.STOP)
                .triggerableAnim("open", OPEN_ANIM));

        controllers.add(new AnimationController<>(this, "close_controller",
                state -> PlayState.STOP)
                .triggerableAnim("close", CLOSE_ANIM));
    }

    public void triggerTurnUpAnim(Level level) {
        if (level instanceof ServerLevel) {
            triggerAnim("open_controller", "open");
        }
    }

    public void triggerTurnDownAnim(Level level) {
        if (level instanceof ServerLevel) {
            triggerAnim("close_controller", "close");
        }
    }

    @Override
    public void handleNormalInteract(Player player) {
        if (!hasManivela()) {
            return;
        }
        increaseState();
    }

    @Override
    public boolean skipAttackInteraction(net.minecraft.world.entity.Entity entity) {
        if (entity instanceof Player) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player && hasManivela()) {
            if (!this.level().isClientSide) {
                decreaseState();
            }
            return false;
        }
        return false;
    }

    private void increaseState() {
        int currentState = getState();
        if (currentState < 3) {
            setState(currentState + 1);
            triggerTurnUpAnim(this.level());
            System.out.println("Estado aumentado a: " + getState());
        }
    }

    private void decreaseState() {
        int currentState = getState();
        if (currentState > 0) {
            setState(currentState - 1);
            triggerTurnDownAnim(this.level());
            System.out.println("Estado disminuido a: " + getState());
        }
    }
}
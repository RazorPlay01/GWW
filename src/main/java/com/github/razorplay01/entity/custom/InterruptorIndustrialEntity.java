package com.github.razorplay01.entity.custom;

import com.github.razorplay01.entity.custom.util.Util;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.ArrayList;
import java.util.List;

public class InterruptorIndustrialEntity extends BaseEntity {

    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(
            InterruptorIndustrialEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation ANIMATION_ON = RawAnimation.begin().thenPlayAndHold("On");
    private static final RawAnimation ANIMATION_OFF = RawAnimation.begin().thenPlayAndHold("Off");

    private final List<Vec3> linkedCables = new ArrayList<>();

    public InterruptorIndustrialEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, 0); // 0 = OFF, 1 = ON
    }

    public int getState() {
        return this.entityData.get(STATE);
    }

    public void setState(int state) {
        this.entityData.set(STATE, state == 1 ? 1 : 0);
    }

    public boolean isOn() {
        return getState() == 1;
    }

    // ==================== LINKING CABLES ====================

    public void linkCable(CableEntity cable) {
        if (cable == null) return;
        Vec3 relativePos = cable.position().subtract(this.position());
        if (!linkedCables.contains(relativePos)) {
            linkedCables.add(relativePos);
        }
    }

    public void unlinkAllCables() {
        linkedCables.clear();
    }

    public boolean areAllCablesReady() {
        if (linkedCables.isEmpty()) return true;

        Vec3 interruptorPos = this.position();

        for (Vec3 relPos : linkedCables) {
            Vec3 absolutePos = interruptorPos.add(relPos);

            List<CableEntity> found = this.level().getEntitiesOfClass(CableEntity.class,
                    AABB.ofSize(absolutePos, 5, 5, 5),
                    c -> c.position().distanceToSqr(absolutePos) < 1.5);

            if (found.isEmpty() || !found.get(0).isActive() || !found.get(0).isCorrect()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void handleNormalInteract(Player player) {
        if (player.level().isClientSide) return;

        if (areAllCablesReady()) {
            boolean newState = !isOn();
            setState(newState ? 1 : 0);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§6[Interruptor] §fActivado: §" + (newState ? "aON" : "cOFF")));
        } else {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§c[Interruptor] No puedes activarlo. Todos los cables deben estar activos y en estado correcto."));
        }
    }

    // ==================== PERSISTENCIA ====================

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("State", getState());
        Util.saveLinkedList(tag, "LinkedCables", linkedCables);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setState(tag.getInt("State"));
        linkedCables.clear();
        linkedCables.addAll(Util.loadLinkedList(tag, "LinkedCables"));
    }

    public static AttributeSupplier.Builder setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Double.POSITIVE_INFINITY);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                this,
                "interruptor_controller",
                0,
                state -> isOn() ? state.setAndContinue(ANIMATION_ON) : state.setAndContinue(ANIMATION_OFF)
        ));
    }

    public List<Vec3> getLinkedCables() {
        return linkedCables;
    }
}
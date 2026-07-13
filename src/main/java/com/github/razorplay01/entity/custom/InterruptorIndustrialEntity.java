package com.github.razorplay01.entity.custom;

import com.github.razorplay01.entity.custom.util.Util;
import lombok.Getter;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.ArrayList;
import java.util.List;

@Getter
public class InterruptorIndustrialEntity extends BaseEntity {

    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(
            InterruptorIndustrialEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation ANIMATION_ON = RawAnimation.begin().thenPlayAndHold("On");
    private static final RawAnimation ANIMATION_OFF = RawAnimation.begin().thenPlayAndHold("Off");

    private final List<Vec3> linkedCables = new ArrayList<>();
    private final List<Vec3> linkedUblablas = new ArrayList<>();
    private final List<Vec3> linkedPanels = new ArrayList<>();

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
        int newState = (state == 1) ? 1 : 0;
        int oldState = this.entityData.get(STATE);
        this.entityData.set(STATE, newState);
        if (newState != oldState) {
            onStateChanged(newState == 1);
        }
    }

    private void onStateChanged(boolean nowOn) {
        PanelCodigoEntity panel = getLinkedPanel();
        if (panel != null) {
            panel.setPowered(nowOn); // ON → encendido, OFF → apagado
        }

        applyBlindness(!nowOn);
    }


    private void applyBlindness(boolean apply) {
        UblablaEntity ublabla = getLinkedUblabla();
        if (ublabla == null) {
            return;
        }

        Vec3 patrolCenter = ublabla.getPatrolCenter();
        double patrolRadius = ublabla.getPatrolRadius();
        if (patrolCenter == null) return;

        AABB region = new AABB(
                patrolCenter.x - patrolRadius,
                patrolCenter.y - 30,
                patrolCenter.z - patrolRadius,
                patrolCenter.x + patrolRadius,
                patrolCenter.y + 30,
                patrolCenter.z + patrolRadius
        );

        List<Player> players = this.level().getEntitiesOfClass(Player.class, region);
        for (Player player : players) {
            if (apply) {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, -1, 0, false, false, true));
            } else {
                player.removeEffect(MobEffects.BLINDNESS);
            }
        }
    }

    public boolean isOn() {
        return getState() == 1;
    }

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

    public void linkUblabla(UblablaEntity ublabla) {
        if (ublabla == null) return;
        linkedUblablas.clear();
        linkedUblablas.add(ublabla.position().subtract(this.position()));
        if (!isOn()) {
            applyBlindness(true);
        }
    }

    public void unlinkUblabla() {
        if (!isOn()) {
            applyBlindness(false);
        }
        linkedUblablas.clear();
    }

    private UblablaEntity getLinkedUblabla() {
        if (linkedUblablas.isEmpty()) return null;
        Vec3 relPos = linkedUblablas.get(0);
        Vec3 expectedPos = this.position().add(relPos);
        List<UblablaEntity> found = this.level().getEntitiesOfClass(UblablaEntity.class,
                AABB.ofSize(expectedPos, 5, 5, 5),
                u -> u.position().distanceToSqr(expectedPos) < 1.5);
        return found.isEmpty() ? null : found.get(0);
    }

    public void linkPanel(PanelCodigoEntity panel) {
        if (panel == null) return;
        linkedPanels.clear();
        linkedPanels.add(panel.position().subtract(this.position()));
    }

    public void unlinkPanel() {
        linkedPanels.clear();
    }

    public PanelCodigoEntity getLinkedPanel() {
        if (linkedPanels.isEmpty()) return null;
        Vec3 relPos = linkedPanels.get(0);
        Vec3 expectedPos = this.position().add(relPos);
        List<PanelCodigoEntity> found = this.level().getEntitiesOfClass(PanelCodigoEntity.class,
                AABB.ofSize(expectedPos, 5, 5, 5),
                p -> p.position().distanceToSqr(expectedPos) < 1.5);
        return found.isEmpty() ? null : found.get(0);
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

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("State", getState());
        Util.saveLinkedList(tag, "LinkedCables", linkedCables);
        Util.saveLinkedList(tag, "LinkedUblablas", linkedUblablas);
        Util.saveLinkedList(tag, "LinkedPanels", linkedPanels);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setState(tag.getInt("State"));
        linkedCables.clear();
        linkedCables.addAll(Util.loadLinkedList(tag, "LinkedCables"));
        linkedUblablas.clear();
        linkedUblablas.addAll(Util.loadLinkedList(tag, "LinkedUblablas"));
        linkedPanels.clear();
        linkedPanels.addAll(Util.loadLinkedList(tag, "LinkedPanels"));

        applyBlindness(!isOn());
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
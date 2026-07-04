package com.github.razorplay01.entity.custom;

import com.github.razorplay01.entity.custom.util.PuzzleEntityChecker;
import com.github.razorplay01.system.NoiseDetectionSystem;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class UblablaEntity extends PathfinderMob implements GeoEntity {

    // ── Synched Data ──
    private static final EntityDataAccessor<Integer> STATE =
            SynchedEntityData.defineId(UblablaEntity.class, EntityDataSerializers.INT);

    // ── Estados ──
    public static final int STATE_PATROL = 0;
    public static final int STATE_ALERT = 1;
    public static final int STATE_INVESTIGATING = 2;
    public static final int STATE_CHASING = 3;
    public static final int STATE_ATTACKING = 4;
    public static final int STATE_CHECKING = 5;

    // ── Duraciones (ticks) ──
    private static final int ALERT_DURATION = 100;          // 5 segundos
    private static final int CHASE_MAX_DURATION = 200;      // 10 segundos
    private static final int CHECK_WAIT_DURATION = 60;      // 3 segundos
    private static final int INVESTIGATION_TIMEOUT = 300;   // 15 segundos
    private static final int NOISE_CHECK_INTERVAL = 12;
    private static final float NOISE_THRESHOLD = 0.72f;
    private static final double CATCH_DISTANCE_SQ = 5.29;   // ~2.3 bloques
    private static final double LOSE_TARGET_DISTANCE_SQ = 2500.0; // 50 bloques

    // ── Configuración de zona ──
    @Getter @Setter private Vec3 patrolCenter;
    @Getter private double patrolRadius = 25.0;
    @Getter @Setter private BlockPos spawnPos;
    @Getter @Setter private BlockPos investigationTarget;
    @Getter private BlockPos jailMin;
    @Getter private BlockPos jailMax;

    // ── Timers internos ──
    private int noiseCheckCooldown = 0;
    private int alertTimer = 0;
    private int investigationTimer = 0;
    private int chaseTimer = 0;
    private int chaseMessageCooldown = 0;
    private int checkWaitTimer = 0;

    // ── Animaciones ──
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.walk");
    private static final RawAnimation CHECK = RawAnimation.begin().thenPlay("animation.check");
    private static final RawAnimation CHASE_ANIM = RawAnimation.begin().thenLoop("animation.chase");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.ataque");

    // ── Mensajes de persecución ──
    private static final String[] CHASE_MESSAGES = {
            "¡No huyas!",
            "¡Vuelve aquí!",
            "No te haré nada... malo.",
            "¡Todo debe estar en su lugar!"
    };

    // ══════════════════════════════════════════
    // Constructor
    // ══════════════════════════════════════════

    public UblablaEntity(EntityType<? extends UblablaEntity> type, Level level) {
        super(type, level);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        this.setPersistenceRequired();
        this.spawnPos = BlockPos.containing(this.position());
        this.patrolCenter = Vec3.atCenterOf(spawnPos);
    }

    public static AttributeSupplier.Builder setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 90.0);
    }

    // ══════════════════════════════════════════
    // Configuración pública
    // ══════════════════════════════════════════

    public void setJailArea(BlockPos min, BlockPos max) {
        this.jailMin = min;
        this.jailMax = max;
    }

    public void setPatrolRadius(double radius) {
        this.patrolRadius = Math.max(8.0, radius);
    }

    public int getState() {
        return this.entityData.get(STATE);
    }

    private void setState(int state) {
        this.entityData.set(STATE, state);
    }

    // ══════════════════════════════════════════
    // Synched Data
    // ══════════════════════════════════════════

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, STATE_PATROL);
    }

    // ══════════════════════════════════════════
    // Animaciones (GeckoLib)
    // ══════════════════════════════════════════

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "ublabla_controller", 0, this::animationPredicate));
    }

    private <T extends GeoAnimatable> PlayState animationPredicate(AnimationState<T> state) {
        int s = getState();
        switch (s) {
            case STATE_ATTACKING -> state.setAnimation(ATTACK);
            case STATE_CHASING -> state.setAnimation(CHASE_ANIM);
            case STATE_ALERT, STATE_CHECKING -> state.setAnimation(CHECK);
            case STATE_INVESTIGATING -> state.setAnimation(state.isMoving() ? WALK : CHECK);
            default -> state.setAnimation(state.isMoving() ? WALK : IDLE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    // ══════════════════════════════════════════
    // Tick principal (máquina de estados)
    // ══════════════════════════════════════════

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        decrementCooldowns();

        switch (getState()) {
            case STATE_PATROL -> tickPatrol();
            case STATE_ALERT -> tickAlert();
            case STATE_INVESTIGATING -> tickInvestigating();
            case STATE_CHECKING -> tickChecking();
            case STATE_CHASING -> tickChasing();
            // STATE_ATTACKING es transitorio, no necesita tick
        }
    }

    private void decrementCooldowns() {
        if (noiseCheckCooldown > 0) noiseCheckCooldown--;
        if (chaseMessageCooldown > 0) chaseMessageCooldown--;
    }

    // ── Estado: PATROL ──
    private void tickPatrol() {
        if (noiseCheckCooldown > 0) return;

        noiseCheckCooldown = NOISE_CHECK_INTERVAL;
        float noise = getHighestGroupNoise();

        if (noise > NOISE_THRESHOLD) {
            setState(STATE_ALERT);
            alertTimer = 0;
            broadcastMessage("¿Qué ha sido eso?");
        }
    }

    // ── Estado: ALERT ──
    private void tickAlert() {
        alertTimer++;
        int remainingSeconds = (ALERT_DURATION - alertTimer) / 20;

        if (remainingSeconds > 0) {
            showActionBarMessage("§eUblabla viene en camino... §c" + remainingSeconds + "s");
        } else {
            showActionBarMessage("§c¡Ublabla está aquí!");
        }

        if (alertTimer < ALERT_DURATION) return;

        if (investigationTarget != null) {
            setState(STATE_INVESTIGATING);
            investigationTimer = 0;
            navigateTo(investigationTarget);
        } else {
            broadcastMessage("No sé a dónde ir...");
            resetToPatrol();
        }
    }

    // ── Estado: INVESTIGATING ──
    private void tickInvestigating() {
        investigationTimer++;

        if (investigationTarget != null &&
                this.position().distanceToSqr(Vec3.atCenterOf(investigationTarget)) <= 2.25) {
            setState(STATE_CHECKING);
            getNavigation().stop();
            checkWaitTimer = CHECK_WAIT_DURATION;
            broadcastMessage("Voy a revisar esto con atención...");
            return;
        }

        if (investigationTimer > INVESTIGATION_TIMEOUT) {
            broadcastMessage("No encuentro el lugar... mejor me retiro.");
            resetToPatrol();
        }
    }

    // ── Estado: CHECKING ──
    private void tickChecking() {
        if (checkWaitTimer > 0) {
            checkWaitTimer--;
            return;
        }

        Optional<String> anomalyMessage = detectAnomalies();

        if (anomalyMessage.isPresent()) {
            broadcastMessage(anomalyMessage.get());

            if (areAllPlayersInsideJail()) {
                // Jugadores dentro de la jaula pero hay anomalía → castigo directo
                broadcastMessage("§c¡Sé que han sido ustedes! No escaparán del castigo.");
                punishAndReset();
            } else {
                // Jugadores fuera de la jaula → persecución normal
                startChasing();
            }
        } else {
            broadcastMessage("Solo fue mi imaginación...");
            resetToPatrol();
        }
    }

    private boolean areAllPlayersInsideJail() {
        if (jailMin == null || jailMax == null) return false;

        AABB jailBox = new AABB(jailMin.getCenter(), jailMax.getCenter());
        List<Player> players = level().getEntitiesOfClass(Player.class, buildPatrolArea());

        if (players.isEmpty()) return false;

        for (Player p : players) {
            if (!jailBox.contains(p.position())) {
                return false;
            }
        }
        return true;
    }

    private void punishAndReset() {
        setState(STATE_ATTACKING);
        playPunishSound();
        teleportPlayersToJail();
        resetToPatrol();
    }

    private void playPunishSound() {
        AABB area = buildPatrolArea();
        level().getEntitiesOfClass(Player.class, area).forEach(player -> {
            if (player instanceof ServerPlayer sp) {
                sp.level().playSound(
                        null,                              // null = todos escuchan
                        sp.blockPosition(),
                        SoundEvents.WARDEN_ROAR,           // Sonido template
                        SoundSource.HOSTILE,
                        1.0f,                              // Volumen
                        0.8f                               // Pitch (más grave)
                );
            }
        });
    }


    // ── Estado: CHASING ──
    private void tickChasing() {
        chaseTimer++;

        // Verificar/actualizar objetivo
        if (!hasValidTarget()) {
            Player nearest = level().getNearestPlayer(this, 50.0);
            if (nearest != null) {
                this.setTarget(nearest);
            } else {
                resetToPatrol();
                return;
            }
        }

        // ¿Lo atrapó?
        if (this.getTarget() != null && this.distanceToSqr(this.getTarget()) <= CATCH_DISTANCE_SQ) {
            captureAndReset();
            return;
        }

        int chaseTimerSeconds = (CHASE_MAX_DURATION - chaseTimer) / 20;
        if (chaseTimerSeconds > 0) {
            showActionBarMessage("§eUblabla te atrapara en... §c" + chaseTimerSeconds + "s");
        }

        // ¿Se acabó el tiempo?
        if (chaseTimer >= CHASE_MAX_DURATION) {
            broadcastMessage("§cNo me queda otra opción que utilizar mi arma secreta.");
            captureAndReset();
            return;
        }

        // Mensajes aleatorios durante la persecución
        if (chaseMessageCooldown <= 0 && getRandom().nextInt(60) == 0) {
            broadcastMessage(CHASE_MESSAGES[getRandom().nextInt(CHASE_MESSAGES.length)]);
            chaseMessageCooldown = 80;
        }
    }

    // ══════════════════════════════════════════
    // Detección de anomalías (delegada a PuzzleEntityChecker)
    // ══════════════════════════════════════════

    /**
     * Detecta anomalías en el área de patrulla.
     * Primero comprueba si hay jugadores fuera de la zona segura,
     * luego delega al sistema de checkers registrados.
     *
     * @return mensaje de la anomalía encontrada, o vacío si no hay ninguna
     */
    private Optional<String> detectAnomalies() {
        // 1. Comprobar jugadores fuera de la zona segura
        Optional<String> outsideCheck = checkPlayersOutsideJail();
        if (outsideCheck.isPresent()) {
            return outsideCheck;
        }

        // 2. Delegar al sistema de checkers registrados
        AABB area = buildPatrolArea();
        Optional<PuzzleEntityChecker.AnomalyResult> result =
                PuzzleEntityChecker.findFirstAnomaly(level(), area);

        return result.map(PuzzleEntityChecker.AnomalyResult::message);
    }

    /**
     * Comprueba si algún jugador está fuera de la zona de la cárcel.
     */
    private Optional<String> checkPlayersOutsideJail() {
        if (jailMin == null || jailMax == null) return Optional.empty();

        AABB jailBox = new AABB(jailMin.getCenter(), jailMax.getCenter());
        List<Player> players = level().getEntitiesOfClass(Player.class,
                new AABB(spawnPos).inflate(patrolRadius * 2));

        for (Player p : players) {
            if (!jailBox.contains(p.position())) {
                return Optional.of("§c¡Has salido de la zona segura! Eso no está permitido.");
            }
        }
        return Optional.empty();
    }

    // ══════════════════════════════════════════
    // Acciones de transición
    // ══════════════════════════════════════════

    private void startChasing() {
        setState(STATE_CHASING);
        chaseTimer = 0;
        Player nearest = level().getNearestPlayer(this, 50.0);
        if (nearest != null) {
            this.setTarget(nearest);
        } else {
            resetToPatrol();
        }
    }

    private void captureAndReset() {
        setState(STATE_ATTACKING);
        playPunishSound();
        teleportPlayersToJail();
        resetToPatrol();
    }

    /**
     * Reinicia todos los timers, limpia el objetivo y vuelve al spawn.
     */
    public void resetToPatrol() {
        getNavigation().stop();
        if (spawnPos != null) {
            this.teleportTo(spawnPos.getX() + 0.5, spawnPos.getY() + 0.1, spawnPos.getZ() + 0.5);
        }
        this.setTarget(null);
        setState(STATE_PATROL);
        alertTimer = 0;
        chaseTimer = 0;
        investigationTimer = 0;
        checkWaitTimer = 0;
        broadcastMessage("Volviendo a mi puesto...");
    }

    // ══════════════════════════════════════════
    // Teletransporte
    // ══════════════════════════════════════════

    private void teleportPlayersToJail() {
        AABB area = buildPatrolArea();
        List<Player> players = level().getEntitiesOfClass(Player.class, area);

        BlockPos destination;
        String message;

        if (jailMin != null && jailMax != null) {
            destination = new BlockPos(
                    (jailMin.getX() + jailMax.getX()) / 2,
                    (jailMin.getY() + jailMax.getY()) / 2,
                    (jailMin.getZ() + jailMax.getZ()) / 2
            );
            message = "§cUblabla te ha encerrado en la cárcel.";
        } else {
            destination = spawnPos != null ? spawnPos : BlockPos.containing(this.position());
            message = "§cUblabla te ha devuelto al inicio.";
        }

        for (Player p : players) {
            if (p instanceof ServerPlayer sp) {
                sp.teleportTo(destination.getX() + 0.5, destination.getY() + 1.0, destination.getZ() + 0.5);
                sp.sendSystemMessage(Component.literal(message));
            }
        }
    }

    // ══════════════════════════════════════════
    // Utilidades
    // ══════════════════════════════════════════

    private boolean hasValidTarget() {
        LivingEntity target = this.getTarget();
        return target != null
                && target.isAlive()
                && this.distanceToSqr(target) <= LOSE_TARGET_DISTANCE_SQ;
    }

    private float getHighestGroupNoise() {
        if (patrolCenter == null || level().getServer() == null) return 0;

        AABB area = buildPatrolArea();
        float max = 0;

        for (ServerPlayer player : level().getServer().getPlayerList().getPlayers()) {
            if (area.contains(player.position())) {
                float noise = NoiseDetectionSystem.getNoiseLevel(player.getUUID());
                if (noise > max) max = noise;
            }
        }
        return max;
    }

    private void navigateTo(BlockPos target) {
        if (target == null) return;
        getNavigation().moveTo(target.getX() + 0.5, target.getY() + 0.1, target.getZ() + 0.5, 1.15);
    }

    /**
     * Construye el AABB del área de patrulla centrada en patrolCenter.
     */
    private AABB buildPatrolArea() {
        double cx = patrolCenter != null ? patrolCenter.x : position().x;
        double cy = patrolCenter != null ? patrolCenter.y : position().y;
        double cz = patrolCenter != null ? patrolCenter.z : position().z;

        return new AABB(
                cx - patrolRadius, cy - 30, cz - patrolRadius,
                cx + patrolRadius, cy + 30, cz + patrolRadius
        );
    }

    // ── Mensajes ──

    private void broadcastMessage(String message) {
        AABB area = buildPatrolArea();
        level().getEntitiesOfClass(Player.class, area).forEach(player ->
                player.sendSystemMessage(Component.literal("§6[Ublabla] §f" + message))
        );
    }

    private void showActionBarMessage(String message) {
        AABB area = buildPatrolArea();
        level().getEntitiesOfClass(Player.class, area).forEach(player ->
                player.displayClientMessage(Component.literal(message), true)
        );
    }

    // ══════════════════════════════════════════
    // Goals (IA)
    // ══════════════════════════════════════════

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new UblablaChaseGoal(this, 1.35D));
        this.targetSelector.addGoal(1, new UblablaChaseTargetGoal(this));
    }

    static class UblablaChaseTargetGoal extends NearestAttackableTargetGoal<Player> {
        private final UblablaEntity mob;

        public UblablaChaseTargetGoal(UblablaEntity mob) {
            super(mob, Player.class, 0, true, false, null);
            this.mob = mob;
        }

        @Override
        public boolean canUse() {
            return mob.getState() == STATE_CHASING && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return mob.getState() == STATE_CHASING && super.canContinueToUse();
        }

        @Override
        public void stop() {
            super.stop();
            if (mob.getState() != STATE_CHASING) {
                mob.setTarget(null);
            }
        }
    }

    static class UblablaChaseGoal extends Goal {
        private final UblablaEntity mob;
        private final double speed;

        public UblablaChaseGoal(UblablaEntity mob, double speed) {
            this.mob = mob;
            this.speed = speed;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return mob.getState() == STATE_CHASING
                    && mob.getTarget() != null
                    && mob.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void tick() {
            LivingEntity target = mob.getTarget();
            if (target == null) return;

            // Si está a distancia de captura, el tick principal lo maneja
            if (mob.distanceToSqr(target) <= CATCH_DISTANCE_SQ) return;

            if (!mob.getNavigation().isInProgress()) {
                mob.getNavigation().moveTo(target, speed);
            }
        }
    }

    // ══════════════════════════════════════════
    // NBT (persistencia)
    // ══════════════════════════════════════════

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("State", getState());
        tag.putInt("AlertTimer", alertTimer);
        tag.putInt("CheckWaitTimer", checkWaitTimer);

        saveBlockPos(tag, "Spawn", spawnPos);
        saveBlockPos(tag, "Invest", investigationTarget);
        saveBlockPos(tag, "JailMin", jailMin);
        saveBlockPos(tag, "JailMax", jailMax);

        if (patrolCenter != null) {
            tag.putDouble("PatrolX", patrolCenter.x);
            tag.putDouble("PatrolY", patrolCenter.y);
            tag.putDouble("PatrolZ", patrolCenter.z);
            tag.putDouble("PatrolRadius", patrolRadius);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setState(tag.getInt("State"));
        alertTimer = tag.getInt("AlertTimer");
        checkWaitTimer = tag.getInt("CheckWaitTimer");

        spawnPos = loadBlockPos(tag, "Spawn");
        investigationTarget = loadBlockPos(tag, "Invest");
        jailMin = loadBlockPos(tag, "JailMin");
        jailMax = loadBlockPos(tag, "JailMax");

        if (tag.contains("PatrolX")) {
            patrolCenter = new Vec3(
                    tag.getDouble("PatrolX"),
                    tag.getDouble("PatrolY"),
                    tag.getDouble("PatrolZ")
            );
            patrolRadius = tag.getDouble("PatrolRadius");
        }
    }

    private void saveBlockPos(CompoundTag tag, String prefix, BlockPos pos) {
        if (pos == null) return;
        tag.putInt(prefix + "X", pos.getX());
        tag.putInt(prefix + "Y", pos.getY());
        tag.putInt(prefix + "Z", pos.getZ());
    }

    private BlockPos loadBlockPos(CompoundTag tag, String prefix) {
        if (!tag.contains(prefix + "X")) return null;
        return new BlockPos(
                tag.getInt(prefix + "X"),
                tag.getInt(prefix + "Y"),
                tag.getInt(prefix + "Z")
        );
    }
}
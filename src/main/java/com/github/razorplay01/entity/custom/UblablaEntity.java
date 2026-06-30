package com.github.razorplay01.entity.custom;

import com.github.razorplay01.entity.custom.util.PuzzleEntityChecker;
import com.github.razorplay01.system.NoiseDetectionSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
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

public class UblablaEntity extends PathfinderMob implements GeoEntity {

    private static final EntityDataAccessor<Integer> STATE =
            SynchedEntityData.defineId(UblablaEntity.class, EntityDataSerializers.INT);

    public static final int STATE_PATROL = 0;
    public static final int STATE_ALERT = 1;
    public static final int STATE_INVESTIGATING = 2;
    public static final int STATE_CHASING = 3;
    public static final int STATE_ATTACKING = 4;

    private Vec3 patrolCenter;
    private double patrolRadius = 25.0;
    private BlockPos spawnPos;

    private int noiseCheckCooldown = 0;
    private int investigationTimer = 0;
    private int chaseMessageCooldown = 0;

    private int alertTimer = 0;
    private static final int ALERT_DURATION = 100;

    private Entity targetAnomaly = null;
    private BlockPos investigationPos = null;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.idle");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.walk");
    protected static final RawAnimation CHECK = RawAnimation.begin().thenPlay("animation.check");
    protected static final RawAnimation CHASE_ANIM = RawAnimation.begin().thenLoop("animation.chase");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.ataque");

    public UblablaEntity(EntityType<? extends UblablaEntity> type, Level level) {
        super(type, level);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        this.setPersistenceRequired();
        this.spawnPos = BlockPos.containing(this.position());

        if (getNavigation() instanceof GroundPathNavigation nav) {
            nav.setCanPassDoors(false);
            nav.setCanFloat(true);
        }
    }

    public static AttributeSupplier.Builder setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 90.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, STATE_PATROL);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "ublabla_controller", 0, this::animationPredicate));
    }

    private <T extends GeoAnimatable> PlayState animationPredicate(AnimationState<T> state) {
        int s = getState();

        if (s == STATE_ATTACKING) {
            state.setAnimation(ATTACK);
        } else if (s == STATE_CHASING) {
            state.setAnimation(CHASE_ANIM);
        } else if (s == STATE_ALERT) {
            state.setAnimation(CHECK);
        } else if (s == STATE_INVESTIGATING) {
            if (state.isMoving()) {
                state.setAnimation(WALK);
            } else {
                state.setAnimation(CHECK);
            }
        } else if (state.isMoving()) {
            state.setAnimation(WALK);
        } else {
            state.setAnimation(IDLE);
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    public void setPatrolArea(Vec3 center, double radius) {
        this.patrolCenter = center;
        this.patrolRadius = Math.max(8.0, radius);
        this.spawnPos = BlockPos.containing(center);
    }

    public int getState() {
        return this.entityData.get(STATE);
    }

    private void setState(int state) {
        this.entityData.set(STATE, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        if (noiseCheckCooldown > 0) noiseCheckCooldown--;
        if (chaseMessageCooldown > 0) chaseMessageCooldown--;

        int currentState = getState();

        if (currentState == STATE_PATROL) {
            if (noiseCheckCooldown <= 0) {
                noiseCheckCooldown = 12;
                float noise = getHighestGroupNoise();
                if (noise > 0.72f) {
                    enterAlertState();
                }
            }
        }

        if (currentState == STATE_ALERT) {
            alertTimer++;

            if (noiseCheckCooldown <= 0) {
                noiseCheckCooldown = 12;
                float noise = getHighestGroupNoise();
                if (noise > 0.72f) {
                    enterInvestigatingState();
                    return;
                }
            }

            if (alertTimer >= ALERT_DURATION) {
                broadcastMessage("Habrá sido mi imaginación...");
                setState(STATE_PATROL);
                alertTimer = 0;
            }
        }

        if (currentState == STATE_INVESTIGATING) {
            investigationTimer++;
            if (investigationTimer >= 160) {
                finishInvestigation();
            }
        }

        if (currentState == STATE_CHASING) {
            if (this.getTarget() == null || !this.getTarget().isAlive()
                    || this.distanceToSqr(this.getTarget()) > 2500.0D) {
                Player nearest = level().getNearestPlayer(this, 50.0D);
                if (nearest != null) {
                    this.setTarget(nearest);
                } else {
                    this.setTarget(null);
                    setState(STATE_PATROL);
                    return;
                }
            }

            if (chaseMessageCooldown <= 0 && getRandom().nextInt(60) == 0) {
                sendChaseMessage();
                chaseMessageCooldown = 80;
            }
        }
    }

    private float getHighestGroupNoise() {
        if (patrolCenter == null || level().getServer() == null) return 0;

        AABB area = new AABB(
                patrolCenter.x - patrolRadius, patrolCenter.y - 30, patrolCenter.z - patrolRadius,
                patrolCenter.x + patrolRadius, patrolCenter.y + 30, patrolCenter.z + patrolRadius
        );

        float max = 0;
        for (ServerPlayer player : level().getServer().getPlayerList().getPlayers()) {
            if (area.contains(player.position())) {
                float noise = NoiseDetectionSystem.getNoiseLevel(player.getUUID());
                if (noise > max) max = noise;
            }
        }
        return max;
    }

    private void enterAlertState() {
        setState(STATE_ALERT);
        alertTimer = 0;
        broadcastMessage("¿Qué ha sido eso?");
    }

    private void enterInvestigatingState() {
        setState(STATE_INVESTIGATING);
        alertTimer = 0;
        investigationTimer = 0;

        broadcastMessage("Ahora iré a revisarlo...");
        broadcastMessage("Será mejor que todo esté en su lugar.");

        findNearestAnomaly();

        if (investigationPos == null) {
            broadcastMessage("No encuentro nada fuera de lugar...");
            setState(STATE_PATROL);
        }
    }

    private void findNearestAnomaly() {
        investigationPos = null;
        targetAnomaly = null;

        if (patrolCenter == null) return;

        AABB area = new AABB(
                patrolCenter.x - patrolRadius, patrolCenter.y - 20, patrolCenter.z - patrolRadius,
                patrolCenter.x + patrolRadius, patrolCenter.y + 20, patrolCenter.z + patrolRadius
        );

        targetAnomaly = level().getEntities(this, area).stream()
                .filter(e -> PuzzleEntityChecker.hasAnyAnomaly(level(), e.getBoundingBox().inflate(2)))
                .min((a, b) -> Double.compare(a.distanceTo(this), b.distanceTo(this)))
                .orElse(null);

        if (targetAnomaly != null) {
            investigationPos = targetAnomaly.blockPosition();
        }
    }

    private void finishInvestigation() {
        if (targetAnomaly != null && targetAnomaly.isAlive() && distanceTo(targetAnomaly) < 6.0F) {
            setState(STATE_CHASING);
            broadcastMessage("§c¡Esto no debería estar así!");

            Player nearest = level().getNearestPlayer(this, 50.0D);
            if (nearest != null) {
                this.setTarget(nearest);
            }
        } else {
            broadcastMessage("Solo fue mi imaginación...");
            setState(STATE_PATROL);
        }

        targetAnomaly = null;
        investigationPos = null;
        investigationTimer = 0;
    }

    private void sendChaseMessage() {
        String[] messages = {
                "¡No huyas!",
                "¡Vuelve aquí!",
                "No te haré nada... malo.",
                "¡Todo debe estar en su lugar!"
        };
        broadcastMessage(messages[getRandom().nextInt(messages.length)]);
    }

    private void broadcastMessage(String message) {
        AABB area = new AABB(
                patrolCenter != null ? patrolCenter.x - patrolRadius : position().x - 40,
                patrolCenter != null ? patrolCenter.y - 30 : position().y - 30,
                patrolCenter != null ? patrolCenter.z - patrolRadius : position().z - 40,
                patrolCenter != null ? patrolCenter.x + patrolRadius : position().x + 40,
                patrolCenter != null ? patrolCenter.y + 30 : position().y + 30,
                patrolCenter != null ? patrolCenter.z + patrolRadius : position().z + 40
        );

        level().getEntitiesOfClass(Player.class, area).forEach(player ->
                player.sendSystemMessage(Component.literal("§6[Ublabla] §f" + message))
        );
    }

    private void teleportPlayersToSpawn() {
        AABB area = new AABB(
                patrolCenter != null ? patrolCenter.x - patrolRadius : position().x - 40,
                patrolCenter != null ? patrolCenter.y - 30 : position().y - 30,
                patrolCenter != null ? patrolCenter.z - patrolRadius : position().z - 40,
                patrolCenter != null ? patrolCenter.x + patrolRadius : position().x + 40,
                patrolCenter != null ? patrolCenter.y + 30 : position().y + 30,
                patrolCenter != null ? patrolCenter.z + patrolRadius : position().z + 40
        );

        List<Player> players = level().getEntitiesOfClass(Player.class, area);
        BlockPos safeSpawn = getSafeSpawnPosition();

        for (Player p : players) {
            if (p instanceof ServerPlayer sp) {
                BlockPos tpPos = sp.getRespawnPosition() != null ? sp.getRespawnPosition() : safeSpawn;
                if (tpPos == null) tpPos = BlockPos.containing(this.position());

                sp.teleportTo(tpPos.getX() + 0.5, tpPos.getY() + 1.2, tpPos.getZ() + 0.5);
                sp.sendSystemMessage(Component.literal("§cUblabla te ha devuelto al inicio."));
            }
        }

        this.setTarget(null);
        setState(STATE_PATROL);
    }

    private BlockPos getSafeSpawnPosition() {
        if (spawnPos != null) return spawnPos;
        if (patrolCenter != null) return BlockPos.containing(patrolCenter);
        return BlockPos.containing(this.position());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new UblablaChaseGoal(this, 1.35D));
        this.goalSelector.addGoal(3, new InvestigateAnomalyGoal(this, 1.15D));
        this.goalSelector.addGoal(4, new UblablaPatrolGoal(this, 1.0D));
        this.targetSelector.addGoal(1, new UblablaChaseTargetGoal(this));
    }

    static class UblablaPatrolGoal extends WaterAvoidingRandomStrollGoal {
        private final UblablaEntity mob;

        public UblablaPatrolGoal(UblablaEntity mob, double speed) {
            super(mob, speed, 0.75F);
            this.mob = mob;
        }

        @Override
        public boolean canUse() {
            return mob.getState() == STATE_PATROL && mob.patrolCenter != null && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return mob.getState() == STATE_PATROL && super.canContinueToUse();
        }
    }

    static class InvestigateAnomalyGoal extends Goal {
        private final UblablaEntity mob;
        private final double speed;

        public InvestigateAnomalyGoal(UblablaEntity mob, double speed) {
            this.mob = mob;
            this.speed = speed;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return mob.getState() == STATE_INVESTIGATING && mob.investigationPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            return mob.getState() == STATE_INVESTIGATING && mob.investigationPos != null;
        }

        @Override
        public void start() {
            moveToTarget();
        }

        @Override
        public void tick() {
            if (mob.investigationPos == null) return;

            if (mob.position().distanceToSqr(Vec3.atCenterOf(mob.investigationPos)) <= 9.0D) {
                mob.getNavigation().stop();
            }
        }

        private void moveToTarget() {
            Path path = mob.getNavigation().createPath(mob.investigationPos, 2);
            if (path != null) {
                mob.getNavigation().moveTo(path, speed);
            } else {
                mob.getNavigation().moveTo(
                        mob.investigationPos.getX() + 0.5,
                        mob.investigationPos.getY() + 0.1,
                        mob.investigationPos.getZ() + 0.5,
                        speed
                );
            }
        }
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
            if (mob.getState() != STATE_CHASING) return false;

            if (mob.getTarget() == null) {
                Player nearest = mob.level().getNearestPlayer(mob, 50.0D);
                if (nearest != null) {
                    mob.setTarget(nearest);
                } else {
                    return false;
                }
            }

            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return mob.getState() == STATE_CHASING
                    && mob.getTarget() != null
                    && mob.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = mob.getTarget();
            if (target == null || !target.isAlive()) {
                mob.setState(STATE_PATROL);
                mob.setTarget(null);
                return;
            }

            if (mob.distanceToSqr(target) <= 5.29D) {
                mob.setState(STATE_ATTACKING);
                mob.teleportPlayersToSpawn();
                mob.setTarget(null);
                return;
            }

            if (!mob.getNavigation().isInProgress()) {
                mob.getNavigation().moveTo(target, speed);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("State", getState());
        tag.putInt("AlertTimer", alertTimer);

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

        if (tag.contains("PatrolX")) {
            patrolCenter = new Vec3(
                    tag.getDouble("PatrolX"),
                    tag.getDouble("PatrolY"),
                    tag.getDouble("PatrolZ")
            );
            patrolRadius = tag.getDouble("PatrolRadius");
            spawnPos = BlockPos.containing(patrolCenter);
        }
    }
}
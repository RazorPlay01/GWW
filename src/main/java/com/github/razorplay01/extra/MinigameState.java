package com.github.razorplay01.extra;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class MinigameState {
    private static final double EXTRA_DISTANCE = 3.0;
    private static final int SLIME_SPAWN_DELAY = 20;
    private static final double PUSH_MULTIPLIER = 1.5;

    private final ServerLevel world;
    private final Vec3 center;
    private final double radius;
    private final double radiusSq;
    private final int totalPigs;
    private final double slimeSpeed;

    private int remainingTicks;
    private int pigsSpawned = 0;

    private final Set<UUID> trappedPlayers = new HashSet<>();
    private final Set<UUID> eliminatedPlayers = new HashSet<>();
    private final Set<UUID> activePigs = new HashSet<>();
    private final List<PendingSlime> pendingSlimes = new ArrayList<>();
    private final List<MovingSlime> movingSlimes = new ArrayList<>();

    private static class PendingSlime {
        final UUID pigUUID;
        final Vec3 startPos;
        final Vec3 direction;
        int cooldown;

        PendingSlime(UUID pigUUID, Vec3 startPos, Vec3 direction, int cooldown) {
            this.pigUUID = pigUUID;
            this.startPos = startPos;
            this.direction = direction;
            this.cooldown = cooldown;
        }
    }

    private static class MovingSlime {
        final UUID slimeUUID;
        final UUID pigUUID;
        final Vec3 direction;
        Vec3 position;
        boolean hasEnteredCircle;

        MovingSlime(UUID slimeUUID, UUID pigUUID, Vec3 position, Vec3 direction) {
            this.slimeUUID = slimeUUID;
            this.pigUUID = pigUUID;
            this.position = position;
            this.direction = direction;
            this.hasEnteredCircle = false;
        }
    }

    public MinigameState(ServerLevel world, Vec3 center, double radius, int durationSeconds, int totalPigs, double slimeSpeed) {
        this.world = world;
        this.center = center;
        this.radius = radius;
        this.radiusSq = radius * radius;
        this.remainingTicks = durationSeconds * 20;
        this.totalPigs = totalPigs;
        this.slimeSpeed = slimeSpeed;
    }

    public boolean isActive() {
        return remainingTicks > 0;
    }

    public boolean tick(MinecraftServer server) {
        if (!isActive()) return false;

        remainingTicks--;
        drawParticleCircle();
        enforceBoundary();
        maybeSpawnPig();
        updatePendingSlimes();
        updateMovingSlimes();

        if (remainingTicks == 0) {
            endGame();
            return false;
        }
        return true;
    }

    private void updatePendingSlimes() {
        pendingSlimes.removeIf(p -> {
            if (--p.cooldown <= 0) {
                spawnSlime(p.pigUUID, p.startPos, p.direction);
                return true;
            }
            return false;
        });
    }

    private void updateMovingSlimes() {
        movingSlimes.removeIf(ms -> {
            Vec3 newPos = ms.position.add(ms.direction.scale(slimeSpeed));
            ms.position = newPos;

            Entity slimeEntity = world.getEntity(ms.slimeUUID);
            if (slimeEntity == null) {
                removePigByUUID(ms.pigUUID);
                return true;
            }

            slimeEntity.teleportTo(newPos.x, newPos.y, newPos.z);
            checkCollision(ms, slimeEntity);

            double distToCenter = newPos.distanceTo(center);
            if (!ms.hasEnteredCircle && distToCenter <= radius) {
                ms.hasEnteredCircle = true;
            }

            if (ms.hasEnteredCircle && distToCenter > radius + EXTRA_DISTANCE) {
                removeSlimeAndPig(ms.slimeUUID, ms.pigUUID);
                return true;
            }
            return false;
        });
    }

    private void checkCollision(MovingSlime ms, Entity slime) {
        AABB slimeBox = slime.getBoundingBox();
        for (ServerPlayer player : world.players()) {
            if (!trappedPlayers.contains(player.getUUID()) ||
                    eliminatedPlayers.contains(player.getUUID())) continue;

            if (player.getBoundingBox().intersects(slimeBox)) {
                Vec3 push = ms.direction.scale(slimeSpeed * PUSH_MULTIPLIER);
                player.setDeltaMovement(player.getDeltaMovement().add(push));
                player.hurtMarked = true;
                world.sendParticles(ParticleTypes.SWEEP_ATTACK,
                        player.getX(), player.getY() + 1, player.getZ(), 1, 0, 0, 0, 0);
                break;
            }
        }
    }

    private void eliminatePlayer(ServerPlayer player) {
        if (eliminatedPlayers.add(player.getUUID())) {
            trappedPlayers.remove(player.getUUID());
            player.setGameMode(GameType.SPECTATOR);
            player.sendSystemMessage(Component.literal("§c¡Has salido de la esfera! Pierdes el minijuego."), false);
            world.sendParticles(ParticleTypes.EXPLOSION,
                    player.getX(), player.getY() + 1, player.getZ(), 3, 0.5, 0.5, 0.5, 0);
        }
    }

    private void maybeSpawnPig() {
        if (pigsSpawned >= totalPigs || remainingTicks <= 0) return;

        double spawnChance = (double) (totalPigs - pigsSpawned) / remainingTicks;
        if (Math.random() < spawnChance) {
            spawnPig();
            pigsSpawned++;
        }
    }

    private void spawnPig() {
        double angle = 2 * Math.PI * Math.random();
        double spawnDistance = radius + 0.5 + Math.random() * 0.5;

        Vec3 pigPos = new Vec3(
                center.x + spawnDistance * Math.cos(angle),
                center.y,
                center.z + spawnDistance * Math.sin(angle)
        );

        Vec3 toCenter = center.subtract(pigPos).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-toCenter.x, toCenter.z));
        float deviation = (float) (Math.random() * 40 - 20);
        float finalYaw = yaw + deviation;

        Pig pig = new Pig(EntityType.PIG, world);
        pig.setPos(pigPos.x, pigPos.y, pigPos.z);
        pig.setYRot(finalYaw);
        pig.setYHeadRot(finalYaw);
        pig.yRotO = finalYaw;
        pig.yHeadRotO = finalYaw;
        pig.setNoAi(true);
        pig.setNoGravity(true);
        pig.setInvulnerable(true);
        pig.setPersistenceRequired();
        pig.setSilent(true);
        world.addFreshEntity(pig);

        UUID pigUUID = pig.getUUID();
        activePigs.add(pigUUID);

        Vec3 shootDirection = new Vec3(
                -Math.sin(Math.toRadians(finalYaw)),
                0,
                Math.cos(Math.toRadians(finalYaw))
        ).normalize();

        pendingSlimes.add(new PendingSlime(pigUUID, pigPos, shootDirection, SLIME_SPAWN_DELAY));
    }

    private void spawnSlime(UUID pigUUID, Vec3 startPos, Vec3 direction) {
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));

        Slime slime = new Slime(EntityType.SLIME, world);
        slime.setSize(1, true);
        slime.setPos(startPos.x, startPos.y, startPos.z);
        slime.setYRot(yaw);
        slime.setYHeadRot(yaw);
        slime.yRotO = yaw;
        slime.yHeadRotO = yaw;
        slime.setNoAi(true);
        slime.setNoGravity(true);
        slime.setInvulnerable(true);
        slime.setPersistenceRequired();
        slime.setSilent(true);

        world.addFreshEntity(slime);
        movingSlimes.add(new MovingSlime(slime.getUUID(), pigUUID, startPos, direction));
    }

    private void removeSlimeAndPig(UUID slimeUUID, UUID pigUUID) {
        removeEntity(slimeUUID);
        removePigByUUID(pigUUID);
    }

    private void removePigByUUID(UUID pigUUID) {
        removeEntity(pigUUID);
        activePigs.remove(pigUUID);
    }

    private void removeEntity(UUID uuid) {
        Entity entity = world.getEntity(uuid);
        if (entity != null) entity.discard();
    }

    public void endGame() {
        cleanupAllEntities();

        for (ServerPlayer player : world.players()) {
            if (!eliminatedPlayers.contains(player.getUUID())) {
                player.sendSystemMessage(Component.literal("⏰ ¡Minijuego terminado! Has sobrevivido."), false);
            }
            if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                player.setGameMode(GameType.SURVIVAL);
            }
        }

        trappedPlayers.clear();
        eliminatedPlayers.clear();
    }

    private void cleanupAllEntities() {
        activePigs.forEach(this::removeEntity);
        movingSlimes.forEach(ms -> removeEntity(ms.slimeUUID));

        activePigs.clear();
        movingSlimes.clear();
        pendingSlimes.clear();
    }

    private void drawParticleCircle() {
        int points = Math.max(40, (int) (radius * 2 * Math.PI / 0.3));
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);
            world.sendParticles(ParticleTypes.END_ROD, x, center.y, z, 1, 0, 0, 0, 0);
        }
    }

    private void enforceBoundary() {
        for (ServerPlayer player : world.players()) {
            if (eliminatedPlayers.contains(player.getUUID())) continue;

            boolean isInside = player.position().distanceToSqr(center) <= radiusSq;
            UUID playerUUID = player.getUUID();

            if (!isInside) {
                if (trappedPlayers.contains(playerUUID)) {
                    eliminatePlayer(player);
                }
            } else if (trappedPlayers.add(playerUUID)) {
                player.sendSystemMessage(Component.literal("§a¡Has entrado en la zona de juego! No podrás salir."), false);
            }
        }
    }

    public Vec3 getCenter() {
        return center;
    }
    public ServerLevel getWorld() {
        return world;
    }
}
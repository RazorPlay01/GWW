package com.github.razorplay01.system;

import com.github.razorplay01.GWW;
import com.github.razorplay01.api.noise.NoiseAPI;
import com.github.razorplay01.network.FabricCustomPayload;
import com.github.razorplay01.network.packet.NoisePacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NoiseDetectionSystem {

    private static final Map<UUID, PlayerNoiseData> PLAYER_NOISE_DATA = new HashMap<>();
    private static final Map<UUID, UUID> PLAYER_GROUPS = new HashMap<>(); // player -> groupLeader
    private static final float DECAY_RATE = 0.015f;
    private static final float MAX_NOISE = 1.0f;

    public static class NoiseConfig {
        public static final float WALKING_BASE = 0.1f;
        public static final float SNEAKING_BASE = 0.05f;
        public static final float SPRINTING_BASE = 0.2f;

        public static final float SURFACE_MULTIPLIER_SOFT = 0.7f;
        public static final float SURFACE_MULTIPLIER_NORMAL = 1.0f;
        public static final float SURFACE_MULTIPLIER_HARD = 1.3f;
        public static final float SURFACE_MULTIPLIER_LOUD = 1.6f;

        public static final float JUMPING = 0.25f;
        public static final float LANDING_SOFT = 0.2f;
        public static final float LANDING_NORMAL = 0.4f;
        public static final float LANDING_HARD = 0.7f;
        public static final float BLOCK_BREAK = 0.5f;
        public static final float BLOCK_PLACE = 0.4f;
        public static final float DAMAGE_TAKEN = 0.6f;
        public static final float ATTACK = 0.35f;
        public static final float ITEM_USE = 0.25f;

        public static final double MIN_MOVEMENT_SPEED = 0.001;
        public static final double SPRINT_THRESHOLD = 0.1;
    }

    public static PlayerNoiseData getPlayerData(UUID playerId) {
        return PLAYER_NOISE_DATA.computeIfAbsent(playerId, id -> new PlayerNoiseData());
    }

    private static UUID getGroupLeader(UUID playerId) {
        return PLAYER_GROUPS.getOrDefault(playerId, playerId);
    }

    public static String getGroupId(UUID playerId) {
        return getGroupLeader(playerId).toString().substring(0, 8);
    }

    public static void addNoise(ServerPlayer player, float amount) {
        if (player == null) return;
        UUID leaderId = getGroupLeader(player.getUUID());
        PlayerNoiseData data = getPlayerData(leaderId);
        if (!data.isEnabled()) return;

        amount *= data.getMultiplier();
        float newNoise = Math.min(data.getCurrentNoise() + amount, MAX_NOISE);
        data.setCurrentNoise(newNoise);
        data.setLastNoiseTime(System.currentTimeMillis());

        syncGroupToClients(leaderId);
    }

    public static void tick(ServerPlayer player) {
        PlayerNoiseData data = getPlayerData(player.getUUID());
        if (!data.isEnabled()) return;

        detectMovementNoise(player, data);
        applyGroupDecay(getGroupLeader(player.getUUID()));

        data.setLastPosition(player.position());
        data.setWasOnGround(player.onGround());
        data.setWasSneaking(player.isCrouching());
        data.setWasSprinting(player.isSprinting());
        data.setTicksSinceLastStep(data.getTicksSinceLastStep() + 1);

        if (player.tickCount % 5 == 0) {
            syncGroupToClients(getGroupLeader(player.getUUID()));
        }
    }

    private static void applyGroupDecay(UUID leaderId) {
        PlayerNoiseData data = getPlayerData(leaderId);
        if (data.getCurrentNoise() > 0) {
            data.setCurrentNoise(Math.max(0, data.getCurrentNoise() - DECAY_RATE));
        }
    }

    private static void detectMovementNoise(ServerPlayer player, PlayerNoiseData data) {
        Vec3 currentPos = player.position();
        Vec3 lastPos = data.getLastPosition();

        if (lastPos.equals(Vec3.ZERO)) {
            data.setLastPosition(currentPos);
            return;
        }

        double deltaX = currentPos.x - lastPos.x;
        double deltaZ = currentPos.z - lastPos.z;
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (horizontalDistance < NoiseConfig.MIN_MOVEMENT_SPEED) {
            return;
        }

        if (player.onGround()) {
            handleFootsteps(player, data, horizontalDistance);
        }

        if (player.onGround() && !data.isWasOnGround()) {
            handleLanding(player, data);
        }

        if (!player.onGround() && data.isWasOnGround()) {
            handleJump(player, data);
        }
    }

    private static void handleFootsteps(ServerPlayer player, PlayerNoiseData data, double distance) {
        data.setAccumulatedDistance(data.getAccumulatedDistance() + (float) distance);
        float requiredDistance = data.getStepDistance();

        if (data.getAccumulatedDistance() >= requiredDistance) {
            float noiseAmount = calculateFootstepNoise(player, data);
            addNoise(player, noiseAmount);
            data.setAccumulatedDistance(0);
            data.setTicksSinceLastStep(0);
        }
    }

    private static float calculateFootstepNoise(ServerPlayer player, PlayerNoiseData data) {
        float baseNoise = player.isSprinting() ? NoiseConfig.SPRINTING_BASE :
                player.isCrouching() ? NoiseConfig.SNEAKING_BASE : NoiseConfig.WALKING_BASE;

        float surfaceMultiplier = getSurfaceMultiplier(player);
        float speedMultiplier = getSpeedMultiplier(player);

        return baseNoise * surfaceMultiplier * speedMultiplier;
    }

    private static float getSurfaceMultiplier(ServerPlayer player) {
        BlockPos blockBelow = player.blockPosition().below();
        BlockState blockState = player.level().getBlockState(blockBelow);
        Block block = blockState.getBlock();

        float customNoise = NoiseAPI.getBlockNoise(block);
        if (customNoise > 0) return customNoise * 2;

        SoundType soundType = blockState.getSoundType();

        if (soundType == SoundType.WOOL || soundType == SoundType.MOSS_CARPET)
            return NoiseConfig.SURFACE_MULTIPLIER_SOFT;
        if (soundType == SoundType.GRAVEL || soundType == SoundType.SNOW || soundType == SoundType.SAND || soundType == SoundType.SOUL_SAND)
            return NoiseConfig.SURFACE_MULTIPLIER_LOUD;
        if (soundType == SoundType.METAL || soundType == SoundType.CHAIN || soundType == SoundType.ANVIL)
            return NoiseConfig.SURFACE_MULTIPLIER_HARD * 1.2f;
        if (soundType == SoundType.WOOD || soundType == SoundType.BAMBOO_WOOD)
            return NoiseConfig.SURFACE_MULTIPLIER_HARD;

        return NoiseConfig.SURFACE_MULTIPLIER_NORMAL;
    }

    private static float getSpeedMultiplier(ServerPlayer player) {
        Vec3 delta = player.getDeltaMovement();
        double speed = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        if (speed > NoiseConfig.SPRINT_THRESHOLD * 1.5) return 1.3f;
        if (speed > NoiseConfig.SPRINT_THRESHOLD) return 1.15f;
        return 1.0f;
    }

    private static void handleLanding(ServerPlayer player, PlayerNoiseData data) {
        double fallSpeed = Math.abs(player.getDeltaMovement().y);
        float landingNoise = fallSpeed < 0.3 ? NoiseConfig.LANDING_SOFT :
                fallSpeed < 0.6 ? NoiseConfig.LANDING_NORMAL : NoiseConfig.LANDING_HARD;

        landingNoise *= getSurfaceMultiplier(player);
        if (fallSpeed >= 0.6) landingNoise *= Math.min(2.0f, (float) fallSpeed);

        addNoise(player, landingNoise);
    }

    private static void handleJump(ServerPlayer player, PlayerNoiseData data) {
        float jumpNoise = NoiseConfig.JUMPING;
        if (player.isSprinting()) jumpNoise *= 1.5f;
        else if (player.isCrouching()) jumpNoise *= 0.5f;
        addNoise(player, jumpNoise);
    }

    public static void onBlockBreak(ServerPlayer player, BlockState state) {
        float noise = NoiseConfig.BLOCK_BREAK;
        SoundType st = state.getSoundType();
        if (st == SoundType.GLASS) noise *= 1.5f;
        else if (st == SoundType.METAL || st == SoundType.ANVIL) noise *= 1.3f;
        else if (st == SoundType.WOOL) noise *= 0.5f;
        addNoise(player, noise);
    }

    public static void onBlockPlace(ServerPlayer player, BlockState state) {
        float noise = NoiseConfig.BLOCK_PLACE;
        SoundType st = state.getSoundType();
        if (st == SoundType.METAL || st == SoundType.ANVIL) noise *= 1.4f;
        else if (st == SoundType.WOOL || st == SoundType.MOSS_CARPET) noise *= 0.6f;
        addNoise(player, noise);
    }

    public static void onDamageTaken(ServerPlayer player, float damage) {
        float noise = NoiseConfig.DAMAGE_TAKEN * Math.min(2.0f, damage / 10f);
        addNoise(player, noise);
    }

    public static void onAttack(ServerPlayer player) {
        float noise = NoiseConfig.ATTACK;
        if (player.isSprinting()) noise *= 1.3f;
        addNoise(player, noise);
    }

    public static void onItemUse(ServerPlayer player, ItemStack stack) {
        float itemNoise = NoiseAPI.getItemNoise(stack.getItem());
        addNoise(player, itemNoise > 0 ? itemNoise : NoiseConfig.ITEM_USE);
    }

    public static void toggleSystem(ServerPlayer player, boolean enabled) {
        UUID leader = getGroupLeader(player.getUUID());
        PlayerNoiseData data = getPlayerData(leader);
        data.setEnabled(enabled);
        if (!enabled) data.reset();
        syncGroupToClients(leader);
    }

    private static void syncGroupToClients(UUID leaderId) {
        PlayerNoiseData data = getPlayerData(leaderId);
        NoisePacket packet = new NoisePacket(data.getCurrentNoise(), DECAY_RATE, data.isEnabled());

        // Sincronizar a todos los miembros del grupo
        for (Map.Entry<UUID, UUID> entry : PLAYER_GROUPS.entrySet()) {
            if (entry.getValue().equals(leaderId)) {
                ServerPlayer member = getPlayerByUUID(entry.getKey());
                if (member != null) {
                    ServerPlayNetworking.send(member, new FabricCustomPayload(packet));
                }
            }
        }
        // También al líder
        ServerPlayer leader = getPlayerByUUID(leaderId);
        if (leader != null) {
            ServerPlayNetworking.send(leader, new FabricCustomPayload(packet));
        }
    }

    private static ServerPlayer getPlayerByUUID(UUID uuid) {
        return GWW.server.getPlayerList().getPlayer(uuid);
    }

    public static void linkPlayers(UUID p1, UUID p2) {
        UUID leader = getGroupLeader(p1);
        PLAYER_GROUPS.put(p2, leader);
        PlayerNoiseData d1 = getPlayerData(leader);
        PlayerNoiseData d2 = getPlayerData(p2);
        d2.setCurrentNoise(d1.getCurrentNoise());
        d2.setEnabled(d1.isEnabled());
    }

    public static void unlinkPlayer(UUID playerId) {
        PLAYER_GROUPS.remove(playerId);
        getPlayerData(playerId).setGroupLeader(null);
    }

    public static int linkPlayersInArea(ServerLevel level, BlockPos center, int radius) {
        int count = 0;
        UUID first = null;
        for (ServerPlayer p : level.players()) {
            if (p.blockPosition().distSqr(center) <= radius * radius) {
                if (first == null) {
                    first = p.getUUID();
                } else {
                    linkPlayers(first, p.getUUID());
                }
                count++;
            }
        }
        return count;
    }

    public static boolean isEnabledFor(UUID playerId) {
        return getPlayerData(getGroupLeader(playerId)).isEnabled();
    }

    public static float getNoiseLevel(UUID playerId) {
        return getPlayerData(getGroupLeader(playerId)).getCurrentNoise();
    }

    public static void removePlayer(UUID playerId) {
        PLAYER_NOISE_DATA.remove(playerId);
        PLAYER_GROUPS.remove(playerId);
    }

    public static void clearAll() {
        PLAYER_NOISE_DATA.clear();
        PLAYER_GROUPS.clear();
    }
}
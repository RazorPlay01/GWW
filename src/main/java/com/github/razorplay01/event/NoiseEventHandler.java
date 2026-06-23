package com.github.razorplay01.event;

import com.github.razorplay01.system.NoiseDetectionSystem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public class NoiseEventHandler {

    public static void register() {

        // Tick del servidor para actualizar ruido de todos los jugadores
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                NoiseDetectionSystem.tick(player);
            }
        });

        // Romper bloques
        /*PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                NoiseDetectionSystem.onBlockBreak(serverPlayer, state);
            }
        });*/

        // Colocar bloques
        /*UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player instanceof ServerPlayer serverPlayer && !world.isClientSide()) {
                var blockState = world.getBlockState(hitResult.getBlockPos());
                NoiseDetectionSystem.onBlockPlace(serverPlayer, blockState);
            }
            return InteractionResult.PASS;
        });*/

        // Atacar entidades
        /*AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                NoiseDetectionSystem.onAttack(serverPlayer);
            }
            return InteractionResult.PASS;
        });*/

        // Usar items
        /*UseItemCallback.EVENT.register((player, world, hand) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                NoiseDetectionSystem.onItemUse(serverPlayer, player.getItemInHand(hand));
            }
            return InteractionResult.PASS;
        });*/
    }
}
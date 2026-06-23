package com.github.razorplay01.command;

import com.github.razorplay01.system.NoiseDetectionSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class NoiseCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("noise")
                        .then(Commands.literal("toggle")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            boolean enabled = BoolArgumentType.getBool(context, "enabled");

                                            NoiseDetectionSystem.toggleSystem(player, enabled);

                                            player.sendSystemMessage(Component.literal(
                                                    "Sistema de ruido " + (enabled ? "activado" : "desactivado")
                                            ));

                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("status")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    var data = NoiseDetectionSystem.getPlayerData(player.getUUID());

                                    player.sendSystemMessage(Component.literal(
                                            String.format("Ruido actual: %.2f%% | Estado: %s",
                                                    data.getCurrentNoise() * 100,
                                                    data.isEnabled() ? "Activo" : "Inactivo")
                                    ));

                                    return 1;
                                })
                        )
        );
    }
}
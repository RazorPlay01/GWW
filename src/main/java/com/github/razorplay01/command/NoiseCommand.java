package com.github.razorplay01.command;

import com.github.razorplay01.system.NoiseDetectionSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Collections;

public class NoiseCommand {
    private NoiseCommand() {
        /* This utility class should not be instantiated */
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("noise")
                        // Toggle simple (sin true/false)
                        .then(Commands.literal("toggle")
                                .executes(context -> toggleNoise(context, null)) // self
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> toggleNoise(context, EntityArgument.getPlayers(context, "targets")))
                                )
                        )
                        // Status
                        .then(Commands.literal("status")
                                .executes(context -> showStatus(context, null))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> showStatus(context, EntityArgument.getPlayers(context, "targets")))
                                )
                        )
                        // Link players
                        .then(Commands.literal("link")
                                .then(Commands.argument("player1", EntityArgument.player())
                                        .then(Commands.argument("player2", EntityArgument.player())
                                                .executes(NoiseCommand::linkPlayers)
                                        )
                                )
                        )
                        // Unlink
                        .then(Commands.literal("unlink")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(NoiseCommand::unlinkPlayer)
                                )
                        )
                        // Link by area (coordenadas + radio)
                        .then(Commands.literal("linkarea")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 100))
                                                .executes(NoiseCommand::linkArea)
                                        )
                                )
                        )
        );
    }

    private static int toggleNoise(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets) throws CommandSyntaxException {
        if (targets == null) {
            ServerPlayer player = context.getSource().getPlayerOrException();
            targets = Collections.singletonList(player);
        }

        boolean newState = !NoiseDetectionSystem.isEnabledFor(targets.iterator().next().getUUID()); // toggle basado en el primero

        for (ServerPlayer p : targets) {
            NoiseDetectionSystem.toggleSystem(p, newState);
            p.sendSystemMessage(Component.literal("§aSistema de ruido " + (newState ? "§2activado" : "§cdesactivado")));
        }
        return 1;
    }

    private static int showStatus(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets) throws CommandSyntaxException {
        if (targets == null) {
            ServerPlayer player = context.getSource().getPlayerOrException();
            targets = Collections.singletonList(player);
        }

        for (ServerPlayer p : targets) {
            var data = NoiseDetectionSystem.getPlayerData(p.getUUID());
            p.sendSystemMessage(Component.literal(
                    String.format("§eRuido actual: §b%.2f%% §7| §eEstado: §f%s §7| §eGrupo: §f%s",
                            data.getCurrentNoise() * 100,
                            data.isEnabled() ? "Activo" : "Inactivo",
                            NoiseDetectionSystem.getGroupId(p.getUUID())
                    )
            ));
        }
        return 1;
    }

    private static int linkPlayers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer p1 = EntityArgument.getPlayer(context, "player1");
        ServerPlayer p2 = EntityArgument.getPlayer(context, "player2");
        NoiseDetectionSystem.linkPlayers(p1.getUUID(), p2.getUUID());
        context.getSource().sendSuccess(() -> Component.literal("§aJugadores vinculados en el mismo grupo de ruido"), false);
        return 1;
    }

    private static int unlinkPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(context, "player");
        NoiseDetectionSystem.unlinkPlayer(p.getUUID());
        context.getSource().sendSuccess(() -> Component.literal("§aJugador desvinculado del grupo"), false);
        return 1;
    }

    private static int linkArea(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        int radius = IntegerArgumentType.getInteger(context, "radius");

        int count = NoiseDetectionSystem.linkPlayersInArea(context.getSource().getLevel(), pos, radius);
        context.getSource().sendSuccess(() -> Component.literal("§aVinculados " + count + " jugadores en el área"), false);
        return 1;
    }
}
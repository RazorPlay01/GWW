package com.github.razorplay01.command;

import com.github.razorplay01.entity.custom.Cuadro1Entity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class CuadroCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cuadro")
                .requires(source -> source.hasPermission(2)) // Nivel de operador 2

                // Comando para establecer posición inicial actual
                .then(Commands.literal("setinitial")
                        .then(Commands.argument("cuadro", EntityArgument.entity())
                                .executes(CuadroCommand::setInitialFromCurrent)
                        )
                )

                // Comando para establecer posición inicial con coordenadas
                .then(Commands.literal("setinitialpos")
                        .then(Commands.argument("cuadro", EntityArgument.entity())
                                .then(Commands.argument("x", FloatArgumentType.floatArg())
                                        .then(Commands.argument("y", FloatArgumentType.floatArg())
                                                .then(Commands.argument("z", FloatArgumentType.floatArg())
                                                        .executes(CuadroCommand::setInitialPosition)
                                                )
                                        )
                                )
                        )
                )

                // Comando para establecer rotación inicial
                .then(Commands.literal("setinitialrot")
                        .then(Commands.argument("cuadro", EntityArgument.entity())
                                .then(Commands.argument("yaw", FloatArgumentType.floatArg())
                                        .then(Commands.argument("pitch", FloatArgumentType.floatArg())
                                                .executes(CuadroCommand::setInitialRotation)
                                        )
                                )
                        )
                )

                // Comando para resetear el estado del puzzle
                .then(Commands.literal("reset")
                        .then(Commands.argument("cuadro", EntityArgument.entity())
                                .executes(context -> resetPuzzle(context))
                        )
                )

                // Comando para teletransportar el cuadro a su posición inicial
                .then(Commands.literal("teleportinitial")
                        .then(Commands.argument("cuadro", EntityArgument.entity())
                                .executes(context -> teleportToInitial(context))
                        )
                )

                // Comando para verificar si está bien colocado
                .then(Commands.literal("check")
                        .then(Commands.argument("cuadro", EntityArgument.entity())
                                .executes(context -> checkPlacement(context))
                        )
                )
        );
    }

    // Establecer posición y rotación inicial desde la posición actual
    private static int setInitialFromCurrent(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "cuadro");

            if (!(entity instanceof Cuadro1Entity cuadro)) {
                context.getSource().sendFailure(Component.literal("§cLa entidad no es un cuadro"));
                return 0;
            }

            cuadro.setInitialPosition(cuadro.getX(), cuadro.getY(), cuadro.getZ());
            cuadro.setInitialRotation(cuadro.getYRot(), cuadro.getXRot());

            context.getSource().sendSuccess(() -> Component.literal(
                    String.format("§aPosición inicial establecida en: §f%.2f, %.2f, %.2f",
                            cuadro.getX(), cuadro.getY(), cuadro.getZ())
            ), true);
            context.getSource().sendSuccess(() -> Component.literal(
                    String.format("§aRotación inicial establecida en: §fYaw: %.1f°, Pitch: %.1f°",
                            cuadro.getYRot(), cuadro.getXRot())
            ), true);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    // Establecer posición inicial con coordenadas específicas
    private static int setInitialPosition(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "cuadro");

            if (!(entity instanceof Cuadro1Entity cuadro)) {
                context.getSource().sendFailure(Component.literal("§cLa entidad no es un cuadro"));
                return 0;
            }

            float x = FloatArgumentType.getFloat(context, "x");
            float y = FloatArgumentType.getFloat(context, "y");
            float z = FloatArgumentType.getFloat(context, "z");

            cuadro.setInitialPosition(x, y, z);

            context.getSource().sendSuccess(() -> Component.literal(
                    String.format("§aPosición inicial establecida en: §f%.2f, %.2f, %.2f", x, y, z)
            ), true);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    // Establecer rotación inicial
    private static int setInitialRotation(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "cuadro");

            if (!(entity instanceof Cuadro1Entity cuadro)) {
                context.getSource().sendFailure(Component.literal("§cLa entidad no es un cuadro"));
                return 0;
            }

            float yaw = FloatArgumentType.getFloat(context, "yaw");
            float pitch = FloatArgumentType.getFloat(context, "pitch");

            cuadro.setInitialRotation(yaw, pitch);

            context.getSource().sendSuccess(() -> Component.literal(
                    String.format("§aRotación inicial establecida en: §fYaw: %.1f°, Pitch: %.1f°", yaw, pitch)
            ), true);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    // Resetear el estado del puzzle
    private static int resetPuzzle(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "cuadro");

            if (!(entity instanceof Cuadro1Entity cuadro)) {
                context.getSource().sendFailure(Component.literal("§cLa entidad no es un cuadro"));
                return 0;
            }

            // Usar reflexión o añadir métodos públicos en Cuadro1Entity
            cuadro.resetPuzzleState();

            context.getSource().sendSuccess(() -> Component.literal(
                    "§aEstado del puzzle reseteado"
            ), true);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    // Teletransportar el cuadro a su posición inicial
    private static int teleportToInitial(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "cuadro");

            if (!(entity instanceof Cuadro1Entity cuadro)) {
                context.getSource().sendFailure(Component.literal("§cLa entidad no es un cuadro"));
                return 0;
            }

            cuadro.setPos(cuadro.getInitialPosition());
            cuadro.setYRot(cuadro.getInitialYaw());
            cuadro.setXRot(cuadro.getInitialPitch());

            context.getSource().sendSuccess(() -> Component.literal(
                    "§aCuadro teletransportado a su posición inicial"
            ), true);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    // Verificar si el cuadro está bien colocado
    private static int checkPlacement(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "cuadro");

            if (!(entity instanceof Cuadro1Entity cuadro)) {
                context.getSource().sendFailure(Component.literal("§cLa entidad no es un cuadro"));
                return 0;
            }

            boolean isCorrect = cuadro.checkIfCorrectlyPlaced();
            double distance = cuadro.getDistanceToInitialPosition();
            float yawDiff = Math.abs(cuadro.getYawDifferenceFromInitial());

            if (isCorrect) {
                context.getSource().sendSuccess(() -> Component.literal(
                        "§a✓ El cuadro está correctamente colocado"
                ), false);
            } else {
                context.getSource().sendFailure(Component.literal(
                        "§c✗ El cuadro NO está correctamente colocado"
                ));
                context.getSource().sendFailure(Component.literal(
                        String.format("§eDistancia: §f%.2f §ebloques (máx: 0.5)", distance)
                ));
                context.getSource().sendFailure(Component.literal(
                        String.format("§eDiferencia de rotación: §f%.1f° §e(máx: 15°)", yawDiff)
                ));
            }

            return isCorrect ? 1 : 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }
}
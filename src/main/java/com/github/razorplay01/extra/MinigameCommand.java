package com.github.razorplay01.extra;

import com.github.razorplay01.GWW;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class MinigameCommand {
    private MinigameCommand() {
        /* This utility class should not be instantiated */
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("cc")
                .requires(source -> source.hasPermission(2)) // requiere operador o nivel 2
                .then(literal("start")
                        .then(argument("centro", Vec3Argument.vec3())
                                .then(argument("tiempo_segundos", IntegerArgumentType.integer(1, 3600))
                                        .then(argument("radio", DoubleArgumentType.doubleArg(1, 100))
                                                .then(argument("dificultad", IntegerArgumentType.integer(1, 1000))
                                                        .then(argument("bulletSpeed", FloatArgumentType.floatArg(0, 1000))
                                                                .executes(context -> {
                                                                    CommandSourceStack source = context.getSource();
                                                                    Vec3 center = Vec3Argument.getVec3(context, "centro");
                                                                    int tiempo = IntegerArgumentType.getInteger(context, "tiempo_segundos");
                                                                    double radio = DoubleArgumentType.getDouble(context, "radio");
                                                                    int dificultad = IntegerArgumentType.getInteger(context, "dificultad");
                                                                    float bulletSpeed = FloatArgumentType.getFloat(context, "bulletSpeed");

                                                                    // Detener cualquier minijuego anterior
                                                                    if (GWW.currentGame != null && GWW.currentGame.isActive()) {
                                                                        source.sendSuccess(() -> Component.literal("⚠️ Ya hay un minijuego activo, se detendrá automáticamente."), false);
                                                                    }

                                                                    GWW.currentGame = new MinigameState(
                                                                            source.getLevel(), center, radio, tiempo, dificultad, bulletSpeed
                                                                    );
                                                                    source.sendSuccess(() -> Component.literal(
                                                                            String.format("✅ Minijuego iniciado en (%.1f, %.1f, %.1f) durante %d segundos, radio %.1f, dificultad '%s'",
                                                                                    center.x, center.y, center.z, tiempo, radio, dificultad)
                                                                    ), false);
                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(literal("stop")
                        .executes(context -> {
                            if (GWW.currentGame != null) {
                                GWW.currentGame.endGame();
                                GWW.currentGame = null;
                                context.getSource().sendSuccess(() -> Component.literal("🛑 Minijuego detenido manualmente"), false);
                            } else {
                                context.getSource().sendSuccess(() -> Component.literal("❌ No hay ningún minijuego activo"), false);
                            }
                            return 1;
                        })
                )
        );
    }
}

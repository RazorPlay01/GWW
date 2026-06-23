package com.github.razorplay01;

import com.github.razorplay01.EscapeRoomManager.EscapeRoomData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EscapeRoomCommands {

    private static final Map<String, EscapeRoomData> loadedRooms = new HashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("escaperoom")
                .requires(source -> source.hasPermission(2))

                // Capturar área
                .then(Commands.literal("capture")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 100))
                                        .executes(context -> {
                                            String name = StringArgumentType.getString(context, "name");
                                            int radius = IntegerArgumentType.getInteger(context, "radius");

                                            CommandSourceStack source = context.getSource();
                                            ServerLevel level = source.getLevel();
                                            BlockPos pos = BlockPos.containing(source.getPosition());

                                            EscapeRoomData data = EscapeRoomManager.captureArea(level, pos, radius, name);
                                            loadedRooms.put(name, data);

                                            source.sendSuccess(() -> Component.literal(
                                                    "§aEscape room '" + name + "' capturado con " +
                                                            data.getEntities().size() + " entidades en " + pos.toShortString()
                                            ), true);

                                            return 1;
                                        })
                                )
                        )
                )

                // Guardar en archivo
                .then(Commands.literal("save")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    CommandSourceStack source = context.getSource();

                                    if (!loadedRooms.containsKey(name)) {
                                        source.sendFailure(Component.literal("§cEscape room '" + name + "' no encontrado"));
                                        return 0;
                                    }

                                    try {
                                        File file = new File("escaperooms/" + name + ".dat");
                                        file.getParentFile().mkdirs();

                                        EscapeRoomManager.saveToFile(loadedRooms.get(name), file);
                                        source.sendSuccess(() -> Component.literal(
                                                "§aEscape room '" + name + "' guardado en archivo"
                                        ), true);
                                        return 1;
                                    } catch (Exception e) {
                                        source.sendFailure(Component.literal("§cError al guardar: " + e.getMessage()));
                                        e.printStackTrace();
                                        return 0;
                                    }
                                })
                        )
                )

                // Cargar desde archivo
                .then(Commands.literal("load")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    CommandSourceStack source = context.getSource();

                                    try {
                                        File file = new File("escaperooms/" + name + ".dat");
                                        if (!file.exists()) {
                                            source.sendFailure(Component.literal("§cArchivo no encontrado: escaperooms/" + name + ".dat"));
                                            return 0;
                                        }

                                        EscapeRoomData data = EscapeRoomManager.loadFromFile(file);
                                        loadedRooms.put(name, data);

                                        source.sendSuccess(() -> Component.literal(
                                                "§aEscape room '" + name + "' cargado desde archivo (" +
                                                        data.getEntities().size() + " entidades)"
                                        ), true);
                                        return 1;
                                    } catch (Exception e) {
                                        source.sendFailure(Component.literal("§cError al cargar: " + e.getMessage()));
                                        e.printStackTrace();
                                        return 0;
                                    }
                                })
                        )
                )

                // Pegar en posición actual del jugador
                .then(Commands.literal("paste")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    CommandSourceStack source = context.getSource();

                                    if (!loadedRooms.containsKey(name)) {
                                        source.sendFailure(Component.literal("§cEscape room '" + name + "' no encontrado. Usa /escaperoom load <name> primero"));
                                        return 0;
                                    }

                                    ServerLevel level = source.getLevel();
                                    BlockPos pos = BlockPos.containing(source.getPosition());

                                    EscapeRoomManager.restoreAt(level, loadedRooms.get(name), pos);

                                    source.sendSuccess(() -> Component.literal(
                                            "§aEscape room '" + name + "' pegado en " + pos.toShortString()
                                    ), true);
                                    return 1;
                                })
                        )
                )

                // NUEVO: Pegar en coordenadas específicas
                .then(Commands.literal("pasteat")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> {
                                            String name = StringArgumentType.getString(context, "name");
                                            CommandSourceStack source = context.getSource();

                                            if (!loadedRooms.containsKey(name)) {
                                                source.sendFailure(Component.literal("§cEscape room '" + name + "' no encontrado. Usa /escaperoom load <name> primero"));
                                                return 0;
                                            }

                                            ServerLevel level = source.getLevel();
                                            BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");

                                            EscapeRoomManager.restoreAt(level, loadedRooms.get(name), pos);

                                            source.sendSuccess(() -> Component.literal(
                                                    "§aEscape room '" + name + "' pegado en " + pos.toShortString()
                                            ), true);
                                            return 1;
                                        })
                                )
                        )
                )

                // Resetear
                .then(Commands.literal("reset")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 100))
                                        .executes(context -> {
                                            String name = StringArgumentType.getString(context, "name");
                                            int radius = IntegerArgumentType.getInteger(context, "radius");
                                            CommandSourceStack source = context.getSource();

                                            if (!loadedRooms.containsKey(name)) {
                                                source.sendFailure(Component.literal("§cEscape room '" + name + "' no encontrado"));
                                                return 0;
                                            }

                                            ServerLevel level = source.getLevel();
                                            EscapeRoomData data = loadedRooms.get(name);
                                            EscapeRoomManager.reset(level, data, radius);

                                            source.sendSuccess(() -> Component.literal(
                                                    "§aEscape room '" + name + "' reseteado en " +
                                                            data.getCenterPos().toShortString()
                                            ), true);
                                            return 1;
                                        })
                                )
                        )
                )

                // Limpiar área
                .then(Commands.literal("clear")
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 100))
                                .executes(context -> {
                                    int radius = IntegerArgumentType.getInteger(context, "radius");
                                    CommandSourceStack source = context.getSource();

                                    ServerLevel level = source.getLevel();
                                    BlockPos pos = BlockPos.containing(source.getPosition());

                                    EscapeRoomManager.clearArea(level, pos, radius);

                                    source.sendSuccess(() -> Component.literal(
                                            "§aÁrea limpiada en " + pos.toShortString() + " (radio: " + radius + ")"
                                    ), true);
                                    return 1;
                                })
                        )
                )

                // NUEVO: Limpiar en coordenadas específicas
                .then(Commands.literal("clearat")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 100))
                                        .executes(context -> {
                                            int radius = IntegerArgumentType.getInteger(context, "radius");
                                            CommandSourceStack source = context.getSource();

                                            ServerLevel level = source.getLevel();
                                            BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");

                                            EscapeRoomManager.clearArea(level, pos, radius);

                                            source.sendSuccess(() -> Component.literal(
                                                    "§aÁrea limpiada en " + pos.toShortString() + " (radio: " + radius + ")"
                                            ), true);
                                            return 1;
                                        })
                                )
                        )
                )

                // Listar escape rooms cargados
                .then(Commands.literal("list")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();

                            if (loadedRooms.isEmpty()) {
                                source.sendSuccess(() -> Component.literal("§eNo hay escape rooms cargados"), false);
                                return 1;
                            }

                            source.sendSuccess(() -> Component.literal("§6=== Escape Rooms Cargados ==="), false);
                            loadedRooms.forEach((name, data) -> {
                                source.sendSuccess(() -> Component.literal(
                                        "§e- " + name + " §7(" + data.getEntities().size() + " entidades, centro: " +
                                                data.getCenterPos().toShortString() + ")"
                                ), false);
                            });

                            return 1;
                        })
                )

                // Información detallada de un escape room
                .then(Commands.literal("info")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    CommandSourceStack source = context.getSource();

                                    if (!loadedRooms.containsKey(name)) {
                                        source.sendFailure(Component.literal("§cEscape room '" + name + "' no encontrado"));
                                        return 0;
                                    }

                                    EscapeRoomData data = loadedRooms.get(name);
                                    source.sendSuccess(() -> Component.literal("§6=== Info: " + name + " ==="), false);
                                    source.sendSuccess(() -> Component.literal("§eCentro: §f" + data.getCenterPos().toShortString()), false);
                                    source.sendSuccess(() -> Component.literal("§eEntidades: §f" + data.getEntities().size()), false);

                                    // Mostrar tipos de entidades
                                    Map<String, Integer> entityTypes = new HashMap<>();
                                    data.getEntities().forEach(snapshot -> {
                                        String type = snapshot.getEntityType();
                                        entityTypes.put(type, entityTypes.getOrDefault(type, 0) + 1);
                                    });

                                    source.sendSuccess(() -> Component.literal("§eDesglose:"), false);
                                    entityTypes.forEach((type, count) -> {
                                        source.sendSuccess(() -> Component.literal("  §7- " + type + ": §f" + count), false);
                                    });

                                    return 1;
                                })
                        )
                )
        );
    }
}
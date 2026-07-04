package com.github.razorplay01;

import com.github.razorplay01.EscapeRoomManager.EscapeRoomData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EscapeRoomCommands {
    private EscapeRoomCommands() {
        /* This utility class should not be instantiated */
    }

    private static final Map<String, EscapeRoomData> loadedRooms = new HashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("escaperoom")
                .requires(source -> source.hasPermission(2))

                // Capturar área
                .then(Commands.literal("capture")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 100))
                                        .executes(EscapeRoomCommands::capture)
                                )
                        )
                )

                // Guardar en archivo
                .then(Commands.literal("save")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(EscapeRoomCommands::save)
                        )
                )

                // Cargar desde archivo
                .then(Commands.literal("load")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(EscapeRoomCommands::load)
                        )
                )

                // Pegar en posición actual del jugador
                .then(Commands.literal("paste")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(EscapeRoomCommands::paste)
                        )
                )

                // Pegar en coordenadas específicas
                .then(Commands.literal("pasteat")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(EscapeRoomCommands::pasteAt)
                                )
                        )
                )

                // Limpiar área
                .then(Commands.literal("clear")
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 100))
                                .executes(EscapeRoomCommands::clear)
                        )
                )

                // Limpiar en coordenadas específicas
                .then(Commands.literal("clearat")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 100))
                                        .executes(EscapeRoomCommands::clearAt)
                                )
                        )
                )

                // Listar escape rooms cargados
                .then(Commands.literal("list")
                        .executes(EscapeRoomCommands::list)
                )

                // Información detallada de un escape room
                .then(Commands.literal("info")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(EscapeRoomCommands::info)
                        )
                )

                // Subcomandos para instancias
                .then(Commands.literal("instance")
                        .then(Commands.literal("add")
                                .then(Commands.argument("roomname", StringArgumentType.string())
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(EscapeRoomCommands::instanceAdd))))
                        .then(Commands.literal("list")
                                .then(Commands.argument("roomname", StringArgumentType.string())
                                        .executes(EscapeRoomCommands::instanceList)))
                        .then(Commands.literal("create")
                                .then(Commands.argument("roomname", StringArgumentType.string())
                                        .then(Commands.argument("id", StringArgumentType.string())
                                                .executes(EscapeRoomCommands::instanceCreate))))
                        .then(Commands.literal("createall")
                                .then(Commands.argument("roomname", StringArgumentType.string())
                                        .executes(EscapeRoomCommands::instanceCreateAll)))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("roomname", StringArgumentType.string())
                                        .then(Commands.argument("id", StringArgumentType.string())
                                                .executes(EscapeRoomCommands::instanceRemove))))
                        .then(Commands.literal("clearall")
                                .then(Commands.argument("roomname", StringArgumentType.string())
                                        .executes(EscapeRoomCommands::instanceClearAll)))
                )
        );
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Asegura que el preset con el nombre dado esté cargado en memoria.
     * Si ya está cargado, devuelve true. Si no, intenta cargarlo desde el archivo.
     * En caso de error, envía un mensaje de error al source y devuelve false.
     */
    private static boolean ensurePresetLoaded(String name, CommandSourceStack source) {
        if (loadedRooms.containsKey(name)) {
            return true;
        }

        // Intentar cargar desde archivo
        File file = new File("escaperooms/" + name + ".dat");
        if (!file.exists()) {
            source.sendFailure(Component.literal(
                    "§cEl preset '" + name + "' no existe como archivo. Debes guardarlo primero con /escaperoom save <nombre>."
            ));
            return false;
        }

        try {
            EscapeRoomData data = EscapeRoomManager.loadFromFile(file);
            loadedRooms.put(name, data);
            source.sendSuccess(() -> Component.literal(
                    "§aPreset '" + name + "' cargado automáticamente desde archivo."
            ), true);
            return true;
        } catch (Exception e) {
            source.sendFailure(Component.literal(
                    "§cError al cargar el preset '" + name + "': " + e.getMessage()
            ));
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene la posición de una instancia guardada por su índice.
     * Si el índice no es válido, envía un mensaje de error y devuelve null.
     */
    private static BlockPos getPositionById(String room, String idStr, CommandSourceStack source) {
        List<BlockPos> positions = EscapeRoomManager.getInstancePositions(room);
        if (positions.isEmpty()) {
            source.sendFailure(Component.literal("§cNo hay posiciones guardadas para '" + room + "'."));
            return null;
        }
        try {
            int index = Integer.parseInt(idStr);
            if (index < 0 || index >= positions.size()) {
                source.sendFailure(Component.literal("§cÍndice inválido. Rango: 0 - " + (positions.size() - 1)));
                return null;
            }
            return positions.get(index);
        } catch (NumberFormatException e) {
            source.sendFailure(Component.literal("§cEl ID debe ser un número entero."));
            return null;
        }
    }

    // ==================== COMANDOS PRINCIPALES ====================

    private static int capture(CommandContext<CommandSourceStack> context) {
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
    }

    private static int save(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        CommandSourceStack source = context.getSource();

        if (!loadedRooms.containsKey(name)) {
            source.sendFailure(Component.literal("§cEl preset '" + name + "' no está cargado en memoria."));
            return 0;
        }

        try {
            File file = new File("escaperooms/" + name + ".dat");
            file.getParentFile().mkdirs();
            EscapeRoomManager.saveToFile(loadedRooms.get(name), file);
            source.sendSuccess(() -> Component.literal(
                    "§aEscape room '" + name + "' guardado en archivo."
            ), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cError al guardar: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int load(CommandContext<CommandSourceStack> context) {
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
    }

    private static int paste(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        CommandSourceStack source = context.getSource();

        if (!ensurePresetLoaded(name, source)) {
            return 0;
        }

        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPos.containing(source.getPosition());
        EscapeRoomManager.restoreAt(level, loadedRooms.get(name), pos);

        source.sendSuccess(() -> Component.literal(
                "§aEscape room '" + name + "' pegado en " + pos.toShortString()
        ), true);
        return 1;
    }

    private static int pasteAt(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        CommandSourceStack source = context.getSource();

        if (!ensurePresetLoaded(name, source)) {
            return 0;
        }

        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
        EscapeRoomManager.restoreAt(level, loadedRooms.get(name), pos);

        source.sendSuccess(() -> Component.literal(
                "§aEscape room '" + name + "' pegado en " + pos.toShortString()
        ), true);
        return 1;
    }

    private static int clear(CommandContext<CommandSourceStack> context) {
        int radius = IntegerArgumentType.getInteger(context, "radius");
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPos.containing(source.getPosition());

        EscapeRoomManager.clearArea(level, pos, radius);

        source.sendSuccess(() -> Component.literal(
                "§aÁrea limpiada en " + pos.toShortString() + " (radio: " + radius + ")"
        ), true);
        return 1;
    }

    private static int clearAt(CommandContext<CommandSourceStack> context) {
        int radius = IntegerArgumentType.getInteger(context, "radius");
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");

        EscapeRoomManager.clearArea(level, pos, radius);

        source.sendSuccess(() -> Component.literal(
                "§aÁrea limpiada en " + pos.toShortString() + " (radio: " + radius + ")"
        ), true);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (loadedRooms.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§eNo hay escape rooms cargados."), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("§6=== Escape Rooms Cargados ==="), false);
        loadedRooms.forEach((name, data) -> source.sendSuccess(() -> Component.literal(
                "§e- " + name + " §7(" + data.getEntities().size() + " entidades, centro: " +
                        data.getCenterPos().toShortString() + ")"
        ), false));

        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        CommandSourceStack source = context.getSource();

        if (!ensurePresetLoaded(name, source)) {
            return 0;
        }

        EscapeRoomData data = loadedRooms.get(name);
        source.sendSuccess(() -> Component.literal("§6=== Info: " + name + " ==="), false);
        source.sendSuccess(() -> Component.literal("§eCentro: §f" + data.getCenterPos().toShortString()), false);
        source.sendSuccess(() -> Component.literal("§eEntidades: §f" + data.getEntities().size()), false);

        // Desglose por tipo
        Map<String, Integer> entityTypes = new HashMap<>();
        data.getEntities().forEach(snapshot -> {
            String type = snapshot.getEntityType();
            entityTypes.put(type, entityTypes.getOrDefault(type, 0) + 1);
        });

        source.sendSuccess(() -> Component.literal("§eDesglose:"), false);
        entityTypes.forEach((type, count) -> source.sendSuccess(() -> Component.literal("  §7- " + type + ": §f" + count), false));

        return 1;
    }

    // ==================== COMANDOS DE INSTANCIAS (MODIFICADOS) ====================

    private static int instanceAdd(CommandContext<CommandSourceStack> context) {
        String room = StringArgumentType.getString(context, "roomname");
        BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
        CommandSourceStack source = context.getSource();

        // Verificar que el preset existe antes de añadir la posición
        if (!ensurePresetLoaded(room, source)) {
            return 0;
        }

        EscapeRoomManager.addInstancePosition(room, pos);
        source.sendSuccess(() -> Component.literal(
                "§aPosición añadida para '" + room + "' en " + pos.toShortString()
        ), true);
        return 1;
    }

    private static int instanceList(CommandContext<CommandSourceStack> context) {
        String room = StringArgumentType.getString(context, "roomname");
        List<BlockPos> positions = EscapeRoomManager.getInstancePositions(room);
        CommandSourceStack source = context.getSource();

        if (positions.isEmpty()) {
            source.sendFailure(Component.literal("§cNo hay instancias guardadas para '" + room + "'."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§6=== Instancias de " + room + " ==="), false);
        for (int i = 0; i < positions.size(); i++) {
            int finalI = i;
            source.sendSuccess(() -> Component.literal(
                    "§e" + finalI + ": " + positions.get(finalI).toShortString()
            ), false);
        }
        return 1;
    }

    private static int instanceCreate(CommandContext<CommandSourceStack> context) {
        String room = StringArgumentType.getString(context, "roomname");
        String idStr = StringArgumentType.getString(context, "id");
        CommandSourceStack source = context.getSource();

        // Asegurar que el preset está cargado (intenta cargar automáticamente)
        if (!ensurePresetLoaded(room, source)) {
            return 0;
        }

        BlockPos pos = getPositionById(room, idStr, source);
        if (pos == null) {
            return 0;
        }

        EscapeRoomManager.clearArea(source.getLevel(), pos, 60);
        EscapeRoomManager.restoreAt(source.getLevel(), loadedRooms.get(room), pos);

        source.sendSuccess(() -> Component.literal(
                "§aInstancia creada en " + pos.toShortString()
        ), true);
        return 1;
    }

    private static int instanceCreateAll(CommandContext<CommandSourceStack> context) {
        String room = StringArgumentType.getString(context, "roomname");
        CommandSourceStack source = context.getSource();

        if (!ensurePresetLoaded(room, source)) {
            return 0;
        }

        List<BlockPos> positions = EscapeRoomManager.getInstancePositions(room);
        if (positions.isEmpty()) {
            source.sendFailure(Component.literal("§cNo hay posiciones guardadas para '" + room + "'."));
            return 0;
        }

        ServerLevel level = source.getLevel();
        EscapeRoomData data = loadedRooms.get(room);

        for (BlockPos pos : positions) {
            EscapeRoomManager.restoreAt(level, data, pos);
        }

        source.sendSuccess(() -> Component.literal(
                "§aSe crearon " + positions.size() + " instancias de '" + room + "'."
        ), true);
        return 1;
    }

    private static int instanceRemove(CommandContext<CommandSourceStack> context) {
        String room = StringArgumentType.getString(context, "roomname");
        String idStr = StringArgumentType.getString(context, "id");
        CommandSourceStack source = context.getSource();

        try {
            int index = Integer.parseInt(idStr);
            if (EscapeRoomManager.removeInstance(room, index)) {
                source.sendSuccess(() -> Component.literal("§cInstancia eliminada."), true);
            } else {
                source.sendFailure(Component.literal("§cÍndice inválido o no hay instancias."));
            }
        } catch (NumberFormatException e) {
            source.sendFailure(Component.literal("§cEl ID debe ser un número entero."));
        }
        return 1;
    }

    private static int instanceClearAll(CommandContext<CommandSourceStack> context) {
        String room = StringArgumentType.getString(context, "roomname");
        EscapeRoomManager.clearAllInstances(room);
        context.getSource().sendSuccess(() -> Component.literal(
                "§cTodas las instancias de '" + room + "' han sido eliminadas."
        ), true);
        return 1;
    }
}
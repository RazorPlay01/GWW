package com.github.razorplay01.command;

import com.github.razorplay01.entity.custom.LuzTortugaEntity;
import com.github.razorplay01.entity.custom.PanelFusiblesEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EscapeRoomConfigCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("escaperoom")
                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("config")
                        // === CONFIGURACIÓN PARA PANEL FUSIBLES ===
                        .then(Commands.literal("panel_fusible")
                                .then(Commands.argument("panel", EntityArgument.entity())

                                        // /escaperoom config panel_fusible <panel> link <turtle> <1|2>
                                        .then(Commands.literal("link")
                                                .then(Commands.argument("turtle", EntityArgument.entity())
                                                        .then(Commands.argument("puzzleId", IntegerArgumentType.integer(1, 2))
                                                                .executes(EscapeRoomConfigCommand::linkTurtleToPanel)
                                                        )
                                                )
                                        )

                                        // /escaperoom config panel_fusible <panel> unlink
                                        .then(Commands.literal("unlink")
                                                .executes(EscapeRoomConfigCommand::unlinkAllFromPanel)
                                        )

                                        // /escaperoom config panel_fusible <panel> list
                                        .then(Commands.literal("list")
                                                .executes(EscapeRoomConfigCommand::listPanelLinks)
                                        )
                                )
                        )
                )
        );
    }

    // ====================== PANEL FUSIBLES ======================

    private static int linkTurtleToPanel(CommandContext<CommandSourceStack> context) {
        try {
            Entity panelEntity = EntityArgument.getEntity(context, "panel");
            Entity turtleEntity = EntityArgument.getEntity(context, "turtle");
            int puzzleId = IntegerArgumentType.getInteger(context, "puzzleId");

            if (!(panelEntity instanceof PanelFusiblesEntity panel)) {
                context.getSource().sendFailure(Component.literal("§cLa entidad seleccionada no es un Panel de Fusibles."));
                return 0;
            }

            if (!(turtleEntity instanceof LuzTortugaEntity turtle)) {
                context.getSource().sendFailure(Component.literal("§cLa segunda entidad debe ser una Luz Tortuga."));
                return 0;
            }

            Vec3 roomCenter = panel.position(); // Fallback actual. Puedes mejorarlo pasando el centro del room.

            panel.linkTurtle(puzzleId, turtle, roomCenter);

            context.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Tortuga vinculada correctamente al Puzzle " + puzzleId + " del panel."
            ), true);

            return 1;

        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int unlinkAllFromPanel(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "panel");

            if (entity instanceof PanelFusiblesEntity panel) {
                panel.unlinkAllTurtles();
                context.getSource().sendSuccess(() ->
                        Component.literal("§aTodas las vinculaciones del panel han sido eliminadas."), true);
                return 1;
            }

            context.getSource().sendFailure(Component.literal("§cLa entidad no es un Panel de Fusibles."));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int listPanelLinks(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "panel");

            if (entity instanceof PanelFusiblesEntity panel) {
                context.getSource().sendSuccess(() -> Component.literal("§6=== Vinculaciones del Panel ==="), false);
                context.getSource().sendSuccess(() -> Component.literal(
                        "§ePuzzle 1 → " + panel.getLinkedCount(1) + " tortugas"), false);
                context.getSource().sendSuccess(() -> Component.literal(
                        "§ePuzzle 2 → " + panel.getLinkedCount(2) + " tortugas"), false);
                return 1;
            }

            context.getSource().sendFailure(Component.literal("§cLa entidad no es un Panel de Fusibles."));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }
}
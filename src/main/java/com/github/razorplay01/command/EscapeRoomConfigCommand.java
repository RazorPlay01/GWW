package com.github.razorplay01.command;

import com.github.razorplay01.entity.custom.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class EscapeRoomConfigCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("escaperoom")
                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("config")
                        .then(Commands.literal("panel_fusible")
                                .then(Commands.argument("panel", EntityArgument.entity())
                                        .then(Commands.literal("link")
                                                .then(Commands.argument("puerta", EntityArgument.entity())
                                                        .executes(EscapeRoomConfigCommand::linkDoorToPanel)
                                                )
                                                .then(Commands.argument("turtle", EntityArgument.entity())
                                                        .then(Commands.argument("puzzleId", IntegerArgumentType.integer(1, 2))
                                                                .executes(EscapeRoomConfigCommand::linkTurtleToPanel)
                                                        )
                                                )
                                        )
                                        .then(Commands.literal("unlink")
                                                .executes(EscapeRoomConfigCommand::unlinkAllDoorsFromPanel)
                                        )
                                        .then(Commands.literal("list")
                                                .executes(EscapeRoomConfigCommand::listPanelDoors)
                                        )
                                        .then(Commands.literal("unlink")
                                                .executes(EscapeRoomConfigCommand::unlinkAllFromPanel)
                                        )
                                        .then(Commands.literal("list")
                                                .executes(EscapeRoomConfigCommand::listPanelLinks)
                                        )
                                )
                        )
                        .then(Commands.literal("cuadro")
                                .then(Commands.literal("setinitial")
                                        .then(Commands.argument("cuadro", EntityArgument.entity())
                                                .executes(EscapeRoomConfigCommand::setInitialFromCurrent)
                                        )
                                )
                                .then(Commands.literal("setinitialpos")
                                        .then(Commands.argument("cuadro", EntityArgument.entity())
                                                .then(Commands.argument("x", FloatArgumentType.floatArg())
                                                        .then(Commands.argument("y", FloatArgumentType.floatArg())
                                                                .then(Commands.argument("z", FloatArgumentType.floatArg())
                                                                        .executes(EscapeRoomConfigCommand::setInitialPosition)
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .then(Commands.literal("setinitialrot")
                                        .then(Commands.argument("cuadro", EntityArgument.entity())
                                                .then(Commands.argument("yaw", FloatArgumentType.floatArg())
                                                        .then(Commands.argument("pitch", FloatArgumentType.floatArg())
                                                                .executes(EscapeRoomConfigCommand::setInitialRotation)
                                                        )
                                                )
                                        )
                                )
                                .then(Commands.literal("reset")
                                        .then(Commands.argument("cuadro", EntityArgument.entity())
                                                .executes(EscapeRoomConfigCommand::resetPuzzle)
                                        )
                                )
                                .then(Commands.literal("teleportinitial")
                                        .then(Commands.argument("cuadro", EntityArgument.entity())
                                                .executes(EscapeRoomConfigCommand::teleportToInitial)
                                        )
                                )
                                .then(Commands.literal("check")
                                        .then(Commands.argument("cuadro", EntityArgument.entity())
                                                .executes(EscapeRoomConfigCommand::checkPlacement)
                                        )
                                )
                        )
                        .then(Commands.literal("interruptor")
                                .then(Commands.argument("interruptor", EntityArgument.entity())
                                        .then(Commands.literal("link")
                                                .then(Commands.argument("cable", EntityArgument.entity())
                                                        .executes(EscapeRoomConfigCommand::linkCableToInterruptor)
                                                )
                                        )
                                        .then(Commands.literal("unlink")
                                                .executes(EscapeRoomConfigCommand::unlinkAllFromInterruptor)
                                        )
                                        .then(Commands.literal("list")
                                                .executes(EscapeRoomConfigCommand::listInterruptorLinks)
                                        )
                                )
                        )
                        .then(Commands.literal("reja")
                                .then(Commands.argument("reja", EntityArgument.entity())
                                        .then(Commands.literal("link")
                                                .then(Commands.argument("panel", EntityArgument.entity())
                                                        .executes(EscapeRoomConfigCommand::linkPanelToReja)
                                                )
                                        )
                                        .then(Commands.literal("unlink")
                                                .executes(EscapeRoomConfigCommand::unlinkAllFromReja)
                                        )
                                        .then(Commands.literal("list")
                                                .executes(EscapeRoomConfigCommand::listRejaLinks)
                                        )
                                )
                        )
                        .then(Commands.literal("valvula")
                                .then(Commands.argument("valvula", EntityArgument.entity())
                                        .then(Commands.literal("addparticle")
                                                .then(Commands.argument("worldX", DoubleArgumentType.doubleArg())
                                                        .then(Commands.argument("worldY", DoubleArgumentType.doubleArg())
                                                                .then(Commands.argument("worldZ", DoubleArgumentType.doubleArg())
                                                                        .then(Commands.argument("dirX", DoubleArgumentType.doubleArg())
                                                                                .then(Commands.argument("dirY", DoubleArgumentType.doubleArg())
                                                                                        .then(Commands.argument("dirZ", DoubleArgumentType.doubleArg())
                                                                                                .then(Commands.argument("speed", DoubleArgumentType.doubleArg(0))
                                                                                                        .then(Commands.argument("radius", DoubleArgumentType.doubleArg(0.1))
                                                                                                                .then(Commands.argument("pushForce", DoubleArgumentType.doubleArg(0))
                                                                                                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 100))
                                                                                                                                .executes(EscapeRoomConfigCommand::addParticleEmitter)
                                                                                                                        )
                                                                                                                        .executes(ctx -> addParticleEmitterWithDefaults(ctx, 5))
                                                                                                                )
                                                                                                        )
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                        .then(Commands.literal("listparticles")
                                                .executes(EscapeRoomConfigCommand::listParticleEmitters)
                                        )
                                        .then(Commands.literal("removeparticle")
                                                .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                                        .executes(EscapeRoomConfigCommand::removeParticleEmitter)
                                                )
                                        )
                                        .then(Commands.literal("clearparticles")
                                                .executes(EscapeRoomConfigCommand::clearParticleEmitters)
                                        )
                                )
                        )
                )
        );
    }

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

            Vec3 roomCenter = panel.position();

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

    private static int resetPuzzle(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "cuadro");

            if (!(entity instanceof Cuadro1Entity cuadro)) {
                context.getSource().sendFailure(Component.literal("§cLa entidad no es un cuadro"));
                return 0;
            }

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

    private static int linkCableToInterruptor(CommandContext<CommandSourceStack> context) {
        try {
            Entity interruptorEntity = EntityArgument.getEntity(context, "interruptor");
            Entity cableEntity = EntityArgument.getEntity(context, "cable");

            if (!(interruptorEntity instanceof InterruptorIndustrialEntity interruptor)) {
                context.getSource().sendFailure(Component.literal("§cLa entidad seleccionada no es un Interruptor Industrial."));
                return 0;
            }

            if (!(cableEntity instanceof CableEntity cable)) {
                context.getSource().sendFailure(Component.literal("§cLa segunda entidad debe ser un Cable."));
                return 0;
            }

            interruptor.linkCable(cable);

            context.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Cable vinculado correctamente al Interruptor Industrial."
            ), true);

            return 1;

        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int unlinkAllFromInterruptor(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "interruptor");

            if (entity instanceof InterruptorIndustrialEntity interruptor) {
                interruptor.unlinkAllCables();
                context.getSource().sendSuccess(() ->
                        Component.literal("§aTodas las vinculaciones del interruptor han sido eliminadas."), true);
                return 1;
            }

            context.getSource().sendFailure(Component.literal("§cLa entidad no es un Interruptor Industrial."));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int listInterruptorLinks(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "interruptor");

            if (entity instanceof InterruptorIndustrialEntity interruptor) {
                int count = interruptor.getLinkedCables().size();

                context.getSource().sendSuccess(() -> Component.literal("§6=== Vinculaciones del Interruptor ==="), false);
                context.getSource().sendSuccess(() -> Component.literal(
                        "§eCables vinculados: §f" + count), false);

                if (count > 0) {
                    context.getSource().sendSuccess(() -> Component.literal("§7Lista de cables vinculados:"), false);
                    for (int i = 0; i < count; i++) {
                        Vec3 rel = interruptor.getLinkedCables().get(i);
                        int finalI = i;
                        context.getSource().sendSuccess(() -> Component.literal(
                                String.format("§e%d → §f%.2f, %.2f, %.2f", finalI + 1, rel.x, rel.y, rel.z)
                        ), false);
                    }
                }
                return 1;
            }

            context.getSource().sendFailure(Component.literal("§cLa entidad no es un Interruptor Industrial."));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int linkPanelToReja(CommandContext<CommandSourceStack> context) {
        try {
            Entity rejaEntity = EntityArgument.getEntity(context, "reja");
            Entity panelEntity = EntityArgument.getEntity(context, "panel");

            if (!(rejaEntity instanceof RejaDuctoEntity reja)) {
                context.getSource().sendFailure(Component.literal("§cLa entidad seleccionada no es una Reja de Ducto."));
                return 0;
            }

            if (!(panelEntity instanceof PanelEnergiaEntity panel)) {
                context.getSource().sendFailure(Component.literal("§cLa segunda entidad debe ser un Panel de Energía."));
                return 0;
            }

            Vec3 roomCenter = reja.position();
            reja.linkPowerPanel(panel, roomCenter);

            context.getSource().sendSuccess(() -> Component.literal(
                    "§a✓ Panel de Energía vinculado correctamente a la Reja."
            ), true);
            return 1;

        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int unlinkAllFromReja(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "reja");

            if (entity instanceof RejaDuctoEntity reja) {
                reja.unlinkAllPowerPanels();
                context.getSource().sendSuccess(() ->
                        Component.literal("§aTodas las vinculaciones de la reja han sido eliminadas."), true);
                return 1;
            }

            context.getSource().sendFailure(Component.literal("§cLa entidad no es una Reja de Ducto."));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int listRejaLinks(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "reja");

            if (entity instanceof RejaDuctoEntity reja) {
                int count = reja.getLinkedPowerPanels().size();
                context.getSource().sendSuccess(() -> Component.literal("§6=== Vinculaciones de la Reja ==="), false);
                context.getSource().sendSuccess(() -> Component.literal(
                        "§ePaneles de Energía vinculados: §f" + count), false);
                return 1;
            }

            context.getSource().sendFailure(Component.literal("§cLa entidad no es una Reja de Ducto."));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int addParticleEmitter(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(ctx, "valvula");
        if (!(entity instanceof ValvulaEntity valvula)) {
            ctx.getSource().sendFailure(Component.literal("§cLa entidad no es una válvula."));
            return 0;
        }

        double worldX = DoubleArgumentType.getDouble(ctx, "worldX");
        double worldY = DoubleArgumentType.getDouble(ctx, "worldY");
        double worldZ = DoubleArgumentType.getDouble(ctx, "worldZ");
        double dirX = DoubleArgumentType.getDouble(ctx, "dirX");
        double dirY = DoubleArgumentType.getDouble(ctx, "dirY");
        double dirZ = DoubleArgumentType.getDouble(ctx, "dirZ");
        double speed = DoubleArgumentType.getDouble(ctx, "speed");
        double radius = DoubleArgumentType.getDouble(ctx, "radius");
        double pushForce = DoubleArgumentType.getDouble(ctx, "pushForce");
        int count = IntegerArgumentType.getInteger(ctx, "count");

        ValvulaEntity.ParticleEmitter emitter = ValvulaEntity.ParticleEmitter.fromWorldCoordinates(
                valvula,
                worldX, worldY, worldZ,
                dirX, dirY, dirZ,
                speed, radius, pushForce, count
        );

        valvula.addParticleEmitter(emitter);

        int index = valvula.getParticleEmitters().size() - 1;

        ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✔ Emisor añadido [" + index + "]\n" +
                        "§7  Mundo: §f" + String.format("%.2f, %.2f, %.2f", worldX, worldY, worldZ) + "\n" +
                        "§7  Relativo: §f" + String.format("%.2f, %.2f, %.2f", emitter.offsetX, emitter.offsetY, emitter.offsetZ) + "\n" +
                        "§7  Dir: §f" + String.format("%.2f, %.2f, %.2f", dirX, dirY, dirZ) + "\n" +
                        "§7  Speed: §f" + speed + " §7| Count: §f" + count + "\n" +
                        "§7  Radius: §f" + radius + " §7| PushForce: §f" + pushForce
        ), false);
        return 1;
    }

    private static int addParticleEmitterWithDefaults(CommandContext<CommandSourceStack> ctx, int defaultCount) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(ctx, "valvula");
        if (!(entity instanceof ValvulaEntity valvula)) {
            ctx.getSource().sendFailure(Component.literal("§cLa entidad no es una válvula."));
            return 0;
        }

        double worldX = DoubleArgumentType.getDouble(ctx, "worldX");
        double worldY = DoubleArgumentType.getDouble(ctx, "worldY");
        double worldZ = DoubleArgumentType.getDouble(ctx, "worldZ");
        double dirX = DoubleArgumentType.getDouble(ctx, "dirX");
        double dirY = DoubleArgumentType.getDouble(ctx, "dirY");
        double dirZ = DoubleArgumentType.getDouble(ctx, "dirZ");
        double speed = DoubleArgumentType.getDouble(ctx, "speed");
        double radius = DoubleArgumentType.getDouble(ctx, "radius");
        double pushForce = DoubleArgumentType.getDouble(ctx, "pushForce");

        ValvulaEntity.ParticleEmitter emitter = ValvulaEntity.ParticleEmitter.fromWorldCoordinates(
                valvula,
                worldX, worldY, worldZ,
                dirX, dirY, dirZ,
                speed, radius, pushForce, defaultCount
        );

        valvula.addParticleEmitter(emitter);

        int index = valvula.getParticleEmitters().size() - 1;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✔ Emisor añadido [" + index + "] con count: " + defaultCount
        ), false);
        return 1;
    }

    private static int listParticleEmitters(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(ctx, "valvula");
        if (!(entity instanceof ValvulaEntity valvula)) {
            ctx.getSource().sendFailure(Component.literal("§cLa entidad no es una válvula."));
            return 0;
        }

        List<ValvulaEntity.ParticleEmitter> emitters = valvula.getParticleEmitters();

        if (emitters.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("§eNo hay emisores configurados."), false);
            return 1;
        }

        StringBuilder sb = new StringBuilder("§6=== Emisores de Partículas ===\n");
        sb.append("§7State: ").append(valvula.getState()).append("/3");
        sb.append(" | Activas: ").append(valvula.areParticlesActive() ? "§aSí" : "§cNo").append("\n\n");

        for (int i = 0; i < emitters.size(); i++) {
            ValvulaEntity.ParticleEmitter e = emitters.get(i);
            Vec3 worldPos = e.getWorldPosition(valvula);
            Vec3 worldDir = e.getWorldDirection(valvula);

            sb.append(String.format("§e[%d]\n", i));
            sb.append(String.format("  §7Offset local: §f%.2f, %.2f, %.2f\n", e.offsetX, e.offsetY, e.offsetZ));
            sb.append(String.format("  §7Pos mundo:    §f%.2f, %.2f, %.2f\n", worldPos.x, worldPos.y, worldPos.z));
            sb.append(String.format("  §7Dir local:    §f%.2f, %.2f, %.2f\n", e.dirX, e.dirY, e.dirZ));
            sb.append(String.format("  §7Dir mundo:    §f%.2f, %.2f, %.2f\n", worldDir.x, worldDir.y, worldDir.z));
            sb.append(String.format("  §7Speed: §f%.3f §7| Count: §f%d\n", e.speed, e.count));
            sb.append(String.format("  §7Radius: §f%.2f §7| PushForce: §f%.2f\n\n", e.radius, e.pushForce));
        }

        String finalMsg = sb.toString();
        ctx.getSource().sendSuccess(() -> Component.literal(finalMsg), false);
        return 1;
    }

    private static int removeParticleEmitter(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(ctx, "valvula");
        if (!(entity instanceof ValvulaEntity valvula)) {
            ctx.getSource().sendFailure(Component.literal("§cLa entidad no es una válvula."));
            return 0;
        }

        int index = IntegerArgumentType.getInteger(ctx, "index");

        if (index < 0 || index >= valvula.getParticleEmitters().size()) {
            ctx.getSource().sendFailure(Component.literal(
                    "§cÍndice inválido. Rango: 0-" + (valvula.getParticleEmitters().size() - 1)
            ));
            return 0;
        }

        valvula.removeParticleEmitter(index);
        ctx.getSource().sendSuccess(() -> Component.literal("§a✔ Emisor [" + index + "] eliminado."), false);
        return 1;
    }

    private static int clearParticleEmitters(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(ctx, "valvula");
        if (!(entity instanceof ValvulaEntity valvula)) {
            ctx.getSource().sendFailure(Component.literal("§cLa entidad no es una válvula."));
            return 0;
        }

        int count = valvula.getParticleEmitters().size();
        valvula.clearParticleEmitters();
        ctx.getSource().sendSuccess(() -> Component.literal("§a✔ " + count + " emisores eliminados."), false);
        return 1;
    }

    private static int linkDoorToPanel(CommandContext<CommandSourceStack> context) {
        try {
            Entity panelEntity = EntityArgument.getEntity(context, "panel");
            Entity doorEntity = EntityArgument.getEntity(context, "puerta");

            if (!(panelEntity instanceof PanelFusiblesEntity panel)) {
                context.getSource().sendFailure(Component.literal("§cPrimera entidad debe ser un Panel de Fusibles."));
                return 0;
            }

            if (!(doorEntity instanceof PuertaMetalicaEntity door)) {
                context.getSource().sendFailure(Component.literal("§cSegunda entidad debe ser una Puerta Metálica."));
                return 0;
            }

            Vec3 roomCenter = panel.position();
            panel.linkDoor(door, roomCenter);

            context.getSource().sendSuccess(() ->
                    Component.literal("§a✓ Puerta metálica vinculada correctamente al panel."), true);
            return 1;

        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int unlinkAllDoorsFromPanel(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "panel");

            if (entity instanceof PanelFusiblesEntity panel) {
                panel.unlinkAllDoors();
                context.getSource().sendSuccess(() ->
                        Component.literal("§aTodas las puertas vinculadas han sido eliminadas."), true);
                return 1;
            }

            context.getSource().sendFailure(Component.literal("§cLa entidad no es un Panel de Fusibles."));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int listPanelDoors(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "panel");

            if (entity instanceof PanelFusiblesEntity panel) {
                context.getSource().sendSuccess(() -> Component.literal("§6=== Puertas Vinculadas ==="), false);
                context.getSource().sendSuccess(() -> Component.literal(
                        "§ePuertas metálicas vinculadas: §f" + panel.getLinkedDoorsCount()), false);
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
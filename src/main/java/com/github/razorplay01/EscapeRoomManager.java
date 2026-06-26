package com.github.razorplay01;

import com.github.razorplay01.entity.custom.*;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EscapeRoomManager {
    private EscapeRoomManager() {
        /* This utility class should not be instantiated */
    }

    @Getter
    public static class EscapeRoomData {
        private final BlockPos centerPos;
        private final List<EntitySnapshot> entities;
        private final String name;

        public EscapeRoomData(String name, BlockPos centerPos) {
            this.name = name;
            this.centerPos = centerPos;
            this.entities = new ArrayList<>();
        }

        public void addEntity(EntitySnapshot snapshot) {
            entities.add(snapshot);
        }

        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Name", name);
            tag.putInt("CenterX", centerPos.getX());
            tag.putInt("CenterY", centerPos.getY());
            tag.putInt("CenterZ", centerPos.getZ());

            ListTag entitiesList = new ListTag();
            for (EntitySnapshot snapshot : entities) {
                entitiesList.add(snapshot.serialize());
            }
            tag.put("Entities", entitiesList);

            return tag;
        }

        public static EscapeRoomData deserialize(CompoundTag tag) {
            String name = tag.getString("Name");
            BlockPos center = new BlockPos(
                    tag.getInt("CenterX"),
                    tag.getInt("CenterY"),
                    tag.getInt("CenterZ")
            );

            EscapeRoomData data = new EscapeRoomData(name, center);

            ListTag entitiesList = tag.getList("Entities", 10);
            for (int i = 0; i < entitiesList.size(); i++) {
                data.addEntity(EntitySnapshot.deserialize(entitiesList.getCompound(i)));
            }

            return data;
        }
    }

    @Getter
    public static class EntitySnapshot {
        private final String entityType;
        private final Vec3 relativePos;
        private final float yaw;
        private final float pitch;
        private final CompoundTag entityData;

        // Datos adicionales para entidades de puzzle
        private final boolean hasPuzzleData;
        private final Vec3 relativeInitialPos;
        private final float initialYaw;
        private final float initialPitch;

        public EntitySnapshot(Entity entity, Vec3 centerPos) {
            // Guardar posición relativa al centro
            Vec3 entityPos = entity.position();
            this.relativePos = new Vec3(
                    entityPos.x - centerPos.x,
                    entityPos.y - centerPos.y,
                    entityPos.z - centerPos.z
            );

            this.yaw = entity.getYRot();
            this.pitch = entity.getXRot();

            this.entityData = new CompoundTag();
            entity.save(entityData);
            this.entityType = entityData.getString("id");

            // Capturar datos del puzzle si es un Cuadro1Entity
            if (entity instanceof Cuadro1Entity cuadro) {
                this.hasPuzzleData = true;
                Vec3 initialPos = cuadro.getInitialPosition();

                // Guardar posición inicial RELATIVA al centro
                this.relativeInitialPos = new Vec3(
                        initialPos.x - centerPos.x,
                        initialPos.y - centerPos.y,
                        initialPos.z - centerPos.z
                );

                this.initialYaw = cuadro.getInitialYaw();
                this.initialPitch = cuadro.getInitialPitch();
            } else {
                this.hasPuzzleData = false;
                this.relativeInitialPos = Vec3.ZERO;
                this.initialYaw = 0.0F;
                this.initialPitch = 0.0F;
            }
        }

        private EntitySnapshot(String entityType, Vec3 relativePos, float yaw, float pitch,
                               CompoundTag entityData, boolean hasPuzzleData,
                               Vec3 relativeInitialPos, float initialYaw, float initialPitch) {
            this.entityType = entityType;
            this.relativePos = relativePos;
            this.yaw = yaw;
            this.pitch = pitch;
            this.entityData = entityData;
            this.hasPuzzleData = hasPuzzleData;
            this.relativeInitialPos = relativeInitialPos;
            this.initialYaw = initialYaw;
            this.initialPitch = initialPitch;
        }

        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Type", entityType);
            tag.putDouble("RelX", relativePos.x);
            tag.putDouble("RelY", relativePos.y);
            tag.putDouble("RelZ", relativePos.z);
            tag.putFloat("Yaw", yaw);
            tag.putFloat("Pitch", pitch);
            tag.put("EntityData", entityData);

            // Guardar datos del puzzle
            tag.putBoolean("HasPuzzleData", hasPuzzleData);
            if (hasPuzzleData) {
                tag.putDouble("RelInitialX", relativeInitialPos.x);
                tag.putDouble("RelInitialY", relativeInitialPos.y);
                tag.putDouble("RelInitialZ", relativeInitialPos.z);
                tag.putFloat("InitialYaw", initialYaw);
                tag.putFloat("InitialPitch", initialPitch);
            }

            return tag;
        }

        public static EntitySnapshot deserialize(CompoundTag tag) {
            String type = tag.getString("Type");
            Vec3 relPos = new Vec3(
                    tag.getDouble("RelX"),
                    tag.getDouble("RelY"),
                    tag.getDouble("RelZ")
            );
            float yaw = tag.getFloat("Yaw");
            float pitch = tag.getFloat("Pitch");
            CompoundTag data = tag.getCompound("EntityData");

            boolean hasPuzzleData = tag.getBoolean("HasPuzzleData");
            Vec3 relInitialPos = Vec3.ZERO;
            float initialYaw = 0.0F;
            float initialPitch = 0.0F;

            if (hasPuzzleData) {
                relInitialPos = new Vec3(
                        tag.getDouble("RelInitialX"),
                        tag.getDouble("RelInitialY"),
                        tag.getDouble("RelInitialZ")
                );
                initialYaw = tag.getFloat("InitialYaw");
                initialPitch = tag.getFloat("InitialPitch");
            }

            return new EntitySnapshot(type, relPos, yaw, pitch, data,
                    hasPuzzleData, relInitialPos, initialYaw, initialPitch);
        }
    }

    /**
     * Captura todas las entidades en un área y las guarda con coordenadas relativas
     */
    public static EscapeRoomData captureArea(ServerLevel level, BlockPos centerPos,
                                             int radius, String name) {
        EscapeRoomData data = new EscapeRoomData(name, centerPos);

        AABB area = new AABB(centerPos).inflate(radius);
        Vec3 center = Vec3.atCenterOf(centerPos);

        List<Entity> entities = level.getEntities((Entity) null, area,
                entity -> entity instanceof com.github.razorplay01.entity.custom.BaseEntity);

        for (Entity entity : entities) {
            data.addEntity(new EntitySnapshot(entity, center));
        }

        return data;
    }

    /**
     * Guarda el escape room en un archivo
     */
    public static void saveToFile(EscapeRoomData data, File file) throws IOException {
        CompoundTag tag = data.serialize();
        file.getParentFile().mkdirs();
        NbtIo.writeCompressed(tag, file.toPath());
    }

    /**
     * Carga el escape room desde un archivo
     */
    public static EscapeRoomData loadFromFile(File file) throws IOException {
        CompoundTag tag = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
        return EscapeRoomData.deserialize(tag);
    }

    /**
     * Elimina todas las entidades del escape room en un área
     */
    public static void clearArea(ServerLevel level, BlockPos centerPos, int radius) {
        AABB area = new AABB(centerPos).inflate(radius);

        List<Entity> entities = level.getEntities((Entity) null, area,
                entity -> entity instanceof com.github.razorplay01.entity.custom.BaseEntity);

        for (Entity entity : entities) {
            entity.discard();
        }
    }

    /**
     * Restaura el escape room en una nueva ubicación
     */
    public static void restoreAt(ServerLevel level, EscapeRoomData data, BlockPos newCenterPos) {
        Vec3 newCenter = Vec3.atCenterOf(newCenterPos);

        for (EntitySnapshot snapshot : data.getEntities()) {
            // Calcular posición absoluta
            Vec3 relPos = snapshot.getRelativePos();
            Vec3 absolutePos = new Vec3(
                    newCenter.x + relPos.x,
                    newCenter.y + relPos.y,
                    newCenter.z + relPos.z
            );

            // Crear entidad desde NBT
            CompoundTag entityTag = snapshot.getEntityData().copy();

            // Actualizar posición
            ListTag posList = new ListTag();
            posList.add(DoubleTag.valueOf(absolutePos.x));
            posList.add(DoubleTag.valueOf(absolutePos.y));
            posList.add(DoubleTag.valueOf(absolutePos.z));
            entityTag.put("Pos", posList);

            // Actualizar rotación
            ListTag rotList = new ListTag();
            rotList.add(FloatTag.valueOf(snapshot.getYaw()));
            rotList.add(FloatTag.valueOf(snapshot.getPitch()));
            entityTag.put("Rotation", rotList);

            // Si tiene datos de puzzle, calcular posición inicial absoluta
            if (snapshot.isHasPuzzleData()) {
                Vec3 relInitialPos = snapshot.getRelativeInitialPos();
                Vec3 absoluteInitialPos = new Vec3(
                        newCenter.x + relInitialPos.x,
                        newCenter.y + relInitialPos.y,
                        newCenter.z + relInitialPos.z
                );

                // Actualizar datos iniciales en el NBT
                entityTag.putFloat("InitialX", (float) absoluteInitialPos.x);
                entityTag.putFloat("InitialY", (float) absoluteInitialPos.y);
                entityTag.putFloat("InitialZ", (float) absoluteInitialPos.z);
                entityTag.putFloat("InitialYaw", snapshot.getInitialYaw());
                entityTag.putFloat("InitialPitch", snapshot.getInitialPitch());

                // Actualizar rotaciones bloqueadas
                entityTag.putFloat("LockedYaw", snapshot.getYaw());
                entityTag.putFloat("LockedPitch", snapshot.getPitch());
            }

            // Generar nuevo UUID para evitar conflictos
            entityTag.putUUID("UUID", java.util.UUID.randomUUID());

            // Spawnear entidad
            try {
                Entity entity = EntityType.loadEntityRecursive(entityTag, level, e -> e);
                if (entity != null) {
                    level.addFreshEntity(entity);
                    if (entity instanceof InterruptorIndustrialEntity interruptor) {
                        if (interruptor.isOn() && !interruptor.areAllCablesReady()) {
                            interruptor.setState(0);
                        }
                    }

                    // Post-procesamiento para entidades de puzzle
                    if (entity instanceof Cuadro1Entity cuadro && snapshot.isHasPuzzleData()) {
                        Vec3 relInitialPos = snapshot.getRelativeInitialPos();
                        Vec3 absoluteInitialPos = new Vec3(
                                newCenter.x + relInitialPos.x,
                                newCenter.y + relInitialPos.y,
                                newCenter.z + relInitialPos.z
                        );

                        // Establecer posición inicial correcta
                        cuadro.setInitialPosition(absoluteInitialPos);
                        cuadro.setInitialRotation(snapshot.getInitialYaw(), snapshot.getInitialPitch());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error al cargar entidad: " + e.getMessage());
                e.printStackTrace();
            }
        }
        reLinkPanels(level, newCenterPos);
        reLinkInterruptors(level, newCenterPos);
        reLinkRejas(level, newCenterPos);
    }

    private static void reLinkPanels(ServerLevel level, BlockPos centerPos) {
        level.getEntitiesOfClass(PanelFusiblesEntity.class,
                        new AABB(centerPos).inflate(150), p -> true)
                .forEach(panel -> {
                    // Actualizar Puzzle 1 con la nueva lógica de 3 estados
                    int state1 = calculatePuzzleState(panel, 1);
                    panel.updateLinkedTurtles(1, state1);

                    // Actualizar Puzzle 2
                    int state2 = calculatePuzzleState(panel, 2);
                    panel.updateLinkedTurtles(2, state2);
                });
    }

    private static void reLinkInterruptors(ServerLevel level, BlockPos centerPos) {
        List<InterruptorIndustrialEntity> interruptors = level.getEntitiesOfClass(
                InterruptorIndustrialEntity.class,
                new AABB(centerPos).inflate(150),
                i -> true
        );

        List<CableEntity> allCables = level.getEntitiesOfClass(
                CableEntity.class,
                new AABB(centerPos).inflate(150),
                c -> true
        );

        for (InterruptorIndustrialEntity interruptor : interruptors) {
            // Guardar las posiciones relativas originales ANTES de limpiar
            List<Vec3> savedRelPositions = new ArrayList<>(interruptor.getLinkedCables());

            interruptor.unlinkAllCables();

            for (Vec3 relPos : savedRelPositions) {
                // relPos es relativo al interruptor, calcular posición absoluta del cable esperado
                Vec3 expectedCablePos = interruptor.position().add(relPos);

                // Buscar cable cercano a esa posición
                CableEntity closest = null;
                double closestDist = 4.0; // umbral máximo

                for (CableEntity cable : allCables) {
                    double dist = cable.position().distanceToSqr(expectedCablePos);
                    if (dist < closestDist) {
                        closestDist = dist;
                        closest = cable;
                    }
                }

                if (closest != null) {
                    interruptor.linkCable(closest); // sin roomCenter, relativo al interruptor
                }
            }
        }
    }

    private static void reLinkRejas(ServerLevel level, BlockPos centerPos) {
        List<RejaDuctoEntity> rejas = level.getEntitiesOfClass(
                RejaDuctoEntity.class, new AABB(centerPos).inflate(150), r -> true);

        List<PanelEnergiaEntity> allPanels = level.getEntitiesOfClass(
                PanelEnergiaEntity.class, new AABB(centerPos).inflate(150), p -> true);

        for (RejaDuctoEntity reja : rejas) {
            List<Vec3> saved = new ArrayList<>(reja.getLinkedPowerPanels());
            reja.unlinkAllPowerPanels();

            for (Vec3 relPos : saved) {
                Vec3 expected = reja.position().add(relPos);
                PanelEnergiaEntity closest = null;
                double minDist = 5.0;

                for (PanelEnergiaEntity panel : allPanels) {
                    double dist = panel.position().distanceToSqr(expected);
                    if (dist < minDist) {
                        minDist = dist;
                        closest = panel;
                    }
                }
                if (closest != null) {
                    reja.linkPowerPanel(closest, reja.position());
                }
            }

            // Comprobar si ya debería estar abierta
            if (reja.isPowerPanelActive()) {
                reja.tryOpenAutomatically();
            }
        }
    }

    /**
     * Calcula el estado correcto según la nueva lógica:
     * 0 = incompleto (faltan fusibles)
     * 1 = lleno pero incorrecto
     * 2 = resuelto correctamente
     */
    private static int calculatePuzzleState(PanelFusiblesEntity panel, int puzzleId) {
        boolean solved = (puzzleId == 1) ? panel.isPuzzle1Solved() : panel.isPuzzle2Solved();

        if (solved) {
            return 2; // Correcto
        }

        // Verificar si todos los slots están llenos pero no está resuelto
        boolean allFilled = (puzzleId == 1)
                ? (panel.hasSlot(0) && panel.hasSlot(1) && panel.hasSlot(2))
                : (panel.hasSlot(3) && panel.hasSlot(4) && panel.hasSlot(5));

        return allFilled ? 1 : 0;
    }

    /**
     * Resetea el escape room (elimina y restaura en la misma posición)
     */
    public static void reset(ServerLevel level, EscapeRoomData originalData, int radius) {
        clearArea(level, originalData.centerPos, radius);
        restoreAt(level, originalData, originalData.centerPos);
    }
}
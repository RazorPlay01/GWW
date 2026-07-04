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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EscapeRoomManager {
    private EscapeRoomManager() {
        /* This utility class should not be instantiated */
    }

    private static final Map<String, List<BlockPos>> instancePositions = new HashMap<>();
    private static final File INSTANCES_FILE = new File("escaperooms/instances.dat");

    @Getter
    public static class EscapeRoomData {
        private final BlockPos centerPos;
        private final List<EntitySnapshot> entities;
        private final String name;
        private final int radius;

        public EscapeRoomData(String name, BlockPos centerPos, int radius) {
            this.name = name;
            this.centerPos = centerPos;
            this.radius = radius;
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
            tag.putInt("Radius", radius);

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
            int radius = tag.contains("Radius") ? tag.getInt("Radius") : 15;

            EscapeRoomData data = new EscapeRoomData(name, center, radius);

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

        private final boolean hasPuzzleData;
        private final Vec3 relativeInitialPos;
        private final float initialYaw;
        private final float initialPitch;

        public EntitySnapshot(Entity entity, Vec3 centerPos) {
            Vec3 entityPos = entity.position();
            this.relativePos = entityPos.subtract(centerPos);

            this.yaw = entity.getYRot();
            this.pitch = entity.getXRot();

            this.entityData = new CompoundTag();
            entity.save(entityData);
            this.entityType = entityData.getString("id");

            switch (entity) {
                case BaseCuadroEntity cuadro -> {
                    this.hasPuzzleData = true;
                    Vec3 initialPos = cuadro.getInitialPosition();
                    this.relativeInitialPos = initialPos.subtract(centerPos);
                    this.initialYaw = cuadro.getInitialYaw();
                    this.initialPitch = cuadro.getInitialPitch();
                }
                case PalancaEntity palanca -> {
                    this.hasPuzzleData = true;
                    Vec3 initialPos = palanca.getInitialPosition();
                    this.relativeInitialPos = initialPos.subtract(centerPos);
                    this.initialYaw = 0.0F;
                    this.initialPitch = 0.0F;
                }
                case UblablaEntity ublabla -> {
                    this.hasPuzzleData = true;
                    this.relativeInitialPos = Vec3.ZERO;
                    this.initialYaw = 0.0F;
                    this.initialPitch = 0.0F;

                    if (ublabla.getPatrolCenter() != null) {
                        BlockPos relPatrol = BlockPos.containing(ublabla.getPatrolCenter().subtract(centerPos));
                        entityData.putInt("PatrolCenterX", relPatrol.getX());
                        entityData.putInt("PatrolCenterY", relPatrol.getY());
                        entityData.putInt("PatrolCenterZ", relPatrol.getZ());
                    }
                    entityData.putDouble("PatrolRadius", ublabla.getPatrolRadius());

                    if (ublabla.getSpawnPos() != null) {
                        BlockPos relSpawn = ublabla.getSpawnPos().subtract(BlockPos.containing(centerPos));
                        entityData.putInt("SpawnX", relSpawn.getX());
                        entityData.putInt("SpawnY", relSpawn.getY());
                        entityData.putInt("SpawnZ", relSpawn.getZ());
                    }

                    if (ublabla.getJailMin() != null && ublabla.getJailMax() != null) {
                        BlockPos relMin = ublabla.getJailMin().subtract(BlockPos.containing(centerPos));
                        BlockPos relMax = ublabla.getJailMax().subtract(BlockPos.containing(centerPos));
                        entityData.putInt("JailMinX", relMin.getX());
                        entityData.putInt("JailMinY", relMin.getY());
                        entityData.putInt("JailMinZ", relMin.getZ());
                        entityData.putInt("JailMaxX", relMax.getX());
                        entityData.putInt("JailMaxY", relMax.getY());
                        entityData.putInt("JailMaxZ", relMax.getZ());
                    }

                    if (ublabla.getInvestigationTarget() != null) {
                        BlockPos relInvest = ublabla.getInvestigationTarget().subtract(BlockPos.containing(centerPos));
                        entityData.putInt("InvestX", relInvest.getX());
                        entityData.putInt("InvestY", relInvest.getY());
                        entityData.putInt("InvestZ", relInvest.getZ());
                    }
                }
                default -> {
                    this.hasPuzzleData = false;
                    this.relativeInitialPos = Vec3.ZERO;
                    this.initialYaw = 0.0F;
                    this.initialPitch = 0.0F;
                }
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

    public static void loadInstances() {
        instancePositions.clear();
        if (!INSTANCES_FILE.exists()) return;

        try {
            CompoundTag tag = NbtIo.readCompressed(INSTANCES_FILE.toPath(), NbtAccounter.unlimitedHeap());

            if (tag.contains("Rooms", 9)) {  // 9 = ListTag
                ListTag roomsList = tag.getList("Rooms", 10); // 10 = CompoundTag

                for (int i = 0; i < roomsList.size(); i++) {
                    CompoundTag roomTag = roomsList.getCompound(i);
                    String roomName = roomTag.getString("Name");

                    if (roomTag.contains("Positions", 9)) {
                        ListTag posList = roomTag.getList("Positions", 10);
                        List<BlockPos> positions = new ArrayList<>();

                        for (int j = 0; j < posList.size(); j++) {
                            CompoundTag posTag = posList.getCompound(j);
                            positions.add(new BlockPos(
                                    posTag.getInt("X"),
                                    posTag.getInt("Y"),
                                    posTag.getInt("Z")
                            ));
                        }
                        if (!positions.isEmpty()) {
                            instancePositions.put(roomName, positions);
                        }
                    }
                }
            }
            System.out.println("§a[EscapeRoom] Instancias cargadas: " + instancePositions.size() + " rooms");
        } catch (Exception e) {
            System.err.println("§cError cargando instancias.dat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void saveInstances() {
        try {
            CompoundTag tag = new CompoundTag();
            ListTag roomsList = new ListTag();

            for (Map.Entry<String, List<BlockPos>> entry : instancePositions.entrySet()) {
                CompoundTag roomTag = new CompoundTag();
                roomTag.putString("Name", entry.getKey());

                ListTag posList = new ListTag();
                for (BlockPos pos : entry.getValue()) {
                    CompoundTag posTag = new CompoundTag();
                    posTag.putInt("X", pos.getX());
                    posTag.putInt("Y", pos.getY());
                    posTag.putInt("Z", pos.getZ());
                    posList.add(posTag);
                }
                roomTag.put("Positions", posList);
                roomsList.add(roomTag);
            }

            tag.put("Rooms", roomsList);

            INSTANCES_FILE.getParentFile().mkdirs();
            NbtIo.writeCompressed(tag, INSTANCES_FILE.toPath());

        } catch (Exception e) {
            System.err.println("§cError guardando instancias: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void addInstancePosition(String roomName, BlockPos pos) {
        instancePositions.computeIfAbsent(roomName, k -> new ArrayList<>()).add(pos);
        saveInstances();
    }

    public static List<BlockPos> getInstancePositions(String roomName) {
        return instancePositions.getOrDefault(roomName, List.of());
    }

    public static boolean removeInstance(String roomName, int index) {
        List<BlockPos> positions = instancePositions.get(roomName);
        if (positions == null || index < 0 || index >= positions.size()) return false;
        positions.remove(index);
        if (positions.isEmpty()) instancePositions.remove(roomName);
        saveInstances();
        return true;
    }

    public static void clearAllInstances(String roomName) {
        instancePositions.remove(roomName);
        saveInstances();
    }

    /**
     * Captura todas las entidades en un área y las guarda con coordenadas relativas
     */
    public static EscapeRoomData captureArea(ServerLevel level, BlockPos centerPos,
                                             int radius, String name) {
        EscapeRoomData data = new EscapeRoomData(name, centerPos, radius);

        AABB area = new AABB(centerPos).inflate(radius);
        Vec3 center = Vec3.atCenterOf(centerPos);

        List<Entity> entities = level.getEntities((Entity) null, area,
                entity -> entity instanceof BaseEntity ||
                        entity instanceof UblablaEntity);

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
                entity -> entity instanceof BaseEntity ||
                        entity instanceof UblablaEntity);

        for (Entity entity : entities) {
            entity.discard();
        }
    }

    /**
     * Restaura el escape room en una nueva ubicación
     */
    public static void restoreAt(ServerLevel level, EscapeRoomData data, BlockPos newCenterPos) {
        Vec3 newCenter = Vec3.atCenterOf(newCenterPos);
        clearArea(level, newCenterPos, data.getRadius() + 5);
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

            // Generar nuevo UUID para evitar conflictos
            entityTag.putUUID("UUID", java.util.UUID.randomUUID());

            // Spawnear entidad
            try {
                Entity entity = EntityType.loadEntityRecursive(entityTag, level, e -> e);
                if (entity != null) {
                    level.addFreshEntity(entity);

                    // Post-procesamiento para entidades de puzzle y especiales
                    switch (entity) {
                        case BaseCuadroEntity cuadro when snapshot.isHasPuzzleData() -> {
                            Vec3 relInitialPos = snapshot.getRelativeInitialPos();
                            Vec3 absoluteInitialPos = new Vec3(
                                    newCenter.x + relInitialPos.x,
                                    newCenter.y + relInitialPos.y,
                                    newCenter.z + relInitialPos.z
                            );

                            cuadro.setInitialPosition(absoluteInitialPos);
                            if (cuadro instanceof Cuadro1Entity c1) {
                                c1.setInitialRotation(snapshot.getInitialYaw(), snapshot.getInitialPitch());
                            }
                            cuadro.resetPuzzleState();
                        }
                        case PalancaEntity palanca when snapshot.isHasPuzzleData() -> {
                            Vec3 relInitialPos = snapshot.getRelativeInitialPos();
                            Vec3 absoluteInitialPos = new Vec3(
                                    newCenter.x + relInitialPos.x,
                                    newCenter.y + relInitialPos.y,
                                    newCenter.z + relInitialPos.z
                            );

                            palanca.setInitialPosition(absoluteInitialPos);
                            palanca.resetPuzzleState();
                        }
                        case UblablaEntity ublabla -> {
                            CompoundTag extraData = snapshot.getEntityData();

                            if (extraData.contains("PatrolCenterX")) {
                                BlockPos relPatrol = new BlockPos(
                                        extraData.getInt("PatrolCenterX"),
                                        extraData.getInt("PatrolCenterY"),
                                        extraData.getInt("PatrolCenterZ")
                                );
                                ublabla.setPatrolCenter(Vec3.atCenterOf(newCenterPos.offset(relPatrol)));
                            }
                            if (extraData.contains("PatrolRadius")) {
                                ublabla.setPatrolRadius(extraData.getDouble("PatrolRadius"));
                            }

                            if (extraData.contains("SpawnX")) {
                                BlockPos relSpawn = new BlockPos(
                                        extraData.getInt("SpawnX"),
                                        extraData.getInt("SpawnY"),
                                        extraData.getInt("SpawnZ")
                                );
                                ublabla.setSpawnPos(newCenterPos.offset(relSpawn));
                            }

                            if (extraData.contains("JailMinX")) {
                                BlockPos relMin = new BlockPos(
                                        extraData.getInt("JailMinX"),
                                        extraData.getInt("JailMinY"),
                                        extraData.getInt("JailMinZ"));
                                BlockPos relMax = new BlockPos(
                                        extraData.getInt("JailMaxX"),
                                        extraData.getInt("JailMaxY"),
                                        extraData.getInt("JailMaxZ"));
                                ublabla.setJailArea(
                                        newCenterPos.offset(relMin),
                                        newCenterPos.offset(relMax)
                                );
                            }

                            if (extraData.contains("InvestX")) {
                                BlockPos relInvest = new BlockPos(
                                        extraData.getInt("InvestX"),
                                        extraData.getInt("InvestY"),
                                        extraData.getInt("InvestZ"));
                                ublabla.setInvestigationTarget(newCenterPos.offset(relInvest));
                            }

                            ublabla.resetToPatrol();
                        }
                        default -> {
                            // []
                        }
                    }

                    // Reset de interruptores si es necesario
                    if (entity instanceof InterruptorIndustrialEntity interruptor) {
                        if (interruptor.isOn() && !interruptor.areAllCablesReady()) {
                            interruptor.setState(0);
                        }
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
        reLinkDoors(level, newCenterPos);
        reLinkCodigoPanels(level, newCenterPos);
    }

    private static void reLinkPanels(ServerLevel level, BlockPos centerPos) {
        level.getEntitiesOfClass(PanelFusiblesEntity.class,
                        new AABB(centerPos).inflate(150), p -> true)
                .forEach(panel -> {
                    int state1 = calculatePuzzleState(panel, 1);
                    panel.updateLinkedTurtles(1, state1);

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
            List<Vec3> savedRelPositions = new ArrayList<>(interruptor.getLinkedCables());

            interruptor.unlinkAllCables();

            for (Vec3 relPos : savedRelPositions) {
                Vec3 expectedCablePos = interruptor.position().add(relPos);

                CableEntity closest = null;
                double closestDist = 4.0;

                for (CableEntity cable : allCables) {
                    double dist = cable.position().distanceToSqr(expectedCablePos);
                    if (dist < closestDist) {
                        closestDist = dist;
                        closest = cable;
                    }
                }

                if (closest != null) {
                    interruptor.linkCable(closest);
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

            if (reja.isPowerPanelActive()) {
                reja.tryOpenAutomatically();
            }
        }
    }

    private static void reLinkDoors(ServerLevel level, BlockPos centerPos) {
        List<PanelFusiblesEntity> panels = level.getEntitiesOfClass(
                PanelFusiblesEntity.class, new AABB(centerPos).inflate(150), p -> true);

        for (PanelFusiblesEntity panel : panels) {
            List<Vec3> savedDoors = new ArrayList<>(panel.getLinkedDoors());
            if (panel.areBothPuzzlesSolved()) {
                panel.updateAllLinkedDoors();
            } else {
                panel.updateAllLinkedDoors();
            }
        }
    }

    private static void reLinkCodigoPanels(ServerLevel level, BlockPos centerPos) {
        List<PanelCodigoEntity> panels = level.getEntitiesOfClass(
                PanelCodigoEntity.class, new AABB(centerPos).inflate(150), p -> true);

        for (PanelCodigoEntity panel : panels) {
            if (panel.isSolved()) {
                panel.updateAllLinkedDoors();
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
            return 2;
        }

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
package com.github.razorplay01.entity.custom;

import com.github.darkpred.morehitboxes.api.EntityHitboxData;
import com.github.darkpred.morehitboxes.api.EntityHitboxDataFactory;
import com.github.darkpred.morehitboxes.api.GeckoLibMultiPartEntity;
import com.github.darkpred.morehitboxes.api.MultiPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.AnimatableManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.razorplay01.entity.custom.util.Util.*;

public class PanelCodigoEntity extends BaseEntity implements GeckoLibMultiPartEntity<PanelCodigoEntity> {
    private EntityHitboxData<PanelCodigoEntity> hitboxData;
    private final List<Vec3> linkedDoors = new ArrayList<>();
    // =============================================
    // SYNCED DATA (servidor <-> cliente)
    // =============================================

    // Guarda el progreso actual como string: ej "circulo,cuadrado"
    private static final EntityDataAccessor<String> CURRENT_INPUT =
            SynchedEntityData.defineId(PanelCodigoEntity.class, EntityDataSerializers.STRING);

    // Si el puzzle ya fue resuelto
    private static final EntityDataAccessor<Boolean> PUZZLE_SOLVED =
            SynchedEntityData.defineId(PanelCodigoEntity.class, EntityDataSerializers.BOOLEAN);

    // Si el puzzle está bloqueado temporalmente (por error)
    private static final EntityDataAccessor<Boolean> PUZZLE_LOCKED =
            SynchedEntityData.defineId(PanelCodigoEntity.class, EntityDataSerializers.BOOLEAN);

    // =============================================
    // CONFIGURACIÓN DEL PUZZLE
    // =============================================

    // Secuencia correcta: el orden en que deben presionarse los 4 botones
    private static final List<String> CORRECT_SEQUENCE = Arrays.asList(
            "triangulo", "circulo", "hexagono", "cuadrado"
    );

    // Ticks de bloqueo tras error (40 ticks = 2 segundos)
    private static final int LOCK_DURATION_TICKS = 40;
    private int lockTimer = 0;

    // =============================================
    // CONSTRUCTOR
    // =============================================

    public PanelCodigoEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.hitboxData = EntityHitboxDataFactory.create(this);
    }

    public static AttributeSupplier.Builder setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Double.POSITIVE_INFINITY);
    }

    // =============================================
    // DEFINIR DATOS SINCRONIZADOS
    // =============================================

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CURRENT_INPUT, "");
        builder.define(PUZZLE_SOLVED, false);
        builder.define(PUZZLE_LOCKED, false);
    }

    // =============================================
    // GETTERS Y SETTERS SINCRONIZADOS
    // =============================================

    public List<String> getCurrentInput() {
        String data = this.entityData.get(CURRENT_INPUT);
        if (data.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(data.split(",")));
    }

    public void setCurrentInput(List<String> input) {
        this.entityData.set(CURRENT_INPUT, String.join(",", input));
    }

    public boolean isSolved() {
        return this.entityData.get(PUZZLE_SOLVED);
    }

    public void setSolved(boolean solved) {
        this.entityData.set(PUZZLE_SOLVED, solved);
    }

    public boolean isLocked() {
        return this.entityData.get(PUZZLE_LOCKED);
    }

    public void setLocked(boolean locked) {
        this.entityData.set(PUZZLE_LOCKED, locked);
    }

    // =============================================
    // PERSISTENCIA NBT
    // =============================================

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("PuzzleSolved", isSolved());
        tag.putString("CurrentInput", this.entityData.get(CURRENT_INPUT));
        tag.putBoolean("PuzzleLocked", isLocked());
        tag.putInt("LockTimer", this.lockTimer);
        saveLinkedList(tag, "LinkedDoors", linkedDoors);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setSolved(tag.getBoolean("PuzzleSolved"));
        this.entityData.set(CURRENT_INPUT, tag.getString("CurrentInput"));
        setLocked(tag.getBoolean("PuzzleLocked"));
        this.lockTimer = tag.getInt("LockTimer");
        linkedDoors.clear();
        linkedDoors.addAll(loadLinkedList(tag, "LinkedDoors"));
    }

    // =============================================
    // TICK - MANEJAR TEMPORIZADOR DE BLOQUEO
    // =============================================

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && isLocked()) {
            lockTimer--;
            if (lockTimer <= 0) {
                setLocked(false);
                setCurrentInput(new ArrayList<>());
                lockTimer = 0;
            }
        }
    }

    // =============================================
    // INTERACCIÓN
    // =============================================

    @Override
    public InteractionResult interactAt(Player player, Vec3 hitVec, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || this.level().isClientSide) {
            return InteractionResult.PASS;
        }

        Vec3 worldHitVec = new Vec3(
                this.getX() + hitVec.x,
                this.getY() + hitVec.y,
                this.getZ() + hitVec.z
        );

        List<MultiPart<PanelCodigoEntity>> panelParts = this.hitboxData.getCustomParts();

        MultiPart<PanelCodigoEntity> closestPart = null;
        double closestDist = Double.MAX_VALUE;

        for (MultiPart<PanelCodigoEntity> part : panelParts) {
            AABB box = part.getEntity().getBoundingBox();

            if (box.contains(worldHitVec)) {
                double dist = distanceToAABB(box, worldHitVec);
                if (dist < closestDist) {
                    closestDist = dist;
                    closestPart = part;
                }
            }
        }

        if (closestPart == null) {
            closestPart = findClosestPartByRaycast(player, panelParts);
        }

        if (closestPart != null) {
            handlePartInteract(player, closestPart);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private double distanceToAABB(AABB box, Vec3 point) {
        double dx = Math.max(box.minX - point.x, Math.max(0, point.x - box.maxX));
        double dy = Math.max(box.minY - point.y, Math.max(0, point.y - box.maxY));
        double dz = Math.max(box.minZ - point.z, Math.max(0, point.z - box.maxZ));
        return dx * dx + dy * dy + dz * dz;
    }

    private MultiPart<PanelCodigoEntity> findClosestPartByRaycast(
            Player player,
            List<MultiPart<PanelCodigoEntity>> parts) {

        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        double reach = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        Vec3 endPos = eyePos.add(lookVec.scale(reach));

        MultiPart<PanelCodigoEntity> closestPart = null;
        double closestHitDist = Double.MAX_VALUE;

        for (MultiPart<PanelCodigoEntity> part : parts) {
            AABB box = part.getEntity().getBoundingBox().inflate(0.03);
            var hit = box.clip(eyePos, endPos);

            if (hit.isPresent()) {
                double dist = eyePos.distanceToSqr(hit.get());
                if (dist < closestHitDist) {
                    closestHitDist = dist;
                    closestPart = part;
                }
            }
        }

        return closestPart;
    }

    // =============================================
    // LÓGICA DEL PUZZLE
    // =============================================

    private void handlePartInteract(Player player, MultiPart<PanelCodigoEntity> part) {
        String partName = part.getPartName();

        // --- Botón "button": muestra el estado actual del puzzle ---
        if (partName.equals("button")) {
            handleStatusButton(player);
            return;
        }

        // --- Si ya está resuelto, no hacer nada más ---
        if (isSolved()) {
            player.sendSystemMessage(Component.literal("§a✔ El puzzle ya está resuelto."));
            return;
        }

        // --- Si está bloqueado por error, no aceptar input ---
        if (isLocked()) {
            player.sendSystemMessage(Component.literal("§c✖ Panel bloqueado. Espera..."));
            playSound(player, false);
            return;
        }

        // --- Validar si la parte presionada es un botón del puzzle ---
        if (!CORRECT_SEQUENCE.contains(partName)) {
            return; // No es una parte válida del puzzle
        }

        // --- Añadir input ---
        List<String> input = getCurrentInput();
        int currentStep = input.size();

        // Verificar si el botón presionado coincide con el paso actual
        if (CORRECT_SEQUENCE.get(currentStep).equals(partName)) {
            // ✅ Correcto
            input.add(partName);
            setCurrentInput(input);

            int remaining = CORRECT_SEQUENCE.size() - input.size();
            player.sendSystemMessage(Component.literal(
                    "§a✔ " + formatName(partName) + " correcto! §7(" + input.size() + "/" + CORRECT_SEQUENCE.size() + ")"
            ));
            playSound(player, true);

            // Verificar si completó la secuencia
            if (input.size() == CORRECT_SEQUENCE.size()) {
                onPuzzleSolved(player);
            }

        } else {
            // ❌ Incorrecto - reiniciar y bloquear
            onPuzzleFailed(player, partName);
        }
    }

    private void onPuzzleSolved(Player player) {
        setSolved(true);
        setCurrentInput(new ArrayList<>());

        player.sendSystemMessage(Component.literal("§a§l★ ¡PUZZLE RESUELTO! ★"));

        this.level().playSound(null, this.blockPosition(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1.0F, 1.0F);

        // === ABRIR PUERTAS VINCULADAS ===
        updateAllLinkedDoors();
    }

    /**
     * Se llama cuando el jugador presiona un botón incorrecto
     */
    private void onPuzzleFailed(Player player, String wrongPart) {
        player.sendSystemMessage(Component.literal(
                "§c✖ ¡Incorrecto! " + formatName(wrongPart) + " no es el siguiente. Reiniciando..."
        ));
        playSound(player, false);

        // Bloquear panel temporalmente
        setLocked(true);
        lockTimer = LOCK_DURATION_TICKS;
        setCurrentInput(new ArrayList<>());
    }

    /**
     * Botón de estado: muestra información del puzzle
     */
    private void handleStatusButton(Player player) {
        if (isSolved()) {
            player.sendSystemMessage(Component.literal("§a§l★ Puzzle completado ★"));
            // Toggle de puertas al presionar el botón cuando ya está resuelto
            toggleLinkedDoors();
            player.sendSystemMessage(Component.literal("§e↔ Puertas toggled"));
        } else if (isLocked()) {
            player.sendSystemMessage(Component.literal("§c⏳ Panel bloqueado..."));
        } else {
            List<String> input = getCurrentInput();
            if (input.isEmpty()) {
                player.sendSystemMessage(Component.literal("§e▶ Puzzle sin iniciar. Presiona los símbolos en el orden correcto."));
            } else {
                player.sendSystemMessage(Component.literal(
                        "§e▶ Progreso: " + input.size() + "/" + CORRECT_SEQUENCE.size()
                ));
            }
        }
    }

    // =============================================
    // UTILIDADES
    // =============================================

    private void playSound(Player player, boolean success) {
        if (success) {
            this.level().playSound(null, this.blockPosition(),
                    SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 1.0F, 1.0F + (getCurrentInput().size() * 0.15F));
        } else {
            this.level().playSound(null, this.blockPosition(),
                    SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
        }
    }

    private String formatName(String partName) {
        return partName.substring(0, 1).toUpperCase() + partName.substring(1);
    }

    /**
     * Método para resetear el puzzle externamente (por comando, redstone, etc.)
     */
    public void resetPuzzle() {
        setSolved(false);
        setLocked(false);
        setCurrentInput(new ArrayList<>());
        lockTimer = 0;
    }

    // =============================================
    // GECKOLIB / MULTIPART
    // =============================================

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public void handleNormalInteract(Player player) {
    }

    @Override
    public EntityHitboxData<PanelCodigoEntity> getEntityHitboxData() {
        if (hitboxData == null) {
            hitboxData = EntityHitboxDataFactory.create(this);
        }
        return hitboxData;
    }

    @Override
    public boolean partHurt(MultiPart<PanelCodigoEntity> multiPart, @NotNull DamageSource damageSource, float v) {
        return false;
    }

    public void linkDoor(PuertaMetalicaEntity door, Vec3 roomCenter) {
        if (door == null || roomCenter == null) return;

        Vec3 relativePos = door.position().subtract(roomCenter);

        if (!linkedDoors.contains(relativePos)) {
            linkedDoors.add(relativePos);
            updateAllLinkedDoors(); // aplicar estado actual
        }
    }

    public void unlinkAllDoors() {
        linkedDoors.clear();
    }

    public int getLinkedDoorsCount() {
        return linkedDoors.size();
    }

    public List<Vec3> getLinkedDoors() {
        return new ArrayList<>(linkedDoors);
    }

    public void updateAllLinkedDoors() {
        if (linkedDoors.isEmpty()) return;

        Vec3 panelPos = this.position();
        boolean shouldOpen = isSolved();

        for (Vec3 relPos : linkedDoors) {
            Vec3 absolutePos = panelPos.add(relPos);

            this.level().getEntitiesOfClass(PuertaMetalicaEntity.class,
                            AABB.ofSize(absolutePos, 5, 5, 5),
                            d -> d.position().distanceToSqr(absolutePos) < 3.0)
                    .forEach(door -> door.setOpen(shouldOpen));
        }
    }

    /**
     * Toggle (abrir/cerrar) todas las puertas vinculadas
     */
    private void toggleLinkedDoors() {
        if (linkedDoors.isEmpty()) return;

        Vec3 panelPos = this.position();
        // Usamos el estado contrario al actual de la primera puerta
        boolean currentState = false;

        // Buscar estado actual
        for (Vec3 relPos : linkedDoors) {
            Vec3 absolutePos = panelPos.add(relPos);
            List<PuertaMetalicaEntity> doors = this.level().getEntitiesOfClass(
                    PuertaMetalicaEntity.class,
                    AABB.ofSize(absolutePos, 5, 5, 5),
                    d -> d.position().distanceToSqr(absolutePos) < 3.0);

            if (!doors.isEmpty()) {
                currentState = doors.get(0).isOpen();
                break;
            }
        }

        boolean newState = !currentState;

        for (Vec3 relPos : linkedDoors) {
            Vec3 absolutePos = panelPos.add(relPos);
            this.level().getEntitiesOfClass(PuertaMetalicaEntity.class,
                            AABB.ofSize(absolutePos, 5, 5, 5),
                            d -> d.position().distanceToSqr(absolutePos) < 3.0)
                    .forEach(door -> door.setOpen(newState));
        }
    }
}
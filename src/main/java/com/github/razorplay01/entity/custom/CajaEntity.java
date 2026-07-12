package com.github.razorplay01.entity.custom;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.ModEntities;
import com.github.razorplay01.item.ModItems;
import com.github.razorplay01.system.NoiseDetectionSystem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.ArrayList;
import java.util.List;

public class CajaEntity extends BaseEntity {

    // ========== DATOS SINCRONIZADOS (estado de apertura) ==========
    private static final EntityDataAccessor<Boolean> IS_OPEN =
            SynchedEntityData.defineId(CajaEntity.class, EntityDataSerializers.BOOLEAN);

    // ========== ANIMACIONES ==========
    private static final RawAnimation ANIMATION_IDLE = RawAnimation.begin().thenLoop("animation.idle");
    private static final RawAnimation ANIMATION_OPEN = RawAnimation.begin().thenPlayAndHold("animation.open");

    // ========== CONTENIDO CONFIGURABLE ==========
    // Lista de ItemStack (cada uno con su NBT completo, como en /give)
    private final List<ItemStack> boxContents = new ArrayList<>();

    // NBT completo para la entidad a invocar (formato /summon, obligatorio campo "id")
    private CompoundTag spawnNbt = new CompoundTag();

    // Offset relativo a la posición de la caja (defecto: (0, 0.2, 0))
    private Vec3 spawnOffset = new Vec3(0, 0.2, 0);

    // ========== ESTADO INTERNO ==========
    private boolean itemsSpawned = false;
    private int openAnimationTicks = 0;
    private int collisionCheckCooldown = 0;

    private static final int SPAWN_DELAY = 10;
    private static final int COLLISION_CHECK_INTERVAL = 5;

    public CajaEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        // Inicialmente no hay contenido; se establecerá vía NBT o por defecto en read
    }

    // ========== DEFINE SYNC DATA ==========
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_OPEN, false);
    }

    // ========== GETTERS / SETTERS ==========
    public boolean isOpen() {
        return this.entityData.get(IS_OPEN);
    }

    public void setOpen(boolean open) {
        this.entityData.set(IS_OPEN, open);
        if (open && !this.level().isClientSide) {
            openAnimationTicks = 0;
            this.level().playSound(null, this.blockPosition(), SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            Player nearestPlayer = this.level().getNearestPlayer(this, 20.0D);
            NoiseDetectionSystem.addNoise((ServerPlayer) nearestPlayer, 1.0f);
        }
    }

    // NBT de spawn (debe contener "id")
    public CompoundTag getSpawnNbt() {
        return spawnNbt;
    }

    public void setSpawnNbt(CompoundTag nbt) {
        this.spawnNbt = nbt.copy();
    }

    // Offset
    public Vec3 getSpawnOffset() {
        return spawnOffset;
    }

    public void setSpawnOffset(Vec3 offset) {
        this.spawnOffset = offset;
    }

    public void setSpawnOffset(double x, double y, double z) {
        this.spawnOffset = new Vec3(x, y, z);
    }

    // Acceso a la lista de contenidos (para añadir items desde fuera)
    public List<ItemStack> getBoxContents() {
        return boxContents;
    }

    // ========== ATRIBUTOS ==========
    public static AttributeSupplier.Builder setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Double.POSITIVE_INFINITY);
    }

    // ========== TICK ==========
    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        if (isOpen() && !itemsSpawned) {
            openAnimationTicks++;
            if (openAnimationTicks >= SPAWN_DELAY) {
                spawnBoxContents();
                itemsSpawned = true;
            }
        }

        if (!isOpen()) {
            collisionCheckCooldown--;
            if (collisionCheckCooldown <= 0) {
                collisionCheckCooldown = COLLISION_CHECK_INTERVAL;
                checkForInteractiveEntityCollision();
            }
        }
    }

    // ========== COLISIÓN ==========
    private void checkForInteractiveEntityCollision() {
        this.level().getEntitiesOfClass(PalancaEntity.class, this.getBoundingBox().inflate(0.5D))
                .forEach(palancaEntity -> {
                    setOpen(true);
                    onOpenedByCollision(palancaEntity);
                });
    }

    protected void onOpenedByCollision(PalancaEntity triggerEntity) {
        if (!this.level().isClientSide) {
            this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5F, 1.5F);
        }
    }

    // ========== ANIMACIONES ==========
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "caja_controller", 0,
                state -> isOpen() ? state.setAndContinue(ANIMATION_OPEN) : state.setAndContinue(ANIMATION_IDLE)));
    }

    // ========== INTERACCIÓN DEL JUGADOR ==========
    @Override
    public void handleNormalInteract(Player player) {
        if (!player.level().isClientSide) {
            if (!isOpen()) {
                player.sendSystemMessage(Component.literal("§7Esta caja se abre cuando algo colisiona con ella"));
            } else {
                player.sendSystemMessage(Component.literal("§aLa caja ya está abierta"));
            }
        }
    }

    // ========== SPANWEO DE CONTENIDO ==========
    private void spawnBoxContents() {
        if (this.level().isClientSide) return;

        // 1. Si hay NBT de entidad (con "id"), generar entidad
        if (!spawnNbt.isEmpty() && spawnNbt.contains("id")) {
            spawnSpecialEntity();
            return;
        }

        // 2. Si hay items en la lista, soltarlos
        if (!boxContents.isEmpty()) {
            spawnItemsFromList(boxContents);
            return;
        }

        // 3. No hay contenido definido → no hacer nada
        // (se puede enviar un log de advertencia si se desea)
    }

    private void spawnItemsFromList(List<ItemStack> items) {
        Vec3 center = this.position().add(0, 0.5, 0);
        int count = items.size();

        for (int i = 0; i < count; i++) {
            ItemStack stack = items.get(i).copy();
            if (stack.isEmpty()) continue;

            ItemEntity itemEntity = new ItemEntity(this.level(), center.x, center.y, center.z, stack);

            double angle = (Math.PI * 2 * i) / Math.max(count, 1);
            double speed = 0.15;

            itemEntity.setDeltaMovement(
                    Math.cos(angle) * speed,
                    0.45 + (i * 0.05),
                    Math.sin(angle) * speed
            );
            itemEntity.setPickUpDelay(20);

            this.level().addFreshEntity(itemEntity);
        }

        this.level().playSound(null, this.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8F, 0.6F);
    }

    private void spawnSpecialEntity() {
        CompoundTag finalNbt = spawnNbt.copy(); // ya contiene "id"

        try {
            ServerLevel serverLevel = (ServerLevel) this.level();

            // Posición de spawn = posición de la caja + offset
            Vec3 spawnPos = this.position().add(spawnOffset);

            // Usar el mismo método que el comando /summon
            Entity entity = EntityType.loadEntityRecursive(finalNbt, serverLevel, (e) -> {
                e.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, e.getYRot(), e.getXRot());
                return e;
            });

            if (entity == null) {
                GWW.LOGGER.error("§c[Falló al crear entidad desde NBT: {}]", finalNbt);
                return;
            }

            // Si es un Mob, inicializarlo como en el comando /summon
            if (entity instanceof Mob mob) {
                mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(entity.blockPosition()),
                        MobSpawnType.COMMAND, (SpawnGroupData) null);
            }

            // Agregar al mundo
            if (!serverLevel.tryAddFreshEntityWithPassengers(entity)) {
                GWW.LOGGER.error("§c[No se pudo agregar la entidad (UUID duplicado?)]");
            }

        } catch (Exception e) {
            GWW.LOGGER.error("§c[Error al invocar entidad desde NBT: {}]", e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== RESET ==========
    public void reset() {
        setOpen(false);
        itemsSpawned = false;
        openAnimationTicks = 0;
        collisionCheckCooldown = 0;
    }

    public void forceOpen() {
        if (!isOpen()) setOpen(true);
    }

    // ========== NBT ==========
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsOpen", isOpen());
        tag.putBoolean("ItemsSpawned", itemsSpawned);

        // Guardar items (cada uno con todo su NBT)
        ListTag itemList = new ListTag();
        var provider = this.level().registryAccess();
        for (ItemStack stack : boxContents) {
            if (!stack.isEmpty()) {
                itemList.add(stack.save(provider));
            }
        }
        tag.put("BoxContents", itemList);

        // Guardar NBT de spawn (formato /summon)
        if (!spawnNbt.isEmpty()) {
            tag.put("SpawnNbt", spawnNbt);
        }

        // Guardar offset
        tag.putDouble("SpawnOffsetX", spawnOffset.x);
        tag.putDouble("SpawnOffsetY", spawnOffset.y);
        tag.putDouble("SpawnOffsetZ", spawnOffset.z);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setOpen(tag.getBoolean("IsOpen"));
        itemsSpawned = tag.getBoolean("ItemsSpawned");

        var provider = this.level().registryAccess();
        boolean hasCustomContent = false;

        // Leer items
        if (tag.contains("BoxContents")) {
            Tag raw = tag.get("BoxContents");
            if (raw instanceof ListTag list) {
                boxContents.clear();
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag itemTag = list.getCompound(i);
                    ItemStack stack = ItemStack.parseOptional(provider, itemTag);
                    if (!stack.isEmpty()) {
                        boxContents.add(stack);
                    }
                }
                if (!boxContents.isEmpty()) {
                    hasCustomContent = true;
                }
            }
        }

        // Leer NBT de spawn
        if (tag.contains("SpawnNbt", 10)) {
            spawnNbt = tag.getCompound("SpawnNbt").copy();
            if (!spawnNbt.isEmpty() && spawnNbt.contains("id")) {
                hasCustomContent = true;
            }
        }

        // Leer offset
        double ox = tag.getDouble("SpawnOffsetX");
        double oy = tag.getDouble("SpawnOffsetY");
        double oz = tag.getDouble("SpawnOffsetZ");
        this.spawnOffset = new Vec3(ox, oy, oz);
    }

    // ========== COLISIÓN ==========
    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CajaEntity extends BaseEntity {

    // ========== DATOS SINCRONIZADOS (solo para el estado de apertura) ==========
    private static final EntityDataAccessor<Boolean> IS_OPEN =
            SynchedEntityData.defineId(CajaEntity.class, EntityDataSerializers.BOOLEAN);

    // ========== ANIMACIONES ==========
    private static final RawAnimation ANIMATION_IDLE = RawAnimation.begin().thenLoop("animation.idle");
    private static final RawAnimation ANIMATION_OPEN = RawAnimation.begin().thenPlayAndHold("animation.open");

    // ========== CONTENIDO CONFIGURABLE (variables de instancia, guardadas en NBT) ==========
    private final List<ItemStack> boxContents = new ArrayList<>();
    private String spawnEntityType = "";
    private CompoundTag spawnEntityData = new CompoundTag();
    private int activeLootTableIndex = 0; // Solo para compatibilidad legacy

    // ========== ESTADO INTERNO ==========
    private boolean itemsSpawned = false;
    private int openAnimationTicks = 0;
    private int collisionCheckCooldown = 0;

    private static final int SPAWN_DELAY = 10;
    private static final int COLLISION_CHECK_INTERVAL = 5;

    public CajaEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
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

    public int getActiveLootTableIndex() {
        return activeLootTableIndex;
    }

    public void setActiveLootTableIndex(int index) {
        this.activeLootTableIndex = Math.clamp(index, 0, 2);
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

        // 1. Invocar entidad si está definida
        if (!spawnEntityType.isEmpty()) {
            spawnSpecialEntity();
            return;
        }

        // 2. Soltar items si hay
        if (!boxContents.isEmpty()) {
            spawnItemsFromList(boxContents);
            return;
        }

        // 3. Fallback al sistema antiguo (compatibilidad)
        int tableIndex = getActiveLootTableIndex();
        if (tableIndex == 2) {
            spawnEntityType = "escaperoom:valvula";
            spawnSpecialEntity();
        } else {
            List<ItemStack> oldLoot = switch (tableIndex) {
                case 1 -> createLootTable2();
                default -> createLootTable1();
            };
            spawnItemsFromList(oldLoot);
        }
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
        if (spawnEntityType.isEmpty()) return;

        Optional<EntityType<?>> optionalType = EntityType.byString(spawnEntityType);
        if (optionalType.isEmpty()) {
            if (this.level().getServer() != null) {
                GWW.LOGGER.error("§c[Tipo de entidad no válido: {}]", spawnEntityType);
            }
            return;
        }

        EntityType<?> type = optionalType.get();
        Entity newEntity = type.create(this.level());
        if (newEntity == null) return;

        Vec3 spawnPos = this.position().add(0, 0.2, 0);
        newEntity.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        newEntity.setDeltaMovement(0, 0.4, 0);

        if (!spawnEntityData.isEmpty()) {
            try {
                CompoundTag entityNbt = new CompoundTag();
                newEntity.save(entityNbt);
                spawnEntityData.getAllKeys().forEach(key -> {
                    if (!key.equals("UUID") && !key.equals("Pos") && !key.equals("Rotation")) {
                        entityNbt.put(key, spawnEntityData.get(key));
                    }
                });
                newEntity.load(entityNbt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.level().addFreshEntity(newEntity);
    }

    // ========== MÉTODOS LEGACY ==========
    private List<ItemStack> createLootTable1() {
        List<ItemStack> loot = new ArrayList<>();
        loot.add(new ItemStack(ModItems.HOJA_PISTA, 1));
        loot.add(new ItemStack(ModItems.FUSIBLE_AZUL, 1));
        return loot;
    }

    private List<ItemStack> createLootTable2() {
        List<ItemStack> loot = new ArrayList<>();
        loot.add(new ItemStack(ModItems.HOJA_PISTA, 1));
        loot.add(new ItemStack(ModItems.LLAVE_ATICO, 1));
        return loot;
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
        tag.putInt("ActiveLootTable", activeLootTableIndex);

        // Guardar items
        ListTag itemList = new ListTag();
        var provider = this.level().registryAccess();
        for (ItemStack stack : boxContents) {
            if (!stack.isEmpty()) {
                itemList.add(stack.save(provider));
            }
        }
        tag.put("BoxContents", itemList);

        // Guardar entidad a invocar
        if (!spawnEntityType.isEmpty()) {
            tag.putString("SpawnEntity", spawnEntityType);
            if (!spawnEntityData.isEmpty()) {
                tag.put("SpawnEntityData", spawnEntityData);
            }
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setOpen(tag.getBoolean("IsOpen"));
        itemsSpawned = tag.getBoolean("ItemsSpawned");
        if (tag.contains("ActiveLootTable")) {
            activeLootTableIndex = tag.getInt("ActiveLootTable");
        }

        var provider = this.level().registryAccess();
        boolean hasCustomContent = false;

        // Leer BoxContents
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

        // Leer SpawnEntity
        if (tag.contains("SpawnEntity", 8)) {
            spawnEntityType = tag.getString("SpawnEntity");
            if (!spawnEntityType.isEmpty()) {
                hasCustomContent = true;
            }
        }
        if (tag.contains("SpawnEntityData", 10)) {
            spawnEntityData = tag.getCompound("SpawnEntityData");
        }

        // Si no se definió contenido personalizado, usar fallback por defecto
        if (!hasCustomContent) {
            boxContents.clear();
            boxContents.add(new ItemStack(ModItems.HOJA_PISTA, 1));
            boxContents.add(new ItemStack(ModItems.FUSIBLE_AZUL, 1));
        }
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

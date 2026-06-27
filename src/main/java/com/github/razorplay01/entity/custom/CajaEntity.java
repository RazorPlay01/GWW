package com.github.razorplay01.entity.custom;

import com.github.razorplay01.api.noise.NoiseAPI;
import com.github.razorplay01.api.noise.NoiseEvent;
import com.github.razorplay01.entity.ModEntities;
import com.github.razorplay01.item.ModItems;
import com.github.razorplay01.system.NoiseDetectionSystem;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.ArrayList;
import java.util.List;

//todo: cambair zombie por manivela
public class CajaEntity extends BaseEntity {

    private static final EntityDataAccessor<Boolean> IS_OPEN = SynchedEntityData.defineId(CajaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ACTIVE_LOOT_TABLE = SynchedEntityData.defineId(CajaEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation ANIMATION_IDLE = RawAnimation.begin().thenLoop("animation.idle");
    private static final RawAnimation ANIMATION_OPEN = RawAnimation.begin().thenPlayAndHold("animation.open");

    private boolean itemsSpawned = false;
    private int openAnimationTicks = 0;
    private int collisionCheckCooldown = 0;

    private static final int SPAWN_DELAY = 10;
    private static final int COLLISION_CHECK_INTERVAL = 5;

    public CajaEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_OPEN, false);
        builder.define(ACTIVE_LOOT_TABLE, 0);
    }

    public int getActiveLootTableIndex() {
        return this.entityData.get(ACTIVE_LOOT_TABLE);
    }

    public void setActiveLootTableIndex(int index) {
        this.entityData.set(ACTIVE_LOOT_TABLE, Math.clamp(index, 0, 2));
    }

    private List<ItemStack> getCurrentLootContents() {
        return switch (getActiveLootTableIndex()) {
            case 1 -> createLootTable2();
            case 2 -> new ArrayList<>(); // La tabla 3 no usa items
            default -> createLootTable1();
        };
    }

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

    public static AttributeSupplier.Builder setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Double.POSITIVE_INFINITY);
    }

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

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "caja_controller", 0,
                state -> isOpen() ? state.setAndContinue(ANIMATION_OPEN) : state.setAndContinue(ANIMATION_IDLE)));
    }

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

    private void spawnBoxContents() {
        if (this.level().isClientSide) return;

        int tableIndex = getActiveLootTableIndex();

        if (tableIndex == 2) {
            spawnSpecialEntity();
        } else {
            spawnItems();
        }
    }

    private void spawnItems() {
        List<ItemStack> contents = getCurrentLootContents();
        Vec3 center = this.position().add(0, 0.5, 0);

        for (int i = 0; i < contents.size(); i++) {
            ItemStack stack = contents.get(i).copy();
            if (stack.isEmpty()) continue;

            ItemEntity itemEntity = new ItemEntity(this.level(), center.x, center.y, center.z, stack);

            double angle = (Math.PI * 2 * i) / Math.max(contents.size(), 1);
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
        Entity specialEntity = ModEntities.VALVULA.create(this.level());

        if (specialEntity != null) {
            Vec3 spawnPos = this.position().add(0, 0.2, 0);
            specialEntity.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            specialEntity.setDeltaMovement(0, 0.4, 0);
            this.level().addFreshEntity(specialEntity);
        }
    }

    public void reset() {
        setOpen(false);
        itemsSpawned = false;
        openAnimationTicks = 0;
        collisionCheckCooldown = 0;
    }

    public void forceOpen() {
        if (!isOpen()) setOpen(true);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsOpen", isOpen());
        tag.putBoolean("ItemsSpawned", itemsSpawned);
        tag.putInt("ActiveLootTable", getActiveLootTableIndex());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setOpen(tag.getBoolean("IsOpen"));
        itemsSpawned = tag.getBoolean("ItemsSpawned");
        if (tag.contains("ActiveLootTable")) {
            setActiveLootTableIndex(tag.getInt("ActiveLootTable"));
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
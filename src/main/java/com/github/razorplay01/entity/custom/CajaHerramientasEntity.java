package com.github.razorplay01.entity.custom;

import com.github.razorplay01.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
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

public class CajaHerramientasEntity extends BaseEntity {

    private static final EntityDataAccessor<Boolean> IS_OPEN = SynchedEntityData.defineId(
            CajaHerramientasEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation ANIMATION_IDLE = RawAnimation.begin().thenLoop("animation.idle");
    private static final RawAnimation ANIMATION_OPEN = RawAnimation.begin().thenPlayAndHold("animation.open");

    // Items que saldrán de la caja (personalizables)
    private final List<ItemStack> boxContents = new ArrayList<>();

    // Control para spawneo de items
    private boolean itemsSpawned = false;
    private int openAnimationTicks = 0;
    private static final int SPAWN_DELAY = 10; // Ticks antes de lanzar items

    public CajaHerramientasEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        initializeDefaultContents();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_OPEN, false);
    }

    /**
     * Inicializa el contenido por defecto de la caja
     */
    private void initializeDefaultContents() {
        boxContents.clear();
        boxContents.add(new ItemStack(ModItems.FUSIBLE_ROJO, 1));
        boxContents.add(new ItemStack(ModItems.FUSIBLE_VERDE, 1));
        boxContents.add(new ItemStack(ModItems.HOJA_PISTA, 1));
    }

    /**
     * Permite personalizar los items de la caja
     */
    public void setBoxContents(List<ItemStack> contents) {
        this.boxContents.clear();
        this.boxContents.addAll(contents);
    }

    /**
     * Añade un item al contenido de la caja
     */
    public void addBoxContent(ItemStack stack) {
        this.boxContents.add(stack);
    }

    public boolean isOpen() {
        return this.entityData.get(IS_OPEN);
    }

    public void setOpen(boolean open) {
        this.entityData.set(IS_OPEN, open);
        if (open && !this.level().isClientSide) {
            openAnimationTicks = 0;
            // Sonido de apertura
            this.level().playSound(null, this.blockPosition(),
                    SoundEvents.CHEST_OPEN, SoundSource.BLOCKS,
                    1.0F, 1.0F);
        }
    }

    public static AttributeSupplier.Builder setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Double.POSITIVE_INFINITY);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && isOpen() && !itemsSpawned) {
            openAnimationTicks++;

            if (openAnimationTicks >= SPAWN_DELAY) {
                spawnBoxContents();
                itemsSpawned = true;
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                this,
                "caja_controller",
                0,
                state -> isOpen()
                        ? state.setAndContinue(ANIMATION_OPEN)
                        : state.setAndContinue(ANIMATION_IDLE)
        ));
    }

    @Override
    public void handleNormalInteract(Player player) {
        if (!player.level().isClientSide) {
            if (!isOpen()) {
                if (hasRequiredItem(player)) {
                    consumeRequiredItem(player);
                    setOpen(true);
                    player.sendSystemMessage(Component.literal("§a¡Has abierto la caja de herramientas!"));
                } else {
                    player.sendSystemMessage(Component.literal("§cNecesitas un §bobjeto §cpara abrir esta caja"));
                }
            } else {
                player.sendSystemMessage(Component.literal("§7La caja ya está abierta"));
            }
        }
    }

    /**
     * Verifica si el jugador tiene el item requerido (diamante)
     */
    private boolean hasRequiredItem(Player player) {
        return player.getInventory().contains(new ItemStack(ModItems.GANZUA));
    }

    /**
     * Consume el item requerido del inventario del jugador
     */
    private void consumeRequiredItem(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.GANZUA)) {
                stack.shrink(1);
                return;
            }
        }
    }

    /**
     * Spawna los items del contenido de la caja disparados hacia arriba
     */
    private void spawnBoxContents() {
        if (this.level().isClientSide || boxContents.isEmpty()) {
            return;
        }

        Vec3 centerPos = this.position().add(0, 0.5, 0);

        for (int i = 0; i < boxContents.size(); i++) {
            ItemStack stack = boxContents.get(i).copy();

            if (stack.isEmpty()) continue;

            ItemEntity itemEntity = new ItemEntity(
                    this.level(),
                    centerPos.x,
                    centerPos.y,
                    centerPos.z,
                    stack
            );

            // Calcular velocidad con dispersión
            double angle = (Math.PI * 2 * i) / boxContents.size();
            double horizontalSpeed = 0.1;

            double motionX = Math.cos(angle) * horizontalSpeed;
            double motionY = 0.4 + (i * 0.05);
            double motionZ = Math.sin(angle) * horizontalSpeed;

            itemEntity.setDeltaMovement(motionX, motionY, motionZ);
            itemEntity.setPickUpDelay(20);

            this.level().addFreshEntity(itemEntity);
        }

        // Sonido de items saliendo
        this.level().playSound(null, this.blockPosition(),
                SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS,
                0.8F, 0.6F);
    }

    /**
     * Resetea la caja a su estado cerrado
     */
    public void reset() {
        setOpen(false);
        itemsSpawned = false;
        openAnimationTicks = 0;
        initializeDefaultContents();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsOpen", isOpen());
        tag.putBoolean("ItemsSpawned", itemsSpawned);

        HolderLookup.Provider provider = this.level().registryAccess();
        ListTag list = new ListTag();
        for (ItemStack stack : boxContents) {
            if (!stack.isEmpty()) {
                list.add(stack.save(provider));
            }
        }
        tag.put("BoxContents", list);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setOpen(tag.getBoolean("IsOpen"));
        itemsSpawned = tag.getBoolean("ItemsSpawned");

        HolderLookup.Provider provider = this.level().registryAccess();

        // Limpiar contenido actual
        boxContents.clear();

        // Intentar leer como ListTag (nuevo formato)
        if (tag.contains("BoxContents", 9)) { // 9 = ListTag
            ListTag list = tag.getList("BoxContents", 10); // 10 = CompoundTag
            for (int i = 0; i < list.size(); i++) {
                CompoundTag itemTag = list.getCompound(i);
                ItemStack stack = ItemStack.parseOptional(provider, itemTag);
                if (!stack.isEmpty()) {
                    boxContents.add(stack);
                }
            }
        }
        // Si no, intentar leer el formato antiguo (CompoundTag con "Item0", etc.)
        else if (tag.contains("BoxContents", 10)) { // 10 = CompoundTag
            CompoundTag contentsTag = tag.getCompound("BoxContents");
            int size = contentsTag.getInt("Size");
            for (int i = 0; i < size; i++) {
                if (contentsTag.contains("Item" + i)) {
                    CompoundTag itemTag = contentsTag.getCompound("Item" + i);
                    ItemStack stack = ItemStack.parseOptional(provider, itemTag);
                    if (!stack.isEmpty()) {
                        boxContents.add(stack);
                    }
                }
            }
        }

        // Si no se cargó ningún contenido, usar los valores por defecto
        if (boxContents.isEmpty()) {
            initializeDefaultContents();
        }
    }
}
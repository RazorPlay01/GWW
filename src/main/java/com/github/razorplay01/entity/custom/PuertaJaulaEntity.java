package com.github.razorplay01.entity.custom;

import com.github.razorplay01.item.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.*;

public class PuertaJaulaEntity extends BaseEntity {

    private static final EntityDataAccessor<Boolean> IS_OPEN = SynchedEntityData.defineId(
            PuertaJaulaEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> IS_UNLOCKED = SynchedEntityData.defineId(
            PuertaJaulaEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Direction> DATA_FACING =
            SynchedEntityData.defineId(PuertaJaulaEntity.class, EntityDataSerializers.DIRECTION);

    // Animaciones
    //todo: falta cambiar la animacion de opne por la otra hay una con condado y otra sin candado
    private static final RawAnimation ANIMATION_IDLE = RawAnimation.begin().thenLoop("animation.idle");
    private static final RawAnimation ANIMATION_OPEN = RawAnimation.begin().thenPlayAndHold("animation.open");
    private static final RawAnimation ANIMATION_CLOSE = RawAnimation.begin().thenPlayAndHold("animation.close");

    // Variable para controlar la animación de cierre
    private boolean wasOpen = false;

    public PuertaJaulaEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_OPEN, false);
        builder.define(IS_UNLOCKED, false);
        builder.define(DATA_FACING, Direction.NORTH);
    }

    public Direction getFacing() {
        return this.entityData.get(DATA_FACING);
    }

    public void setFacing(Direction direction) {
        if (this.entityData.get(DATA_FACING) != direction) {
            this.entityData.set(DATA_FACING, direction);
            this.refreshDimensions();
        }
    }

    @Override
    public void setYRot(float yaw) {
        super.setYRot(yaw);
        setFacing(Direction.fromYRot(yaw));
    }

    public boolean isOpen() {
        return this.entityData.get(IS_OPEN);
    }

    public void setOpen(boolean open) {
        boolean oldState = isOpen();
        this.entityData.set(IS_OPEN, open);

        // Detectar cambio de estado para animación
        if (oldState != open) {
            wasOpen = oldState;
        }
    }

    public boolean isUnlocked() {
        return this.entityData.get(IS_UNLOCKED);
    }

    public void setUnlocked(boolean unlocked) {
        this.entityData.set(IS_UNLOCKED, unlocked);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsOpen", isOpen());
        tag.putBoolean("IsUnlocked", isUnlocked());
        tag.putString("Facing", getFacing().getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setOpen(tag.getBoolean("IsOpen"));
        setUnlocked(tag.getBoolean("IsUnlocked"));
        if (tag.contains("Facing")) {
            Direction dir = Direction.byName(tag.getString("Facing"));
            if (dir != null) {
                setFacing(dir);
            }
        }
    }

    public static AttributeSupplier.Builder setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Double.POSITIVE_INFINITY);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                this,
                "puerta_controller",
                0,
                this::animationPredicate
        ));
    }

    protected <E extends PuertaJaulaEntity> PlayState animationPredicate(final AnimationState<E> state) {
        if (isOpen()) {
            state.setAnimation(ANIMATION_OPEN);
            wasOpen = false;
            return PlayState.CONTINUE;
        }
        else if (wasOpen) {
            state.setAnimation(ANIMATION_CLOSE);
            return PlayState.CONTINUE;
        }
        else {
            state.setAnimation(ANIMATION_IDLE);
            return PlayState.CONTINUE;
        }
    }

    @Override
    public void handleNormalInteract(Player player) {
        if (player.level().isClientSide) return;

        if (!isUnlocked()) {
            // Primera vez: necesita ganzúa
            if (hasRequiredItem(player)) {
                consumeRequiredItem(player);
                setUnlocked(true);
                setOpen(true);
                player.sendSystemMessage(Component.literal("§a¡Has desbloqueado la puerta! Ahora puedes abrirla y cerrarla libremente."));
            } else {
                player.sendSystemMessage(Component.literal("§cNecesitas un §bGanzúa §cpara desbloquear esta puerta"));
            }
        } else {
            // Ya está desbloqueada → toggle
            boolean newState = !isOpen();
            setOpen(newState);

            if (newState) {
                player.sendSystemMessage(Component.literal("§aPuerta abierta"));
            } else {
                player.sendSystemMessage(Component.literal("§ePuerta cerrada"));
            }
        }
    }

    private boolean hasRequiredItem(Player player) {
        return player.getInventory().contains(new ItemStack(ModItems.GANZUA));
    }

    private void consumeRequiredItem(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.GANZUA)) {
                stack.shrink(1);
                return;
            }
        }
    }

    // ==================== COLISIÓN SELECTIVA ====================
    @Override
    public boolean canBeCollidedWith() {
        return !isOpen();
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return !isOpen() && super.canCollideWith(entity);
    }

    @Override
    public void push(Entity entity) {
        if (!isOpen()) {
            super.push(entity);
        }
    }

    @Override
    protected void pushEntities() {
        if (!isOpen()) {
            super.pushEntities();
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (IS_OPEN.equals(key) || DATA_FACING.equals(key)) {
            this.refreshDimensions();
        }
    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        double height = 2.0;
        double width = 1.0;
        double depth = 0.4;

        double hw = width / 2.0;
        double hd = depth / 2.0;

        if (getFacing().getAxis() == Direction.Axis.Z) {
            return new AABB(x - hw, y, z - hd, x + hw, y + height, z + hd);
        } else {
            return new AABB(x - hd, y, z - hw, x + hd, y + height, z + hw);
        }
    }
}
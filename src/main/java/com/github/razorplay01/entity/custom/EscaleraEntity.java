package com.github.razorplay01.entity.custom;

import com.github.darkpred.morehitboxes.api.EntityHitboxData;
import com.github.darkpred.morehitboxes.api.EntityHitboxDataFactory;
import com.github.darkpred.morehitboxes.api.GeckoLibMultiPartEntity;
import com.github.darkpred.morehitboxes.api.MultiPart;
import com.github.razorplay01.entity.BaseInteractiveEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EscaleraEntity extends BaseInteractiveEntity implements GeckoLibMultiPartEntity<EscaleraEntity> {

    private EntityHitboxData<EscaleraEntity> hitboxData;

    public EscaleraEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.hitboxData = EntityHitboxDataFactory.create(this);
        this.noPhysics = false;
    }

    public static AttributeSupplier.Builder setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Double.POSITIVE_INFINITY);
    }

    // ==================== HITBOX DATA ====================

    @Override
    public EntityHitboxData<EscaleraEntity> getEntityHitboxData() {
        if (hitboxData == null) {
            hitboxData = EntityHitboxDataFactory.create(this);
        }
        return hitboxData;
    }

    @Override
    public boolean partHurt(MultiPart<EscaleraEntity> multiPart,
                            @NotNull DamageSource damageSource, float damage) {
        return false;
    }

    // ==================== TICK ====================

    @Override
    public void tick() {
        super.tick();

        // Ejecutar en AMBOS lados (cliente y servidor) para colisión suave
        if (!isBound()) {
            handleSolidStepCollisions();
        }

        if (!this.level().isClientSide && !isBound()) {
            handleGravityAndMovement();
        }
    }

    // ==================== GRAVEDAD ====================

    private void handleGravityAndMovement() {
        this.setNoGravity(false);

        Vec3 motion = this.getDeltaMovement();

        if (!this.onGround()) {
            motion = motion.add(0, -0.08, 0);
            motion = motion.multiply(0.98, 0.98, 0.98);
        } else {
            motion = new Vec3(0, motion.y, 0);
        }

        this.setDeltaMovement(motion);
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    // ==================== COLISIÓN SÓLIDA DE ESCALONES ====================

    /**
     * Simula colisión sólida con los escalones.
     * El jugador NO sube automáticamente - debe saltar.
     */
    private void handleSolidStepCollisions() {
        List<MultiPart<EscaleraEntity>> parts = this.hitboxData.getCustomParts();

        if (parts == null || parts.isEmpty()) {
            return;
        }

        // Buscar jugadores cercanos
        AABB searchArea = this.getBoundingBox().inflate(5.0);
        List<Player> players = this.level().getEntitiesOfClass(Player.class, searchArea);

        for (Player player : players) {
            handlePlayerSolidCollision(player, parts);
        }
    }

    /**
     * Procesa colisión sólida de un jugador con todos los parts.
     */
    private void handlePlayerSolidCollision(Player player, List<MultiPart<EscaleraEntity>> parts) {
        AABB playerBox = player.getBoundingBox();
        Vec3 playerVel = player.getDeltaMovement();

        // AABB del jugador en el siguiente tick (predicción)
        AABB nextPlayerBox = playerBox.move(playerVel);

        for (MultiPart<EscaleraEntity> part : parts) {
            AABB stepBox = part.getEntity().getBoundingBox();

            // Solo procesar si hay intersección predicha
            if (!nextPlayerBox.intersects(stepBox)) {
                continue;
            }

            // Resolver colisión
            resolveCollision(player, playerBox, stepBox);
        }
    }

    /**
     * Resuelve la colisión entre el jugador y un escalón sólido.
     * Empuja al jugador fuera del escalón en la dirección correcta.
     */
    private void resolveCollision(Player player, AABB playerBox, AABB stepBox) {
        Vec3 playerVel = player.getDeltaMovement();

        // Calcular solapamiento en cada eje
        double overlapX = calculateOverlap(playerBox.minX, playerBox.maxX, stepBox.minX, stepBox.maxX);
        double overlapY = calculateOverlap(playerBox.minY, playerBox.maxY, stepBox.minY, stepBox.maxY);
        double overlapZ = calculateOverlap(playerBox.minZ, playerBox.maxZ, stepBox.minZ, stepBox.maxZ);

        // Si no hay solapamiento real, salir
        if (overlapX <= 0 || overlapY <= 0 || overlapZ <= 0) {
            return;
        }

        // Determinar el eje de menor penetración para resolver
        double playerCenterX = (playerBox.minX + playerBox.maxX) / 2;
        double playerCenterY = (playerBox.minY + playerBox.maxY) / 2;
        double playerCenterZ = (playerBox.minZ + playerBox.maxZ) / 2;

        double stepCenterX = (stepBox.minX + stepBox.maxX) / 2;
        double stepCenterY = (stepBox.minY + stepBox.maxY) / 2;
        double stepCenterZ = (stepBox.minZ + stepBox.maxZ) / 2;

        // Encontrar el eje de menor penetración
        if (overlapY <= overlapX && overlapY <= overlapZ) {
            // Resolver en Y (arriba/abajo)
            resolveY(player, playerBox, stepBox, playerCenterY, stepCenterY, playerVel);
        } else if (overlapX <= overlapZ) {
            // Resolver en X
            resolveX(player, playerCenterX, stepCenterX, overlapX, playerVel);
        } else {
            // Resolver en Z
            resolveZ(player, playerCenterZ, stepCenterZ, overlapZ, playerVel);
        }
    }

    /**
     * Calcula el solapamiento entre dos rangos 1D.
     */
    private double calculateOverlap(double minA, double maxA, double minB, double maxB) {
        return Math.min(maxA, maxB) - Math.max(minA, minB);
    }

    /**
     * Resuelve colisión en el eje Y.
     */
    private void resolveY(Player player, AABB playerBox, AABB stepBox,
                          double playerCenterY, double stepCenterY, Vec3 playerVel) {
        if (playerCenterY > stepCenterY) {
            // Jugador está ARRIBA del escalón - pararlo encima
            double newY = stepBox.maxY;
            player.setPos(player.getX(), newY, player.getZ());

            // Cancelar velocidad hacia abajo
            if (playerVel.y < 0) {
                player.setDeltaMovement(playerVel.x, 0, playerVel.z);
            }

            player.setOnGround(true);
            player.fallDistance = 0;
        } else {
            /*// Jugador está ABAJO del escalón - bloquearlo (techo)
            double newY = stepBox.minY - playerBox.getYsize() - 0.01;
            player.setPos(player.getX(), newY, player.getZ());

            // Cancelar velocidad hacia arriba
            */
            if (playerVel.y > 0) {
                player.setDeltaMovement(playerVel.x, 0, playerVel.z);
            }
        }
    }

    /**
     * Resuelve colisión en el eje X.
     */
    private void resolveX(Player player, double playerCenterX, double stepCenterX,
                          double overlapX, Vec3 playerVel) {
        double pushX;
        if (playerCenterX > stepCenterX) {
            // Empujar hacia +X
            pushX = overlapX + 0.01;
        } else {
            // Empujar hacia -X
            pushX = -(overlapX + 0.01);
        }

        player.setPos(player.getX() + pushX, player.getY(), player.getZ());

        // Cancelar velocidad en X
        player.setDeltaMovement(0, playerVel.y, playerVel.z);
    }

    /**
     * Resuelve colisión en el eje Z.
     */
    private void resolveZ(Player player, double playerCenterZ, double stepCenterZ,
                          double overlapZ, Vec3 playerVel) {
        double pushZ;
        if (playerCenterZ > stepCenterZ) {
            // Empujar hacia +Z
            pushZ = overlapZ + 0.01;
        } else {
            // Empujar hacia -Z
            pushZ = -(overlapZ + 0.01);
        }

        player.setPos(player.getX(), player.getY(), player.getZ() + pushZ);

        // Cancelar velocidad en Z
        player.setDeltaMovement(playerVel.x, playerVel.y, 0);
    }

    // ==================== CONFIGURACIÓN DE ENTIDAD ====================

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    // ==================== BOUND/UNBOUND ====================

    @Override
    protected void onBound(Player player) {
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                999999,
                3,
                false,
                false
        ));
        this.setNoGravity(true);
        this.noPhysics = false;
    }

    @Override
    protected void onUnbound(Player player) {
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        this.setNoGravity(false);
        this.noPhysics = false;
        this.setDeltaMovement(0, -0.5, 0);
    }

    @Override
    protected boolean canBound() {
        return true;
    }

    @Override
    public void handleNormalInteract(Player player) {
    }
}
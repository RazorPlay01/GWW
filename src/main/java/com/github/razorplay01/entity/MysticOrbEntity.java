package com.github.razorplay01.entity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;

public class MysticOrbEntity extends BaseInteractiveEntity {

    public MysticOrbEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }
    public static AttributeSupplier.Builder setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Double.POSITIVE_INFINITY);
    }
    @Override
    public void handleNormalInteract(Player player) {
        // Mensaje personalizado
        player.sendSystemMessage(Component.literal("§d✦ El Orbe Místico brilla ante tu presencia..."));
        player.sendSystemMessage(Component.literal("§7¿Deseas llevarlo contigo? (Shift + Click)"));
    }

    @Override
    protected void onBound(Player player) {
        player.sendSystemMessage(Component.literal("§5★ El Orbe Místico ahora te acompaña"));
        player.sendSystemMessage(Component.literal("§7Tu movimiento se ha ralentizado..."));

        // Efectos adicionales personalizados
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 999999, 0));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 999999, 3, false, false));
    }

    @Override
    protected void onUnbound(Player player) {
        player.sendSystemMessage(Component.literal("§5★ Has liberado el Orbe Místico"));
        player.sendSystemMessage(Component.literal("§7Tu velocidad ha vuelto a la normalidad"));

        // Remover efectos
        player.removeEffect(MobEffects.GLOWING);
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }

    @Override
    protected boolean canBound() {
        return true;
    }
}

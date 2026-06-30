package com.github.razorplay01.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PistaItem extends Item {

    public PistaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND) {
            ItemStack stack = player.getItemInHand(hand);

            String command = stack.get(ModComponents.PISTA_COMMAND);

            if (command != null && !command.isEmpty()) {
                // Ejecutar el comando como el jugador (en servidor)
                if (player instanceof ServerPlayer serverPlayer) {
                    // Añade "/" si no la tiene el usuario
                    String cmd = command.startsWith("/") ? command.substring(1) : command;
                    serverPlayer.server.getCommands().performPrefixedCommand(
                            serverPlayer.createCommandSourceStack(),
                            cmd
                    );
                }
            } else {
                player.sendSystemMessage(Component.literal("No hay comando configurado en este item."));
            }
        }

        return super.use(level, player, hand);
    }
}
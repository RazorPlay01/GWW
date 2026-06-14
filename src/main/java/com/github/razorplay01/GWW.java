package com.github.razorplay01;

import com.github.razorplay01.cam.core.ModifierRegistry;
import com.github.razorplay01.cam.starup.PluginFinder;
import com.github.razorplay01.extra.MinigameCommand;
import com.github.razorplay01.extra.MinigameState;
import com.github.razorplay01.network.ClientNetworkManager;
import com.github.razorplay01.network.FabricCustomPayload;
import com.github.razorplay01.network.ServerNetworkManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GWW implements ModInitializer, ClientModInitializer {
	public static final String MOD_ID = "gww";
	public static final String PACKET_BASE_CHANNEL = MOD_ID + ":packets_channel";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static MinigameState currentGame = null;

	@Override
	public void onInitialize() {
		FabricCustomPayload.register();
		ServerNetworkManager.register();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			MinigameCommand.register(dispatcher);
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (currentGame != null && currentGame.isActive()) {
				boolean sigue = currentGame.tick(server);
				if (!sigue) {
					currentGame = null;
				}
			}
		});
		LOGGER.info("Hello Fabric world!");
	}

	@Override
    public void onInitializeClient() {
		PluginFinder.loadPlugin();
		ClientNetworkManager.register();
    }
}
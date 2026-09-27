package de.biggamba.reconnect;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Client-Einstieg. Registriert /reconnect und lässt einmal pro Tick die Lobby-Prüfung laufen.
 */
public final class ReconnectClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Reconnect");

	@Override
	public void onInitializeClient() {
		ReconnectConfig config = ReconnectConfig.load();
		LobbyWatch watch = new LobbyWatch(config);

		ClientTickEvents.END_CLIENT_TICK.register(watch::tick);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register(
				ClientCommands.literal("reconnect")
						.executes(context -> {
							status(config);
							return 1;
						})
						.then(ClientCommands.literal("on").executes(context -> {
							config.enabled = true;
							config.save();

							if (config.server.isBlank()) {
								feedback("An. Es fehlt noch ein Ziel: /reconnect server <server> <sekunden>");
							} else {
								feedback("An. Ziel " + config.server + " nach " + config.seconds
										+ " Sekunden in der Lobby.");
							}

							return 1;
						}))
						.then(ClientCommands.literal("off").executes(context -> {
							config.enabled = false;
							config.save();
							feedback("Aus.");
							return 1;
						}))
						.then(ClientCommands.literal("server")
								.then(ClientCommands.argument("server", StringArgumentType.word())
										.then(ClientCommands.argument("sekunden", IntegerArgumentType.integer(1))
												.executes(context -> {
													config.server = StringArgumentType.getString(context, "server");
													config.seconds = IntegerArgumentType.getInteger(context, "sekunden");
													config.enabled = true;
													config.save();
													feedback("An. Ziel " + config.server + " nach " + config.seconds
															+ " Sekunden in der Lobby.");
													return 1;
												}))))));
	}

	private static void status(ReconnectConfig config) {
		String target = config.server.isBlank() ? "keins" : config.server;
		feedback((config.enabled ? "An" : "Aus") + ", Ziel " + target + ", " + config.seconds
				+ " Sekunden.");
		feedback("/reconnect server <server> <sekunden>  |  /reconnect on  |  /reconnect off");
	}

	static void feedback(String text) {
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.player != null) {
			minecraft.gui.hud.getChat().addClientSystemMessage(line(text));
		}
	}

	static Component line(String text) {
		return Component.literal("[Reconnect] " + text).withStyle(ChatFormatting.GRAY);
	}
}

package de.biggamba.reconnect;

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
 * Client-Einstieg. Einmal pro Tick läuft die Lobby-Prüfung, und {@code /reconnect} macht das
 * Fenster auf — mehr Befehle gibt es nicht, alles andere steht im Fenster.
 */
public final class ReconnectClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Reconnect");

	/**
	 * Aus einem Befehl heraus lässt sich kein Fenster öffnen: der Chat schließt sich danach und
	 * nimmt es gleich wieder mit. Also nur vormerken und im nächsten Tick aufmachen.
	 */
	private boolean openRequested;
	private int waitingTicks;

	@Override
	public void onInitializeClient() {
		ReconnectConfig config = ReconnectConfig.load();
		LobbyWatch watch = new LobbyWatch(config);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			watch.tick(client);

			if (!openRequested) {
				return;
			}

			if (client.gui.screen() == null) {
				openRequested = false;
				client.gui.setScreen(new ReconnectScreen(config));
			} else if (++waitingTicks > 40) {
				// Es ist etwas anderes offen. Nach zwei Sekunden verfällt der Wunsch, statt
				// irgendwann unvermittelt aufzuspringen.
				openRequested = false;
			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register(
				ClientCommands.literal("reconnect").executes(context -> {
					openRequested = true;
					waitingTicks = 0;
					return 1;
				})));
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

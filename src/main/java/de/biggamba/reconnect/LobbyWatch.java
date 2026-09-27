package de.biggamba.reconnect;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;

import de.biggamba.reconnect.mixin.PlayerTabOverlayAccessor;

/**
 * Prüft zweimal pro Sekunde, ob der Tab-Footer die Lobby nennt. Tut er das lange genug,
 * kommt eine Zählung im Chat und danach ein /server auf den eingestellten CityBuild.
 */
public final class LobbyWatch {
	private static final long CHECK_INTERVAL = 500L;

	private final ReconnectConfig config;

	private long lastCheck;
	private long lobbySince;
	private int lastCount = -1;
	private boolean sent;

	public LobbyWatch(ReconnectConfig config) {
		this.config = config;
	}

	public void tick(Minecraft minecraft) {
		if (!config.enabled || config.server.isBlank()) {
			reset();
			return;
		}

		long now = System.currentTimeMillis();

		if (now - lastCheck < CHECK_INTERVAL) {
			return;
		}

		lastCheck = now;

		ClientPacketListener connection = minecraft.getConnection();

		if (minecraft.player == null || connection == null || !inLobby(minecraft)) {
			reset();
			return;
		}

		if (lobbySince == 0L) {
			lobbySince = now;
			lastCount = -1;
			sent = false;
			return;
		}

		if (sent) {
			return;
		}

		int left = config.seconds - (int) ((now - lobbySince) / 1000L);

		if (left <= 0) {
			sent = true;
			message("Teleportiere zu " + config.server);
			connection.sendCommand("server " + config.server);
			return;
		}

		announce(left);
	}

	private void announce(int left) {
		if (left == lastCount) {
			return;
		}

		boolean tail = left <= 3;
		boolean step = left % 5 == 0;
		boolean first = lastCount == -1;

		if (!tail && !step && !first) {
			return;
		}

		lastCount = left;

		if (tail) {
			message(String.valueOf(left));
		} else {
			message("In " + left + " Sekunden geht es zu " + config.server);
		}
	}

	private boolean inLobby(Minecraft minecraft) {
		if (minecraft.gui == null) {
			return false;
		}

		PlayerTabOverlay tabList = minecraft.gui.hud.getTabList();

		if (!(tabList instanceof PlayerTabOverlayAccessor accessor)) {
			return false;
		}

		Component footer = accessor.reconnect$getFooter();

		if (footer == null) {
			return false;
		}

		return footer.getString().toLowerCase(Locale.ROOT).contains("lobby");
	}

	private void reset() {
		lobbySince = 0L;
		lastCount = -1;
		sent = false;
	}

	private static void message(String text) {
		ReconnectClient.feedback(text);
	}
}

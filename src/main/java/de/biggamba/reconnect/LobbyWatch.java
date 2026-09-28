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
 * Optional folgt danach, sobald der CityBuild erreicht ist, noch ein zweiter Befehl.
 */
public final class LobbyWatch {
	private static final long CHECK_INTERVAL = 500L;
	private static final long LANDING_TIMEOUT = 60_000L;

	private final ReconnectConfig config;

	private long lastCheck;
	private long lobbySince;
	private int lastCount = -1;
	private boolean sent;

	private boolean followPending;
	private long followDeadline;
	private long landedAt;

	public LobbyWatch(ReconnectConfig config) {
		this.config = config;
	}

	public void tick(Minecraft minecraft) {
		if (!config.enabled || config.server.isBlank()) {
			reset();
			followPending = false;
			return;
		}

		long now = System.currentTimeMillis();

		if (now - lastCheck < CHECK_INTERVAL) {
			return;
		}

		lastCheck = now;

		// Mitten im Serverwechsel gibt es kurz keinen Spieler und damit keine Verbindung. Das ist
		// kein Disconnect, der Zustand muss also erhalten bleiben.
		ClientPacketListener connection = minecraft.getConnection();

		if (connection == null) {
			return;
		}

		if (followPending && landedAt == 0L && now > followDeadline) {
			followPending = false;
		}

		String footer = footer(minecraft);

		if (footer.contains("lobby")) {
			landedAt = 0L;
			inLobby(connection, now);
			return;
		}

		reset();

		if (followPending) {
			afterLanding(connection, footer, now);
		}
	}

	private void inLobby(ClientPacketListener connection, long now) {
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
			followPending = config.farmEnabled && !config.farmCommand.isBlank();
			followDeadline = now + LANDING_TIMEOUT;
			return;
		}

		announce(left, "geht es zu " + config.server);
	}

	private void afterLanding(ClientPacketListener connection, String footer, long now) {
		if (landedAt == 0L) {
			if (footer.isBlank()) {
				return;
			}

			landedAt = now;
			lastCount = -1;
		}

		int left = config.farmSeconds - (int) ((now - landedAt) / 1000L);

		if (left <= 0) {
			followPending = false;
			landedAt = 0L;
			message("Führe aus: /" + config.farmCommand);
			connection.sendCommand(config.farmCommand);
			return;
		}

		announce(left, "kommt /" + config.farmCommand);
	}

	private void announce(int left, String what) {
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
			message("In " + left + " Sekunden " + what);
		}
	}

	private String footer(Minecraft minecraft) {
		if (minecraft.gui == null) {
			return "";
		}

		PlayerTabOverlay tabList = minecraft.gui.hud.getTabList();

		if (!(tabList instanceof PlayerTabOverlayAccessor accessor)) {
			return "";
		}

		Component footer = accessor.reconnect$getFooter();
		return footer == null ? "" : footer.getString().toLowerCase(Locale.ROOT);
	}

	private void reset() {
		lobbySince = 0L;
		sent = false;
	}

	private static void message(String text) {
		ReconnectClient.feedback(text);
	}
}

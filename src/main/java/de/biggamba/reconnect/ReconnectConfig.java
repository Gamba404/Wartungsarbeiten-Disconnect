package de.biggamba.reconnect;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Die Einstellungen, die das Fenster schreibt und liest: {@code config/reconnect.json}.
 *
 * <p>Wer von der Befehlsfassung kommt, behält seine Werte — die alten Schlüssel {@code farm…}
 * werden einmal übernommen und danach nicht mehr geschrieben.
 */
public final class ReconnectConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final int MAX_REMEMBERED = 12;
	private static final Path PATH =
			FabricLoader.getInstance().getConfigDir().resolve("reconnect.json");

	/** Der große Schalter im Fenster. */
	public boolean enabled = false;

	/** Ziel für {@code /server}, so wie es im Feld steht, zum Beispiel {@code cb1}. */
	public String server = "";

	/** Wartezeit in der Lobby, bevor gesprungen wird. */
	public int seconds = 15;

	/** Der zweite Schalter: nach der Landung noch zum Home-Punkt. */
	public boolean homeEnabled = false;

	/** Wartezeit auf dem CityBuild, bevor der Home-Befehl rausgeht. */
	public int homeSeconds = 10;

	/**
	 * Der Name des Home-Punkts, also nur {@code test}. Daraus wird {@code /home test}.
	 *
	 * <p>Steht hier etwas mit Leerzeichen oder ein führender Schrägstrich, wird es als ganzer
	 * Befehl genommen — so bleibt der alte Weg offen, ohne dass das Feld danach aussieht.
	 */
	public String home = "";

	/**
	 * Servernamen, die du selbst schon benutzt hast — und alles, was du hier von Hand einträgst.
	 *
	 * <p>Der Grund: PrimeBlocks bietet beim Tab nur {@code cb1}, {@code cb2} und {@code cb3} an,
	 * gebraucht wird aber {@code cb1a}. Was der Server nicht nennt, kann der Mod nicht erfinden —
	 * also merkt er sich, was tatsächlich eingetragen wurde, und hängt es beim nächsten Tab unten
	 * an die Liste.
	 */
	public List<String> serverList = new ArrayList<>();

	// Die Schlüssel der Befehlsfassung. Sie stehen nur hier, damit eine alte reconnect.json
	// einmal übernommen werden kann; nach dem ersten Speichern sind sie aus der Datei raus.
	private Boolean farmEnabled;
	private Integer farmSeconds;
	private String farmCommand;

	/** Schiebt einen benutzten Servernamen nach vorn. Doppelte und Groß/Klein zählen als gleich. */
	public void rememberServer(String name) {
		String value = name == null ? "" : name.trim();

		if (value.isBlank()) {
			return;
		}

		serverList.removeIf(known -> known.equalsIgnoreCase(value));
		serverList.addFirst(value);

		while (serverList.size() > MAX_REMEMBERED) {
			serverList.removeLast();
		}
	}

	public static ReconnectConfig load() {
		if (!Files.exists(PATH)) {
			ReconnectConfig config = new ReconnectConfig();
			config.save();
			return config;
		}

		try (Reader reader = Files.newBufferedReader(PATH)) {
			ReconnectConfig config = GSON.fromJson(reader, ReconnectConfig.class);

			if (config == null) {
				config = new ReconnectConfig();
			}

			config.repair();
			config.migrate();
			return config;
		} catch (Exception e) {
			ReconnectClient.LOGGER.warn("Konnte {} nicht lesen, nehme Standardwerte.", PATH, e);
			return new ReconnectConfig();
		}
	}

	public void save() {
		repair();

		try {
			Files.createDirectories(PATH.getParent());

			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(this, writer);
			}
		} catch (Exception e) {
			ReconnectClient.LOGGER.warn("Konnte {} nicht schreiben.", PATH, e);
		}
	}

	/**
	 * Der Befehl, der nach der Landung rausgeht — ohne Schrägstrich, so wie
	 * {@code ClientPacketListener#sendCommand} ihn haben will.
	 */
	public String homeCommand() {
		String value = home.trim();

		if (value.startsWith("/")) {
			value = value.substring(1).trim();
		}

		if (value.isBlank()) {
			return "";
		}

		return value.contains(" ") ? value : "home " + value;
	}

	/**
	 * Übernimmt die Werte der Befehlsfassung. Aus {@code farmCommand: "home 2spawner"} wird der
	 * Home-Punkt {@code 2spawner} — im Fenster steht der Punkt, nicht der ganze Befehl.
	 *
	 * <p>Nur solange im Fenster noch kein Home-Punkt steht: ist einer gesetzt, ist die Datei
	 * längst die neue.
	 */
	private void migrate() {
		if (farmCommand != null && !farmCommand.isBlank() && home.isBlank()) {
			String value = farmCommand.trim();

			if (value.startsWith("/")) {
				value = value.substring(1).trim();
			}

			home = value.toLowerCase(Locale.ROOT).startsWith("home ")
					? value.substring(5).trim() : value;

			if (farmEnabled != null) {
				homeEnabled = farmEnabled;
			}

			if (farmSeconds != null && farmSeconds >= 1) {
				homeSeconds = farmSeconds;
			}

			ReconnectClient.LOGGER.info("Alte Einstellungen übernommen, Home-Punkt: {}", home);
		}

		farmEnabled = null;
		farmSeconds = null;
		farmCommand = null;
	}

	private void repair() {
		if (server == null) {
			server = "";
		}

		if (home == null) {
			home = "";
		}

		if (serverList == null) {
			serverList = new ArrayList<>();
		}

		serverList.removeIf(entry -> entry == null || entry.isBlank());

		server = server.trim();
		home = home.trim();

		if (seconds < 1) {
			seconds = 15;
		}

		if (homeSeconds < 1) {
			homeSeconds = 10;
		}
	}
}

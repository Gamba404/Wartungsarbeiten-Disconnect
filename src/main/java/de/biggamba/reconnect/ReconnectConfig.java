package de.biggamba.reconnect;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

public final class ReconnectConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH =
			FabricLoader.getInstance().getConfigDir().resolve("reconnect.json");

	public boolean enabled = false;
	public String server = "";
	public int seconds = 15;
	public boolean farmEnabled = false;
	public int farmSeconds = 10;
	public String farmCommand = "";

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

			if (config.server == null) {
				config.server = "";
			}

			if (config.seconds < 1) {
				config.seconds = 15;
			}

			if (config.farmCommand == null) {
				config.farmCommand = "";
			}

			if (config.farmSeconds < 1) {
				config.farmSeconds = 10;
			}

			return config;
		} catch (Exception e) {
			ReconnectClient.LOGGER.warn("Konnte {} nicht lesen, nehme Standardwerte.", PATH, e);
			return new ReconnectConfig();
		}
	}

	public void save() {
		try {
			Files.createDirectories(PATH.getParent());

			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(this, writer);
			}
		} catch (Exception e) {
			ReconnectClient.LOGGER.warn("Konnte {} nicht schreiben.", PATH, e);
		}
	}
}

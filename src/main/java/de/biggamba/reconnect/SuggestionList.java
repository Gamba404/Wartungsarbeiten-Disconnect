package de.biggamba.reconnect;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

/**
 * Die Vorschlagsliste unter einem Feld — dasselbe, was Tab im Chat macht, plus eigene Einträge.
 *
 * <p>Der Weg ist der gleiche wie beim Chat: Die Eingabe wird zu einer ganzen Befehlszeile
 * ergänzt ({@code home af}), gegen den Befehlsbaum des Servers geparst, und für Argumente, die
 * der Server selbst vorschlägt, schickt Brigadier eine Anfrage raus.
 *
 * <p>Darunter hängt die Liste, die der Mod sich gemerkt hat. Die braucht es, weil PrimeBlocks
 * beim {@code /server} nur {@code cb1}, {@code cb2}, {@code cb3} nennt, der Sprung aber auf
 * {@code cb1a} geht: was der Server verschweigt, steht sonst nirgends.
 *
 * <p>Die Antwort trifft auf dem Netzwerk-Thread ein, deshalb geht die Auswertung über
 * {@link Minecraft#execute}. Jede Anfrage bekommt eine Nummer; trifft eine alte Antwort später
 * ein als eine neue, wird sie verworfen.
 */
public final class SuggestionList {
	private static final int ROW_HEIGHT = 12;
	private static final int MAX_ROWS = 8;
	private static final int COLOUR_BOX = 0xE0000000;
	private static final int COLOUR_EDGE = 0xFF3F3F3F;
	private static final int COLOUR_ENTRY = 0xFFAAAAAA;
	private static final int COLOUR_OWN = 0xFF8FC7E8;
	private static final int COLOUR_SELECTED = 0xFFFFFF55;

	private final Minecraft minecraft = Minecraft.getInstance();

	private List<Entry> entries = List.of();
	private List<String> extras = List.of();
	private String line = "";
	private String prefix = "";
	private String typed = "";
	private String status = "";
	private int selected;
	private int offset;
	private int pending;

	private int x;
	private int y;
	private int width;

	public boolean isOpen() {
		return !entries.isEmpty();
	}

	/** Kurze Rückmeldung, wenn nichts kam — „keine Vorschläge", „nicht verbunden". */
	public String status() {
		return status;
	}

	/**
	 * Fragt den Server und mischt die eigenen Einträge dazu.
	 *
	 * @param prefix der Teil der Befehlszeile vor der Eingabe, mit Leerzeichen am Ende,
	 *               zum Beispiel {@code "home "}
	 * @param value  was im Feld steht
	 * @param extras eigene Vorschläge, die nach denen des Servers angehängt werden
	 */
	public void request(String prefix, String value, List<String> extras, int x, int y, int width) {
		this.prefix = prefix;
		this.line = prefix + value;
		this.typed = value;
		this.extras = List.copyOf(extras);
		this.x = x;
		this.y = y;
		this.width = width;

		close();

		ClientPacketListener connection = minecraft.getConnection();

		if (connection == null) {
			// Ohne Server bleibt immer noch die eigene Liste.
			show(List.of());
			status = entries.isEmpty() ? "nicht verbunden" : "";
			return;
		}

		status = "frage den Server …";
		int id = ++pending;

		CommandDispatcher<ClientSuggestionProvider> dispatcher = connection.getCommands();
		ParseResults<ClientSuggestionProvider> parse =
				dispatcher.parse(line, connection.getSuggestionsProvider());

		dispatcher.getCompletionSuggestions(parse, line.length())
				.whenComplete((suggestions, error) -> minecraft.execute(() -> {
					if (id != pending) {
						return;
					}

					show(error != null || suggestions == null ? List.of() : suggestions.getList());

					if (!entries.isEmpty()) {
						status = "";
					} else if (error != null) {
						status = "der Server antwortet nicht";
					} else {
						status = "keine Vorschläge";
					}
				}));
	}

	/** Baut die Liste: erst was der Server sagt, dann die eigenen Namen, ohne Doppelte. */
	private void show(List<Suggestion> fromServer) {
		List<Entry> list = new ArrayList<>();

		for (Suggestion suggestion : fromServer) {
			list.add(new Entry(suggestion.getText(), suggestion));
		}

		String lower = typed.toLowerCase(Locale.ROOT);

		for (String own : extras) {
			if (!own.toLowerCase(Locale.ROOT).startsWith(lower)) {
				continue;
			}

			if (list.stream().noneMatch(entry -> entry.text().equalsIgnoreCase(own))) {
				list.add(new Entry(own, null));
			}
		}

		entries = List.copyOf(list);
		selected = 0;
		offset = 0;
	}

	public void close() {
		entries = List.of();
		selected = 0;
		offset = 0;
	}

	public void clearStatus() {
		status = "";
	}

	public void move(int delta) {
		if (entries.isEmpty()) {
			return;
		}

		selected = Math.floorMod(selected + delta, entries.size());
		offset = Math.min(Math.max(offset, selected - MAX_ROWS + 1), selected);
	}

	/** Der neue Feldinhalt, oder {@code null} wenn nichts ausgewählt ist. */
	public String applySelected() {
		if (entries.isEmpty()) {
			return null;
		}

		Entry entry = entries.get(selected);
		close();

		if (entry.suggestion() == null) {
			// Eigener Eintrag: der steht für sich, da gibt es keine Befehlszeile zu ergänzen.
			return entry.text();
		}

		String applied = entry.suggestion().apply(line);
		return applied.startsWith(prefix) ? applied.substring(prefix.length()) : applied;
	}

	/** Nimmt einen Klick entgegen; liefert den neuen Feldinhalt oder {@code null}. */
	public String clicked(double mouseX, double mouseY) {
		if (entries.isEmpty()) {
			return null;
		}

		int rows = visibleRows();

		if (mouseX < x || mouseX > x + boxWidth() || mouseY < y || mouseY > y + rows * ROW_HEIGHT) {
			return null;
		}

		int row = (int) ((mouseY - y) / ROW_HEIGHT);
		selected = Math.min(offset + row, entries.size() - 1);
		return applySelected();
	}

	public void render(GuiGraphicsExtractor context, Font font) {
		if (entries.isEmpty()) {
			return;
		}

		int rows = visibleRows();
		int boxWidth = boxWidth();
		int bottom = y + rows * ROW_HEIGHT;

		context.fill(x - 1, y - 1, x + boxWidth + 1, bottom + 1, COLOUR_EDGE);
		context.fill(x, y, x + boxWidth, bottom, COLOUR_BOX);

		for (int row = 0; row < rows; row++) {
			int index = offset + row;
			Entry entry = entries.get(index);
			int colour = index == selected ? COLOUR_SELECTED
					: entry.suggestion() == null ? COLOUR_OWN : COLOUR_ENTRY;
			context.text(font, entry.text(), x + 3, y + 2 + row * ROW_HEIGHT, colour, false);
		}

		if (entries.size() > rows + offset) {
			context.text(font, "…", x + boxWidth - 6, bottom - ROW_HEIGHT + 2, COLOUR_ENTRY, false);
		}
	}

	private int visibleRows() {
		return Math.min(entries.size() - offset, MAX_ROWS);
	}

	private int boxWidth() {
		Font font = minecraft.font;
		int longest = width;

		for (Entry entry : entries) {
			longest = Math.max(longest, font.width(entry.text()) + 10);
		}

		return longest;
	}

	/** Ein Eintrag. Ohne {@code suggestion} kommt er aus der eigenen Liste. */
	private record Entry(String text, Suggestion suggestion) {
	}
}

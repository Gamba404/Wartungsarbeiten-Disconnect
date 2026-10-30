package de.biggamba.reconnect;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Das Fenster. Vier Felder, zwei Schalter, ein Knopf — und damit ist alles eingestellt, was
 * vorher über {@code /reconnect server …} und {@code /reconnect farm …} lief.
 *
 * <p>Tab in „Server" oder „Home-Punkt" fragt den Server nach echten Vorschlägen, genau wie im
 * Chat. Gespeichert wird beim Schließen, egal ob über „Fertig" oder Esc.
 *
 * <p>Aller eigener Text wird ohne Schatten gezeichnet: auf der hellen Fläche legt der Schatten
 * sich sonst genau auf die Buchstaben.
 */
public final class ReconnectScreen extends Screen {
	private static final int PANEL_WIDTH = 250;
	private static final int PANEL_HEIGHT = 180;
	private static final int PADDING = 12;
	private static final int FIELD_HEIGHT = 20;
	private static final int SECONDS_WIDTH = 64;
	private static final int GAP = 8;

	private static final int TITLE_Y = 9;
	private static final int SEPARATOR_TOP_Y = 23;
	private static final int LABEL_SERVER_Y = 32;
	private static final int FIELD_SERVER_Y = 43;
	private static final int LABEL_HOME_Y = 70;
	private static final int FIELD_HOME_Y = 81;
	private static final int TOGGLE_Y = 109;
	private static final int SEPARATOR_BOTTOM_Y = 135;
	private static final int DONE_Y = 141;
	private static final int HINT_Y = 167;

	private static final int COLOUR_DROP = 0x50000000;
	private static final int COLOUR_PANEL = 0xFFC6C6C6;
	private static final int COLOUR_EDGE = 0xFF000000;
	private static final int COLOUR_LIGHT = 0xFFFFFFFF;
	private static final int COLOUR_SHADE = 0xFF555555;
	private static final int COLOUR_GROOVE = 0xFF8B8B8B;
	private static final int COLOUR_TITLE = 0xFF2F2F2F;
	private static final int COLOUR_LABEL = 0xFF404040;
	private static final int COLOUR_HINT = 0xFF5A5A5A;
	private static final int COLOUR_MISSING = 0xFF8B2B2B;

	private final ReconnectConfig config;
	private final SuggestionList suggestions = new SuggestionList();

	private EditBox serverBox;
	private EditBox secondsBox;
	private EditBox homeBox;
	private EditBox homeSecondsBox;
	private EditBox suggestionTarget;
	private Button enabledButton;
	private Button homeButton;

	private int panelX;
	private int panelY;

	public ReconnectScreen(ReconnectConfig config) {
		super(Component.literal("Reconnect"));
		this.config = config;
	}

	@Override
	protected void init() {
		panelX = (width - PANEL_WIDTH) / 2;
		panelY = (height - PANEL_HEIGHT) / 2;

		int left = panelX + PADDING;
		int contentWidth = PANEL_WIDTH - PADDING * 2;
		int nameWidth = contentWidth - SECONDS_WIDTH - GAP;
		int secondsLeft = left + nameWidth + GAP;

		// Bei einer Fenstergröße läuft init() noch einmal. Dann zählt, was schon getippt wurde,
		// nicht der gespeicherte Stand.
		String server = serverBox == null ? config.server : serverBox.getValue();
		String serverSeconds = secondsBox == null ? String.valueOf(config.seconds) : secondsBox.getValue();
		String home = homeBox == null ? config.home : homeBox.getValue();
		String homeSeconds = homeSecondsBox == null
				? String.valueOf(config.homeSeconds) : homeSecondsBox.getValue();

		suggestions.close();
		suggestionTarget = null;

		serverBox = textField(left, panelY + FIELD_SERVER_Y, nameWidth, server, "cb1");
		secondsBox = numberField(secondsLeft, panelY + FIELD_SERVER_Y, serverSeconds);
		homeBox = textField(left, panelY + FIELD_HOME_Y, nameWidth, home, "afk");
		homeSecondsBox = numberField(secondsLeft, panelY + FIELD_HOME_Y, homeSeconds);

		int toggleWidth = (contentWidth - GAP) / 2;

		enabledButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
			config.enabled = !config.enabled;
			updateToggles();
		}).bounds(left, panelY + TOGGLE_Y, toggleWidth, 20).build());

		homeButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
			config.homeEnabled = !config.homeEnabled;
			updateToggles();
		}).bounds(left + toggleWidth + GAP, panelY + TOGGLE_Y, contentWidth - toggleWidth - GAP, 20)
				.build());

		updateToggles();

		addRenderableWidget(Button.builder(Component.literal("Fertig"), button -> onClose())
				.bounds(left, panelY + DONE_Y, contentWidth, 20).build());

		setInitialFocus(serverBox);
	}

	private EditBox textField(int x, int y, int width, String value, String hint) {
		EditBox box = addRenderableWidget(
				new EditBox(font, x, y, width, FIELD_HEIGHT, Component.literal(hint)));
		box.setMaxLength(48);
		box.setValue(value);
		// GRAY statt DARK_GRAY: der Hinweis steht im schwarzen Feld, dunkelgrau liest sich dort
		// nicht mehr.
		box.setHint(Component.literal(hint).withStyle(ChatFormatting.GRAY));
		box.setResponder(typed -> {
			suggestions.close();
			suggestions.clearStatus();
		});
		return box;
	}

	private EditBox numberField(int x, int y, String value) {
		EditBox box = addRenderableWidget(
				new EditBox(font, x, y, SECONDS_WIDTH, FIELD_HEIGHT, Component.literal("Sekunden")));
		box.setMaxLength(4);
		box.setValue(value);

		// Kein setFilter mehr in 26.3, also beim Tippen aussieben. Der Vergleich bricht die
		// Rückkopplung ab: setValue ruft den Responder erneut auf.
		box.setResponder(typed -> {
			String digits = typed.replaceAll("[^0-9]", "");

			if (!digits.equals(typed)) {
				box.setValue(digits);
			}
		});

		return box;
	}

	private void updateToggles() {
		enabledButton.setMessage(toggleLabel("Reconnect", config.enabled));
		homeButton.setMessage(toggleLabel("Home danach", config.homeEnabled));
	}

	private static Component toggleLabel(String name, boolean on) {
		return Component.literal(name + ": " + (on ? "AN" : "AUS"))
				.withStyle(on ? ChatFormatting.GREEN : ChatFormatting.RED);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY,
			float partialTick) {
		drawPanel(context);
		super.extractRenderState(context, mouseX, mouseY, partialTick);

		int left = panelX + PADDING;
		int secondsLeft = panelX + PANEL_WIDTH - PADDING - SECONDS_WIDTH;

		centred(context, "Reconnect", TITLE_Y, COLOUR_TITLE);
		label(context, "Server", left, LABEL_SERVER_Y);
		label(context, "Sekunden", secondsLeft, LABEL_SERVER_Y);
		label(context, "Home-Punkt", left, LABEL_HOME_Y);
		label(context, "Sekunden", secondsLeft, LABEL_HOME_Y);

		String status = suggestions.status();
		centred(context, status.isBlank() ? hint() : status, HINT_Y,
				status.isBlank() ? hintColour() : COLOUR_HINT);

		// Zuletzt, damit die Liste über den Feldern und Knöpfen darunter liegt.
		suggestions.render(context, font);
	}

	/** Die Zeile ganz unten: was gleich passieren würde, oder was gerade fehlt. */
	private String hint() {
		if (!config.enabled) {
			return "Aus — es passiert nichts.";
		}

		String server = serverBox.getValue().trim();

		if (server.isBlank()) {
			return "Es fehlt der Server.";
		}

		if (config.homeEnabled && homeBox.getValue().isBlank()) {
			return "Es fehlt der Home-Punkt.";
		}

		if (isTextFieldFocused()) {
			return "Tab fragt den Server, was es gibt.";
		}

		String text = "Lobby: " + seconds(secondsBox, config.seconds) + " s, dann /server " + server;

		if (!config.homeEnabled) {
			return text;
		}

		return text + ", dann /" + commandFor(homeBox.getValue().trim());
	}

	private int hintColour() {
		boolean missing = config.enabled && (serverBox.getValue().isBlank()
				|| config.homeEnabled && homeBox.getValue().isBlank());
		return missing ? COLOUR_MISSING : COLOUR_HINT;
	}

	private static String commandFor(String home) {
		String value = home.startsWith("/") ? home.substring(1).trim() : home;
		return value.contains(" ") ? value : "home " + value;
	}

	private void drawPanel(GuiGraphicsExtractor context) {
		int right = panelX + PANEL_WIDTH;
		int bottom = panelY + PANEL_HEIGHT;

		context.fill(panelX + 4, panelY + 4, right + 4, bottom + 4, COLOUR_DROP);
		context.fill(panelX - 1, panelY - 1, right + 1, bottom + 1, COLOUR_EDGE);
		context.fill(panelX, panelY, right, bottom, COLOUR_PANEL);

		// Die zwei Kanten, die ein Minecraft-Fenster ausmachen: hell oben und links, dunkel
		// unten und rechts.
		context.fill(panelX, panelY, right - 1, panelY + 1, COLOUR_LIGHT);
		context.fill(panelX, panelY, panelX + 1, bottom - 1, COLOUR_LIGHT);
		context.fill(panelX + 1, bottom - 1, right, bottom, COLOUR_SHADE);
		context.fill(right - 1, panelY + 1, right, bottom, COLOUR_SHADE);

		groove(context, SEPARATOR_TOP_Y);
		groove(context, SEPARATOR_BOTTOM_Y);
	}

	/** Eine eingelassene Linie: dunkel oben, hell darunter. */
	private void groove(GuiGraphicsExtractor context, int y) {
		int left = panelX + PADDING;
		int right = panelX + PANEL_WIDTH - PADDING;
		context.fill(left, panelY + y, right, panelY + y + 1, COLOUR_GROOVE);
		context.fill(left, panelY + y + 1, right, panelY + y + 2, COLOUR_LIGHT);
	}

	private void label(GuiGraphicsExtractor context, String text, int x, int y) {
		context.text(font, text, x, panelY + y, COLOUR_LABEL, false);
	}

	private void centred(GuiGraphicsExtractor context, String text, int y, int colour) {
		context.text(font, text, panelX + (PANEL_WIDTH - font.width(text)) / 2, panelY + y,
				colour, false);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (suggestions.isOpen()) {
			if (event.isEscape()) {
				suggestions.close();
				return true;
			}

			if (event.isUp()) {
				suggestions.move(-1);
				return true;
			}

			if (event.isDown()) {
				suggestions.move(1);
				return true;
			}

			if (event.isCycleFocus()) {
				suggestions.move(event.hasShiftDown() ? -1 : 1);
				return true;
			}

			if (event.isConfirmation()) {
				applySuggestion(suggestions.applySelected());
				return true;
			}
		} else if (event.isCycleFocus() && isTextFieldFocused()) {
			ask((EditBox) getFocused());
			return true;
		}

		return super.keyPressed(event);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (suggestions.isOpen()) {
			String value = suggestions.clicked(event.x(), event.y());

			if (value != null) {
				applySuggestion(value);
				return true;
			}

			suggestions.close();
		}

		return super.mouseClicked(event, doubleClick);
	}

	private boolean isTextFieldFocused() {
		GuiEventListener focused = getFocused();
		return focused == serverBox || focused == homeBox;
	}

	/**
	 * Fragt für ein Feld an. Vor der Eingabe steht der Befehl, zu dem sie gehört — der Server
	 * kennt {@code server <ziel>} und {@code home <punkt>}, nicht das Feld.
	 */
	private void ask(EditBox box) {
		suggestionTarget = box;
		String value = box.getValue();

		if (value.startsWith("/")) {
			value = value.substring(1);
		}

		// Nur beim Server kommt die eigene Liste dazu: die Homes nennt PrimeBlocks vollständig,
		// die Servernamen nicht.
		String prefix = box == serverBox ? "server " : value.contains(" ") ? "" : "home ";
		List<String> extras = box == serverBox ? config.serverList : List.of();

		suggestions.request(prefix, value, extras, box.getX(), box.getY() + FIELD_HEIGHT + 1,
				box.getWidth());
	}

	private void applySuggestion(String value) {
		if (value == null || suggestionTarget == null) {
			return;
		}

		suggestionTarget.setValue(value);
		suggestionTarget.moveCursorToEnd(false);
		suggestions.close();
		suggestions.clearStatus();
	}

	@Override
	public void onClose() {
		config.server = serverBox.getValue().trim();
		config.rememberServer(config.server);
		config.home = homeBox.getValue().trim();
		config.seconds = seconds(secondsBox, config.seconds);
		config.homeSeconds = seconds(homeSecondsBox, config.homeSeconds);
		config.save();
		super.onClose();
	}

	private static int seconds(EditBox box, int fallback) {
		try {
			int value = Integer.parseInt(box.getValue().trim());
			return value < 1 ? fallback : value;
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}

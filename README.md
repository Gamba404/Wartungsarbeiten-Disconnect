# Reconnect

Holt dich aus der PrimeBlocks-Lobby zurück auf den CityBuild, den du eingestellt hast, und
schickt dich auf Wunsch gleich weiter zu deinem Home-Punkt.

Gedacht für den Fall, dass dich der Server nachts oder nach einer Wartung in die Lobby wirft,
während du AFK in der Farmwelt stehst.

Eingestellt wird alles in einem Fenster — **`/reconnect`** macht es auf. Eine Anleitung für
Spieler steht in [ANLEITUNG_Disconnect.md](ANLEITUNG_Disconnect.md).

Der Mod ist eine reine Rückkehr aus der Lobby. Nach einem vollständigen Verbindungsabbruch
verbindet er nicht neu.

## Das Fenster

```
                    Reconnect

   Server                        Sekunden
   [ cb1a                   ]    [ 15   ]

   Home-Punkt                    Sekunden
   [ afk                    ]    [ 10   ]

   [ Reconnect: AN ]  [ Home danach: AN ]

   [            Fertig             ]

   Lobby: 15 s, dann /server cb1a, dann /home afk
```

Die Zeile unten rechnet mit, während du tippst — da steht immer, was gleich passieren würde,
oder was noch fehlt. Esc und „Fertig" speichern beides.

| Feld | Was rein muss | Was daraus wird |
|---|---|---|
| Server | `cb1a` | `/server cb1a`, sobald du in der Lobby stehst |
| Sekunden | `15` | So lange wird in der Lobby gewartet |
| Home-Punkt | `afk` | `/home afk`, sobald du wieder auf dem CityBuild bist |
| Sekunden | `10` | So lange wird auf dem CityBuild gewartet |

Beim Home-Punkt reicht der Name. Steht da etwas mit Leerzeichen (`warp farm2`) oder ein
Schrägstrich vorne, wird es als ganzer Befehl genommen.

Die beiden Schalter wirken sofort: **Reconnect** ist der große An/Aus-Schalter, **Home danach**
nur die zweite Hälfte.

## Tab fragt den Server

In „Server" und „Home-Punkt" öffnet **Tab** eine Vorschlagsliste: deine echten Homes, die echten
Server. Das sind keine ausgedachten Namen — der Mod stellt dieselbe Anfrage, die der Chat stellt,
wenn du dort hinter `/home ` Tab drückst, und zeigt, was zurückkommt.

| Taste | |
|---|---|
| Tab | Liste öffnen, dann weiterblättern |
| Shift+Tab | zurückblättern |
| Pfeil hoch/runter | auswählen |
| Enter oder Klick | übernehmen |
| Esc | Liste zu (das Fenster bleibt offen) |

Tab ist nur eine Hilfe. Tippen kannst du immer, was du willst — auch Namen, die in keiner Liste
stehen.

### Eigene Servernamen

Bei den Homes nennt PrimeBlocks alles. Beim `/server` nicht: da kommen `cb1`, `cb2`, `cb3`, auf
die Unterserver wie `cb1a` kommt man nur mit Rang, und die nennt der Server nicht. Deshalb hängt
unter den Vorschlägen noch eine eigene Liste — **hellblau** abgesetzt:

* Jeder Name, den du im Feld stehen lässt, wird beim Schließen gemerkt (die letzten zwölf).
* Von Hand pflegen geht auch: `serverList` in `config/reconnect.json`.

## Ablauf

1. Der Footer der Tabliste nennt die Lobby. Der Mod wartet die eingestellte Zeit ab.
2. Im Chat läuft eine Zählung: alle 5 Sekunden eine Zeile, die letzten drei einzeln
   (`3`, `2`, `1`). Dann geht `/server <ziel>` raus.
3. Ist „Home danach" an, startet nach der Landung eine zweite Zählung. Am Ende folgt der
   Home-Befehl.

Verlässt du die Lobby vor Ablauf, bricht die Zählung ab.

Der Home-Befehl gilt nur nach einem Sprung, den der Mod selbst ausgelöst hat. Gehst du von Hand
auf den CityBuild, passiert nichts. Wird der CityBuild nicht innerhalb von 60 Sekunden erreicht
(Wartung, falscher Servername), verfällt der Befehl.

## Woran der Mod die Lobby erkennt

PrimeBlocks schreibt den aktuellen Ort in den Tab-Footer:

```
Du befindest dich derzeit auf: Lobby-1
```

Steht dort `Lobby`, läuft die Zählung. Als gelandet gilt der Mod, sobald der Footer nicht mehr
leer ist und keine Lobby nennt. Auf einem CityBuild passiert sonst nichts.

## Einstellungen

`config/reconnect.json`, wird beim ersten Start angelegt. Alles davon steht auch im Fenster,
bis auf `serverList`:

| Schlüssel | Standard | |
|---|---|---|
| `enabled` | `false` | Großer An/Aus-Schalter |
| `server` | `""` | Ziel für `/server`, z. B. `cb1a` |
| `seconds` | `15` | Wartezeit in der Lobby |
| `homeEnabled` | `false` | Nach der Rückkehr zum Home-Punkt |
| `homeSeconds` | `10` | Wartezeit auf dem CityBuild |
| `home` | `""` | Name des Home-Punkts, z. B. `afk` |
| `serverList` | `[]` | Eigene Servernamen für die Tab-Liste, zuletzt benutzter zuerst |

Wer von der Befehlsfassung kommt, behält seine Werte: die alten Schlüssel `farmEnabled`,
`farmSeconds` und `farmCommand` werden beim ersten Start einmal übernommen (aus
`farmCommand: "home 2spawner"` wird der Home-Punkt `2spawner`) und danach nicht mehr geschrieben.

## Änderungen

* **Fenster statt Befehle.** `/reconnect server …` und `/reconnect farm …` gibt es nicht mehr;
  `/reconnect` öffnet das Fenster, alles andere steht dort drin. Einen eigenen Hotkey gibt es
  bewusst nicht — dafür stellt man zu selten etwas um.
* **Tab-Vervollständigung** für Server und Home-Punkt, dieselbe Quelle wie im Chat.
* **Eigene Serverliste** für die Namen, die der Server nicht nennt.
* Der Stand vor dem Fenster hängt als Tag `stand-chat-befehle` im Repo.

## Bauen

```bash
./gradlew build
```

Ergebnis: `build/libs/reconnect-26.3.1.jar`. Braucht Minecraft **26.3**, Fabric Loader ≥ 0.19.3,
Fabric API und JDK 25 — auch für Gradle selbst, Loom 1.18 startet sonst nicht:

```bash
JAVA_HOME="C:/Users/Chef/.jdks/jdk-25.0.3+9" ./gradlew build
```

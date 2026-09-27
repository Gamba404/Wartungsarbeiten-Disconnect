# Reconnect

Holt dich aus der PrimeBlocks-Lobby zurück auf den CityBuild, den du eingestellt hast.

Stehst du in der Lobby, zählt der Mod im Chat herunter und schickt dann `/server <ziel>`. Gedacht
für den Fall, dass dich der Server nachts in die Lobby wirft, während du AFK stehst.

## Befehle

```
/reconnect server <server> <sekunden>   Ziel und Wartezeit setzen, schaltet gleich an
/reconnect on                           anschalten
/reconnect off                          ausschalten
/reconnect                              zeigt den aktuellen Stand
```

Beispiel: `/reconnect server cb1b 15` — 15 Sekunden nachdem der Mod dich in der Lobby sieht, geht
es mit `/server cb1b` zurück.

Beim Tippen blendet Minecraft die erwarteten Argumente grau ein (`<server>`, `<sekunden>`).

## Die Zählung

Alle 5 Sekunden eine Zeile im Chat (`In 10 Sekunden geht es zu cb1b`), die letzten drei einzeln
(`3`, `2`, `1`), dann der Sprung. Verlässt du die Lobby vorher — von Hand oder weil der Server dich
weiterreicht — bricht die Zählung ab.

## Woran der Mod die Lobby erkennt

PrimeBlocks schreibt den aktuellen Ort in den Tab-Footer:

```
Du befindest dich derzeit auf: Lobby-1
```

Steht dort `Lobby`, läuft die Zählung. Auf einem CityBuild passiert nichts.

## Einstellungen

`config/reconnect.json`, wird beim ersten Start angelegt:

| Schlüssel | Standard | |
|---|---|---|
| `enabled` | `false` | An/Aus, entspricht `/reconnect on` bzw. `off` |
| `server`  | `""`    | Ziel für `/server`, z. B. `cb1b` |
| `seconds` | `15`    | Wartezeit in der Lobby, bevor gesprungen wird |

## Bauen

```bash
./gradlew build
```

Ergebnis: `build/libs/reconnect-26.2.jar`. Braucht Minecraft **26.2**, Fabric Loader ≥ 0.19.3,
Fabric API und JDK 25.

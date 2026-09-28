# Reconnect

Holt dich aus der PrimeBlocks-Lobby zurück auf den CityBuild, den du eingestellt hast, und
führt dort auf Wunsch noch einen zweiten Befehl aus, zum Beispiel `/home afk`.

Gedacht für den Fall, dass dich der Server nachts oder nach einer Wartung in die Lobby wirft,
während du AFK in der Farmwelt stehst. Eine Anleitung für Spieler steht in
[ANLEITUNG_Disconnect.md](ANLEITUNG_Disconnect.md).

Der Mod ist eine reine Rückkehr aus der Lobby. Nach einem vollständigen Verbindungsabbruch
verbindet er nicht neu.

## Befehle

```
/reconnect server <server> <sekunden>   Ziel und Wartezeit setzen, schaltet gleich an
/reconnect on                           anschalten
/reconnect off                          ausschalten
/reconnect                              zeigt den aktuellen Stand

/reconnect farm <sekunden> <befehl>     Befehl nach der Rückkehr, schaltet gleich an
/reconnect farm on                      Befehl nach der Rückkehr an
/reconnect farm off                     Befehl aus, nur zurück auf den CityBuild
```

Beispiele:

* `/reconnect server cb1b 15` — 15 Sekunden nachdem der Mod dich in der Lobby sieht, geht es
  mit `/server cb1b` zurück.
* `/reconnect farm 10 home afk` — sobald du wieder auf dem CityBuild bist, zählt der Mod 10
  Sekunden herunter und schickt `/home afk`.

Beim Tippen blendet Minecraft die erwarteten Argumente grau ein (`<server>`, `<sekunden>`,
`<befehl>`).

## Ablauf

1. Der Footer der Tabliste nennt die Lobby. Der Mod wartet die eingestellte Zeit ab.
2. Im Chat läuft eine Zählung: alle 5 Sekunden eine Zeile, die letzten drei einzeln
   (`3`, `2`, `1`). Dann geht `/server <ziel>` raus.
3. Ist `farm` an, startet nach der Landung eine zweite Zählung. Am Ende folgt der Befehl.

Verlässt du die Lobby vor Ablauf, bricht die Zählung ab.

Der Farm-Befehl gilt nur nach einem Sprung, den der Mod selbst ausgelöst hat. Gehst du von Hand
auf den CityBuild, passiert nichts. Wird der CityBuild nicht innerhalb von 60 Sekunden
erreicht (Wartung, falscher Servername), verfällt der Befehl.

## Woran der Mod die Lobby erkennt

PrimeBlocks schreibt den aktuellen Ort in den Tab-Footer:

```
Du befindest dich derzeit auf: Lobby-1
```

Steht dort `Lobby`, läuft die Zählung. Als gelandet gilt der Mod, sobald der Footer nicht mehr
leer ist und keine Lobby nennt. Auf einem CityBuild passiert sonst nichts.

## Einstellungen

`config/reconnect.json`, wird beim ersten Start angelegt:

| Schlüssel | Standard | |
|---|---|---|
| `enabled` | `false` | An/Aus, entspricht `/reconnect on` bzw. `off` |
| `server` | `""` | Ziel für `/server`, z. B. `cb1b` |
| `seconds` | `15` | Wartezeit in der Lobby, bevor gesprungen wird |
| `farmEnabled` | `false` | Zweiten Befehl nach der Rückkehr ausführen |
| `farmSeconds` | `10` | Wartezeit auf dem CityBuild vor dem Befehl |
| `farmCommand` | `""` | Der Befehl ohne Slash, z. B. `home afk` |

## Änderungen

* **Farm-Befehl:** Nach der Rückkehr kann ein zweiter Befehl mit eigener Wartezeit und eigenem
  An/Aus-Schalter laufen.
* **Fix:** Der Farm-Befehl lief nicht bei jedem Sprung. Beim Serverwechsel gibt es kurz keine
  Verbindung, und der Mod hat das als Disconnect gewertet und den Befehl verworfen. Jetzt
  bleibt der Zustand über den Wechsel erhalten.

## Bauen

```bash
./gradlew build
```

Ergebnis: `build/libs/reconnect-26.2.jar`. Braucht Minecraft **26.2**, Fabric Loader ≥ 0.19.3,
Fabric API und JDK 25.

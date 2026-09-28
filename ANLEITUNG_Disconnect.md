# Reconnect

Wirft dich der Server nachts in die Lobby, während du AFK bist, holt dieser Mod dich
automatisch zurück auf deinen CityBuild. Auf Wunsch geht es danach direkt weiter, zum
Beispiel in die Farmwelt.

## Einbauen

1. Fabric Loader und Fabric API für Minecraft 26.2 installieren.
2. `reconnect-26.2.jar` in den Ordner `mods` legen.
3. Minecraft starten.

## Zurück auf den CityBuild

Alles läuft über den Chat. Einmal einstellen, dann merkt der Mod es sich, auch nach einem
Neustart.

```
/reconnect server cb1b 15
```

Das heißt: Sobald ich in der Lobby stehe, warte 15 Sekunden und schick mich dann auf
`cb1b`. Statt `cb1b` und `15` nimmst du deinen Server und deine Wunschzeit.

```
/reconnect on      anschalten
/reconnect off     ausschalten
/reconnect         zeigt, was gerade eingestellt ist
```

## Danach weiter in die Farmwelt

Nach dem Zurückholen stehst du erst mal auf dem CityBuild. Soll der Mod dort noch einen
Befehl ausführen, stellst du ihn so ein:

```
/reconnect farm 10 home afk
```

Das heißt: Sobald ich wieder auf dem CityBuild bin, warte 10 Sekunden und gib dann
`/home afk` ein. Es geht jeder Befehl, nicht nur `/home`. Ein Schrägstrich vorne ist
egal.

Diesen Zusatz kannst du getrennt ein- und ausschalten:

```
/reconnect farm on     nach der Rückkehr zusätzlich den Befehl ausführen
/reconnect farm off    nur zurück auf den CityBuild, nichts weiter
```

Die Einstellung bleibt gespeichert und gilt bei jeder Rückkehr, nicht nur einmal.

## Was dann passiert

Stehst du in der Lobby, läuft im Chat eine Zählung. Bei 15 Sekunden sieht das so aus:

```
In 15 Sekunden geht es zu cb1b
In 10 Sekunden geht es zu cb1b
In 5 Sekunden geht es zu cb1b
3
2
1
Teleportiere zu cb1b
```

Bist du auf dem CityBuild und `farm` ist an, kommt die gleiche Zählung noch einmal, am Ende
mit `Führe aus: /home afk`.

Gehst du vorher selbst aus der Lobby, bricht die Zählung ab.

## Gut zu wissen

- Der Mod reagiert nur in der Lobby, sonst nie.
- Der Befehl aus `farm` läuft nur, wenn der Mod dich selbst aus der Lobby geholt hat. Gehst du
  von Hand auf den CityBuild, passiert nichts.
- Ist der CityBuild nicht erreichbar, etwa wegen Wartung, verfällt der Befehl nach einer
  Minute. Der Mod versucht es dann nicht erneut, bis du die Lobby einmal verlassen hast.
- Die Zählung sieht nur du.
- Der Mod holt dich aus der Lobby zurück. Bricht deine Verbindung komplett ab, verbindet er
  nicht neu.

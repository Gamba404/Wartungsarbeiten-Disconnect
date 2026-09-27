# Reconnect

Wirft dich der Server nachts in die Lobby, während du AFK bist, holt dieser Mod dich
automatisch wieder auf deinen CityBuild zurück.

## Einbauen

1. Fabric Loader und Fabric API für Minecraft 26.2 installieren.
2. `reconnect-26.2.jar` in den Ordner `mods` legen.
3. Minecraft starten.

## Einstellen

Alles läuft über den Chat. Einmal einstellen, dann merkt der Mod es sich.

```
/reconnect server cb1b 15
```

Damit sagst du: Sobald ich in der Lobby stehe, warte 15 Sekunden und schick mich dann
auf `cb1b`. Statt `cb1b` und `15` nimmst du deinen Server und deine Wunschzeit.

Weitere Befehle:

```
/reconnect on      wieder anschalten
/reconnect off     ausschalten
/reconnect         zeigt, was gerade eingestellt ist
```

## Was dann passiert

Stehst du in der Lobby, läuft im Chat eine Zählung:

```
In 15 Sekunden geht es zu cb1b
In 10 Sekunden geht es zu cb1b
In 5 Sekunden geht es zu cb1b
3
2
1
```

Danach bist du wieder auf deinem CityBuild.

Gehst du vorher selbst raus aus der Lobby, bricht die Zählung ab. Es passiert also
nichts, solange du normal spielst.

## Gut zu wissen

- Der Mod reagiert nur in der Lobby, sonst nie.
- Die Einstellung bleibt gespeichert, auch nach einem Neustart.
- Nur für dich sichtbar. Andere Spieler sehen die Zählung nicht.

# Reconnect

Wirft dich der Server nachts in die Lobby, während du AFK bist, holt dieser Mod dich
automatisch zurück auf deinen CityBuild. Auf Wunsch geht es danach direkt weiter zu deinem
Home-Punkt.

## Einbauen

1. Fabric Loader und Fabric API für Minecraft 26.3 installieren.
2. `reconnect-26.3.1.jar` in den Ordner `mods` legen.
3. Minecraft starten.

## Einstellen

Im Chat `/reconnect` eingeben — es öffnet sich ein Fenster:

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

* **Server** — wohin es aus der Lobby zurückgeht, dazu rechts die Wartezeit in Sekunden.
* **Home-Punkt** — wohin es danach geht. Nur der Name, also `afk` für `/home afk`. Rechts
  wieder die Wartezeit.
* **Reconnect: AN/AUS** — der große Schalter. Aus heißt: der Mod schaut gar nicht hin.
* **Home danach: AN/AUS** — nur die zweite Hälfte. Aus heißt: zurück auf den CityBuild, da
  bleibst du stehen.

Die Zeile ganz unten sagt dir, was gleich passieren würde. Esc oder „Fertig" speichert, auch
über einen Neustart hinweg.

## Tab hilft beim Tippen

Drück in „Server" oder „Home-Punkt" einfach **Tab** — dann fragt der Mod den Server und zeigt
dir, was es gibt. Bei den Homes sind das deine echten (wenn du auf dem passenden CityBuild
stehst), beim Server `cb1`, `cb2`, `cb3`.

Mit Tab blätterst du weiter, mit Enter oder Klick übernimmst du, mit Esc machst du die Liste
wieder zu. Tippen kannst du trotzdem, was du willst — Tab ist nur eine Hilfe.

Namen, die du selbst eingetragen hast, merkt sich der Mod und zeigt sie beim nächsten Mal
hellblau unter den Vorschlägen mit an. Praktisch für Unterserver wie `cb1a`, die der Server
beim Tab nicht nennt.

## Was dann passiert

Stehst du in der Lobby, läuft im Chat eine Zählung. Bei 15 Sekunden sieht das so aus:

```
In 15 Sekunden geht es zu cb1a
In 10 Sekunden geht es zu cb1a
In 5 Sekunden geht es zu cb1a
3
2
1
Teleportiere zu cb1a
```

Bist du auf dem CityBuild und „Home danach" ist an, kommt die gleiche Zählung noch einmal, am
Ende mit `Führe aus: /home afk`.

Gehst du vorher selbst aus der Lobby, bricht die Zählung ab.

## Gut zu wissen

- Der Mod reagiert nur in der Lobby, sonst nie.
- Der Home-Befehl läuft nur, wenn der Mod dich selbst aus der Lobby geholt hat. Gehst du von
  Hand auf den CityBuild, passiert nichts.
- Ist der CityBuild nicht erreichbar, etwa wegen Wartung, verfällt der Befehl nach einer
  Minute. Der Mod versucht es dann nicht erneut, bis du die Lobby einmal verlassen hast.
- Die Zählung sieht nur du.
- Der Mod holt dich aus der Lobby zurück. Bricht deine Verbindung komplett ab, verbindet er
  nicht neu.
- Wer die ältere Fassung mit den Befehlen hatte: deine Einstellungen werden beim ersten Start
  übernommen, du musst nichts neu eintragen.

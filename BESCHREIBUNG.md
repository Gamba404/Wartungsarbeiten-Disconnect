# DisconnectWatch

Automatische Rückkehr aus der Lobby auf deinen CityBuild nach einer einstellbaren Wartezeit,
auf Wunsch mit einem Folgebefehl wie /home afk.

## Zurück auf deinen CityBuild

Wenn du in die Lobby versetzt wirst, startet der Mod einen Countdown und schickt dich
anschließend auf den eingestellten CityBuild. Die mitgelieferte Anleitung nennt die Funktion
„Reconnect“; die Befehle heißen deshalb /reconnect.

## Einmal einstellen

/reconnect server cb1 15

Das bedeutet: In der Lobby 15 Sekunden warten und anschließend zu cb1 wechseln. Ersetze cb1 und
15 durch deinen Zielserver und deine gewünschte Wartezeit.

## Weiter in die Farmwelt

/reconnect farm 10 home afk

Das bedeutet: Sobald du wieder auf dem CityBuild stehst, 10 Sekunden warten und dann /home afk
eingeben. Es funktioniert jeder Befehl. Auch hier läuft vorher ein Countdown im Chat.

Diesen Zusatz schaltest du getrennt ein und aus:

• /reconnect farm on – nach der Rückkehr zusätzlich den Befehl ausführen.
• /reconnect farm off – nur zurück auf den CityBuild, nichts weiter.

Der Befehl läuft bei jeder Rückkehr, nicht nur einmal, und nur wenn der Mod dich selbst aus der
Lobby geholt hat. Gehst du von Hand auf den CityBuild, passiert nichts. Ist der CityBuild nicht
erreichbar, verfällt der Befehl nach einer Minute.

## Weitere Befehle im Minecraft-Chat

• /reconnect on – aktivieren.
• /reconnect off – deaktivieren.
• /reconnect – aktuelle Einstellungen anzeigen.

## So läuft es ab

Im Chat erscheint ein Countdown. Verlässt du die Lobby vorher selbst, wird er abgebrochen.
Außerhalb der Lobby greift die Funktion nicht ein. Die Einstellungen bleiben nach einem Neustart
erhalten; die Zählung ist nur für dich sichtbar.

Die Anleitung beschreibt die Rückkehr aus der Lobby, keine automatische Wiederverbindung nach
einem vollständigen Verbindungsabbruch.

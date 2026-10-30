# Reconnect

Kurzbeschreibung für Modseiten und Discord.

---

Wirft dich der Server in die Lobby, während du AFK bist, holt dich dieser Mod automatisch
zurück auf deinen CityBuild — und auf Wunsch gleich weiter zu deinem Home-Punkt.

Eingestellt wird alles in einem Fenster: `/reconnect` im Chat. Dort stehen Zielserver,
Home-Punkt und die beiden Wartezeiten als Felder, dazu zwei An/Aus-Schalter. Tab vervollständigt
wie im Chat — bei den Homes deine echten (wenn du auf dem passenden CityBuild stehst), beim
Server `cb1`, `cb2`, `cb3`. Tippen kannst du trotzdem, was du willst.

Bis es losgeht, läuft im Chat eine Zählung, die nur du siehst; verlässt du die Lobby vorher
selbst, bricht sie ab. Außerhalb der Lobby greift der Mod nie ein, und die Einstellungen bleiben
nach einem Neustart erhalten.

Der Mod holt dich aus der Lobby zurück. Nach einem vollständigen Verbindungsabbruch verbindet er
nicht neu.

**Minecraft 26.3, Fabric Loader und Fabric API.** Jar in den `mods`-Ordner, fertig.

# Plactions — Proiect Ingineria Programelor

Implementare în **Java (Swing)** a jocului 2D puzzle-platformer **Plactions**, dezvoltată în cadrul disciplinei **Ingineria Programelor** (*Universitatea „Dunărea de Jos” din Galați*)[cite: 21].

---

## Despre Joc

**Plactions** este un joc de tip *puzzle-platformer* în care jucătorul are la dispoziție o coadă limitată de acțiuni speciale (`JUMP`, `DASH`) pentru a depăși obstacole (țepi, lasere dinamice) și a ajunge la portalul de ieșire al fiecărui nivel.

### Funcționalități implementate
* **Meniu principal interactiv:** navigare între opțiuni (`Start`, reglare volum `Sound <0-100>` și `Music <0-100>`).
* **Selecție de niveluri (Grilă 6×4):** progresie vizuală cu niveluri finalizate (verde), nivel curent disponibil (galben) și niveluri blocate (roșu).
* **Gameplay fluid la 60 FPS:** interpolare sub-pixel a mișcării personajului, gravitație și sistem complet de `Undo` pentru acțiuni.

---

## Șabloane de Proiectare (Design Patterns)

Arhitectura aplicației respectă separarea responsabilităților prin utilizarea tuturor celor trei categorii de șabloane GoF:

### 1. Șabloane Creaționale (Creational)
* **Factory Method (`LevelFactory`):** Generează dinamic obiectele de pe hartă (`WallTile`, `SpikeTile`, `LaserTile`, `ExitTile`, `AirTile`) pe baza matricei de caractere a nivelului.

### 2. Șabloane Structurale (Structural)
* **Composite (`LevelComponent`, `TileGroup`):** Organizează ierarhic elementele nivelului, permițând randarea (`render()`) și actualizarea (`update()`) uniformă atât a unui singur `Tile`, cât și a întregului nivel.
* **Adapter (`SoundAdapter`, `SoundPlayer`):** Adaptează sistemul audio nativ Java la interfața de evenimente sonore ale jocului (`playAction()`, `playDeath()`, `playWin()`).

### 3. Șabloane Comportamentale (Behavioral)
* **Command (`ActionCommand`, `JumpCommand`, `DashCommand`, `ActionQueue`):** Încapsulează acțiunile jucătorului într-o coadă executabilă secvențial, oferind suport pentru anularea ultimei comenzi (`Undo`).
* **State (`LaserState`, `LaserOnState`, `LaserOffState`, `PlayerState`, `GameScreen`):** Gestionează tranzițiile de stare ale laserelor (`ON` periculos / `OFF` inofensiv), ale jucătorului (`GROUNDED`, `IN_AIR`, `DEAD`) și ale ecranelor aplicației (`MENU`, `LEVEL_SELECT`, `PLAYING`).
* **Observer (`GameObserver`, `LevelManager`):** Notifică managerul de nivel la apariția evenimentelor critice (`onPlayerDied()`, `onLevelCompleted()`, `onOutOfActions()`).

---

## Structura Proiectului

```text
src/
├── main/java/com/plactions/
│   ├── main/                 # Punctul de intrare (Main.java)
│   ├── model/                # Entități (Player, Tile, WallTile, SpikeTile, LaserTile, ExitTile, AirTile)
│   ├── patterns/
│   │   ├── creational/       # LevelFactory
│   │   ├── structural/       # LevelComponent, TileGroup, SoundPlayer, SoundAdapter
│   │   └── behavioral/       # ActionCommand, ActionQueue, LaserState, GameObserver, LevelManager
│   └── view/                 # Interfața grafică și bucla de joc (GamePanel)
└── test/java/com/plactions/  # Teste unitare JUnit 5 (PlactionsPatternsTest)
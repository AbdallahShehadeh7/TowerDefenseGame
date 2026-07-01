# Intelligent Tower Defense

## Project Overview

Intelligent Tower Defense is a Java Swing grid-based strategy game for my AP Computer Science A / Advanced Programming final project.

The player places towers on a 2D grid to stop enemies from reaching the base. The game includes enemy waves, tower upgrades, tower selling, pathfinding, sorting, recursion, save/load, scoring, and object-oriented programming.

## How To Run

1. Open the project in IntelliJ IDEA.
2. Open `src/Main.java`.
3. Click the green Run button.
4. The game window will open.

## How To Play

- Click `Basic`, `Sniper`, or `Rapid` to choose a tower type.
- Left click a grass tile to build a tower.
- Left click a tower to select it.
- Click `Upgrade` to upgrade the selected tower.
- Right click a tower to sell it.
- Click `Start Wave` to begin the wave.
- Click `Pause` to pause or resume.
- Click `Save` to save the game.
- Click `Load` to load saved stats.
- Click `Reset` to restart.

## Main Features

- Java Swing GUI
- 10 by 16 grid map
- 2D array grid system
- BFS pathfinding
- Recursive grid traversal
- Bubble sort for high scores
- Enemy danger sorting
- Tower upgrades
- Tower selling
- Multiple tower types
- Multiple enemy types
- Save and load system
- Score system
- Win and game over screens

## Classes Used

The project uses multiple classes:

- `Main`
- `GamePanel`
- `Grid`
- `Tile`
- `Tower`
- `BasicTower`
- `SniperTower`
- `RapidTower`
- `Enemy`
- `NormalEnemy`
- `FastEnemy`
- `TankEnemy`
- `Projectile`
- `WaveManager`
- `SaveManager`
- `ScoreManager`

## 2D Array

The project uses a 2D array in `Grid.java`.

```java
Tile[][] tiles;
The grid stores grass tiles, path tiles, the start tile, the base tile, and tower locations.
Object-Oriented Programming
The project uses classes and objects to separate the game into smaller parts.
Examples:
Grid manages the map.
Tile stores each grid cell.
Tower represents tower behavior.
Enemy represents enemy behavior.
WaveManager controls enemy waves.
SaveManager controls saving and loading.
ScoreManager controls scores.
Inheritance and Polymorphism
The tower classes use inheritance.
BasicTower, SniperTower, and RapidTower all extend Tower.
The enemy classes also use inheritance.
NormalEnemy, FastEnemy, and TankEnemy all extend Enemy.
This allows the game to use different tower and enemy types while sharing common behavior.
Searching Algorithm
The project uses Breadth-First Search, also called BFS, in Grid.java.
BFS searches the grid from the start tile to the base tile. The enemies use the path found by BFS to move through the map.
Sorting Algorithm
The project uses sorting in two places.
First, enemies are sorted by danger score so towers target dangerous enemies first.
Second, high scores are sorted using bubble sort in ScoreManager.java.
Recursion
The project uses recursion in Grid.java.
The method recursiveCountGrass counts connected grass tiles by calling itself in four directions: up, down, left, and right.
Autonomous AI Behavior
The game has autonomous behavior because towers make decisions automatically.
Towers search for enemies in range and choose targets without the player controlling each shot.
The wave system also increases difficulty by adding fast enemies and tank enemies in later waves.
Save and Load
The project uses file persistence.
SaveManager.java saves and loads game stats.
ScoreManager.java saves and loads scores.
The save files are:
savegame.txt
scores.txt
Advanced Features
The project includes these advanced features:
Adaptive difficulty
Multiple tower strategies
Multiple enemy strategies
Sorting-based target priority
Save and load persistence
Score tracking
Dynamic visual updates
Testing
Tests completed:
Start wave: enemies spawn and move.
Build tower: tower appears on grass.
Build on path: tower is not placed.
Select tower: tower information appears.
Upgrade tower: tower stats improve.
Sell tower: tower is removed and money increases.
Enemy reaches base: lives decrease.
Lives reach zero: game over screen appears.
Survive final wave: win screen appears.
Save game: save file is created.
Load game: saved stats are loaded.
GitHub Repository
https://github.com/AbdallahShehadeh7/TowerDefenseGame
Author
Abdallah Shehadeh
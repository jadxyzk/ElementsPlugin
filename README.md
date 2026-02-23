# ElementsPlugin 🌍🔥💧🌬️

A Paper Minecraft plugin featuring four elements: **Fire, Water, Earth, and Air** with a full progression system.

---

## Features

- 🎲 **Random element assignment** on first join
- ⬆️ **3 levels per element** — progress by getting kills or using Energy Bottles
- ☠️ **Death penalty** — 3 deaths = lose a level
- 💎 **Element Change Gem** — craft it to randomly switch to a new element
- ⚔️ **3 unique abilities per element**, each stronger than the last
- ⏱️ **Cooldown HUD** displayed above the hotbar via action bar
- 💾 **Persistent data** — progress saves across sessions

---

## Installation (GitHub Actions — No coding needed)

1. Create a free account at [github.com](https://github.com)
2. Create a new repository and upload all these files (keep the folder structure!)
3. Go to the **Actions** tab in your repository
4. Click **"Build ElementsPlugin"** → **"Run workflow"**
5. Wait ~1 minute, then go to the completed run
6. Download the **ElementsPlugin** artifact — it contains your `.jar`!
7. Drop the `.jar` into your server's `/plugins` folder
8. Restart your server

---

## Commands

| Command | Description |
|---------|-------------|
| `/element` | View your current element, level, and progress |
| `/ability fire1` | Use ability 1 of fire (replace fire/number as needed) |
| `/ability fire2` | Use ability 2 of fire |
| `/ability fire3` | Use ability 3 of fire |
| `/elementadmin set <player> <element>` | Set a player's element (OP only) |
| `/elementadmin reset <player>` | Reset a player's progress (OP only) |
| `/elementadmin setlevel <player> <1-3>` | Set a player's level (OP only) |

---

## Abilities

### 🔥 Fire
| Level | Ability | Description | Cooldown |
|-------|---------|-------------|----------|
| 1 | **Ignite** | Sets all nearby enemies on fire | 30s |
| 2 | **Fireball** | Launches an explosive fireball | 60s |
| 3 | **Fire Ring** | Explodes in a ring of fire dealing massive damage | 90s |

### 💧 Water
| Level | Ability | Description | Cooldown |
|-------|---------|-------------|----------|
| 1 | **Slow** | Slows all nearby enemies | 30s |
| 2 | **Burst** | Knocks back and damages nearby enemies | 60s |
| 3 | **Heal** | Restores 50% of your max health | 90s |

### 🌍 Earth
| Level | Ability | Description | Cooldown |
|-------|---------|-------------|----------|
| 1 | **Launch** | Launches nearby enemies into the air | 30s |
| 2 | **Wall** | Summons a stone wall in front of you (lasts 10s) | 60s |
| 3 | **Earthquake** | Massive AoE damage + slowness | 90s |

### 🌬️ Air
| Level | Ability | Description | Cooldown |
|-------|---------|-------------|----------|
| 1 | **Dash** | Dashes forward at high speed | 30s |
| 2 | **Cyclone** | Launches all nearby enemies into the air | 60s |
| 3 | **Tornado** | Pulls enemies toward you for 2 seconds, dealing damage | 90s |

---

## Crafting Recipes

### Element Change Gem
```
D D D
D N D
D D D
```
D = Diamond, N = Nether Star  
Right-click to use — assigns a **random different element**

### Energy Bottle
```
G G G
G B G
G G G
```
G = Glowstone Dust, B = Glass Bottle  
Right-click to use — counts as 1 kill toward your next level

---

## Progression

- **Level 1 → 2:** 3 kills
- **Level 2 → 3:** 5 kills
- **Max Level (3):** 10 kills to reach from level 2
- **Death penalty:** 3 deaths = lose a level (resets death counter)

---

## Configuration

Edit `plugins/ElementsPlugin/config.yml` to change cooldowns, kill requirements, death penalties, and messages.

---

## Requirements
- Paper 1.21.x
- Java 17+

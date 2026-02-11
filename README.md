# 📦 CategoryHopper

![Version](https://img.shields.io/badge/version-1.0-blue)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21-green)
![Java](https://img.shields.io/badge/Java-21-orange)

**The ultimate smart-sorting solution for modern Minecraft servers.**

**CategoryHopper** allows players to filter items based on broad categories (like *Food*, *Ores*, *Redstone*) using a single hopper. Say goodbye to massive sorting systems that require a separate chest for every single item type!

---

## ✨ Features

* **Smart Filtering:** Sort entire groups of items (e.g., allow all "Logs" but reject "Cobblestone").
* **Intuitive GUI:** Sneak + Right-Click a Filter Hopper to open a visual menu.
* **Visual Feedback:** The GUI highlights the active category with a glowing effect and dynamic window titles.
* **Fully Configurable:** Add, remove, or modify categories via `categories.json`.
* **Secure:** Prevents GUI item theft (Anti-Shift-Click/Drag protection).
* **Permissions:** Granular control over crafting, placing, and using the hoppers.

---

## 📥 Installation

1.  Download the `CategoryHopper.jar` file.
2.  Place it into your server's `/plugins` folder.
3.  Restart your server.
4.  The default `categories.json` will be generated automatically.

**Requirements:**
* Minecraft 1.21+ (Paper, Spigot, or Purpur)
* Java 21+

---

## 🎮 How to Use

1.  **Obtain a Filter Hopper:**
    * Craft it using the server recipe (Default: Hopper surrounded by Iron & Redstone).
    * Or use the admin command: `/givehopper <player>`.
2.  **Place the Hopper:** It looks like a normal hopper but stores special data.
3.  **Open the Menu:** **Sneak (Shift) + Right-Click** the hopper.
4.  **Select a Category:** Click on an icon (e.g., 🍎 for Food).
    * The icon will **glow** and the chat will confirm your selection.
    * The hopper will now *only* accept items from that list.
5.  **Disable Filter:** Click the 🚫 Barrier icon to reset the hopper (it will accept nothing or behave normally depending on configuration).

---

## ⚙️ Configuration

The plugin uses `categories.json` for easy editing. You can define your own categories here.

**Location:** `/plugins/CategoryHopper/categories.json`

### Example JSON Structure:
```json
{
  "FOOD": {
    "displayName": "Food & Consumables",
    "displayMaterial": "APPLE",
    "items": [
      "APPLE",
      "COOKED_BEEF",
      "BREAD",
      "GOLDEN_CARROT"
    ]
  },
  "RARE": {
    "displayName": "Shiny Treasures",
    "displayMaterial": "DIAMOND",
    "items": [
      "DIAMOND",
      "EMERALD",
      "NETHER_STAR"
    ]
  }
}

```

> **Note:** Use valid Bukkit Material names (e.g., `OAK_LOG`, not `Oak Log`).

After editing the file, run `/chreload` to apply changes instantly.

---

## 📜 Commands & Permissions

| Command | Description | Permission |
| --- | --- | --- |
| `/givehopper <player> [amount]` | Give a specific amount of hoppers. | `categoryhopper.admin` |
| `/chreload` | Reloads the configuration file. | `categoryhopper.admin` |

### Player Permissions

| Permission Node | Description | Default |
| --- | --- | --- |
| `categoryhopper.craft` | Allow crafting the hopper. | `true` |
| `categoryhopper.place` | Allow placing the hopper block. | `true` |
| `categoryhopper.use` | Allow opening the GUI to change filters. | `true` |
| `categoryhopper.*` | Grant all plugin permissions. | `op` |

---

## 🛠️ Developer Info

This plugin uses:

* **Gson** for JSON parsing.
* **PersistentDataContainer (PDC)** to store filter data directly on the Item/Block (no database required).
* **Material Caching** for optimized performance.

---

## 📄 License

This project is licensed under the MIT License
<img src="https://t.bkit.co/w_698d11f30eb21.gif" />

# Tripad Extension

A **modular floating button framework** for [Starsector](https://fractalsoftworks.com/)'s campaign map. Mods can register draggable buttons that players can arrange, lock together for group movement, and toggle via LunaLib settings.

![Starsector 0.98a-RC7](https://img.shields.io/badge/Starsector-0.98a--RC7-blue)
![Version 1.0.0](https://img.shields.io/badge/Version-1.0.0-orange)
![License: MIT](https://img.shields.io/badge/License-MIT-green)

## Features

- **Floating Campaign Buttons** — Rectangular buttons with chamfered corners and metallic frame, matching the game's native UI style
- **Drag & Drop** — Buttons can be freely dragged around the campaign map
- **Snap-to-Lock Grouping** — Drag buttons near each other to snap them into groups; right-click to unlock
- **LunaLib Settings** — Toggle individual buttons on/off from the mod settings menu
- **Modder API** — Simple registration interface for other mods to add their own buttons
- **Game-Accurate Colors** — Uses the game's own `buttonBg`, `buttonBgDark`, and `buttonText` color keys

## Built-in Buttons

| Button | Requires | Description |
|--------|----------|-------------|
| **NexusUI** | [NexusUI](https://github.com/TheAlanK/NexusUI) | Opens the NexusUI dashboard |
| **Operatives** | [Nexerelin](https://fractalsoftworks.com/forum/index.php?topic=9175.0) | Opens the Nexerelin agents/operatives intel panel |

## Installation

1. Install [LazyLib](https://fractalsoftworks.com/forum/index.php?topic=5444.0) and [LunaLib](https://fractalsoftworks.com/forum/index.php?topic=25658.0)
2. Download the latest release or clone this repository
3. Copy the `TripadExtension` folder into your `Starsector/mods/` directory
4. Enable **Tripad Extension** in the Starsector launcher

## For Modders — Registering a Button

### 1. Add Tripad Extension as a dependency

In your `mod_info.json`:
```json
{
  "dependencies": [
    {"id": "tripad_extension", "name": "Tripad Extension"}
  ]
}
```

### 2. Create a button in your ModPlugin

```java
import com.tripadextension.*;

public class MyModPlugin extends BaseModPlugin {
    @Override
    public void onGameLoad(boolean newGame) {
        TripadManager mgr = TripadManager.getInstance();

        TripadButtonSpec spec = new TripadButtonSpec("my_button")
            .setLabel("My Mod")
            .setTooltip("Open My Mod panel")
            .setIconColor(new Color(100, 200, 100))
            .setSortOrder(50)
            .setHandler(new TripadClickHandler() {
                public void onClick() {
                    // Your button action here
                }
            });

        mgr.registerButton(spec);
    }
}
```

### Optional Integration (Tripad Extension Not Required)

If your mod should work **with or without** Tripad Extension, use the lazy class loading pattern:

**1. Create a helper class** that imports Tripad types:

```java
import com.tripadextension.*;

public class MyTripadIntegration {
    public static void register() {
        TripadManager mgr = TripadManager.getInstance();
        TripadButtonSpec spec = new TripadButtonSpec("my_button")
            .setLabel("My Mod")
            .setTooltip("Open My Mod")
            .setSortOrder(50)
            .setHandler(new TripadClickHandler() {
                public void onClick() { /* ... */ }
            });
        mgr.registerButton(spec);
    }
}
```

**2. Guard the call** in your ModPlugin:

```java
if (Global.getSettings().getModManager().isModEnabled("tripad_extension")) {
    try {
        MyTripadIntegration.register();
    } catch (Throwable e) {
        log.warn("Tripad integration failed: " + e.getMessage());
    }
}
```

## API Reference

### `TripadButtonSpec`

| Method | Description |
|--------|-------------|
| `setLabel(String)` | Button text displayed on the campaign map |
| `setTooltip(String)` | Hover tooltip text |
| `setIconColor(Color)` | Accent color for the button |
| `setSortOrder(int)` | Position order (lower = further left) |
| `setHandler(TripadClickHandler)` | Click callback |

### `TripadManager`

| Method | Description |
|--------|-------------|
| `getInstance()` | Get the singleton manager |
| `registerButton(TripadButtonSpec)` | Register a new button |
| `unregisterButton(String id)` | Remove a button by ID |
| `getButton(String id)` | Get a button by ID |
| `getButtons()` | Get all registered buttons |

## Dependencies

- [LazyLib](https://fractalsoftworks.com/forum/index.php?topic=5444.0)
- [LunaLib](https://fractalsoftworks.com/forum/index.php?topic=25658.0)

## Part of the Nexus Ecosystem

- [NexusUI](https://github.com/TheAlanK/NexusUI) — UI framework with floating overlay, tabbed panels, and REST API
- [NexusDashboard](https://github.com/TheAlanK/NexusDashboard) — Fleet, colonies, factions overview
- [NexusCheats](https://github.com/TheAlanK/NexusCheats) — In-game cheat panel
- [NexusProfiler](https://github.com/TheAlanK/NexusProfiler) — Performance diagnostics
- [NexusTactical](https://github.com/TheAlanK/NexusTactical) — Combat fleet visualization

## License

[MIT](LICENSE)

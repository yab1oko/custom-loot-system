<h1 align="center">Custom Loot System (CTS)</h1>
<p align="center"><b>A customizable loot system with props, blocks, airdrops, and an in-game loot table editor.</b></p>

<p align="center">
<code>Minecraft 1.20.1</code> · <code>Forge</code> · <code>Client & Server</code> · <code><a href="https://github.com/yab1oko/custom-loot-system">GitHub</a></code>
</p>

---

## ✨ Overview
CTS is a mapmaking and adventure tool for placing loot in the world without stuffing every corner with vanilla chests. You configure everything in Creative; Survival players just find props, search them, and take what drops. The feel is closer to Tarkov, 7 Days to Die, or Unturned than to a normal Minecraft loot chest.

<p align="center">
<iframe width="560" height="315" src="https://www.youtube.com/embed/2szhDgz2Yc4" title="CTS overview" frameborder="0" allowfullscreen></iframe>
</p>

## 🧩 Features

### Loot Plate
An invisible floor or wall plate that shows a single item. Right-click to pick it up; the item respawns after a timer. Fill template slots or point it at a loot table, set chance, stack size, and random durability per entry. Flat items lie on the ground; blocks stay 3D. Configured plates stay hidden until you hold a loot plate.

<p align="center">
<img src="https://media.forgecdn.net/attachments/description/null/description_010650c8-70d7-4d6d-b956-725ee025b158.gif" alt="Loot Plate" width="375">
</p>

### Searchable Props
Props look like junk, storage, or furniture. Hold use to search, then open a chest-style inventory. They support template items or loot-table mode, a configurable search time, full and empty models, open sounds, and a respawn timer. Optional “needs tool” mode only opens with a matching held item — tools lose durability, stackables are consumed.

### Conversion Wand
Turns a container (vanilla chest/barrel/hopper/shulker and more, plus modded chests) into a CTS loot container while keeping its look. Loot tables and inventory contents are kept (not dropped); if items were present, the config screen opens so you can tune chances and counts. Confirm before converting; use the wand again to revert. Double chests are not supported. Add mod chests to the `#cts:convertible` and `#cts:chest_like` block tags for convert + lid animation.

### Chest Library
Creative tool that stores CTS loot-container setups (props and converted containers) in a **per-world** library. Open the library to rename, delete, preview contents/loot table, or take a placeable item of the same block. Shift+right-click a CTS container (or use Save in its config) to add it to the library.

### Loot Table Editor
An in-game book for building chest loot tables and exporting JSON for datapacks and map folders. Entries support chance, stack size, random durability, and random enchantments, plus tag slips with member preview and filters. After exporting to `.minecraft`, run `/exportlt` in the world to embed them, then `/reload`.

<p align="center">
<img src="https://media.forgecdn.net/attachments/description/1713931/description_dd2b52c1-a126-4682-b803-bd3a59dabead.png" alt="Loot Table Editor" width="278">
</p>

### Tag Slip
Drop a tag slip into any loot slot on a plate, prop, or book. Pick an item or block tag, preview members, then choose All, None, or a custom subset. Selected members can roll on their own when exported; damageable and enchantable items still support random durability and enchantments.

<p align="center">
<img src="https://media.forgecdn.net/attachments/description/null/description_c1b7a21e-c1c0-4b70-8e7d-76e120968fd7.png" alt="Tag slip settings" width="233">
<img src="https://media.forgecdn.net/attachments/description/null/description_8c34a922-4c61-49d0-a658-547889b51869.png" alt="Tag members preview" width="283">
</p>

## 🪂 Airdrops
Airdrops send a parachute crate into the world — once in an area, or on a timer around a point. Players get a radio warning, then the crate falls with a parachute and opens as searchable loot.

<p align="center">
<iframe width="560" height="315" src="https://www.youtube.com/embed/0rfys6DjqYE" title="CTS airdrops" frameborder="0" allowfullscreen></iframe>
</p>

<p align="center">
<img src="https://media.forgecdn.net/attachments/description/1713931/description_374f199f-23b8-458a-85d9-eadd63121a94.gif" alt="Airdrop crate landing" width="448">
</p>

**Commands**
- `/cts airdrop x1 z1 x2 z2 [loot_table]` — one drop somewhere inside the area
- `/cts airdrop auto <radius> <minutes> [loot_table]` — keep dropping around the spot where you ran the command
- `/cts airdrop auto status` — radius, interval, and time until the next drop
- `/cts airdrop auto clear` — turn automatic drops off

If you omit the loot table, CTS uses the default from config (`config/cts-common.toml` / Mods → CTS → Config), usually `cts:airdrop`.

## 🎮 How to use
In Creative, open the CTS tab (Props, Blocks, Breakables). Shift + right-click a configured block to edit it. Survival players never see the config UI.

Open the pause menu → **Mods** to reach CTS settings (default respawn time and default airdrop loot table). Blocks you already placed keep their own timers.

<p align="center">
<a href="https://discord.gg/tXMBWwSuRp"><img src="https://media.forgecdn.net/attachments/description/1713931/description_c645ea44-90cd-4e8b-9d57-eaeab7081ee4.png" alt="Join Discord" width="560"></a>
</p>

## More mods by yab1oko
- [Cold Weapons](https://www.curseforge.com/minecraft/mc-mods/cold-weapons)
- [YBLK Fashion](https://www.curseforge.com/minecraft/mc-mods/yblk-fashion)
- [YBLK Features](https://www.curseforge.com/minecraft/mc-mods/yblk-features)
- [Roleplay Materials](https://www.curseforge.com/minecraft/mc-mods/roleplay-materials)

## Feedback
Bugs, balance notes, and ideas: join the Discord — [yab1oko mods](https://discord.gg/tXMBWwSuRp).

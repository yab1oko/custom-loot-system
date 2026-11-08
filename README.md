# Custom Loot System (CTS)

**Custom Loot System (CTS)** is a Forge 1.20.1 mapmaking and adventure tool for placing loot in the world without ugly chests everywhere.  
Configure everything in Creative. Players in Survival just find props, search them, and take what drops. Mod implies to add a loot system like in Tarkov, 7 Days to die or Unturned

<p align="center">
<iframe width="560" height="315" src="https://www.youtube.com/embed/2szhDgz2Yc4" title="CTS overview" frameborder="0" allowfullscreen></iframe>
</p>

## Loot Plate

An invisible floor/wall plate that shows a single item. Right-click to pick it up; the item respawns after a timer.

- Fill 27 template slots, or point it at a loot table
- Set chance, stack size, and random durability per entry
- Flat items lie on the ground; blocks stay 3D
- Hidden when configured; visible again if you hold a loot plate

<p align="center">
<img src="https://media.forgecdn.net/attachments/description/null/description_010650c8-70d7-4d6d-b956-725ee025b158.gif" alt="Loot Plate" width="375">
</p>

## Searchable Props

World props that look like junk, storage, or furniture. Hold use to search them, then open a chest-style inventory.

- Template items or loot-table mode
- Configurable search time
- Optional “needs tool” mode: only open with a matching held item, tools lose durability, stackables are consumed
- Full and empty models, open sounds
- Respawn timer after loot is taken

<p align="center">
<img src="https://media.forgecdn.net/attachments/description/1713931/description_374f199f-23b8-458a-85d9-eadd63121a94.gif" alt="" width="448">
</p>

## Conversion Wand

Turn a vanilla container (chest, barrel, hopper, dispenser, dropper, shulker box, …) into a CTS loot container while keeping its look. Confirm before converting; use again to revert. Double chests are not supported.

## Loot Table Editor

In-game book for building chest loot tables and exporting JSON for datapacks / map folders.

- Per-entry chance, stack size, random durability, random enchantments
- Tag slips supported with member preview and filters
- After exporting to .minecraft, run /exportlt in the world to embed, then /reload

<p align="center">
<img src="https://media.forgecdn.net/attachments/description/1713931/description_dd2b52c1-a126-4682-b803-bd3a59dabead.png" alt="Loot table Editor" width="278">
</p>

## Tag Slip

Drop a tag slip into any loot slot (plate, prop, or book). Pick an item/block tag, preview members in the side panel, then choose All, None, or a custom subset.

- Each selected member can roll on its own when exported
- Damageable items support random durability; enchantable items support random enchantments

<p align="center">
<img src="https://media.forgecdn.net/attachments/description/null/description_c1b7a21e-c1c0-4b70-8e7d-76e120968fd7.png" alt="Tag Slip interface" width="233">
<img src="https://media.forgecdn.net/attachments/description/null/description_8c34a922-4c61-49d0-a658-547889b51869.png" alt="Tag slip visual" width="283">
</p>

## Airdrop system

<p align="center">
<iframe width="560" height="315" src="https://www.youtube.com/embed/0rfys6DjqYE" title="CTS airdrops" frameborder="0" allowfullscreen></iframe>
</p>

## Creative workflow

One CTS tab with folders for Props, Blocks, and Breakables. Shift+right-click configured blocks to edit settings. Survival players never see the config UI.

<p align="center">
<a href="https://discord.gg/tXMBWwSuRp"><img src="https://media.forgecdn.net/attachments/description/1713931/description_c645ea44-90cd-4e8b-9d57-eaeab7081ee4.png" alt="" width="560"></a>
</p>

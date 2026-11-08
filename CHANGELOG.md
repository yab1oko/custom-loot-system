# Changelog

## 3.0

### Контейнеры открываются
Обысканный контейнер больше не меняет картинку целиком. Крышка, дверца или ящик доезжают до открытого положения и так же закрываются, если лут снова появился.

- **Ящик с патронами** — крышка откидывается назад и оседает за корпусом
- **Ящик с инструментами** — крышка поднимается
- **Оружейный кейс**, **аптечка**, **ящик**, **медицинский ящик**, **бочка отходов** — крышка открывается
- **Сейф**, **микроволновка**, **холодильник**, **стиральная машина** — дверца открывается. У микроволновки и стиральной машины стекло остаётся прозрачным
- **Унитаз** — крышка поднимается
- **Касса** — выдвигается денежный ящик
- **Кухонные шкафы** — дверцы распахиваются
- **Кухонные ящики** — дверца открывается
- **Комоды** — ящики выдвигаются

### Анимации персонажа
Если установлен **Player Animator**, действия видно на персонаже, в том числе от первого лица:

- обыск контейнера
- рубка дерева и разбор баррикады
- добыча металлической руды
- откручивание вентиляционной решётки отвёрткой

Без Player Animator эти действия работают как раньше, просто без позы. Если стоит **Real First Person**, анимации не спорят с видом от первого лица.

### Настройки
Кнопка конфига в списке модов (то же самое в `config/cts-common.toml`):

- **Респавн лута по умолчанию** — через сколько секунд новый контейнер или табличка с лутом наполняются снова. 0 — без респавна. Уже поставленные блоки не меняются. По умолчанию 1 час.
- **Таблица лута аирдропа** — какая таблица берётся, если в команде её не написать.

### Аирдроп
- `/cts airdrop x1 z1 x2 z2` — разовый сброс в зоне. Таблицу лута можно дописать в конце, иначе берётся из настроек.
- `/cts airdrop auto радиус минуты` — аирдропы сами падают вокруг точки, где введена команда. Таблицу тоже можно указать.
- `/cts airdrop auto status` — радиус, интервал и когда следующий.
- `/cts airdrop auto clear` — выключить.

### Прочее
- Версия мода: **3.0**

## 3.0 (English)

### Containers open
A searched container no longer swaps to a different picture. The lid, door, or drawer moves into the open pose, and closes again if the loot comes back.

- **Ammo Box** — the lid flips back and settles behind the body
- **Toolbox** — the lid lifts
- **Weapon Case**, **Medkit**, **Crate**, **Medical Crate**, **Waste Drum** — the lid opens
- **Safe**, **Microwave**, **Fridge**, **Washing Machine** — the door opens. Microwave and washing machine glass stays see-through
- **Toilet** — the lid lifts
- **Cash Register** — the drawer slides out
- **Kitchen cabinets** — the doors swing open
- **Kitchen drawers** — the door opens
- **Dressers** — the drawers slide out

### Character animations
With **Player Animator** installed, these actions show on the character, including in first person:

- searching a container
- chopping a tree and breaking a barricade
- mining metal ore
- unscrewing a ventilation grille with a screwdriver

Without Player Animator the actions work as before, just without the pose. With **Real First Person**, the animations do not fight the first-person view.

### Settings
Config button in the mods list (same values in `config/cts-common.toml`):

- **Default loot respawn** — seconds before a newly placed container or loot plate refills. 0 means no respawn. Blocks already placed keep their own timer. Default is 1 hour.
- **Airdrop loot table** — the table used when a command does not name one.

### Airdrop
- `/cts airdrop x1 z1 x2 z2` — one drop inside the area. Add a loot table at the end, or the one from settings is used.
- `/cts airdrop auto radius minutes` — drops keep coming around the spot where the command was run. A loot table can be added too.
- `/cts airdrop auto status` — radius, interval, and when the next one is due.
- `/cts airdrop auto clear` — turn it off.

### Other
- Mod version: **3.0**

## 2.1

### Soft dependency
- Scrap / wood drops now use **Roleplay Materials** (`roleplay_materials:scrap`, `roleplay_materials:wood`) instead of `rpgmaterials`

### Tool-locked props
- Opening a prop with a required item now matches **full NBT** (not just item id)
- **Damage** and **RepairCost** are ignored, so worn tools still work
- Mod version: **2.1**

## 2.0

### Breakables (new creative folder)
- **Barricade** — hold use with axe/pickaxe; crack overlay; stages; configurable respawn (Shift+RMB in creative)
- **Ventilation grille** — hold use with screwdriver; opens then respawns
- **Metal ore** — hold use with pickaxe; becomes stone when depleted; drops scrap/iron toward the player; default respawn **30 min**
- **Small tree** / **Large tree** — place one item, grows a multiblock tree; hold axe to chop; stump left behind; respawn grows a new tree
  - Small: ~8s chop, −3 tool durability, drops 3–7 logs
  - Large: ~12s chop, −5 tool durability, wider canopy, drops 6–10 logs
  - Crack overlay on the whole tree; biome-tinted leaves; creative-only place (enchanted sapling icon)

### Loot Table Editor
- Reworked **dark custom UI** (no generic chest look)
- **Load** / **Export** loot tables from/to disk, with table-id suggestions
- Multi-page grid for large tables
- Shared entry editor: chance, count / random durability, **random enchantments**
- Tag slips fully supported in the book (preview, filter, export)

### Tag Slip
- Side panel to **preview tag members** and toggle which ones can drop
- **All** / **None** / per-item pick (filtered subset stored on the slip)
- Works in loot plates, searchable props, and the loot book
- Export: each chosen member rolls as its own pool entry

### Entry editor / loot UX
- **Random Enchantments** toggle (ENC mark) for enchantable items — plates, props, loot book, JSON export/import
- Cleaner entry panel layout (sliders + toggles)

### Creative / UX
- Creative tab folders: **Props**, **Blocks**, **Breakables**
- Tooltips for breakables (respawn config, tree caution)
- Crosshair prompts for break / open / need tool

### Other
- Soft dependency on `rpgmaterials` for metal/wood scrap drops when present
- Barricade / ventilation tool durability cost: **3**
- Mod version: **2.0**

## 1.2

### Loot plate
- 3D items larger (scale 0.75) with tuned floor height
- Floor plates: blocks sit on the plate instead of floating (`FIXED`)
- Wall plates: items flush against the plate face (wrong-side offset fixed)

### Other
- Mod version: **1.2**

## 1.1

### Loot plate
- 3D items (tools/weapons with custom models) use ground/fixed display transforms so they lie on the plate instead of sticking upright out of the ground
- Vertical (wall) plates: item yaw aligned with item-frame convention

### Other
- Mod version: **1.1**

## 1.0

Changes since **0.3** (and earlier 0.2.x).

### Airdrop
- Full airdrop system: `/cts airdrop x1 z1 x2 z2 [loot_table]`, warning broadcast, falling crate with parachute
- Curved descent, golden campfire-style smoke column while falling and after landing
- Lands as a **2×2×2** structure (hitbox matches the model), BER + flight animation
- One-shot loot: table rolls once, no regen on reopen
- Despawn timers: **30 min** untouched, **5 min** after partial loot, immediate when empty; timer shown in GUI
- Search time **20 seconds**
- Radio chat to all players: `[Radio] An airdrop is incoming!` and `[Radio] Airdrop sighted near X, Z`
- Ambient `airdrop` sound within **200 blocks** when the entity spawns
- Short loot-table ids (`airdrop` → `cts:airdrop`) + command suggestions

### Props / containers (0.2.x → 0.3 and follow-ups)
- Converted chest / barrel / shulker: open models and sounds
- Chest prop: correct facing
- Corpse: facing, texture variants, search/open sounds, hitbox and models without skew

### Other
- Mod version: **1.0**

# axe_chop — Blockbench

## Важно про топор

**Не крути `right_item` в Blockbench.** Белые кубы ≠ ванильный топор в игре.

В Minecraft перед предметом уже стоят свои повороты руки (`ItemInHandLayer`: −90° X, +180° Y) + display-трансформ топора. В Blockbench этого нет, поэтому ориентация куба и топора **не совпадают** — это ограничение Player Animator ([issue #63](https://github.com/KosmX/minecraftPlayerAnimator/issues/63)), не баг экспорта.

| В Blockbench | В моде |
|--------------|--------|
| Только руки (`right_arm` / `left_arm`) | Руки из BB + `rightItem` калибруется в JSON под игру |

## Руки

1. Открыть `model_item.bbmodel`
2. Import `axe_chop.animation.json`
3. Править **только** `right_arm` / `left_arm`
4. Export → кинуть файл сюда / сказать в чат

## Хват топора

Правится в `assets/cts/player_animation/axe_chop.json` → `rightItem`  
(pitch / yaw / roll / x / y / z). Подгоняем по скринам в игре, не по BB.

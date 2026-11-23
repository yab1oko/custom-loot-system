package cts.yblk.cts.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import cts.yblk.cts.Cts;
import cts.yblk.cts.client.ui.CtsTheme;

/**
 * Cloth Config UI — only loaded when cloth_config is present ({@link ClothConfigHook}).
 */
final class ClothConfigScreens {
    private ClothConfigScreens() {
    }

    static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("screen." + Cts.MODID + ".config"))
                .setSavingRunnable(CtsConfig::save);

        ConfigEntryBuilder entries = builder.entryBuilder();

        ConfigCategory loot = builder.getOrCreateCategory(Component.translatable("config." + Cts.MODID + ".loot"));
        loot.addEntry(entries.startIntField(
                        Component.translatable("config." + Cts.MODID + ".loot.defaultRespawnSeconds"),
                        CtsConfig.defaultLootRespawnSeconds())
                .setDefaultValue(CtsConfig.FALLBACK_LOOT_RESPAWN_SECONDS)
                .setMin(0)
                .setMax(36000)
                .setTooltip(Component.translatable("config." + Cts.MODID + ".loot.defaultRespawnSeconds.tooltip"))
                .setSaveConsumer(CtsConfig::setDefaultLootRespawnSeconds)
                .build());

        ConfigCategory airdrop = builder.getOrCreateCategory(Component.translatable("config." + Cts.MODID + ".airdrop"));
        airdrop.addEntry(entries.startStrField(
                        Component.translatable("config." + Cts.MODID + ".airdrop.defaultLootTable"),
                        CtsConfig.defaultAirdropLootTable())
                .setDefaultValue(CtsConfig.FALLBACK_AIRDROP_LOOT_TABLE)
                .setTooltip(Component.translatable("config." + Cts.MODID + ".airdrop.defaultLootTable.tooltip"))
                .setSaveConsumer(CtsConfig::setDefaultAirdropLootTable)
                .build());
        airdrop.addEntry(entries.startBooleanToggle(
                        Component.translatable("config." + Cts.MODID + ".airdrop.plane"),
                        CtsConfig.airdropPlane())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config." + Cts.MODID + ".airdrop.plane.tooltip"))
                .setSaveConsumer(CtsConfig::setAirdropPlane)
                .build());

        ConfigCategory ui = builder.getOrCreateCategory(Component.translatable("config." + Cts.MODID + ".ui"));
        ui.addEntry(entries.startEnumSelector(
                        Component.translatable("config." + Cts.MODID + ".ui.creativeTheme"),
                        CtsTheme.class,
                        CtsTheme.current())
                .setDefaultValue(CtsTheme.WOOD)
                .setEnumNameProvider(value -> Component.translatable(((CtsTheme) value).nameKey()))
                .setTooltip(Component.translatable(CtsTheme.current().descriptionKey()))
                .setSaveConsumer(theme -> CtsConfig.setCreativeTheme(theme.id()))
                .build());
        ui.addEntry(entries.startBooleanToggle(
                        Component.translatable("config." + Cts.MODID + ".ui.buttonSounds"),
                        CtsConfig.buttonSounds())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config." + Cts.MODID + ".ui.buttonSounds.tooltip"))
                .setSaveConsumer(CtsConfig::setButtonSounds)
                .build());
        ui.addEntry(entries.startBooleanToggle(
                        Component.translatable("config." + Cts.MODID + ".ui.refillSound"),
                        CtsConfig.refillSound())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config." + Cts.MODID + ".ui.refillSound.tooltip"))
                .setSaveConsumer(CtsConfig::setRefillSound)
                .build());

        return builder.build();
    }
}

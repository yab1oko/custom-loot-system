package cts.yblk.cts.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import cts.yblk.cts.Cts;

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

        return builder.build();
    }
}

package cts.yblk.cts.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import cts.yblk.cts.Cts;

public class ModNetwork {
    private static final String PROTOCOL = "22";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Cts.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, UpdateLootPlatePacket.class,
                UpdateLootPlatePacket::encode,
                UpdateLootPlatePacket::decode,
                UpdateLootPlatePacket::handle);
        CHANNEL.registerMessage(id++, UpdateCanStashPacket.class,
                UpdateCanStashPacket::encode,
                UpdateCanStashPacket::decode,
                UpdateCanStashPacket::handle);
        CHANNEL.registerMessage(id++, OpenCanStashPacket.class,
                OpenCanStashPacket::encode,
                OpenCanStashPacket::decode,
                OpenCanStashPacket::handle);
        CHANNEL.registerMessage(id++, UpdateEntityPlatePacket.class,
                UpdateEntityPlatePacket::encode,
                UpdateEntityPlatePacket::decode,
                UpdateEntityPlatePacket::handle);
        CHANNEL.registerMessage(id++, UpdateItemEntryPacket.class,
                UpdateItemEntryPacket::encode,
                UpdateItemEntryPacket::decode,
                UpdateItemEntryPacket::handle);
        CHANNEL.registerMessage(id++, UpdateLootBookEntryPacket.class,
                UpdateLootBookEntryPacket::encode,
                UpdateLootBookEntryPacket::decode,
                UpdateLootBookEntryPacket::handle);
        CHANNEL.registerMessage(id++, LootBookPagePacket.class,
                LootBookPagePacket::encode,
                LootBookPagePacket::decode,
                LootBookPagePacket::handle);
        CHANNEL.registerMessage(id++, LootBookNamePacket.class,
                LootBookNamePacket::encode,
                LootBookNamePacket::decode,
                LootBookNamePacket::handle);
        CHANNEL.registerMessage(id++, ExportLootBookPacket.class,
                ExportLootBookPacket::encode,
                ExportLootBookPacket::decode,
                ExportLootBookPacket::handle);
        CHANNEL.registerMessage(id++, LoadLootBookPacket.class,
                LoadLootBookPacket::encode,
                LoadLootBookPacket::decode,
                LoadLootBookPacket::handle);
        CHANNEL.registerMessage(id++, SyncLootBookPacket.class,
                SyncLootBookPacket::encode,
                SyncLootBookPacket::decode,
                SyncLootBookPacket::handle);
        CHANNEL.registerMessage(id++, RequestLootTablesPacket.class,
                RequestLootTablesPacket::encode,
                RequestLootTablesPacket::decode,
                RequestLootTablesPacket::handle);
        CHANNEL.registerMessage(id++, LootTableListPacket.class,
                LootTableListPacket::encode,
                LootTableListPacket::decode,
                LootTableListPacket::handle);
        CHANNEL.registerMessage(id++, ConvertContainerPacket.class,
                ConvertContainerPacket::encode,
                ConvertContainerPacket::decode,
                ConvertContainerPacket::handle);
        CHANNEL.registerMessage(id++, BreakBarricadePacket.class,
                BreakBarricadePacket::encode,
                BreakBarricadePacket::decode,
                BreakBarricadePacket::handle);
        CHANNEL.registerMessage(id++, UpdateBarricadePacket.class,
                UpdateBarricadePacket::encode,
                UpdateBarricadePacket::decode,
                UpdateBarricadePacket::handle);
        CHANNEL.registerMessage(id++, BreakVentilationGrillePacket.class,
                BreakVentilationGrillePacket::encode,
                BreakVentilationGrillePacket::decode,
                BreakVentilationGrillePacket::handle);
        CHANNEL.registerMessage(id++, UpdateVentilationGrillePacket.class,
                UpdateVentilationGrillePacket::encode,
                UpdateVentilationGrillePacket::decode,
                UpdateVentilationGrillePacket::handle);
        CHANNEL.registerMessage(id++, BreakMetalOrePacket.class,
                BreakMetalOrePacket::encode,
                BreakMetalOrePacket::decode,
                BreakMetalOrePacket::handle);
        CHANNEL.registerMessage(id++, UpdateMetalOrePacket.class,
                UpdateMetalOrePacket::encode,
                UpdateMetalOrePacket::decode,
                UpdateMetalOrePacket::handle);
        CHANNEL.registerMessage(id++, BreakHarvestTreePacket.class,
                BreakHarvestTreePacket::encode,
                BreakHarvestTreePacket::decode,
                BreakHarvestTreePacket::handle);
        CHANNEL.registerMessage(id++, UpdateHarvestTreePacket.class,
                UpdateHarvestTreePacket::encode,
                UpdateHarvestTreePacket::decode,
                UpdateHarvestTreePacket::handle);
        CHANNEL.registerMessage(id++, SearchAnimPacket.class,
                SearchAnimPacket::encode,
                SearchAnimPacket::decode,
                SearchAnimPacket::handle);
        CHANNEL.registerMessage(id++, RequestChestLibraryPacket.class,
                RequestChestLibraryPacket::encode,
                RequestChestLibraryPacket::decode,
                RequestChestLibraryPacket::handle);
        CHANNEL.registerMessage(id++, SyncChestLibraryPacket.class,
                SyncChestLibraryPacket::encode,
                SyncChestLibraryPacket::decode,
                SyncChestLibraryPacket::handle);
        CHANNEL.registerMessage(id++, RenameSavedChestPacket.class,
                RenameSavedChestPacket::encode,
                RenameSavedChestPacket::decode,
                RenameSavedChestPacket::handle);
        CHANNEL.registerMessage(id++, DeleteSavedChestPacket.class,
                DeleteSavedChestPacket::encode,
                DeleteSavedChestPacket::decode,
                DeleteSavedChestPacket::handle);
        CHANNEL.registerMessage(id++, TakeSavedChestPacket.class,
                TakeSavedChestPacket::encode,
                TakeSavedChestPacket::decode,
                TakeSavedChestPacket::handle);
        CHANNEL.registerMessage(id++, SaveChestToLibraryPacket.class,
                SaveChestToLibraryPacket::encode,
                SaveChestToLibraryPacket::decode,
                SaveChestToLibraryPacket::handle);
    }
}

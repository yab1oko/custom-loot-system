package cts.yblk.cts.client;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cts.yblk.cts.Cts;
import cts.yblk.cts.block.BarricadeBlock;
import cts.yblk.cts.block.CanStashBlock;
import cts.yblk.cts.block.EntityPlateBlock;
import cts.yblk.cts.block.LootPlateBlock;
import cts.yblk.cts.block.HarvestTreeStructure;
import cts.yblk.cts.block.MetalOreBlock;
import cts.yblk.cts.block.VentilationGrilleBlock;

@Mod.EventBusSubscriber(modid = Cts.MODID, value = Dist.CLIENT)
public class PlateOutlineEvents {
    @SubscribeEvent
    public static void hideOutline(RenderHighlightEvent.Block event) {
        if (!(event.getCamera().getEntity() instanceof net.minecraft.world.entity.player.Player player)) {
            return;
        }
        BlockHitResult hit = event.getTarget();
        var state = player.level().getBlockState(hit.getBlockPos());
        if (CanStashBlock.isSearchable(state) && !player.getAbilities().instabuild) {
            event.setCanceled(true);
            return;
        }
        if (state.getBlock() instanceof BarricadeBlock && !player.getAbilities().instabuild) {
            event.setCanceled(true);
            return;
        }
        if (state.getBlock() instanceof VentilationGrilleBlock && !player.getAbilities().instabuild) {
            event.setCanceled(true);
            return;
        }
        // Metal ore keeps a solid stone look when depleted — only hide outline while still mineable.
        if (state.getBlock() instanceof MetalOreBlock
                && MetalOreBlock.isMineableTarget(state)
                && !player.getAbilities().instabuild) {
            event.setCanceled(true);
            return;
        }
        if (HarvestTreeStructure.isChopTarget(player.level(), hit.getBlockPos())
                && !player.getAbilities().instabuild) {
            event.setCanceled(true);
            return;
        }
        if (state.getBlock() instanceof LootPlateBlock && state.getValue(LootPlateBlock.CONFIGURED)) {
            event.setCanceled(true);
            return;
        }
        if (state.getBlock() instanceof EntityPlateBlock && state.getValue(EntityPlateBlock.CONFIGURED)) {
            event.setCanceled(true);
        }
    }
}

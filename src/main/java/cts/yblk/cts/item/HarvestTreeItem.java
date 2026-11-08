package cts.yblk.cts.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import cts.yblk.cts.block.HarvestTreeStructure;

/** Enchantment glint marks the tree as a heavy creative-only prop. */
public class HarvestTreeItem extends BlockItem {
    private final HarvestTreeStructure.Size size;

    public HarvestTreeItem(Block block, HarvestTreeStructure.Size size, Properties properties) {
        super(block, properties);
        this.size = size;
    }

    public HarvestTreeStructure.Size getTreeSize() {
        return size;
    }

    @Override
    public String getDescriptionId() {
        // BlockItem would otherwise reuse the shared harvest_tree block name.
        return this.getOrCreateDescriptionId();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        return context.getPlayer() != null
                && context.getPlayer().getAbilities().instabuild
                && super.canPlace(context, state);
    }
}

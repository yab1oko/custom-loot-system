package cts.yblk.cts.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.jetbrains.annotations.Nullable;
import cts.yblk.cts.client.ConversionWandClient;
import cts.yblk.cts.registry.ModBlocks;
import cts.yblk.cts.registry.ModTags;

import java.util.List;

public class ConversionWandItem extends Item {
    public ConversionWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }
        if (!player.getAbilities().instabuild) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("item.cts.creative_only"), true);
            }
            return InteractionResult.FAIL;
        }

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        boolean revert = state.is(ModBlocks.CONVERTED_CONTAINER.get());
        boolean convert = !revert && canConvert(level, pos, state);
        if (!convert && !revert) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("gui.cts.convert_invalid"), true);
            }
            return InteractionResult.FAIL;
        }
        if (level.isClientSide) {
            ConversionWandClient.openConfirm(pos, revert);
        }
        return InteractionResult.SUCCESS;
    }

    public static boolean canConvert(Level level, BlockPos pos, BlockState state) {
        if (state.is(ModTags.PROP_CONTAINER) || state.is(ModBlocks.CONVERTED_CONTAINER.get())) {
            return false;
        }
        if (!state.is(ModTags.CONVERTIBLE)) {
            return false;
        }
        if (state.hasProperty(BlockStateProperties.CHEST_TYPE)
                && state.getValue(BlockStateProperties.CHEST_TYPE) != ChestType.SINGLE) {
            return false;
        }
        BlockEntity entity = level.getBlockEntity(pos);
        return entity instanceof BaseContainerBlockEntity;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.cts.conversion_wand.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.cts.conversion_wand.tooltip").withStyle(ChatFormatting.DARK_GRAY));
    }
}

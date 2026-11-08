package cts.yblk.cts.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.client.ChestLibraryClient;

import java.util.List;

public class ChestLibraryItem extends Item {
    public ChestLibraryItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("item.cts.creative_only"), true);
            }
            return InteractionResultHolder.fail(stack);
        }
        if (level.isClientSide) {
            ChestLibraryClient.openLibrary();
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
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
        if (player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof CanStashBlockEntity) {
            if (level.isClientSide) {
                ChestLibraryClient.openSavePrompt(pos, state.getBlock().getName().getString());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return use(level, player, context.getHand()).getResult();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.cts.chest_library.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.cts.chest_library.tooltip").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.cts.chest_library.tooltip_save").withStyle(ChatFormatting.DARK_GRAY));
    }
}

package cts.yblk.cts.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import cts.yblk.cts.blockentity.CanStashBlockEntity;
import cts.yblk.cts.blockentity.ConvertedContainerBlockEntity;
import cts.yblk.cts.registry.ModBlockEntities;

public class ConvertedContainerBlock extends CanStashBlock {
    public ConvertedContainerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean usesPlayerFacing() {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(LOOTED, false);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        // Facing is set by conversion wand / placement; do not randomize.
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof ConvertedContainerBlockEntity converted) {
            return converted.getMimic().getShape(level, pos, context);
        }
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof ConvertedContainerBlockEntity converted) {
            return converted.getMimic().getCollisionShape(level, pos, context);
        }
        return Shapes.block();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public SoundType getSoundType(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos, @Nullable net.minecraft.world.entity.Entity entity) {
        if (level.getBlockEntity(pos) instanceof ConvertedContainerBlockEntity converted) {
            return converted.getMimic().getSoundType();
        }
        return super.getSoundType(state);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ConvertedContainerBlockEntity converted) {
            return converted.getMimic().getLightEmission();
        }
        return super.getLightEmission(state, level, pos);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConvertedContainerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, ModBlockEntities.CONVERTED_CONTAINER.get(), CanStashBlockEntity::clientTick)
                : createTickerHelper(type, ModBlockEntities.CONVERTED_CONTAINER.get(), CanStashBlockEntity::serverTick);
    }

    public static Direction facingFrom(BlockState original) {
        if (original.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
            return original.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
        }
        if (original.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)) {
            Direction facing = original.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING);
            if (facing.getAxis().isHorizontal()) {
                return facing;
            }
        }
        return Direction.NORTH;
    }
}

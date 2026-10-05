package com.chaos.gravestone.block;

import org.jetbrains.annotations.Nullable;

import com.chaos.gravestone.GravestoneCompass;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GravestoneBlock extends BaseEntityBlock {

	public static final MapCodec<GravestoneBlock> CODEC = simpleCodec(GravestoneBlock::new);
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	private static final float BREAK_HARDNESS = 1.5F;
	// La forma del modelo mirando al norte, y sus rotaciones.
	private static final VoxelShape SHAPE_NORTH = Shapes.or(Block.box(1, 0, 4, 15, 3, 15), Block.box(2, 3, 8, 14, 16, 12));
	private static final VoxelShape SHAPE_SOUTH = Shapes.or(Block.box(1, 0, 1, 15, 3, 12), Block.box(2, 3, 4, 14, 16, 8));
	private static final VoxelShape SHAPE_EAST = Shapes.or(Block.box(1, 0, 1, 12, 3, 15), Block.box(4, 3, 2, 8, 16, 14));
	private static final VoxelShape SHAPE_WEST = Shapes.or(Block.box(4, 0, 1, 15, 3, 15), Block.box(8, 3, 2, 12, 16, 14));

	public GravestoneBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case SOUTH -> SHAPE_SOUTH;
			case EAST -> SHAPE_EAST;
			case WEST -> SHAPE_WEST;
			default -> SHAPE_NORTH;
		};
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new GravestoneBlockEntity(pos, state);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof GravestoneBlockEntity grave) || grave.isDecorative()) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		if (!(player instanceof ServerPlayer sp)) {
			return InteractionResult.PASS;
		}
		if (!grave.canOpen(player)) {
			player.displayClientMessage(
					Component.translatable("message.chaosgravestone.not_yours", grave.getOwnerName()), true);
			return InteractionResult.CONSUME;
		}
		grave.restoreTo(sp);
		level.removeBlock(pos, false);
		return InteractionResult.CONSUME;
	}

	/**
	 * Se rompen como la piedra las decorativas y, si es una tumba, solo su dueño.
	 * Para cualquier otro jugador es irrompible.
	 */
	@Override
	protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof GravestoneBlockEntity grave
				&& (grave.isDecorative() || grave.isOwner(player))) {
			return player.getDestroySpeed(state) / BREAK_HARDNESS / 30.0F;
		}
		return super.getDestroyProgress(state, player, level, pos);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide && level.getBlockEntity(pos) instanceof GravestoneBlockEntity grave) {
			if (grave.isDecorative()) {
				if (!player.isCreative()) {
					popResource(level, pos, new ItemStack(this));
				}
			} else if (player instanceof ServerPlayer sp && grave.isOwner(sp)) {
				// El dueño la rompe: todo cae al suelo (onRemove -> dropAll), no a su inventario.
				GravestoneCompass.removeFrom(sp, grave.getGraveId());
			}
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof GravestoneBlockEntity grave) {
			grave.dropAll();
		}
		super.onRemove(state, level, pos, newState, movedByPiston);
	}
}

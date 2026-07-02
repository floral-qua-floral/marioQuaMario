package com.fqf.charaformact.bapping;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact.packets.BappingPackets;
import com.fqf.charaformact.util.CfaClientHelperManager;
import com.fqf.charaformact.util.CfaGamerules;
import com.fqf.charaformact.util.CfaSounds;
import com.fqf.charaformact_api.interfaces.BapResult;
import com.fqf.charaformact_api.interfaces.Bappable;
import com.fqf.charaformact_api.util.CfaTags;
import com.google.common.collect.ImmutableSet;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

public class BlockBappingUtil {
	private static final Map<World, WorldBapsInfo> PER_WORLD_BAP_STORAGE = new HashMap<>();
	public static @Nullable WorldBapsInfo getBapsInfoNullable(World world) {
		return PER_WORLD_BAP_STORAGE.get(world);
	}

	public static void conditionallyHideBlockPos(World world, BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
		if(cir.getReturnValue() == null) return;
		WorldBapsInfo worldBaps = getBapsInfoNullable(world);
		if(worldBaps == null) return;
		if(worldBaps.HIDDEN.contains(pos)) {
			Direction direction;
			if(worldBaps.ALL_BAPS.get(pos) instanceof BumpingBlockInfo bump) {
				direction = bump.DISPLACEMENT_DIRECTION.getOpposite();
			}
			else direction = Direction.DOWN;
			cir.setReturnValue(world.getFluidState(pos.offset(direction)).getFluid().getDefaultState().getBlockState());
		}
	}

	private static boolean forcingVanillaHardnessCheck = false;
	public static boolean shouldApplyHardnessMixin() {
		return !forcingVanillaHardnessCheck;
	}
	private static float getVanillaHardness(BlockView world, BlockPos pos, BlockState state) {
		forcingVanillaHardnessCheck = true;
		try {
			return state.getHardness(world, pos);
		}
		finally {
			forcingVanillaHardnessCheck = false;
		}
	}
	public static float getVanillaHardnessForMixin(BlockState instance, BlockView blockView, BlockPos blockPos, Operation<Float> original) {
		// This is silly duplicated code and it's not good, but I wanted to make sure that the mixin still calls the original operation.
		// Just in case another mod's mixin wants to do something there...??????
		// Hopefully this will make it more compatible? If anyone decides they want to do that ever in the whole world???
		forcingVanillaHardnessCheck = true;
		try {
			return original.call(instance, blockView, blockPos);
		}
		finally {
			forcingVanillaHardnessCheck = false;
		}
	}

	private static BapResult handleBapResultForAdventureMode(BapResult original, BlockState state, PlayerEntity bapper) {
		if(bapper.getAbilities().allowModifyWorld || !CfaGamerules.restrictAdventureBapping) return original;

		return switch(original) {
			case BREAK -> BapResult.BUMP_EMBRITTLE;
			case BREAK_WITHOUT_POWERING -> BapResult.BUMP_EMBRITTLE_WITHOUT_POWERING;
			case BUST -> state.isIn(CfaTags.NOT_POWERED_WHEN_BAPPED) ? BapResult.BUMP_EMBRITTLE : BapResult.BUMP_EMBRITTLE_WITHOUT_POWERING;
			default -> original;
		};
	}

	@SuppressWarnings("UnusedReturnValue")
	public static BapResult attemptBap(CfaPlayerData data, World world, BlockPos pos, Direction direction, int strength, boolean fullyNetwork) {
		BlockState blockState = world.getBlockState(pos);

		BapResult result = handleBapResultForAdventureMode(((Bappable) blockState.getBlock()).cfa$getBapResult(
				data, world,
				pos, blockState, getVanillaHardness(world, pos, blockState),
				direction, strength
		), blockState, data.getPlayer());

		networkAndStoreBapInfo(world, pos, direction, strength, data.getPlayer(), result, fullyNetwork);

		return result;
	}
	public static void networkAndStoreBapInfo(World world, BlockPos pos, Direction direction, int strength, @Nullable Entity bapper, BapResult result, boolean fullyNetwork) {
		AbstractBapInfo info = makeBapInfo(world, pos, direction, strength, bapper, result);

		if(result != BapResult.FAIL && fullyNetwork && bapper instanceof PlayerEntity player && player.isMainPlayer()) {
			CfaClientHelperManager.packetSender.bapBlockC2S(pos, direction, player.cfa$getCfaData().getAction());
			CfaClientHelperManager.packetSender.conditionallySaveBapToReplayMod(pos, direction, strength, result, bapper);
		}

		if(info != null) {
			storeBapInfo(info, true);
			if(!world.isClient) {
				Objects.requireNonNull(bapper, "Bapper is null, but there's no way to network a null bapper.");
				BappingPackets.bapS2C(
						(ServerWorld) info.WORLD, info.POS,
						direction, strength, result,
						bapper, fullyNetwork
				);
			}
		}
	}

	public static @Nullable AbstractBapInfo makeBapInfo(World world, BlockPos pos, Direction direction, int strength, @Nullable Entity bapper, BapResult result) {
		BlockState blockState = world.getBlockState(pos);
		((Bappable) blockState.getBlock()).cfa$onBapped(
				bapper instanceof PlayerEntity player ? player.cfa$getCfaData() : null,
				world, pos, blockState, direction, strength,
				result);
		switch(result) {
			case BUMP, BUMP_WITHOUT_POWERING, BUMP_EMBRITTLE, BUMP_EMBRITTLE_WITHOUT_POWERING, BREAK,
				 BREAK_WITHOUT_POWERING -> {
				world.playSound(bapper, pos, CfaSounds.BUMP, SoundCategory.BLOCKS, 0.4F, 1.0F);
				BlockSoundGroup group = blockState.getSoundGroup();
				world.playSound(bapper, pos, group.getPlaceSound(), SoundCategory.BLOCKS, group.pitch * 0.8F, group.volume);
			}
		}
		switch(result) {
			case BUMP, BUMP_WITHOUT_POWERING -> {
				return new BumpingBlockInfo(world, pos, result, direction, bapper);
			}
			case BUMP_EMBRITTLE, BUMP_EMBRITTLE_WITHOUT_POWERING -> {
				return new BumpingEmbrittlingBlockInfo(world, pos, result, direction, bapper);
			}
			case BREAK, BREAK_WITHOUT_POWERING -> {
				return new BapBreakingBlockInfo(world, pos, result, direction, bapper);
			}
			case BUST -> {
				if(bapper instanceof PlayerEntity player) mineBlockWithBap(world, pos, player);
				else world.breakBlock(pos, true, bapper);
				return null;
			}
			default -> {
				return null;
			}
		}
	}

	public static void mineBlockWithBap(World world, BlockPos pos, PlayerEntity player) {
		// Code taken from ServerPlayerInteractionManager and ClientPlayerInteractionManager.
		BlockState blockState = world.getBlockState(pos);
		Block block = blockState.getBlock();

		if(block instanceof OperatorBlock && player.isCreativeLevelTwoOp()) {
			if(!world.isClient)
				world.updateListeners(pos, blockState, blockState, Block.NOTIFY_ALL);
			return;
		}

		BlockState iDunnoWhatThisDoes = block.onBreak(world, pos, blockState, player);

		boolean removedSuccessfully;

		if(world.isClient) {
			FluidState fluidState = world.getFluidState(pos);
			removedSuccessfully = world.setBlockState(pos, fluidState.getBlockState(), Block.NOTIFY_ALL_AND_REDRAW);
		}
		else {
			removedSuccessfully = world.removeBlock(pos, false);
		}

		if(removedSuccessfully) {
			block.onBroken(world, pos, iDunnoWhatThisDoes);

			if(!world.isClient && !player.isCreative())
				block.afterBreak(world, player, pos, iDunnoWhatThisDoes, world.getBlockEntity(pos), ItemStack.EMPTY);
		}
	}

	private static final int MAX_PROPAGATIONS = 10;

	public static void storeBapInfo(AbstractBapInfo info, boolean canPropagate) {
		if(canPropagate && info instanceof BumpingBlockInfo bumping) {
			ImmutableSet.Builder<AbstractBapInfo> builder = ImmutableSet.builderWithExpectedSize(0);

			tryWithBlockRemoved(info.WORLD, info.POS, info.WORLD.getBlockState(info.POS), () -> {
				Direction opposite = bumping.DISPLACEMENT_DIRECTION.getOpposite();
				for(Direction direction : Direction.values()) {
					propagateBaps(bumping, direction, 1, direction == opposite ? MAX_PROPAGATIONS - 1 : 0, builder);
				}
			});

			for(AbstractBapInfo newBap : builder.build()) {
				storeBapInfo(newBap, false);
			}
		}

		WorldBapsInfo worldBaps = getBapsInfoNullable(info.WORLD);
		if(worldBaps == null) {
			worldBaps = new WorldBapsInfo();
			PER_WORLD_BAP_STORAGE.put(info.WORLD, worldBaps);
		}

		addOrRemoveFromSets(info, worldBaps, true);
	}

	private static void propagateBaps(BumpingBlockInfo info, Direction direction, int distance, int remaining, ImmutableSet.Builder<AbstractBapInfo> builder) {
		BlockPos indirectPos = info.POS.offset(direction, distance);
		BlockState indirectState = info.WORLD.getBlockState(indirectPos);

		// I don't know if this is necessary because I haven't got a clue what Air returns for canPlaceAt.
		if(indirectState.isAir()) return;

		// Check if the block could be legally placed at its current position if the currently bapped blocks were all air.
		if(!indirectState.canPlaceAt(info.WORLD, indirectPos)) {
			// This block cannot be placed here, which means it's probably attached to one of the blocks being bapped.
			// That means we should bap this block too, and keep propagating.
			AbstractBapInfo newInfo;
			boolean power = !indirectState.isIn(CfaTags.NOT_POWERED_WHEN_BAPPED);
			if(indirectState.isIn(CfaTags.DESTROYED_BY_INDIRECT_BAP))
				newInfo = new BapBreakingBlockInfo(info.WORLD, indirectPos,
						power ? BapResult.BREAK : BapResult.BREAK_WITHOUT_POWERING, info.DISPLACEMENT_DIRECTION, info.BAPPER);
			else if(indirectState.getHardness(info.WORLD, indirectPos) == 0)
				newInfo = new BumpingBlockInfo(info.WORLD, indirectPos,
						power ? BapResult.BUMP : BapResult.BUMP_WITHOUT_POWERING, info.DISPLACEMENT_DIRECTION, info.BAPPER);
			else
				newInfo = new BumpingEmbrittlingBlockInfo(info.WORLD, indirectPos,
						power ? BapResult.BUMP_EMBRITTLE : BapResult.BUMP_EMBRITTLE_WITHOUT_POWERING, info.DISPLACEMENT_DIRECTION, info.BAPPER);

			builder.add(newInfo);
		}

		if(distance < remaining)
			tryWithBlockRemoved(info.WORLD, indirectPos, indirectState, () ->
					propagateBaps(info, direction, distance + 1, remaining, builder));
	}

	private static void tryWithBlockRemoved(World world, BlockPos removedPos, BlockState removedState, Runnable runnable) {
		// We remove the block by directly editing the palette like an actual lunatic because we're going to change it all
		// back within the tick anyways, so we don't want to trigger any block or rendering updates.
		WorldChunk bapWorldChunk = world.getWorldChunk(removedPos);
		PalettedContainer<BlockState> container = bapWorldChunk.getSection(bapWorldChunk.getSectionIndex(removedPos.getY())).getBlockStateContainer();
		try {
			container.set(removedPos.getX() & 15, removedPos.getY() & 15, removedPos.getZ() & 15, Blocks.AIR.getDefaultState());
			runnable.run();
		}
		finally {
			container.set(removedPos.getX() & 15, removedPos.getY() & 15, removedPos.getZ() & 15, removedState);
		}
	}

	// We represent the sets as enums instead of themselves so that we can store them in a list and then check that list
	// with .contains(). If we just put the sets themselves in a list, then that would give false positives, since they
	// can be considered equal if their contents are equal! Upsetting!!
	private enum WorldBapsSet {
		HIDDEN { @Override public Set<BlockPos> get(WorldBapsInfo world) { return world.HIDDEN; } },
		BRITTLE { @Override public Set<BlockPos> get(WorldBapsInfo world) { return world.BRITTLE; } },
		POWERED { @Override public Set<BlockPos> get(WorldBapsInfo world) { return world.POWERED; } };
		public abstract Set<BlockPos> get(WorldBapsInfo world);
	}

	public static void addOrRemoveFromSets(AbstractBapInfo info, WorldBapsInfo world, boolean adding) {
		List<WorldBapsSet> sets = switch(info.RESULT) {
			case BUMP, BREAK -> List.of(WorldBapsSet.HIDDEN, WorldBapsSet.POWERED);
			case BUMP_WITHOUT_POWERING, BREAK_WITHOUT_POWERING -> List.of(WorldBapsSet.HIDDEN);
			case BUMP_EMBRITTLE -> List.of(WorldBapsSet.HIDDEN, WorldBapsSet.BRITTLE, WorldBapsSet.POWERED);
			case BUMP_EMBRITTLE_WITHOUT_POWERING -> List.of(WorldBapsSet.HIDDEN, WorldBapsSet.BRITTLE);
			case EMBRITTLE -> List.of(WorldBapsSet.BRITTLE);
			case BUST, FAIL -> throw new IllegalStateException("BapInfo with BUST or FAIL result shouldn't be possible!");
		};

		if(adding) {
			AbstractBapInfo prevInfo = world.ALL_BAPS.put(info.POS, info);
			if(prevInfo != null) addOrRemoveFromSets(prevInfo, world, false);

			for(WorldBapsSet set : sets) {
				set.get(world).add(info.POS);
			}
		}
		else {
//			world.ALL_BAPS.remove(info.POS); // DON'T ACTUALLY REMOVE IT!!! By now it's already been replaced or removed!
			for(WorldBapsSet set : sets) {
				set.get(world).remove(info.POS);
			}
//			info.finishAndGetReplacement();
		}
		if(sets.contains(WorldBapsSet.HIDDEN)) {
			reRenderPos(info);
			if(!adding && info.WORLD.isClient() && info.RESULT != BapResult.BREAK && info.RESULT != BapResult.BREAK_WITHOUT_POWERING) {
				int lingerFrames = CharaFormAct.CONFIG.getBumpedBlockLingerFrames();
				if(lingerFrames > 0)
					world.HIDDEN_LINGERING.add(new WorldBapsInfo.LingeringInfo(info.POS, sets.contains(WorldBapsSet.BRITTLE), lingerFrames));
			}
		}
		if(sets.contains(WorldBapsSet.POWERED)) info.WORLD.updateNeighbor(info.POS, info.WORLD.getBlockState(info.POS).getBlock(), info.POS);
	}

	public static void reRenderPos(AbstractBapInfo info) {
		reRenderPos(info.WORLD, info.POS);
	}
	public static void reRenderPos(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if(world.isClient()) world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
	}

	public static void commonWorldTick(World world) {
		WorldBapsInfo worldBaps = getBapsInfoNullable(world);
		if(worldBaps == null) return;

		Set<AbstractBapInfo> replacementBaps = null;

		Iterator<AbstractBapInfo> iterator = worldBaps.ALL_BAPS.values().iterator();
		while(iterator.hasNext()) {
			AbstractBapInfo tickBap = iterator.next();

			tickBap.tick();
			if(tickBap.isDone()) {
				AbstractBapInfo replacement = tickBap.finishAndGetReplacement();
				if(replacement != null) {
					if(replacementBaps == null) replacementBaps = new HashSet<>();
					replacementBaps.add(replacement);
				}
				iterator.remove();
				addOrRemoveFromSets(tickBap, worldBaps, false);
			}
		}

		if(replacementBaps != null) for(AbstractBapInfo replacementBap : replacementBaps) {
			storeBapInfo(replacementBap, true);
		}
	}

	public static void serverWorldTick(ServerWorld world) {
		commonWorldTick(world);
	}
}

package com.fqf.mario_qua_mario.bapping;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.packets.MarioBappingPackets;
import com.fqf.mario_qua_mario.util.MarioClientHelperManager;
import com.fqf.mario_qua_mario.util.MarioGamerules;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.fqf.mario_qua_mario_api.interfaces.BapResult;
import com.fqf.mario_qua_mario_api.interfaces.Bappable;
import com.fqf.mario_qua_mario_api.util.MQMTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
	private static float getVanillaHardness(World world, BlockPos pos, BlockState state) {
		forcingVanillaHardnessCheck = true;
		float hardness = state.getHardness(world, pos);
		forcingVanillaHardnessCheck = false;
		return hardness;
	}

	private static BapResult handleBapResultForAdventureMode(BapResult original, BlockState state, PlayerEntity bapper) {
		if(bapper.getAbilities().allowModifyWorld || !MarioGamerules.restrictAdventureBapping) return original;

		return switch(original) {
			case BREAK -> BapResult.BUMP_EMBRITTLE;
			case BREAK_NO_POWER -> BapResult.BUMP_EMBRITTLE_NO_POWER;
			case BUST -> state.isIn(MQMTags.NOT_POWERED_WHEN_BAPPED) ? BapResult.BUMP_EMBRITTLE : BapResult.BUMP_EMBRITTLE_NO_POWER;
			default -> original;
		};
	}

	@SuppressWarnings("UnusedReturnValue")
	public static BapResult attemptBap(MarioPlayerData data, World world, BlockPos pos, Direction direction, int strength) {
		BlockState blockState = world.getBlockState(pos);

		BapResult result = handleBapResultForAdventureMode(((Bappable) blockState.getBlock()).mqm$getBapResult(
				data, world,
				pos, blockState, getVanillaHardness(world, pos, blockState),
				direction, strength
		), blockState, data.getMario());

		AbstractBapInfo info = makeBapInfo(world, pos, direction, strength, data.getMario(), result);

		if(data.getMario().isMainPlayer() && result != BapResult.FAIL) {
			MarioClientHelperManager.packetSender.bapBlockC2S(pos, direction, data.getAction());
			MarioClientHelperManager.packetSender.conditionallySaveBapToReplayMod(pos, direction, strength, result, data.getMario());
		}

		if(info != null) {
			storeBapInfo(info, 0);
			if(data.isServer()) {
				MarioBappingPackets.bapS2C(
						(ServerWorld) world, pos,
						direction, strength, result,
						data.getMario(), false
				);
			}
		}

		return result;
	}

	public static @Nullable AbstractBapInfo makeBapInfo(World world, BlockPos pos, Direction direction, int strength, @Nullable Entity bapper, BapResult result) {
		BlockState blockState = world.getBlockState(pos);
		((Bappable) blockState.getBlock()).mqm$onBapped(
				bapper instanceof PlayerEntity mario ? mario.mqm$getMarioData() : null,
				world, pos, blockState, direction, strength,
				result);
		switch(result) {
			case BUMP, BUMP_NO_POWER, BUMP_EMBRITTLE, BUMP_EMBRITTLE_NO_POWER, BREAK, BREAK_NO_POWER -> {
				world.playSound(bapper, pos, MarioSFX.BUMP, SoundCategory.BLOCKS, 0.4F, 1.0F);
				BlockSoundGroup group = blockState.getSoundGroup();
				world.playSound(bapper, pos, group.getPlaceSound(), SoundCategory.BLOCKS, group.pitch * 0.8F, group.volume);
			}
		}
		switch(result) {
			case BUMP, BUMP_NO_POWER -> {
				return new BumpingBlockInfo(world, pos, result, direction, bapper);
			}
			case BUMP_EMBRITTLE, BUMP_EMBRITTLE_NO_POWER -> {
				return new BumpingEmbrittlingBlockInfo(world, pos, result, direction, bapper);
			}
			case BREAK, BREAK_NO_POWER -> {
				return new BapBreakingBlockInfo(world, pos, result, direction, bapper);
			}
			case BUST -> {
				world.breakBlock(pos, true, bapper);
				return null;
			}
			default -> {
				return null;
			}
		}
	}

	private static void indirectBap(BumpingBlockInfo info, Direction direction, int propagations) {
		BlockPos indirectPos = info.POS.offset(direction);
		BlockState indirectState = info.WORLD.getBlockState(indirectPos);
		if(indirectState.isAir()) return;

		if(!indirectState.canPlaceAt(info.WORLD, indirectPos)) {
			AbstractBapInfo newInfo;
			boolean power = !indirectState.isIn(MQMTags.NOT_POWERED_WHEN_BAPPED);
			if(indirectState.isIn(MQMTags.DESTROYED_BY_INDIRECT_BAP))
				newInfo = new BapBreakingBlockInfo(info.WORLD, indirectPos,
						power ? BapResult.BREAK : BapResult.BREAK_NO_POWER, info.DISPLACEMENT_DIRECTION, info.BAPPER);
			else if(indirectState.getHardness(info.WORLD, indirectPos) == 0)
				newInfo = new BumpingBlockInfo(info.WORLD, indirectPos,
						power ? BapResult.BUMP : BapResult.BUMP_NO_POWER, info.DISPLACEMENT_DIRECTION, info.BAPPER);
			else
				newInfo = new BumpingEmbrittlingBlockInfo(info.WORLD, indirectPos,
						power ? BapResult.BUMP_EMBRITTLE : BapResult.BUMP_EMBRITTLE_NO_POWER, info.DISPLACEMENT_DIRECTION, info.BAPPER);

			storeBapInfo(newInfo, propagations + 1);
		}
	}

	private static final int MAX_PROPAGATIONS = 10;

	public static void storeBapInfo(AbstractBapInfo info, int propagations) {
		if(info instanceof BumpingBlockInfo bumping && propagations < MAX_PROPAGATIONS) {
			BlockState bappedState = info.WORLD.getBlockState(info.POS);

			WorldChunk bapWorldChunk = info.WORLD.getWorldChunk(info.POS);
			PalettedContainer<BlockState> container = bapWorldChunk.getSection(bapWorldChunk.getSectionIndex(info.POS.getY())).getBlockStateContainer();
			container.set(info.POS.getX() & 15, info.POS.getY() & 15, info.POS.getZ() & 15,
					Blocks.AIR.getDefaultState());

			Direction opposite = bumping.DISPLACEMENT_DIRECTION.getOpposite();

			if(propagations == 0) {
				for(Direction direction : Direction.values()) {
					if(direction != opposite)
						indirectBap(bumping, direction, propagations);
				}
			}

			indirectBap(bumping, opposite, propagations);

			container.set(info.POS.getX() & 15, info.POS.getY() & 15, info.POS.getZ() & 15,
					bappedState);
		}

		if(info.WORLD.isClient())
			MarioClientHelperManager.helper.clientBap(info);

		WorldBapsInfo worldBaps = getBapsInfoNullable(info.WORLD);
		if(worldBaps == null) {
			worldBaps = new WorldBapsInfo();
			PER_WORLD_BAP_STORAGE.put(info.WORLD, worldBaps);
		}

		addOrRemoveFromSets(info, worldBaps, true);
	}

	public static void addOrRemoveFromSets(AbstractBapInfo info, WorldBapsInfo world, boolean adding) {
		List<Set<BlockPos>> sets = switch(info.RESULT) {
			case BUMP, BREAK -> List.of(world.HIDDEN, world.POWERED);
			case BUMP_NO_POWER, BREAK_NO_POWER -> List.of(world.HIDDEN);
			case BUMP_EMBRITTLE -> List.of(world.HIDDEN, world.POWERED, world.BRITTLE);
			case BUMP_EMBRITTLE_NO_POWER -> List.of(world.HIDDEN, world.BRITTLE);
			case EMBRITTLE -> List.of(world.BRITTLE);
			case BUST, FAIL -> throw new IllegalStateException("BapInfo with BUST or FAIL result shouldn't be possible!");
		};

		if(adding) {
			AbstractBapInfo prevInfo = world.ALL_BAPS.put(info.POS, info);
			if(prevInfo != null) addOrRemoveFromSets(prevInfo, world, false);

			for(Set<BlockPos> set : sets) {
				set.add(info.POS);
			}
		}
		else {
//			world.ALL_BAPS.remove(info.POS); // DON'T ACTUALLY REMOVE IT!!! By now it's already been replaced or removed!
			for(Set<BlockPos> set : sets) {
				set.remove(info.POS);
			}
			info.finishAndGetReplacement();
		}
		if(sets.contains(world.HIDDEN)) reRenderPos(info);
		if(sets.contains(world.POWERED)) info.WORLD.updateNeighbor(info.POS, info.WORLD.getBlockState(info.POS).getBlock(), info.POS);
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
			storeBapInfo(replacementBap, 0);
		}
	}

	public static void serverWorldTick(ServerWorld world) {
		commonWorldTick(world);
	}
}

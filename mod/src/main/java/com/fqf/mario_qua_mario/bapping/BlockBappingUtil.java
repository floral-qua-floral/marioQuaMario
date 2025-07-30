package com.fqf.mario_qua_mario.bapping;

import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.packets.MarioBappingPackets;
import com.fqf.mario_qua_mario.util.MarioClientHelperManager;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.fqf.mario_qua_mario_api.interfaces.BapResult;
import com.fqf.mario_qua_mario_api.interfaces.Bappable;
import it.unimi.dsi.fastutil.objects.Object2ByteArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

public class BlockBappingUtil {
	private static final Map<World, WorldBapsInfo> PER_WORLD_BAP_STORAGE = new HashMap<>();
	public static @Nullable WorldBapsInfo getBapsInfoNullable(World world) {
		return PER_WORLD_BAP_STORAGE.get(world);
	}

	private static final BlockState EMPTY_BLOCK = Blocks.VOID_AIR.getDefaultState();
	private static FluidState getFluidIntoPos(World world, BlockPos pos, Direction direction) {
		return world.getFluidState(pos.offset(direction));
	}

	private static Fluid getFluidIntoPos(World world, BlockPos pos) {
		// This is ugly and bad but I can't think of a cleaner solution >:(
		FluidState fluid = getFluidIntoPos(world, pos, Direction.UP);
		if(!fluid.isEmpty()) return fluid.getFluid();
		fluid = getFluidIntoPos(world, pos, Direction.NORTH);
		if(!fluid.isEmpty()) return fluid.getFluid();
		fluid = getFluidIntoPos(world, pos, Direction.EAST);
		if(!fluid.isEmpty()) return fluid.getFluid();
		fluid = getFluidIntoPos(world, pos, Direction.SOUTH);
		if(!fluid.isEmpty()) return fluid.getFluid();
		return getFluidIntoPos(world, pos, Direction.WEST).getFluid();
	}
	private static BlockState getEmptyBlockAt(World world, BlockPos pos) {
		return getFluidIntoPos(world, pos).getDefaultState().getBlockState();
	}
	public static void conditionallyHideBlockPos(World world, BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
		if(cir.getReturnValue() == null) return;
		WorldBapsInfo worldBaps = getBapsInfoNullable(world);
		if(worldBaps == null) return;
		if(worldBaps.HIDDEN.contains(pos)) cir.setReturnValue(getEmptyBlockAt(world, pos));
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

	@SuppressWarnings("UnusedReturnValue") // TODO: check for BUST, if so player keeps moving through broken block
	public static BapResult attemptBap(MarioPlayerData data, World world, BlockPos pos, Direction direction, int strength) {
		BlockState blockState = world.getBlockState(pos);

		BapResult result = ((Bappable) blockState.getBlock()).mqm$getBapResult(
				data, world,
				pos, blockState, getVanillaHardness(world, pos, blockState),
				direction, strength
		);

		AbstractBapInfo info = makeBapInfo(world, pos, direction, strength, data.getMario(), result);

		if(data.getMario().isMainPlayer() && result != BapResult.FAIL) {
			MarioClientHelperManager.packetSender.bapBlockC2S(pos, direction, data.getAction());
			MarioClientHelperManager.packetSender.conditionallySaveBapToReplayMod(pos, direction, strength, result, data.getMario());
		}

		if(info != null) {
			storeBapInfo(info);
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

	public static void storeBapInfo(AbstractBapInfo info) {
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
			storeBapInfo(replacementBap);
		}
	}

	public static void serverWorldTick(ServerWorld world) {
		commonWorldTick(world);
	}
}

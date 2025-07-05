package com.fqf.mario_qua_mario.bapping;

import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.packets.MarioBappingPackets;
import com.fqf.mario_qua_mario.util.MarioClientHelperManager;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.fqf.mario_qua_mario_api.interfaces.BapResult;
import com.fqf.mario_qua_mario_api.interfaces.Bappable;
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
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

public class BlockBappingUtil {
	public static final Map<World, Map<BlockPos, AbstractBapInfo>> ALL_BAPPED_BLOCKS = new HashMap<>();
	public static final Map<World, Set<BlockPos>> HIDDEN_BLOCK_POSITIONS = new HashMap<>();
	public static final Map<World, Set<BlockPos>> BRITTLE_BLOCK_POSITIONS = new HashMap<>(); // consider: object2int map
	public static final Map<World, Set<BlockPos>> POWERED_BLOCK_POSITIONS = new HashMap<>();

	private static final BlockState EMPTY_BLOCK = Blocks.VOID_AIR.getDefaultState();
	public static void conditionallyHideBlockPos(World world, BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
		if(cir.getReturnValue() == null) return;
		Set<BlockPos> hiddenBlocksInWorld = HIDDEN_BLOCK_POSITIONS.get(world);
		if(hiddenBlocksInWorld == null) return;
		if(hiddenBlocksInWorld.contains(pos)) cir.setReturnValue(EMPTY_BLOCK);
	}

	@SuppressWarnings("UnusedReturnValue") // TODO: check for BUST, if so player keeps moving through broken block
	public static BapResult attemptBap(MarioPlayerData data, World world, BlockPos pos, Direction direction, int strength) {
		BlockState blockState = world.getBlockState(pos);
		BapResult result = ((Bappable) blockState.getBlock()).mqm$getBapResult(data, world, pos, blockState, direction, strength);
		AbstractBapInfo info = makeBapInfo(world, pos, direction, strength, data.getMario(), result);

		if(data.getMario().isMainPlayer() && result != BapResult.FAIL) {
			MarioClientHelperManager.packetSender.bapBlockC2S(pos, direction, data.getAction());
			MarioClientHelperManager.packetSender.conditionallySaveBapToReplayMod(pos, direction, strength, result, data.getMario());
		}

		if(info != null) {
			storeBapInfo(info, true);
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
				world, blockState, direction, strength, result
		);
		switch(result) {
			case BUMP, BUMP_EMBRITTLE, BREAK -> {
				world.playSound(bapper, pos, MarioSFX.BUMP, SoundCategory.BLOCKS, 0.4F, 1.0F);
				BlockSoundGroup group = blockState.getSoundGroup();
				world.playSound(bapper, pos, group.getPlaceSound(), SoundCategory.BLOCKS, group.pitch * 0.8F, group.volume);
			}
		}
		switch(result) {
			case BUMP -> {
				return new BumpingBlockInfo(world, pos, direction, bapper);
			}
			case BUMP_EMBRITTLE -> {
				return new BumpingEmbrittlingBlockInfo(world, pos, direction, bapper);
			}
			case BREAK -> {
				return new BapBreakingBlockInfo(world, pos, direction, bapper);
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

	public static void storeBapInfo(AbstractBapInfo info, boolean networkFromClient) {
		if(info.WORLD.isClient())
			MarioClientHelperManager.helper.clientBap(info);

		ALL_BAPPED_BLOCKS.putIfAbsent(info.WORLD, new HashMap<>());
		AbstractBapInfo prevInfo = ALL_BAPPED_BLOCKS.get(info.WORLD).put(info.POS, info);
		if(prevInfo != null) prevInfo.finish(); // discard new AbstractBapInfo if provided
		switch(info.RESULT) {
			case BUMP_EMBRITTLE -> {
				getCertain(BRITTLE_BLOCK_POSITIONS, info.WORLD).add(info.POS);
				getCertain(HIDDEN_BLOCK_POSITIONS, info.WORLD).add(info.POS);
				getCertain(POWERED_BLOCK_POSITIONS, info.WORLD).add(info.POS);
				info.WORLD.updateNeighbor(info.POS, info.WORLD.getBlockState(info.POS).getBlock(), info.POS);
				reRenderPos(info);
			}
			case EMBRITTLE -> {
				getCertain(BRITTLE_BLOCK_POSITIONS, info.WORLD).add(info.POS);
			}
			case BUMP -> {
				getCertain(HIDDEN_BLOCK_POSITIONS, info.WORLD).add(info.POS);
				getCertain(POWERED_BLOCK_POSITIONS, info.WORLD).add(info.POS);
				reRenderPos(info);
			}
			case BREAK -> {
				getCertain(HIDDEN_BLOCK_POSITIONS, info.WORLD).add(info.POS);
				reRenderPos(info);
			}
		}
	}

	public static <setContents> Set<setContents> getCertain(Map<World, Set<setContents>> map, World world) {
		map.putIfAbsent(world, new HashSet<>());
		return map.get(world);
	}

	public static void reRenderPos(AbstractBapInfo info) {
		reRenderPos(info.WORLD, info.POS);
	}
	public static void reRenderPos(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if(world.isClient()) world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
	}

	private static final Set<AbstractBapInfo> REPLACEMENT_BAPS = new HashSet<>();
	public static void commonWorldTick(World world) {
		Map<BlockPos, AbstractBapInfo> bapsInWorld = ALL_BAPPED_BLOCKS.get(world);
		if(bapsInWorld == null) return;
		final Iterator<Map.Entry<BlockPos, AbstractBapInfo>> each = bapsInWorld.entrySet().iterator();

		// kinda clueless
		while(each.hasNext()) {
			AbstractBapInfo next = each.next().getValue();

			next.tick();
			if(next.isDone()) {
				AbstractBapInfo replacement = next.finish();
				if(replacement != null) REPLACEMENT_BAPS.add(replacement);
				each.remove();
			}
		}

		if(!REPLACEMENT_BAPS.isEmpty()) {
			REPLACEMENT_BAPS.forEach(info -> storeBapInfo(info, false));
			REPLACEMENT_BAPS.clear();
		}
	}

	public static void serverWorldTick(ServerWorld world) {
		commonWorldTick(world);
	}
}

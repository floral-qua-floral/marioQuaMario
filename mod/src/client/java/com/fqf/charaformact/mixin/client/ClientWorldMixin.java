package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.bapping.BapBreakingBlockInfo;
import com.fqf.charaformact.bapping.BlockBappingUtil;
import com.fqf.charaformact.bapping.WorldBapsInfo;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
	@WrapMethod(method = "handleBlockUpdate")
	private void ignoreEarlyBappedBlockRemovalFromServer(BlockPos pos, BlockState state, int flags, Operation<Void> original) {
		// isLiquid is deprecated but what else do i use to check if a block is being removed???????
		if(state.isAir() || state.isLiquid()) {
			WorldBapsInfo baps = BlockBappingUtil.getBapsInfoNullable((World) (Object) this);
			CharaFormAct.LOGGER.info("Removing block @ {}. Baps: {}", pos, baps);
			if(
					baps != null
					&& baps.ALL_BAPS.get(pos) instanceof BapBreakingBlockInfo breakingInfo
					&& breakingInfo.isAlmostDone()
			)
				return; // The breaking bap is already going to break this block on this very tick, so just let it.
		}

		original.call(pos, state, flags);
	}
}

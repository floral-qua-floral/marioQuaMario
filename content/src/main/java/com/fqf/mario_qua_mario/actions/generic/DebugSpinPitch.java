package com.fqf.mario_qua_mario.actions.generic;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.EntireBodyAnimation;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.ProgressHandler;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.util.MarioContentSFX;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DebugSpinPitch extends Debug {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("debug_spin_pitch");
	}

	@Override
	public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null, new ProgressHandler(7F, true),
				new EntireBodyAnimation(0.5F, (data, arrangement, progress) -> arrangement.setAngles(MathHelper.sin(progress) * 75, 0, 0)),

				null, null,
				null, null,
				null, null,
				null
		);
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions() {
		return List.of();
	}
}

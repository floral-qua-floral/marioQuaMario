package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.aquatic.AquaticPoundDrop;
import com.fqf.mario_qua_mario.actions.aquatic.Submerged;
import com.fqf.mario_qua_mario.actions.grounded.GroundPoundLand;
import com.fqf.mario_qua_mario.collision_attacks.GroundPound;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.google.common.collect.ImmutableList;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class GroundPoundDrop implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("ground_pound_drop");

	public static final AnimationDefinition ANIMATION = GroundPoundFlip.makeAnimation(key -> 1);

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return ANIMATION;
	}

	@Override public @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	@Override public @Nullable BappingRule defineBappingRule() {
		return BappingRule.GROUND_POUND;
	}
	@Override public @Nullable Identifier defineActiveCollisionAttack() {
		return GroundPound.ID;
	}

	public static final CfaStat GROUND_POUND_VEL = new CfaStat(-1.5, TERMINAL_VELOCITY, COLLISION_ATTACK);
	public static final CfaStat GROUND_POUND_STRAINING_VEL = new CfaStat(0.1, DRIFTING);

	@Override public void clientTick(CfaClientData data, boolean isSelf) {
		data.sustainSound(MarioSFX.GROUND_POUND_DROP, data.getPlayer(), SoundCategory.PLAYERS);
	}

	@Override public void travelHook(CfaTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, Fall.FALL_ACCEL, null, Fall.FALL_SPEED);
		double strainVel = GROUND_POUND_STRAINING_VEL.get(data);
		data.setForwardStrafeVel(strainVel * data.getInputs().getForwardInput(), strainVel * data.getInputs().getStrafeInput());
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {
		builder.add(new ActionTransitionDetails(
				SpecialFall.ID,
				data -> data.getYVel() > 0 || data.getInputs().JUMP.isPressed(),
				EvaluatorEnvironment.CLIENT_ONLY,
				data -> data.getInputs().DUCK.isPressed(), // Unbuffer duck to make Ground Pound stalling less trivial
				(data, isSelf, seed) -> data.stopStoredSound(MarioSFX.GROUND_POUND_DROP)
		));
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {
		builder.add(
				Fall.LANDING.variate(
						GroundPoundLand.ID,
						null, EvaluatorEnvironment.COMMON,
						data -> data.setForwardStrafeVel(0, 0),
						(data, isSelf, seed) -> {
							data.stopStoredSound(MarioSFX.GROUND_POUND_DROP);
							data.playSound(MarioSFX.GROUND_POUND_LAND, seed);
						}
				),
				Submerged.SUBMERGE.variate(
						AquaticPoundDrop.ID,
						null, null,
						data -> data.setYVel(data.getYVel() * 0.6),
						(data, isSelf, seed) -> {
							data.stopStoredSound(MarioSFX.GROUND_POUND_DROP);
							data.storeSound(data.playSound(MarioSFX.AQUATIC_GROUND_POUND_DROP, seed));
						}
				)
		);
	}
}
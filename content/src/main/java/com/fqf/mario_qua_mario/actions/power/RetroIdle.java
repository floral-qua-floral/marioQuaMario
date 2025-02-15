package com.fqf.mario_qua_mario.actions.power;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.actions.grounded.SubWalk;
import com.fqf.mario_qua_mario.actions.grounded.WalkRun;
import com.fqf.mario_qua_mario.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.BodyPartAnimation;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.LimbAnimation;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario.util.Powers;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RetroIdle extends SubWalk implements GroundedActionDefinition {
	@Override
	public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("smb3_idle");
	}

	private static LimbAnimation makeArmAnimation(AnimationHelper helper, int factor) {
		return new LimbAnimation(true, (data, arrangement, progress) -> {
			arrangement.pitch -= 52;
			float offset = Math.min(1, Math.min(Math.abs(90 - arrangement.pitch), Math.abs(270 - arrangement.pitch)) / 90);
			arrangement.addPos(
					0,
					offset * 1,
					offset * 2.6F
			);
		});
	}
	private static LimbAnimation makeLegAnimation(AnimationHelper helper, int factor) {
		return new LimbAnimation(true, (data, arrangement, progress) -> {

		});
	}

	@Override
	public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null,
				null,
				null,
				new BodyPartAnimation((data, arrangement, progress) -> {

				}),
				new BodyPartAnimation((data, arrangement, progress) -> {

				}),
				makeArmAnimation(helper, 1),
				makeArmAnimation(helper, -1),
				makeLegAnimation(helper, 1),
				makeLegAnimation(helper, -1),
				new LimbAnimation(true, (data, arrangement, progress) -> {

				})
		);
	}

	@Override
	public @NotNull List<TransitionDefinition> getBasicTransitions(GroundedActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						MarioQuaMarioContent.makeID("walk_run"),
						WalkRun::meetsWalkRunRequirement,
						EvaluatorEnvironment.CLIENT_ONLY
				),
				new TransitionDefinition(
						MarioQuaMarioContent.makeID("sub_walk"),
						data -> !data.hasPower(Powers.SMB3_IDLE) || !SubWalk.isIdle(data),
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}
}

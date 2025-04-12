package com.fqf.mario_qua_mario_content.actions.power;

import com.fqf.mario_qua_mario_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.LimbAnimation;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario_api.util.AbstractIdleAction;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.actions.airborne.Fall;
import com.fqf.mario_qua_mario_content.actions.grounded.SubWalk;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RetroIdle extends AbstractIdleAction implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("smb3_idle");
	@Override public @NotNull Identifier getID() {
		return ID;
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
	}@Override
	public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null, null, null,
				null, null,
				makeArmAnimation(helper, 1), makeArmAnimation(helper, -1),
				null, null,
				new LimbAnimation(true, (data, arrangement, progress) -> {

				})
		);
	}

	@Override
	public @NotNull Identifier getWakeupID() {
		return SubWalk.ID;
	}

	@Override
	public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(GroundedActionHelper helper) {
		return List.of(
				Fall.FALL
		);
	}
}

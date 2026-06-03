package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class SpecialFall extends Fall implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("special_fall");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @Nullable AnimationDefinition getAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				AnimationFlag.Execution.RANDOMLY_MIRROR,
				(posture, data, animationTime, helper) -> {
					helper.symmetricallyAnimate(posture, posture.RIGHT_ARM, arrangement -> {
						arrangement.x += -1.345F;
						arrangement.roll += 70;
					});

					posture.RIGHT_LEG.addPos(0, -4.5F, -4.25F);
					posture.RIGHT_LEG.pitch += 9.1F;

					posture.LEFT_LEG.pitch -= 9.5F;
				}
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of(
				new TransitionInjectionDefinition(
						TransitionInjectionDefinition.InjectionPlacement.BEFORE,
						Fall.ID,
						ActionCategory.GROUNDED,
						(nearbyTransition, castableHelper) -> nearbyTransition.variate(this.getID(), data ->
								data.getYVel() > 0 && nearbyTransition.evaluator().shouldTransition(data))
				)
		);
	}
}

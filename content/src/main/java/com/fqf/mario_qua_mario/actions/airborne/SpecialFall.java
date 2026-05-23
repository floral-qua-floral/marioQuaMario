package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.LimbAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.PiecemealPlayermodelAnimation;
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

	private static LimbAnimation makeArmAnimation(int factor) {
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.roll += 70 * factor;
			arrangement.x += -1.345F * factor;
//			arrangement.y += -2.333F;
		});
	}

	@Override public @Nullable PiecemealPlayermodelAnimation getOldAnimation(AnimationHelper helper) {
		return new PiecemealPlayermodelAnimation(
				(data, rightArmBusy, leftArmBusy, headRelativeYaw) -> data.getPlayer().getRandom().nextBoolean(),
				null, null, null, null,
				makeArmAnimation(1), makeArmAnimation(-1),
				new LimbAnimation(false, (data, arrangement, progress) -> {
					arrangement.pitch += 9.1F;
					arrangement.z -= 4.25F;
					arrangement.y -= 4.5F;
				}),
				new LimbAnimation(false, (data, arrangement, progress) ->
						arrangement.pitch -= 9.5F),
				null
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

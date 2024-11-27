package com.floralquafloral.registries.states.action.baseactions.aquatic;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.definitions.actions.AirborneActionDefinition;
import com.floralquafloral.definitions.actions.AquaticActionDefinition;
import com.floralquafloral.definitions.actions.CharaStat;
import com.floralquafloral.definitions.actions.StatCategory;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioTravelData;
import com.floralquafloral.registries.states.action.baseactions.airborne.GroundPound;
import com.floralquafloral.util.MarioSFX;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AquaticGroundPound extends AquaticActionDefinition {
	public static CharaStat AQUATIC_GROUND_POUND_DRAG = new CharaStat(0.19, StatCategory.WATER_DRAG);
	public static CharaStat AQUATIC_GROUND_POUND_DRAG_MIN = new CharaStat(0.02, StatCategory.WATER_DRAG);

	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "aquatic_ground_pound");
	}
	@Override public @Nullable String getAnimationName() {
		return "ground-pound";
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations() {
		return null;
	}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.PROHIBIT;
	}
	@Override public SlidingStatus getActionSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}
	@Override public @Nullable Identifier getStompType() {
		return Identifier.of(MarioQuaMario.MOD_ID, "aquatic_ground_pound");
	}
	@Override public BumpingRule getBumpingRule() {
		return BumpingRule.GROUND_POUND;
	}

	@Override public double getGravity() {
		return 0;
	}
	@Override public double getTerminalVelocity() {
		return 0;
	}
	@Override public double getDrag() {
		return 0;
	}
	@Override public double getDragMinimum() {
		return 0;
	}

	@Override public void aquaticTravel(MarioTravelData data) {
		applyAquaticDrag(data, AQUATIC_GROUND_POUND_DRAG, AQUATIC_GROUND_POUND_DRAG_MIN);
		data.getTimers().actionTimer++;
	}

	@Override public void clientTick(MarioClientSideData data, boolean isSelf) {

	}
	@Override public void serverTick(MarioAuthoritativeData data) {

	}

	@Override public List<ActionTransitionDefinition> getPreTravelTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:submerged",
						data -> data.getYVel() >= -0.01
				)
		);
	}

	@Override public List<ActionTransitionDefinition> getInputTransitions() {
		return List.of(
				Swim.SWIM
		);
	}

	@Override public List<ActionTransitionDefinition> getWorldCollisionTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:ground_pound",
						AquaticTransitions.EXIT_WATER.EVALUATOR,
						data -> data.setYVel(Math.min(data.getYVel(), GroundPound.GROUND_POUND_VEL.get(data)))
				),
				new ActionTransitionDefinition("qua_mario:ground_pound_landing",
						AirborneActionDefinition.AerialTransitions.BASIC_LANDING.EVALUATOR,
						data -> data.setForwardStrafeVel(0, 0),
						(data, isSelf, seed) -> {
							data.stopStoredSound(MarioSFX.DIVE);
							data.playSoundEvent(MarioSFX.AQUATIC_GROUND_POUND, seed);
						}
				)
		);
	}

	@Override public List<ActionTransitionInjection> getTransitionInjections() {
		return List.of();
	}

	@Override public List<AttackInterceptionDefinition> getUnarmedAttackInterceptions() {
		return List.of();
	}
}

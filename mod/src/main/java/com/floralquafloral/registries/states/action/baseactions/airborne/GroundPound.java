package com.floralquafloral.registries.states.action.baseactions.airborne;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioTravelData;
import com.floralquafloral.definitions.actions.AirborneActionDefinition;
import com.floralquafloral.definitions.actions.CharaStat;
import com.floralquafloral.definitions.actions.StatCategory;
import com.floralquafloral.util.MarioSFX;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GroundPound extends AirborneActionDefinition {
	public static final CharaStat GROUND_POUND_VEL = new CharaStat(-1.5, StatCategory.TERMINAL_VELOCITY);
	public static final CharaStat GROUND_POUND_STRAINING_VEL = new CharaStat(0.0825, StatCategory.DRIFTING);

	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "ground_pound");
	}
	@Override public @Nullable String getAnimationName() {
		return "ground-pound";
	}
	@Override @Nullable public CameraAnimationSet getCameraAnimations() {
		return null;
	}
	@Override @Nullable public BumpingRule getBumpingRule() {
		return BumpingRule.GROUND_POUND;
	}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.PROHIBIT;
	}
	@Override public SlidingStatus getActionSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}
	@Override public @Nullable Identifier getStompType() {
		return Identifier.of("qua_mario", "ground_pound");
	}

	@Override protected @NotNull CharaStat getGravity() {
		return AerialStats.GRAVITY;
	}
	@Override protected @Nullable CharaStat getJumpGravity() {
		return null;
	}
	@Override protected @NotNull CharaStat getTerminalVelocity() {
		return AerialStats.TERMINAL_VELOCITY;
	}

	@Override public void airborneTravel(MarioTravelData data) {
		double strainVel = GROUND_POUND_STRAINING_VEL.get(data);
		data.setForwardStrafeVel(strainVel * data.getInputs().getForwardInput(), strainVel * data.getInputs().getStrafeInput());
	}

	@Override public void clientTick(MarioClientSideData data, boolean isSelf) {

	}

	@Override public void serverTick(MarioAuthoritativeData data) {

	}

	@Override public List<ActionTransitionDefinition> getPreTravelTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:fall",
						data -> data.getYVel() > 0
				)
		);
	}

	@Override public List<ActionTransitionDefinition> getInputTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:fall",
						data -> data.getInputs().JUMP.isPressed()
				)
		);
	}

	@Override public List<ActionTransitionDefinition> getWorldCollisionTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:aquatic_ground_pound",
						AerialTransitions.ENTER_WATER.EVALUATOR,
						data -> data.setYVel(data.getYVel() * 0.4),
						null
				),
				new ActionTransitionDefinition("qua_mario:ground_pound_landing",
						AirborneActionDefinition.AerialTransitions.BASIC_LANDING.EVALUATOR,
						data -> data.setForwardStrafeVel(0, 0),
						(data, isSelf, seed) -> data.playSoundEvent(MarioSFX.GROUND_POUND, seed)
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

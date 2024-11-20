package com.floralquafloral.registries.states.action.baseactions.grounded;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import com.floralquafloral.registries.states.action.GroundedActionDefinition;
import com.floralquafloral.registries.states.action.baseactions.airborne.LongJump;
import com.floralquafloral.stats.CharaStat;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

import java.util.List;

import static com.floralquafloral.stats.StatCategory.*;

public class DuckSlide extends GroundedActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "duck_slide");
	}
	@Override public @Nullable String getAnimationName() {
		return "duck_waddle";
	}
	@Override @Nullable public CameraAnimationSet getCameraAnimations() {
		return null;
	}
	@Override @Nullable public BumpingRule getBumpingRule() {
		return null;
	}

	public static final CharaStat SLIDE_THRESHOLD = new CharaStat(0.25, DUCKING, THRESHOLD);
	public static final CharaStat SLIDE_BOOST = new CharaStat(-0.15);

	public static final CharaStat SLIDE_DRAG = new CharaStat(0.04333, DUCKING, DRAG);
	public static final CharaStat SLIDE_DRAG_MIN = new CharaStat(0.01, DUCKING, DRAG);
	public static final CharaStat SLIDE_REDIRECTION = new CharaStat(4.0, DUCKING, REDIRECTION);

	@Override
	public void groundedTravel(MarioTravelData data) {
		data.getTimers().actionTimer++;
		applyDrag(data,
				SLIDE_DRAG,
				SLIDE_DRAG_MIN,
				data.getInputs().getForwardInput(),
				data.getInputs().getStrafeInput(),
				SLIDE_REDIRECTION
		);
	}

	@Override public void clientTick(MarioClientSideData data, boolean isSelf) {}

	@Override public void serverTick(MarioAuthoritativeData data) {}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.SLIP;
	}
	@Override public SlidingStatus getActionSlidingStatus() {
		return SlidingStatus.SLIDING;
	}
	@Override public @Nullable Identifier getStompType() {
		return null;
	}

	@Override
	public List<ActionTransitionDefinition> getPreTickTransitions() {
		return List.of(
				DuckWaddle.UNDUCK,
				new ActionTransitionDefinition("qua_mario:duck_waddle",
						(data) -> MathHelper.approximatelyEquals(Vector2d.lengthSquared(data.getForwardVel(), data.getStrafeVel()), 0.0)
				)
		);
	}

	@Override public List<ActionTransitionDefinition> getPostTickTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:long_jump",
						data ->
								data.getInputs().getForwardInput() > 0.4 &&
								data.getTimers().actionTimer < 5 &&
								data.getForwardVel() > LongJump.LONG_JUMP_THRESHOLD.get(data)
								&& data.getInputs().JUMP.isPressed(),
						data -> {
							GroundedTransitions.performJump(data, LongJump.LONG_JUMP_VEL, null);
							data.setForwardVel(data.getForwardVel() * 1.4);
//							VoiceLine.LONG_JUMP.play(data, seed);
						},
						(data, isSelf, seed) -> {
							data.playJumpSound(seed);
							data.voice(MarioClientSideData.VoiceLine.LONG_JUMP, seed);
						}
				),
				DuckWaddle.BACKFLIP,
				DuckWaddle.DUCK_JUMP
		);
	}

	@Override
	public List<ActionTransitionDefinition> getPostMoveTransitions() {
		return List.of(
				DuckWaddle.DUCK_FALL
		);
	}

	@Override
	public List<ActionTransitionInjection> getTransitionInjections() {

		return List.of(
				new ActionTransitionInjection(
						ActionTransitionInjection.InjectionPlacement.BEFORE,
						"qua_mario:duck_waddle",
						ActionTransitionInjection.ActionCategory.GROUNDED,
						new ActionTransitionDefinition(
								"qua_mario:duck_slide",
								(data) -> {
									double threshold = SLIDE_THRESHOLD.get(data);
									return
											data.getInputs().DUCK.isHeld()
													&& Vector2d.lengthSquared(data.getForwardVel(), data.getStrafeVel()) > threshold * threshold
													&& !data.getAction().ID.equals(getID());
								},
								GroundedTransitions.DUCK_WADDLE.EXECUTOR_TRAVELLERS,
								GroundedTransitions.DUCK_WADDLE.EXECUTOR_CLIENTS
						)
				),
				new ActionTransitionInjection(
						ActionTransitionInjection.InjectionPlacement.BEFORE,
						"qua_mario:duck_waddle",
						ActionTransitionInjection.ActionCategory.AIRBORNE,
						new ActionTransitionDefinition(
								"qua_mario:duck_slide",
								(data) -> {
									double threshold = SLIDE_THRESHOLD.get(data);
									return
											data.getInputs().DUCK.isHeld()
													&& data.getMario().isOnGround()
													&& (
															data.getMario().isInSneakingPose()
															|| (!(MathHelper.approximatelyEquals(data.getInputs().getForwardInput(), 0)
															&& MathHelper.approximatelyEquals(data.getInputs().getStrafeInput(), 0)))
													)
													&& Vector2d.lengthSquared(data.getForwardVel(), data.getStrafeVel()) > threshold * threshold
													&& !data.getAction().ID.equals(getID());
								},
								null,
								null
						)
				)
		);
	}
}

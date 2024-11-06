package com.floralquafloral.registries.states.action.baseactions.grounded;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.VoiceLine;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.Input;
import com.floralquafloral.mariodata.client.MarioClientData;
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

	public static final CharaStat SLIDE_THRESHOLD = new CharaStat(0.25, DUCKING, THRESHOLD);
	public static final CharaStat SLIDE_BOOST = new CharaStat(-0.15);

	public static final CharaStat SLIDE_DRAG = new CharaStat(0.04333, DUCKING, DRAG);
	public static final CharaStat SLIDE_DRAG_MIN = new CharaStat(0.01, DUCKING, DRAG);
	public static final CharaStat SLIDE_REDIRECTION = new CharaStat(4.0, DUCKING, REDIRECTION);

	@Override
	public void groundedTravel(MarioClientData data) {
		data.actionTimer++;
		applyDrag(data,
				SLIDE_DRAG,
				SLIDE_DRAG_MIN,
				Input.getForwardInput(),
				Input.getStrafeInput(),
				SLIDE_REDIRECTION
		);
	}

	@Override public void clientTick(MarioPlayerData data, boolean isSelf) {}

	@Override public void serverTick(MarioPlayerData data) {}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.SLIP;
	}
	@Override public SlidingStatus getConstantSlidingStatus() {
		return SlidingStatus.SLIDING;
	}
	@Override public @Nullable Identifier getStompType() {
		return null;
	}

	@Override
	public List<ActionTransitionDefinition> getPreTickTransitions() {
		return List.of(
				DuckWaddle.DUCK_FALL
		);
	}

	@Override public List<ActionTransitionDefinition> getPostTickTransitions() {
		return List.of(
				DuckWaddle.UNDUCK,
				new ActionTransitionDefinition("qua_mario:long_jump",
						data ->
								Input.getForwardInput() > 0.4 &&
								data.actionTimer < 5 &&
								data.getForwardVel() > LongJump.LONG_JUMP_THRESHOLD.get(data)
								&& Input.JUMP.isPressed(),
						(data, isSelf, seed) -> {
							GroundedTransitions.performJump(data, LongJump.LONG_JUMP_VEL, null, seed, true);
							data.setForwardVel(data.getForwardVel() * 1.4);
							VoiceLine.LONG_JUMP.play(data, seed);
						},
						(data, seed) -> GroundedTransitions.performJump(data, LongJump.LONG_JUMP_VEL, null, seed, false)
				),
				DuckWaddle.DUCK_JUMP,
				new ActionTransitionDefinition("qua_mario:duck_waddle",
						(data) -> MathHelper.approximatelyEquals(Vector2d.lengthSquared(data.getForwardVel(), data.getStrafeVel()), 0.0)
				)
		);
	}

	@Override
	public List<ActionTransitionDefinition> getPostMoveTransitions() {
		return List.of();
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
											Input.DUCK.isHeld()
													&& Vector2d.lengthSquared(data.getForwardVel(), data.getStrafeVel()) > threshold * threshold
													&& !data.getAction().ID.equals(getID());
								},
								GroundedTransitions.DUCK_WADDLE.EXECUTOR_CLIENT,
								GroundedTransitions.DUCK_WADDLE.EXECUTOR_SERVER
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
											Input.DUCK.isHeld()
													&& data.getMario().isOnGround()
													&& !(MathHelper.approximatelyEquals(Input.getForwardInput(), 0)
													&& MathHelper.approximatelyEquals(Input.getStrafeInput(), 0))
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

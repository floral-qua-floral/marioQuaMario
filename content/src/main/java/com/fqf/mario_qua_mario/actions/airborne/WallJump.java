package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class WallJump extends Jump implements AirborneActionDefinition {
    public static final Identifier ID = MarioQuaMario.makeID("wall_jump");
	@Override public @NotNull Identifier defineID() {
		return ID;
	}

	@Override public @Nullable AnimationDefinition getAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(arrangement, data, animationTime, helper) -> {
					float progress = BonkAir.getDeviation(data);
					float poseProgress = Math.abs(progress);
					float inversion = Math.signum(progress);

					arrangement.addPos(
							helper.interpolateKeyframes(poseProgress,
									0,
									inversion * -5,
									0
							),
							0,
							helper.interpolateKeyframes(poseProgress,
									5,
									0,
									-2
							)
					);
				},
				(posture, data, animationTime, helper) -> {
					final float progress = BonkAir.getDeviation(data);
					final float poseProgress = Math.abs(progress);
					final float inversion = Math.signum(progress);

					posture.TORSO.addAngles(
							helper.interpolateKeyframes(poseProgress,
									-16,
									0,
									9
							),
							0,
							helper.interpolateKeyframes(poseProgress,
									0,
									inversion * 16,
									0
							)
					);

					helper.asymmetricallyAnimate(posture.RIGHT_ARM, posture.LEFT_ARM, (arrangement, isLeft, sideFactor) -> {
						boolean isTrailingLimb = inversion == sideFactor;
						arrangement.addPos(
								helper.interpolateKeyframes(poseProgress,
										0,
										inversion * (isTrailingLimb ? -1 : -0.8F),
										0
								),
								helper.interpolateKeyframes(poseProgress,
										0,
										isTrailingLimb ? -0.5F : 1.5F,
										0
								),
								0
						);
						arrangement.addAngles(
								helper.interpolateKeyframes(poseProgress,
										-56.675F,
										isTrailingLimb ? -20 : -39,
										44
								),
								helper.interpolateKeyframes(poseProgress,
										sideFactor * 5,
										0,
										0
								),
								helper.interpolateKeyframes(poseProgress,
										sideFactor * 12,
										inversion * (isTrailingLimb ? 46 : 9.2F),
										0
								)
						);
					});

					helper.asymmetricallyAnimate(posture.RIGHT_LEG, posture.LEFT_LEG, (arrangement, isLeft, sideFactor) -> {
						boolean isTrailingLimb = inversion == sideFactor;
						arrangement.addPos(
								helper.interpolateKeyframes(poseProgress,
										0,
										inversion * (isTrailingLimb ? -3 : -2),
										0
								),
								helper.interpolateKeyframes(poseProgress,
										isLeft ? -1 : -4.2F,
										isTrailingLimb ? -2 : -1,
										-0.75F
								),
								helper.interpolateKeyframes(poseProgress,
										isLeft ? -3 : -6,
										isTrailingLimb ? 0 : -4,
										1.5F
								)
						);
						arrangement.addAngles(
								helper.interpolateKeyframes(poseProgress,
										isLeft ? -67.75F : -10,
										isTrailingLimb ? 0 : 30,
										38.865F
								),
								helper.interpolateKeyframes(poseProgress,
										0,
										inversion * (isTrailingLimb ? 0 : -17.5F),
										sideFactor * 1.25F
								),
								helper.interpolateKeyframes(poseProgress,
										0,
										inversion * (isTrailingLimb ? 40 : 20),
										0
								)
						);
					});
				}
		);
	}

	public static final CfaStat WALL_JUMP_VEL = new CfaStat(0.78, JUMP_VELOCITY, CLIMBING);
	public static final CfaStat WALL_JUMP_SPEED = new CfaStat(0.4, DRIFTING);

	@Override
	public void clientTick(CfaClientData data, boolean isSelf) {
		data.forceBodyAlignment(true);
	}

	@Override
	public void travelHook(CfaTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, Fall.FALL_ACCEL, JUMP_GRAVITY, Fall.FALL_SPEED);
		BonkAir.BonkVars vars = data.retrieveStateData(BonkAir.BonkVars.class);
		if(vars.noInputTicks-- <= 0) Fall.drift(data, helper);
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new BonkAir.BonkVars(data);
	}
}
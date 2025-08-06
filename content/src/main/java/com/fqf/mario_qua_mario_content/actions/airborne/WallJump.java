package com.fqf.mario_qua_mario_content.actions.airborne;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario_api.util.StatCategory.*;

public class WallJump extends Jump implements AirborneActionDefinition {
    public static final Identifier ID = MarioQuaMarioContent.makeID("wall_jump");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	private static LimbAnimation makeArmAnimation(AnimationHelper helper, int factor) {
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			float poseProgress = Math.abs(progress);
			float inversion = Math.signum(progress);
			boolean isTrailingLimb = inversion == factor;

			arrangement.addAngles(
					helper.interpolateKeyframes(poseProgress,
							-56.675F,
							isTrailingLimb ? -20 : -39,
							44
					),
					helper.interpolateKeyframes(poseProgress,
							factor * 5,
							0,
							0
					),
					helper.interpolateKeyframes(poseProgress,
							factor * 12,
							inversion * (isTrailingLimb ? 46 : 9.2F),
							0
					)
			);

			arrangement.x += helper.interpolateKeyframes(poseProgress,
					0,
					inversion * (isTrailingLimb ? -1 : -0.8F),
					0
			);
			arrangement.y += helper.interpolateKeyframes(poseProgress,
					0,
					isTrailingLimb ? -0.5F : 1.5F,
					0
			);
		});
	}
	private static LimbAnimation makeLegAnimation(AnimationHelper helper, int factor) {
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			float poseProgress = Math.abs(progress);
			float inversion = Math.signum(progress);
			boolean isTrailingLimb = inversion == factor;
			boolean isRight = factor == 1;

			arrangement.addAngles(
					helper.interpolateKeyframes(poseProgress,
							isRight ? -10 : -67.75F,
							isTrailingLimb ? 0 : 30,
							38.865F
					),
					helper.interpolateKeyframes(poseProgress,
							0,
							inversion * (isTrailingLimb ? 0 : -17.5F),
							factor * 1.25F
					),
					helper.interpolateKeyframes(poseProgress,
							0,
							inversion * (isTrailingLimb ? 40 : 20),
							0
					)
			);
			arrangement.addPos(
					helper.interpolateKeyframes(poseProgress,
							0,
							inversion * (isTrailingLimb ? -3 : -2),
							0
					),
					helper.interpolateKeyframes(poseProgress,
							isRight ? -4.2F : -1,
							isTrailingLimb ? -2 : -1,
							-0.75F
					),
					helper.interpolateKeyframes(poseProgress,
							isRight ? -6 : -3,
							isTrailingLimb ? 0 : -4,
							1.5F
					)
			);
		});
	}
	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null,
				new ProgressHandler((data, ticksPassed) -> {
					float deviation = MathHelper.subtractAngles(data.getMario().bodyYaw, data.getVars(BonkAir.BonkVars.class).BONK_YAW);
					return deviation / 180 * 2;
				}),
				new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) -> {
					float poseProgress = Math.abs(progress);
					float inversion = Math.signum(progress);

					arrangement.x += helper.interpolateKeyframes(poseProgress,
							0,
							inversion * -5,
							0
					);
					arrangement.z += helper.interpolateKeyframes(poseProgress,
							5,
							0,
							-2
					);
				}),
				null,
				new BodyPartAnimation((data, arrangement, progress) -> {
					float poseProgress = Math.abs(progress);
					float inversion = Math.signum(progress);

					arrangement.pitch += helper.interpolateKeyframes(poseProgress,
							-16,
							0,
							9
					);
					arrangement.roll += helper.interpolateKeyframes(poseProgress,
							0,
							inversion * 16,
							0
					);
				}),
				makeArmAnimation(helper, 1), makeArmAnimation(helper, -1),
				makeLegAnimation(helper, 1), makeLegAnimation(helper, -1),
				null
		);
	}

	public static final CharaStat WALL_JUMP_VEL = new CharaStat(0.78, JUMP_VELOCITY, CLIMBING);
	public static final CharaStat WALL_JUMP_SPEED = new CharaStat(0.4, DRIFTING);

	@Override
	public void clientTick(IMarioClientData data, boolean isSelf) {
		data.forceBodyAlignment(true);
	}

	@Override
	public void travelHook(IMarioTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, Fall.FALL_ACCEL, JUMP_GRAVITY, Fall.FALL_SPEED);
		BonkAir.BonkVars vars = data.getVars(BonkAir.BonkVars.class);
		if(vars.noInputTicks-- <= 0) Fall.drift(data, helper);
	}

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new BonkAir.BonkVars(data);
	}
}
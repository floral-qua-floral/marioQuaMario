package com.fqf.mario_qua_mario.actions.wallbound;

import com.fqf.charaformact_api.cfadata.*;
import com.fqf.charaformact_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.SpecialFall;
import com.fqf.mario_qua_mario.actions.airborne.WallJump;
import com.fqf.mario_qua_mario.actions.grounded.SubWalk;
import com.fqf.mario_qua_mario.util.*;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class WallSlide implements WallboundActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("wall_slide");
	@Override public @NotNull Identifier defineID() {
		return ID;
	}

	private static float calculateDeviation(CfaAnimatingData data, AnimationHelper helper) {
		return MathHelper.subtractAngles(data.getPlayer().bodyYaw,
				Objects.requireNonNull(helper.getWallInfo(data)).getWallYaw()) / 180 * 2;
	}

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(arrangement, data, animationTime, helper) -> {
					float deviation = calculateDeviation(data, helper);
					float pose = Math.abs(deviation);
					float inversion = Math.signum(deviation);

					arrangement.addPos(
							helper.interpolateKeyframes(pose,
									0,
									inversion * -1.85F,
									0
							),
							0,
							helper.interpolateKeyframes(pose,
									-0.5F,
									0,
									1
							)
					);
					arrangement.yaw += helper.interpolateKeyframes(pose,
							inversion * 25,
							0,
							inversion * -36
					);
				},
				(posture, data, animationTime, helper) -> {
					float deviation = calculateDeviation(data, helper);
					float pose = Math.abs(deviation);
					float inversion = Math.signum(deviation);

					/*TORSO*/ {
						posture.TORSO.z += helper.interpolateKeyframes(pose,
								1,
								0,
								0
						);
						posture.TORSO.addAngles(
								helper.interpolateKeyframes(pose,
										4,
										10,
										0
								),
								helper.interpolateKeyframes(pose,
										inversion * -2,
										inversion * -10,
										0
								),
								helper.interpolateKeyframes(pose,
										0,
										inversion * 8,
										0
								)
						);
					}

					helper.asymmetricallyAnimate(posture.RIGHT_ARM, posture.LEFT_ARM, (arrangement, isLeft, sideFactor) -> {
						boolean isTrailingLimb = inversion == sideFactor;
						arrangement.addPos(
								helper.interpolateKeyframes(pose,
										sideFactor * (isTrailingLimb ? -1 : 0),
										inversion * (isTrailingLimb ? -1 : -0.8F),
										0
								),
								helper.interpolateKeyframes(pose,
										0,
										isTrailingLimb ? -0.5F : 1.5F,
										isTrailingLimb ? 0 : -0.5F
								),
								helper.interpolateKeyframes(pose,
										isTrailingLimb ? -1.15F : 0,
										0,
										isTrailingLimb ? 0 : 1
								)
						);
						arrangement.addAngles(
								helper.interpolateKeyframes(pose,
										0,
										0,
										isTrailingLimb ? 0 : 50
								),
								sideFactor * helper.interpolateKeyframes(pose,
										isTrailingLimb ? 65 : 0,
										0,
										isTrailingLimb ? -54 : -38
								),
								sideFactor * helper.interpolateKeyframes(pose,
										isTrailingLimb ? 175 : 0,
										isTrailingLimb ? 175 : 0,
										isTrailingLimb ? 175 : -50
								)
						);
					});

					helper.asymmetricallyAnimate(posture.RIGHT_LEG, posture.LEFT_LEG, (arrangement, isLeft, sideFactor) -> {
						boolean isTrailingLimb = inversion == sideFactor;
						arrangement.addPos(
								helper.interpolateKeyframes(pose,
										sideFactor * (isTrailingLimb ? 1.6F : 0),
										inversion * (isTrailingLimb ? -1.4F : -2),
										sideFactor * (isTrailingLimb ? 0.4F : 0)
								),
								helper.interpolateKeyframes(pose,
										isTrailingLimb ? -4.6F : 0,
										isTrailingLimb ? -4.6F : -0.5F,
										isTrailingLimb ? -4.6F : -2F
								),
								helper.interpolateKeyframes(pose,
										isTrailingLimb ? -3.2F : 0,
										isTrailingLimb ? -3.5F : 1.5F,
										isTrailingLimb ? -3.5F : 0
								)
						);
						arrangement.addAngles(
								helper.interpolateKeyframes(pose,
										isTrailingLimb ? 27.5F : -25,
										isTrailingLimb ? 10 : 20,
										isTrailingLimb ? 25 : 12
								),
								helper.interpolateKeyframes(pose,
										sideFactor * (isTrailingLimb ? -60 : 50),
										inversion * (isTrailingLimb ? -3 : -60),
										sideFactor * (isTrailingLimb ? -23 : 30)
								),
								helper.interpolateKeyframes(pose,
										sideFactor * (isTrailingLimb ? 0 : -30),
										inversion * (isTrailingLimb ? 11 : 20),
										0
								)
						);
					});
				}
		);
	}
	@Override public @Nullable CameraAnimationSet defineCameraAnimations(AnimationHelper helper) {
		return null;
	}
	@Override public @NotNull SlidingStatus defineSlidingStatus() {
		return SlidingStatus.WALL_SLIDING;
	}

	@Override public @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	@Override public @Nullable BappingRule defineBappingRule() {
		return null;
	}
	@Override public @Nullable Identifier defineActiveCollisionAttack() {
		return null;
	}

	@Override public @NotNull WallBodyAlignment defineBodyAlignment() {
		return WallBodyAlignment.ANY;
	}

	@Override public float defineHeadYawRange() {
		return 360;
	}

	public static boolean canSlideDownBlock(BlockState state) {
		return state.getBlock().getSlipperiness() <= 0.6F && !state.isIn(MQMTags.UNSLIDEABLE_WALLS);
	}

	@Override
	public float calculateWallYaw(CfaReadableMotionData data) {
		return ClimbTransitions.yawOf(data.getRecordedCollisions().getDirectionOfCollisionsWith((collision, block) ->
				canSlideDownBlock(collision.state()), false));
	}

	@Override
	public boolean verifyLegality(CfaReadableMotionData data, WallInfo wall, Vec3d checkOffset) {
		if(!data.getActionID().equals(WallSlide.ID) && data.isClient() && wall.getTowardsWallInput() < 0.3)
			return false; // Yay!
		World world = data.getPlayer().getWorld();
		for(BlockPos wallBlock : wall.getWallBlocks(0.4)) {
			if(canSlideDownBlock(world.getBlockState(wallBlock))) return true;
		}
		return false;
	}

	private static class WallSlideVars extends ActionTimerVars {
		private int holdAwayFromWallTicks;
	}
	@Override public @Nullable Object provideStateData(CfaData data) {
		return new WallSlideVars();
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {
		data.forceBodyAlignment(true);
	}
	@Override public void serverTick(CfaAuthoritativeData data) {

	}
	private static final int GRAVITY_RAMP_UP_TICKS = 5;
	@Override public void travel(CfaTravelData data, WallInfo wall, WallboundActionHelper helper) {
		WallSlideVars vars = data.retrieveStateData(WallSlideVars.class);
		if(data.isClient()) {
			if(wall.getTowardsWallInput() < -0.05)
				vars.holdAwayFromWallTicks += 3;
			else if(wall.getTowardsWallInput() < 0.05)
				vars.holdAwayFromWallTicks++;
			else
				vars.holdAwayFromWallTicks = 0;
		}

		double gravityFactor;
		if(data.getPlayer().isTouchingWaterOrRain()) gravityFactor = 1.5;
		else if(++vars.actionTimer >= GRAVITY_RAMP_UP_TICKS) gravityFactor = 1;
		else gravityFactor = vars.actionTimer * (1.0 / GRAVITY_RAMP_UP_TICKS);

		data.setYVel(Math.max(data.getYVel() - (0.02 * gravityFactor), -0.265));
		if(Math.abs(wall.getSidleVel()) > 0.065)
			helper.setSidleVel(data, wall.getSidleVel() * 0.8);
		else
			helper.setSidleVel(data, wall.getSidleInput() * 0.065);
		helper.setTowardsWallVel(data, 0.2);
	}

	public static final TransitionDefinition WALL_SLIDE = new TransitionDefinition(
			WallSlide.ID,
			MarioVars::checkWallSlide,
			EvaluatorEnvironment.CLIENT_ONLY,
			data -> {
				if(!data.getPlayer().isTouchingWaterOrRain()) {
					data.getPlayer().fallDistance = 0;
					data.setYVel(0);
				}
				if(data.isServer()) data.setForwardStrafeVel(0, 0);
			},
			(data, isSelf, seed) -> {
				if(!data.getPlayer().isTouchingWaterOrRain())
					data.getPlayer().fallDistance = 0;
			}
	);

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<TransitionDefinition> builder, WallboundActionHelper helper) {
		builder.add(
				new TransitionDefinition(
						WallJump.ID,
						data -> data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> {
							data.setYVel(WallJump.WALL_JUMP_VEL.get(data));
							helper.setTowardsWallVel(data, -WallJump.WALL_JUMP_SPEED.get(data));
						},
						(data, isSelf, seed) -> {
							data.playSound(MarioSFX.WALL_JUMP, seed);
							data.playJumpSound(seed);
							data.voice(Voicelines.WALL_JUMP, seed);
						}
				),
				new TransitionDefinition(
						Fall.ID,
						data -> data.getInputs().DUCK.isPressed() || data.retrieveStateData(WallSlideVars.class).holdAwayFromWallTicks > 6,
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> helper.setTowardsWallVel(data, 0),
						null
				)
		);
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<TransitionDefinition> builder, WallboundActionHelper helper) {
		builder.add(
				ClimbTransitions.CLIMB_SOLID.variate(
						null,
						data -> helper.getWallInfo(data).getTowardsWallInput() > 0.3 && ClimbTransitions.CLIMB_SOLID.evaluator().shouldTransition(data)
				),
				new TransitionDefinition(
						SpecialFall.ID,
						data -> !helper.getWallInfo(data).isLegal(),
						EvaluatorEnvironment.COMMON
				),
				new TransitionDefinition(
						SubWalk.ID,
						data -> data.getPlayer().isOnGround(),
						EvaluatorEnvironment.COMMON
				)
		);
	}
}

package com.fqf.mario_qua_mario_content.actions.wallbound;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.mariodata.*;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_content.Voicelines;
import com.fqf.mario_qua_mario_content.actions.airborne.BonkAir;
import com.fqf.mario_qua_mario_content.actions.airborne.Fall;
import com.fqf.mario_qua_mario_content.actions.airborne.SpecialFall;
import com.fqf.mario_qua_mario_content.actions.airborne.WallJump;
import com.fqf.mario_qua_mario_content.actions.grounded.SubWalk;
import com.fqf.mario_qua_mario_content.util.ClimbTransitions;
import com.fqf.mario_qua_mario_content.util.MQMContentTags;
import com.fqf.mario_qua_mario_content.util.MarioContentSFX;
import com.fqf.mario_qua_mario_content.util.MarioVars;
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
	public static final Identifier ID = MarioQuaMarioContent.makeID("wall_slide");
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
							0,
							0,
							isTrailingLimb ? 0 : 50
					),
					factor * helper.interpolateKeyframes(poseProgress,
							isTrailingLimb ? 65 : 0,
							0,
							isTrailingLimb ? -54 : -38
					),
					factor * helper.interpolateKeyframes(poseProgress,
							isTrailingLimb ? 175 : 0,
							isTrailingLimb ? 175 : 0,
							isTrailingLimb ? 175 : -50
					)
			);

			arrangement.addPos(
					helper.interpolateKeyframes(poseProgress,
							factor * (isTrailingLimb ? -1 : 0),
							inversion * (isTrailingLimb ? -1 : -0.8F),
							0
					),
					helper.interpolateKeyframes(poseProgress,
							0,
							isTrailingLimb ? -0.5F : 1.5F,
							isTrailingLimb ? 0 : -0.5F
					),
					helper.interpolateKeyframes(poseProgress,
							isTrailingLimb ? -1.15F : 0,
							0,
							isTrailingLimb ? 0 : 1
					)
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
							isTrailingLimb ? 27.5F : -25,
							isTrailingLimb ? 10 : 20,
							isTrailingLimb ? 25 : 12
					),
					helper.interpolateKeyframes(poseProgress,
							factor * (isTrailingLimb ? -60 : 50),
							inversion * (isTrailingLimb ? -3 : -60),
							factor * (isTrailingLimb ? -23 : 30)
					),
					helper.interpolateKeyframes(poseProgress,
							factor * (isTrailingLimb ? 0 : -30),
							inversion * (isTrailingLimb ? 11 : 20),
							0
					)
			);
			arrangement.addPos(
					helper.interpolateKeyframes(poseProgress,
							factor * (isTrailingLimb ? 1.6F : 0),
							inversion * (isTrailingLimb ? -1.4F : -2),
							factor * (isTrailingLimb ? 0.4F : 0)
					),
					helper.interpolateKeyframes(poseProgress,
							isTrailingLimb ? -4.6F : 0,
							isTrailingLimb ? -4.6F : -0.5F,
							isTrailingLimb ? -4.6F : -2F
					),
					helper.interpolateKeyframes(poseProgress,
							isTrailingLimb ? -3.2F : 0,
							isTrailingLimb ? -3.5F : 1.5F,
							isTrailingLimb ? -3.5F : 0
					)
			);
		});
	}
	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null,
				new ProgressHandler((data, ticksPassed) -> {
					float deviation = MathHelper.subtractAngles(data.getMario().bodyYaw, Objects.requireNonNull(helper.getWallInfo(data)).getWallYaw());
					return deviation / 180 * 2;
				}),
				new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) -> {
					float poseProgress = Math.abs(progress);
					float inversion = Math.signum(progress);

					arrangement.yaw += helper.interpolateKeyframes(poseProgress,
							inversion * 25,
							0,
							inversion * -36
					);

					arrangement.x += helper.interpolateKeyframes(poseProgress,
							0,
							inversion * -1.85F,
							0
					);
					arrangement.z += helper.interpolateKeyframes(poseProgress,
							-0.5F,
							0,
							1
					);
				}),
				null,
				new BodyPartAnimation((data, arrangement, progress) -> {
					float poseProgress = Math.abs(progress);
					float inversion = Math.signum(progress);

					arrangement.addAngles(
							helper.interpolateKeyframes(poseProgress,
									4,
									10,
									0
							),
							helper.interpolateKeyframes(poseProgress,
									inversion * -2,
									inversion * -10,
									0
							),
							helper.interpolateKeyframes(poseProgress,
									0,
									inversion * 8,
									0
							)
					);
					arrangement.z += helper.interpolateKeyframes(poseProgress,
							1,
							0,
							0
					);
				}),
				makeArmAnimation(helper, 1), makeArmAnimation(helper, -1),
				makeLegAnimation(helper, 1), makeLegAnimation(helper, -1),
				null
		);
	}

	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.WALL_SLIDING;
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	@Override public @Nullable BappingRule getBappingRule() {
		return null;
	}
	@Override public @Nullable Identifier getStompTypeID() {
		return null;
	}

	@Override public @NotNull WallBodyAlignment getBodyAlignment() {
		return WallBodyAlignment.ANY;
	}

	@Override public float getHeadYawRange() {
		return 360;
	}

	public static boolean canSlideDownBlock(BlockState state) {
		return state.getBlock().getSlipperiness() <= 0.6F && !state.isIn(MQMContentTags.UNSLIDEABLE_WALLS);
	}

	@Override
	public float getWallYaw(IMarioReadableMotionData data) {
		return ClimbTransitions.yawOf(data.getRecordedCollisions().getDirectionOfCollisionsWith((collision, block) ->
				canSlideDownBlock(collision.state()), false));
	}

	@Override
	public boolean checkLegality(IMarioReadableMotionData data, WallInfo wall, Vec3d checkOffset) {
		if(!data.getActionID().equals(WallSlide.ID) && data.isClient() && wall.getTowardsWallInput() < 0.3)
			return false; // Yay!
		World world = data.getMario().getWorld();
		for(BlockPos wallBlock : wall.getWallBlocks(0.4)) {
			if(canSlideDownBlock(world.getBlockState(wallBlock))) return true;
		}
		return false;
	}

	private static class WallSlideVars {
		private int holdAwayFromWallTicks;
	}
	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new WallSlideVars();
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {
		data.forceBodyAlignment(true);
	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, WallInfo wall, WallboundActionHelper helper) {
		if(data.isClient()) {
			if(wall.getTowardsWallInput() < -0.05)
				data.getVars(WallSlideVars.class).holdAwayFromWallTicks += 3;
			else if(wall.getTowardsWallInput() < 0.05)
				data.getVars(WallSlideVars.class).holdAwayFromWallTicks++;
			else
				data.getVars(WallSlideVars.class).holdAwayFromWallTicks = 0;
		}

		data.setYVel(Math.max(data.getYVel() - 0.02, -0.265));
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
				data.setYVel(0);
				if(data.isServer()) data.setForwardStrafeVel(0, 0);
			},
			null
	);

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(WallboundActionHelper helper) {
		return List.of(

		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(WallboundActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						WallJump.ID,
						data -> data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> {
							data.setYVel(WallJump.WALL_JUMP_VEL.get(data));
							helper.setTowardsWallVel(data, -WallJump.WALL_JUMP_SPEED.get(data));
						},
						(data, isSelf, seed) -> {
							data.playSound(MarioContentSFX.WALL_JUMP, seed);
							data.playJumpSound(seed);
							data.voice(Voicelines.WALL_JUMP, seed);
						}
				),
				new TransitionDefinition(
						Fall.ID,
						data -> data.getInputs().DUCK.isPressed() || data.getVars(WallSlideVars.class).holdAwayFromWallTicks > 6,
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> helper.setTowardsWallVel(data, 0),
						null
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(WallboundActionHelper helper) {
		return List.of(
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
						data -> data.getMario().isOnGround(),
						EvaluatorEnvironment.COMMON
				)
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override
	public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}

package com.fqf.mario_qua_mario.actions.wallbound;

import com.fqf.charaformact_api.HelperGetter;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.cfadata.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.BodyPartAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.EntireBodyAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.LimbAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.PiecemealPlayermodelAnimation;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.charaformact_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.actions.airborne.*;
import com.fqf.mario_qua_mario.actions.generic.ClimbPole;
import com.fqf.mario_qua_mario.actions.grounded.SubWalk;
import com.fqf.mario_qua_mario.util.ClimbTransitions;
import com.fqf.mario_qua_mario.util.ClimbVars;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ClimbWall implements WallboundActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("climb_wall");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	protected float getEntireBodyZOffset(CfaReadableMotionData data) {
		return 2.25F;
	}
	@Override public @Nullable AnimationDefinition getAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(arrangement, data, animationTime, helper) -> arrangement.z += this.getEntireBodyZOffset(data),
				(posture, data, animationTime, helper) -> {
					ClimbOmniDirectionalVars vars = data.retrieveStateData(ClimbOmniDirectionalVars.class);
					WallInfo wall = helper.getWallInfo(data);
					assert wall != null;

					if(data.getYVel() != 0 || wall.getSidleVel() != 0) {
						double factorForInput = 1 / ClimbPole.CLIMB_SPEED.get(data);
						double climbInput = data.getYVel() * factorForInput;
						double sidleInput = wall.getSidleVel() * factorForInput;

						vars.progress += Math.min(1, (float) Vector2d.length(climbInput, sidleInput)) / 2;
						float denominator = (float) Math.max(Math.abs(climbInput), Math.abs(sidleInput));
						vars.xComponent = (float) sidleInput / denominator;
						vars.yComponent = (float) climbInput / denominator;
					}

					float progress = MathHelper.sin(data.retrieveStateData(ClimbOmniDirectionalVars.class).progress);

					posture.TORSO.yaw += progress * 15;

					helper.asymmetricallyAnimate(posture.RIGHT_ARM, posture.LEFT_ARM, (arrangement, isLeft, sideFactor) -> {
						arrangement.y -= progress * 1.9F * sideFactor * vars.yComponent;
						arrangement.addAngles(
								-110 + (-30 * Math.abs(vars.yComponent) * (1 - Math.abs(vars.xComponent))) + (progress * -20 * sideFactor * vars.yComponent),
								sideFactor * (24 * Math.abs(vars.xComponent) + 20 * progress * vars.xComponent),
								0
						);
					});

					helper.asymmetricallyAnimate(posture.RIGHT_LEG, posture.LEFT_LEG, (arrangement, isLeft, sideFactor) -> {
						arrangement.addPos(
								0,
								-3 + progress * -3 * sideFactor * vars.yComponent,
								-3.7F
						);
						float yawRollAdjustment = sideFactor * (3 * Math.abs(vars.xComponent) + 16 * progress * vars.xComponent);
						arrangement.addAngles(
								-10 + progress * -20 * sideFactor * vars.yComponent,
								yawRollAdjustment,
								yawRollAdjustment
						);
					});

					if(posture.TAIL != null) {
						posture.TAIL.pitch = 60;
						if(data.getVelocity().lengthSquared() > 0.1)
							posture.TAIL.roll -= progress * 30;
					}
				}
		);
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
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
	@Override public @Nullable Identifier getCollisionAttackTypeID() {
		return null;
	}

	@Override public @NotNull WallBodyAlignment getBodyAlignment() {
		return WallBodyAlignment.TOWARDS;
	}

	@Override public float getHeadYawRange() {
		return 360;
	}

	public static Direction getWallDirection(CfaReadableMotionData data) {
		return data.getRecordedCollisions().getDirectionOfCollisionsWith((collision, block) -> ClimbTransitions.canClimbBlock(collision.state(), collision.direction()), false);
	}

	@Override
	public float getWallYaw(CfaReadableMotionData data) {
		return ClimbTransitions.yawOf(getWallDirection(data));
	}

	@Override
	public boolean checkLegality(CfaReadableMotionData data, WallInfo wall, Vec3d checkOffset) {
		World world = data.getPlayer().getWorld();
		for(BlockPos wallBlock : wall.getWallBlocks(0.4)) {
			if(ClimbTransitions.canClimbBlock(world.getBlockState(wallBlock), Direction.fromRotation(wall.getWallYaw())))
				return true;
		}
		return false;
	}

	protected static class ClimbOmniDirectionalVars extends ClimbVars {
		private float xComponent = 0.75F;
		private float yComponent = 0.45F;
		public final boolean ALTERNATE_OFFSET;

		private ClimbOmniDirectionalVars(boolean alternateOffset) {
			this.ALTERNATE_OFFSET = alternateOffset;
		}
	}

	protected double getConstantTowardsWallVel() {
		return 0.2;
	}
	protected final double TOWARDS_WALL_VEL = this.getConstantTowardsWallVel();

	protected boolean useAlternateOffset(CfaData data) {
		return false;
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new ClimbOmniDirectionalVars(this.useAlternateOffset(data));
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {
		data.retrieveStateData(ClimbOmniDirectionalVars.class).clientTick(data);
	}
	@Override public void serverTick(CfaAuthoritativeData data) {

	}
	@Override public void travelHook(CfaTravelData data, WallInfo wall, WallboundActionHelper helper) {
		helper.setTowardsWallVel(data, TOWARDS_WALL_VEL);
		data.getPlayer().fallDistance = 0;
		double climbInput = wall.getTowardsWallInput() * (data.getInputs().DUCK.isHeld() ? 0.3 : 1);
		double sidleInput = wall.getSidleInput();
		if(sidleInput != 0 && !wall.wouldBeLegalWithOffset(0, Math.signum(sidleInput) * 0.8)) {
			sidleInput = 0;

			Direction.Axis axis = Direction.fromRotation(HelperGetter.getWallboundActionHelper().getWallInfo(data).getWallYaw()).rotateYClockwise().getAxis();
			double targetCoord = data.getPlayer().getBlockPos().toBottomCenterPos().getComponentAlongAxis(axis);
			if(wall.wouldBeLegalWithOffset(0, targetCoord - data.getPlayer().getPos().getComponentAlongAxis(axis)))
				data.goTo(data.getPlayer().getPos().withAxis(axis, targetCoord));
		}

		double climbSpeed = ClimbPole.CLIMB_SPEED.get(data);
		double yVel = climbInput * climbSpeed;
		double sidleVel = sidleInput * climbSpeed;
		data.setYVel(yVel);
		helper.setSidleVel(data, sidleVel);
	}

	protected Identifier getSideHangActionID() {
		return ClimbWallSideHang.ID;
	}

	public static final float MIN_DEVIATION_TO_SIDE_HANG = 99;

	protected TransitionDefinition.ClientsExecutor getSideHangTransitionClientsExecutor() {
		return null;
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(WallboundActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						this.getSideHangActionID(),
						data -> Math.abs(helper.getWallInfo(data).getYawDeviation()) > MIN_DEVIATION_TO_SIDE_HANG,
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> {
							data.setForwardStrafeVel(0, 0);
							data.setYVel(Math.min(0, data.getYVel()));
						},
						this.getSideHangTransitionClientsExecutor()
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(WallboundActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						WallJump.ID,
						data -> helper.getWallInfo(data) != null
								&& Objects.requireNonNull(helper.getWallInfo(data)).getTowardsWallInput() < -0.45
								&& data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> {
							data.setYVel(WallJump.WALL_JUMP_VEL.get(data));
							double speed = WallJump.WALL_JUMP_SPEED.get(data);
							data.setForwardStrafeVel(data.getInputs().getForwardInput() * speed,
									data.getInputs().getStrafeInput() * speed);
						},
						(data, isSelf, seed) -> {
							data.voice(Voicelines.WALL_JUMP, seed);
							data.playJumpSound(seed);
						}
				),
				new TransitionDefinition(
						Fall.ID,
						data -> data.getInputs().DUCK.isHeld() && data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> {
							helper.setTowardsWallVel(data, 0);
							data.getInputs().DUCK.isPressed(); // Unbuffer Duck
						},
						null
				),
				new TransitionDefinition(
						Jump.ID,
						data -> data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> {
							helper.setTowardsWallVel(data, 0);
							data.setYVel(Jump.JUMP_VEL.get(data));
						},
						(data, isSelf, seed) -> data.playJumpSound(seed)
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(WallboundActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						SpecialFall.ID,
						data -> !helper.getWallInfo(data).isLegal(),
						EvaluatorEnvironment.COMMON,
						data -> helper.setTowardsWallVel(data, 0),
						null
				),
				new TransitionDefinition(
						SubWalk.ID,
						data -> data.getPlayer().isOnGround(),
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}

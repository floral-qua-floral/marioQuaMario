package com.fqf.mario_qua_mario.actions.wallbound;

import com.fqf.charaformact_api.HelperGetter;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.attachments.StateAttachmentKey;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.Jump;
import com.fqf.mario_qua_mario.actions.airborne.SpecialFall;
import com.fqf.mario_qua_mario.actions.airborne.WallJump;
import com.fqf.mario_qua_mario.actions.generic.ClimbPole;
import com.fqf.mario_qua_mario.actions.grounded.SubWalk;
import com.fqf.mario_qua_mario.util.ClimbTransitions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ClimbWall implements WallboundActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("climb_wall");

	protected float getEntireBodyZOffset(CfaReadableMotionData data) {
		return 2.25F;
	}
	@Override public @Nullable AnimationDefinition defineAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(arrangement, data, animationTime, helper) -> arrangement.z += this.getEntireBodyZOffset(data),
				(posture, data, animationTime, helper) -> {
					CommonClimbProgress vars = data.get(COMMON_CLIMB_PROGRESS);
					WallInfo wall = helper.getWallInfo(data);
					assert wall != null;

					float progress = MathHelper.sin(vars.progress);

					posture.TORSO.yaw += progress * 15;

					helper.asymmetricallyAnimate(posture.RIGHT_ARM, posture.LEFT_ARM, (arrangement, isLeft, sideFactor) -> {
						arrangement.y -= progress * 1.9F * sideFactor * vars.verticalIntensity;
						arrangement.addAngles(
								-110 + (-30 * Math.abs(vars.verticalIntensity) * (1 - Math.abs(vars.horizontalIntensity))) + (progress * -20 * sideFactor * vars.verticalIntensity),
								sideFactor * (24 * Math.abs(vars.horizontalIntensity) + 20 * progress * vars.horizontalIntensity),
								0
						);
					});

					helper.asymmetricallyAnimate(posture.RIGHT_LEG, posture.LEFT_LEG, (arrangement, isLeft, sideFactor) -> {
						arrangement.addPos(
								0,
								-3 + progress * -3 * sideFactor * vars.verticalIntensity,
								-3.7F
						);
						float yawRollAdjustment = sideFactor * (3 * Math.abs(vars.horizontalIntensity) + 16 * progress * vars.horizontalIntensity);
						arrangement.addAngles(
								-10 + progress * -20 * sideFactor * vars.verticalIntensity,
								yawRollAdjustment,
								yawRollAdjustment
						);
					});

					if(posture.TAIL != null) {
						posture.TAIL.pitch = -60;
						if(data.getVelocity().lengthSquared() > 0.1)
							posture.TAIL.roll -= progress * 30;
					}
				}
		);
	}
	@Override public void defineActionFlags(ImmutableSet.Builder<ActionFlag> builder) {
		builder.addAll(ActionFlag.FULL_CLIMBING);
		builder.add(ActionFlag.PROHIBIT_FLIGHT, ActionFlag.ALLOW_STEALTH, ActionFlag.NO_STEP, ActionFlag.NO_VIEW_BOB);
	}

	@Override public @NotNull WallBodyAlignment defineBodyAlignment() {
		return WallBodyAlignment.TOWARDS;
	}

	@Override public float defineHeadYawRange() {
		return 360;
	}

	public static Direction getWallDirection(CfaReadableMotionData data) {
		return data.getRecordedCollisions().getDirectionOfCollisionsWith((collision, block) -> ClimbTransitions.canClimbBlock(collision.state(), collision.direction()), false);
	}

	@Override
	public float calculateWallYaw(CfaReadableMotionData data) {
		return ClimbTransitions.yawOf(getWallDirection(data));
	}

	@Override
	public boolean verifyLegality(CfaReadableMotionData data, WallInfo wall, Vec3d checkOffset) {
		World world = data.getPlayer().getWorld();
		for(BlockPos wallBlock : wall.getWallBlocks(0.4)) {
			if(ClimbTransitions.canClimbBlock(world.getBlockState(wallBlock), Direction.fromRotation(wall.getWallYaw())))
				return true;
		}
		return false;
	}

	public static final StateAttachmentKey<CommonClimbProgress> COMMON_CLIMB_PROGRESS =
			StateAttachmentKey.action(CommonClimbProgress::new);
	public static final StateAttachmentKey<Boolean> USE_ALTERNATE_ATTACHMENT =
			StateAttachmentKey.action(() -> Boolean.FALSE);


	public static class CommonClimbProgress {
		public float progress;
		public float nextStepSound = MathHelper.PI;

		public float horizontalIntensity = 0.75F;
		public float verticalIntensity = 0.45F;

		public Vec3d prevPos;

		public CommonClimbProgress(CfaData data) {
			this.prevPos = data.getPlayer().getPos();
		}
	}

	protected double getConstantTowardsWallVel() {
		return 0.2;
	}
	protected final double TOWARDS_WALL_VEL = this.getConstantTowardsWallVel();

	protected boolean useAlternateOffset(CfaData data) {
		return false;
	}

	@Override
	public void onEnter(CfaData data, ImmutableSet.Builder<StateAttachmentKey<?>> attachmentsToKeep) {
		data.put(USE_ALTERNATE_ATTACHMENT, this.useAlternateOffset(data));
	}

	@Override
	public void commonTick(CfaData data) {
		CommonClimbProgress climb = data.get(COMMON_CLIMB_PROGRESS);
		Vec3d delta = data.getPlayer().getPos().subtract(climb.prevPos);

		double deltaY = delta.y;
		double absDeltaY = Math.abs(deltaY);
		double absDeltaX = delta.horizontalLength();
		double deltaX = absDeltaX * -1 * Math.signum(delta.x + delta.z);

		if(absDeltaX != 0 || absDeltaY != 0) {
			climb.progress += (float) (5 * Math.max(absDeltaX, absDeltaY));
			double denominator = Math.max(absDeltaX, absDeltaY);
			climb.horizontalIntensity = (float) (deltaX / denominator);
			climb.verticalIntensity = (float) (deltaY / denominator);
		}

		if(climb.progress > climb.nextStepSound) {
			do climb.nextStepSound += MathHelper.PI; while(climb.progress > climb.nextStepSound);

			if(!data.getPlayer().isSneaky()) {
				if(data.isClient() && data instanceof CfaClientData clientData) {
					long seed = data.getPlayer().getRandom().nextLong();
					if(this.isIntangible())
						ClimbTransitions.playSolidWallStepSound(clientData, seed);
					else
						ClimbTransitions.playIntangibleWallStepSound(clientData, seed);
				}
				else
					data.getPlayer().emitGameEvent(GameEvent.STEP);
			}
		}

		climb.prevPos = data.getPlayer().getPos();
	}

	protected boolean isIntangible() {
		return false;
	}

	@Override public void travel(CfaTravelData data, WallInfo wall, WallboundActionHelper helper) {
		helper.setTowardsWallVel(data, TOWARDS_WALL_VEL);
		data.getPlayer().fallDistance = 0;
		double climbInput = wall.getTowardsWallInput();
		double sidleInput = wall.getSidleInput();
		if(sidleInput != 0 && !wall.wouldBeLegalWithOffset(0, Math.signum(sidleInput) * 0.8)) {
			sidleInput = 0;

			Direction.Axis axis = Direction.fromRotation(HelperGetter.getWallboundActionHelper().getWallInfo(data).getWallYaw()).rotateYClockwise().getAxis();
			double targetCoord = data.getPlayer().getBlockPos().toBottomCenterPos().getComponentAlongAxis(axis);
			if(wall.wouldBeLegalWithOffset(0, targetCoord - data.getPlayer().getPos().getComponentAlongAxis(axis)))
				data.goTo(data.getPlayer().getPos().withAxis(axis, targetCoord));
		}

		ClimbPole.ClimbStatSet stats = ClimbPole.ClimbStatSet.choose(data);
		double yVel = climbInput * stats.getVertical(climbInput).get(data);
		double sidleVel = sidleInput * stats.sidle().get(data);
		data.setYVel(yVel);
		helper.setSidleVel(data, sidleVel);
	}

	protected Identifier getSideHangActionID() {
		return ClimbWallSideHang.ID;
	}

	public static final float MIN_DEVIATION_TO_SIDE_HANG = 99;

	protected ActionTransitionDetails.ClientsExecutor getSideHangTransitionClientsExecutor() {
		return null;
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, WallboundActionHelper helper) {
		builder.add(new ActionTransitionDetails(
				this.getSideHangActionID(),
				data -> Math.abs(helper.getWallInfo(data).getYawDeviation()) > MIN_DEVIATION_TO_SIDE_HANG,
				EvaluatorEnvironment.CLIENT_ONLY,
				SizeChangeBehavior.AUTOMATIC,
				data -> {
					data.setForwardStrafeVel(0, 0);
					data.setYVel(Math.min(0, data.getYVel()));
				},
				this.getSideHangTransitionClientsExecutor()
		));
	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, WallboundActionHelper helper) {
		builder.add(
				new ActionTransitionDetails(
						WallJump.ID,
						data -> helper.getWallInfo(data) != null
								&& Objects.requireNonNull(helper.getWallInfo(data)).getTowardsWallInput() < -0.45
								&& data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						SizeChangeBehavior.AUTOMATIC,
						data -> {
							data.setYVel(WallJump.WALL_JUMP_VEL.get(data));
							double speed = WallJump.WALL_JUMP_SPEED.get(data);
							data.setForwardStrafeVel(data.getInputs().getNormalizedForwardInput() * speed,
									data.getInputs().getNormalizedStrafeInput() * speed);
						},
						(data, isSelf, seed) -> {
							data.voice(Voicelines.WALL_JUMP, seed);
							data.playJumpSound(seed);
						}
				),
				new ActionTransitionDetails(
						Fall.ID,
						data -> data.getInputs().DUCK.isHeld() && data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						SizeChangeBehavior.AUTOMATIC,
						data -> {
							helper.setTowardsWallVel(data, 0);
							data.getInputs().DUCK.isPressed(); // Unbuffer Duck
						},
						null
				),
				new ActionTransitionDetails(
						Jump.ID,
						data -> data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						SizeChangeBehavior.AUTOMATIC,
						data -> {
							helper.setTowardsWallVel(data, 0);
							data.setYVel(Jump.JUMP_VEL.get(data));
						},
						(data, isSelf, seed) -> data.playJumpSound(seed)
				)
		);
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, WallboundActionHelper helper) {
		builder.add(
				new ActionTransitionDetails(
						SpecialFall.ID,
						data -> !helper.getWallInfo(data).isLegal(),
						EvaluatorEnvironment.COMMON,
						SizeChangeBehavior.AUTOMATIC,
						data -> helper.setTowardsWallVel(data, 0),
						null
				),
				new ActionTransitionDetails(
						SubWalk.ID,
						data -> data.getPlayer().isOnGround(),
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}
}

package com.fqf.mario_qua_mario_content.actions.wallbound;

import com.fqf.mario_qua_mario_api.HelperGetter;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.mariodata.*;
import com.fqf.mario_qua_mario_api.mariodata.util.RecordedCollisionSet;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_content.actions.airborne.SpecialFall;
import com.fqf.mario_qua_mario_content.actions.generic.ClimbPole;
import com.fqf.mario_qua_mario_content.util.ClimbTransitions;
import com.fqf.mario_qua_mario_content.util.ClimbVars;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario_api.util.StatCategory.*;

public class ClimbWall implements WallboundActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("climb_wall");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	private static LimbAnimation makeArmAnimation(int factor) {
	    return new LimbAnimation(false, (data, arrangement, progress) -> {
			ClimbOmniDirectionalVars vars = data.getVars(ClimbOmniDirectionalVars.class);
			arrangement.addAngles(
					-110 + (-30 * Math.abs(vars.yComponent) * (1 - Math.abs(vars.xComponent))) + (progress * -20 * factor * vars.yComponent),
					factor * (24 * Math.abs(vars.xComponent) + 20 * progress * vars.xComponent),
					0
			);
			arrangement.y -= progress * 1.9F * factor * vars.yComponent;
	    });
	}
	private static LimbAnimation makeLegAnimation(int factor) {
	    return new LimbAnimation(false, (data, arrangement, progress) -> {
			ClimbOmniDirectionalVars vars = data.getVars(ClimbOmniDirectionalVars.class);
//			boolean isRight = factor == 1;
//			float pseudoProgress = isRight ? progress : MathHelper.cos(vars.progress);
			float yawRollAdjustment = factor * (3 * Math.abs(vars.xComponent) + 16 * progress * vars.xComponent);
			arrangement.addAngles(
					-10 + progress * -20 * factor * vars.yComponent,
					yawRollAdjustment,
					yawRollAdjustment
			);
			arrangement.y -= 3 + progress * 3 * factor * vars.yComponent;
			arrangement.z -= 3.7F;
	    });
	}
	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
	    return new PlayermodelAnimation(
	            null,
	            new ProgressHandler((data, ticksPassed) -> MathHelper.sin(data.getVars(ClimbOmniDirectionalVars.class).progress)),
	            new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) -> {
					arrangement.z += 2.25F;
	            }),
	            null,
				new BodyPartAnimation((data, arrangement, progress) -> {
					arrangement.yaw += progress * 15;
				}),
	            makeArmAnimation(1), makeArmAnimation(-1),
	            makeLegAnimation(1), makeLegAnimation(-1),
	            null
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
	@Override public @Nullable Identifier getStompTypeID() {
		return null;
	}

	@Override public @NotNull WallBodyAlignment getBodyAlignment() {
		return WallBodyAlignment.TOWARDS;
	}

	@Override public float getHeadYawRange() {
		return 360;
	}

	public static Direction getWallDirection(IMarioReadableMotionData data) {
		return data.getRecordedCollisions().getDirectionOfCollisionsWith((collision, block) -> ClimbTransitions.canClimbBlock(collision.state(), collision.direction()), false);
	}

	@Override
	public float getWallYaw(IMarioReadableMotionData data) {
		return ClimbTransitions.yawOf(getWallDirection(data));
	}

	@Override
	public boolean checkLegality(IMarioReadableMotionData data, WallInfo wall) {
		World world = data.getMario().getWorld();
		for(BlockPos wallBlock : wall.getWallBlocks(0.4)) {
			if(ClimbTransitions.canClimbBlock(world.getBlockState(wallBlock), Direction.fromRotation(wall.getWallYaw())))
				return true;
		}
		return false;
	}

	private static class ClimbOmniDirectionalVars extends ClimbVars {
		float xComponent = 0.75F;
		float yComponent = 0.45F;
	}

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new ClimbOmniDirectionalVars();
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {
		data.getVars(ClimbOmniDirectionalVars.class).clientTick(data);
	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, WallInfo wall, WallboundActionHelper helper) {
		helper.setTowardsWallVel(data, 0.3);
		double climbInput = wall.getTowardsWallInput() * (data.getInputs().DUCK.isHeld() ? 0.3 : 1);
		double sidleInput = wall.getSidleInput();
		if(sidleInput != 0 && !wall.wouldBeLegalWithOffset(0, Math.signum(sidleInput) * 0.8)) {
			sidleInput = 0;

			Direction.Axis axis = Direction.fromRotation(HelperGetter.getWallboundActionHelper().getWallInfo(data).getWallYaw()).rotateYClockwise().getAxis();
			double targetCoord = data.getMario().getBlockPos().toBottomCenterPos().getComponentAlongAxis(axis);
			if(wall.wouldBeLegalWithOffset(0, targetCoord - data.getMario().getPos().getComponentAlongAxis(axis)))
				data.goTo(data.getMario().getPos().withAxis(axis, targetCoord));
		}

		double climbSpeed = ClimbPole.CLIMB_SPEED.get(data);
		double yVel = climbInput * climbSpeed;
		double sidleVel = sidleInput * climbSpeed;
		data.setYVel(yVel);
		helper.setSidleVel(data, sidleVel);

		if(climbInput != 0 || sidleInput != 0) {
			ClimbOmniDirectionalVars vars = data.getVars(ClimbOmniDirectionalVars.class);
			vars.progress += Math.min(1, (float) Vector2d.length(climbInput, sidleInput));
			float denominator = (float) Math.max(Math.abs(climbInput), Math.abs(sidleInput));
			vars.xComponent = (float) sidleInput / denominator;
			vars.yComponent = (float) climbInput / denominator;
		}
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(WallboundActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(WallboundActionHelper helper) {
		return List.of(
				ClimbIntangibleDirectional.BACKFLIP_OFF_LADDER,
				ClimbIntangibleDirectional.DROP_OFF_LADDER,
				ClimbIntangibleDirectional.JUMP_OFF_LADDER
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

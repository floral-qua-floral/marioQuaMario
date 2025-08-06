package com.fqf.mario_qua_mario_content.actions.wallbound;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.mariodata.*;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario_content.actions.airborne.Fall;
import com.fqf.mario_qua_mario_content.actions.airborne.SpecialFall;
import com.fqf.mario_qua_mario_content.actions.grounded.SubWalk;
import com.fqf.mario_qua_mario_content.util.ClimbTransitions;
import com.fqf.mario_qua_mario_content.util.MarioContentSFX;
import com.fqf.mario_qua_mario_content.util.MarioVars;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class WallSlide implements WallboundActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("wall_slide");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return null;
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
		return WallBodyAlignment.ANY;
	}

	@Override public float getHeadYawRange() {
		return 360;
	}

	public static boolean canSlideDownBlock(BlockState state) {
		return true;
	}

	@Override
	public float getWallYaw(IMarioReadableMotionData data) {
		return ClimbTransitions.yawOf(data.getRecordedCollisions().getDirectionOfCollisionsWith((collision, block) ->
				canSlideDownBlock(collision.state()), false));
	}

	@Override
	public boolean checkLegality(IMarioReadableMotionData data, WallInfo wall, Vec3d checkOffset) {
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

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, WallInfo wall, WallboundActionHelper helper) {
		if(data.isClient()) {
			if(wall.getTowardsWallInput() < -0.05)
				data.getVars(WallSlideVars.class).holdAwayFromWallTicks++;
			else
				data.getVars(WallSlideVars.class).holdAwayFromWallTicks = 0;
		}

	}

	public static final TransitionDefinition WALL_SLIDE = new TransitionDefinition(
			WallSlide.ID,
			MarioVars::checkWallSlide,
			EvaluatorEnvironment.CLIENT_ONLY,
			null,
			(data, isSelf, seed) -> data.playSound(MarioContentSFX.KICK, seed)
	);

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(WallboundActionHelper helper) {
		return List.of(

		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(WallboundActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						Fall.ID,
						data -> data.getInputs().DUCK.isPressed() || data.getVars(WallSlideVars.class).holdAwayFromWallTicks > 2,
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> helper.setTowardsWallVel(data, 0),
						null
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(WallboundActionHelper helper) {
		return List.of(
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

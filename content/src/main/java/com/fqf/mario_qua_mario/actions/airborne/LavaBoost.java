package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.aquatic.Submerged;
import com.fqf.mario_qua_mario.util.ClimbTransitions;
import com.fqf.mario_qua_mario.util.MQMTags;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class LavaBoost extends Fall implements AirborneActionDefinition {
    public static final Identifier ID = MarioQuaMario.makeID("lava_boost");
	@Override public @NotNull Identifier defineID() {
		return ID;
	}

	@Override public @Nullable AnimationDefinition getAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(arrangement, data, animationTime, helper) -> {
					arrangement.pitch += 28.45F;
					arrangement.y -= 6.75F;
				},
				(posture, data, animationTime, helper) -> {
					helper.symmetricallyAnimate(posture, posture.RIGHT_ARM, arrangement -> {
						arrangement.addPos(0, 1.25F, 1);
						arrangement.addAngles(120, 88, 90);
					});

					float progress = animationTime / 2.2F;
					helper.asymmetricallyAnimate(posture.RIGHT_LEG, posture.LEFT_LEG, (arrangement, isLeft, sideFactor) ->
							arrangement.addAngles(-57.75F + sideFactor * MathHelper.sin(progress) * 40, 0, 0));
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

	public static CfaStat BOOST_VEL = new CfaStat(1.4, JUMP_VELOCITY);

	public static CfaStat REDUCED_FORWARD_ACCEL = Fall.FORWARD_DRIFT_ACCEL.variate(0.5);
	public static CfaStat REDUCED_FORWARD_SPEED = Fall.FORWARD_DRIFT_SPEED.variate(0.5);
	public static CfaStat REDUCED_BACKWARD_ACCEL = Fall.BACKWARD_DRIFT_ACCEL.variate(0.5);
	public static CfaStat REDUCED_BACKWARD_SPEED = Fall.BACKWARD_DRIFT_SPEED.variate(0.5);
	public static CfaStat REDUCED_STRAFE_ACCEL = Fall.STRAFE_DRIFT_ACCEL.variate(0.5);
	public static CfaStat REDUCED_STRAFE_SPEED = Fall.STRAFE_DRIFT_SPEED.variate(0.5);

	public static CfaStat BOOST_REDIRECTION = new CfaStat(3, DRIFTING, REDIRECTION);

	private static class LavaBoostVars {
		private double bounceVel;
	}
	@Override public @Nullable Object provideStateData(CfaData data) {
		return new LavaBoostVars();
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {

	}
	@Override public void serverTick(CfaAuthoritativeData data) {

	}
	@Override public void travelHook(CfaTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, FALL_ACCEL, null, FALL_SPEED);
		data.retrieveStateData(LavaBoostVars.class).bounceVel = data.getYVel() * -0.6;
		if(data.getYVel() < 0) Fall.drift(data, helper);
		else helper.airborneAccel(
				data,
				REDUCED_FORWARD_ACCEL, REDUCED_FORWARD_SPEED,
				REDUCED_BACKWARD_ACCEL, REDUCED_BACKWARD_SPEED,
				REDUCED_STRAFE_ACCEL, REDUCED_STRAFE_SPEED,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				BOOST_REDIRECTION
		);
	}

	private static final double MAX_EJECTION_DISTANCE = 5;
	private static final double EJECTION_STEP_DISTANCE = 0.5;

	private static boolean positionHasProhibitiveFluid(CfaData data, Box box) {
		int minX = MathHelper.floor(box.minX); int maxX = MathHelper.ceil(box.maxX);
		int minY = MathHelper.floor(box.minY); int maxY = MathHelper.ceil(box.maxY);
		int minZ = MathHelper.floor(box.minZ); int maxZ = MathHelper.ceil(box.maxZ);
		World world = data.getPlayer().getWorld();
		BlockPos.Mutable mutable = new BlockPos.Mutable();

		for(int checkX = minX; checkX < maxX; checkX++) {
			for(int checkY = minY; checkY < maxY; checkY++) {
				for(int checkZ = minZ; checkZ < maxZ; checkZ++) {
					mutable.set(checkX, checkY, checkZ);
					FluidState fluidState = world.getFluidState(mutable);
					if(fluidState.isIn(MQMTags.PROHIBITS_LAVA_BOOST_EJECTION)) {
						if(checkY + fluidState.getHeight(world, mutable) >= box.minY) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public static @Nullable Vec3d findLavaBoostEjectionSpot(CfaData data) {
		PlayerEntity player = data.getPlayer();
		Box playerBoundingBox = player.getBoundingBox();
		Box ejectionCheckBox = playerBoundingBox.withMaxY(playerBoundingBox.minY + MAX_EJECTION_DISTANCE);

		int minX = MathHelper.floor(ejectionCheckBox.minX); int maxX = MathHelper.ceil(ejectionCheckBox.maxX);
		int minY = MathHelper.floor(ejectionCheckBox.minY); int maxY = MathHelper.ceil(ejectionCheckBox.maxY);
		int minZ = MathHelper.floor(ejectionCheckBox.minZ); int maxZ = MathHelper.ceil(ejectionCheckBox.maxZ);
		World world = data.getPlayer().getWorld();
		BlockPos.Mutable mutable = new BlockPos.Mutable();

		for(int checkX = minX; checkX < maxX; checkX++) {
			for(int checkY = maxY; checkY >= minY; checkY--) {
				for(int checkZ = minZ; checkZ < maxZ; checkZ++) {
					mutable.set(checkX, checkY, checkZ);
					FluidState fluidState = world.getFluidState(mutable);
					if(fluidState.isIn(MQMTags.PROHIBITS_LAVA_BOOST_EJECTION)) {
						double fluidSurface = checkY + fluidState.getHeight(world, mutable);
						if(fluidSurface < ejectionCheckBox.maxY) {
							// We've found the top of the liquid!
							// Now check if it's actually possible for the player to rise up to this height.
							Box checkCollisionsBox = playerBoundingBox.withMaxY(fluidSurface + player.getHeight());
							if(world.isSpaceEmpty(player, checkCollisionsBox))
								return player.getPos().withAxis(Direction.Axis.Y, fluidSurface);
						}

						// We found lava, but we didn't find the surface of it. Since we start from the top and search
						// downwards, if we haven't found the surface yet, we're for sure not gonna find it even lower.
						return null;
					}
				}
			}
		}

		// Doesn't seem like we're in lava to begin with???? This can be caused by a Lava Cauldron.
		// Trying to lava boost in place is TOO RISKY. If Mario's in a position that causes lava damage every tick and
		// never finds actual lava (such as in a lava cauldron), then lava boosting in place would lock him in that
		// position while he gets cooked to death. Awful...! We could try moving him up an arbitrary amount and then
		// lava boost in place, which would work great for Lava Cauldrons, but bad if you imagine something like a
		// Tinker's Construct smeltery. Mario would get forced up to its ceiling, and every time he sinks he'd trigger
		// a Lava Boost and get forced back up.
		return null;
	}

	public static final TransitionDefinition LAVA_BOOST = new TransitionDefinition(
			LavaBoost.ID,
			data -> {
//				data.getPlayer().isInLava()
				return false;
			},
			EvaluatorEnvironment.COMMON,
			data -> {

			},
			(data, isSelf, seed) -> {

			}
	);

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AirborneActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AirborneActionHelper helper) {
		return List.of(
				Submerged.SUBMERGE,
				Fall.LANDING.variate(
						LavaBoost.ID,
						data ->
								data.getYVel() <= 0
								&& data.retrieveStateData(LavaBoostVars.class).bounceVel > 0.06
								&& Fall.LANDING.evaluator().shouldTransition(data),
						null,
						data -> {
							data.setYVel(data.retrieveStateData(LavaBoostVars.class).bounceVel);
							data.setForwardStrafeVel(data.getForwardVel() * 0.5, data.getStrafeVel() * 0.5);
						},
						(data, isSelf, seed) -> {}
				),
				Fall.LANDING.variate(null, data -> data.getYVel() <= 0 && Fall.LANDING.evaluator().shouldTransition(data)),
				// Mario can start climbing from a Lava Boost, but can't wall-jump
				ClimbTransitions.CLIMB_NON_SOLID_DIRECTIONAL,
				ClimbTransitions.CLIMB_NON_SOLID_NON_DIRECTIONAL,
				ClimbTransitions.CLIMB_SOLID
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of(
				new TransitionInjectionDefinition(
						TransitionInjectionDefinition.InjectionPlacement.BEFORE,
						Submerged.ID,
						(fromAction, fromCategory, existingTransitions) -> true,
						(nearbyTransition, castableHelper) -> LAVA_BOOST
				)
		);
	}

}
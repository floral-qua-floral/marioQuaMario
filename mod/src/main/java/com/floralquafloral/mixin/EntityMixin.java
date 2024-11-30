package com.floralquafloral.mixin;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.StompableEntity;
import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.moveable.MarioMainClientData;
import com.floralquafloral.mariodata.moveable.MarioServerData;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.registries.states.action.ParsedAction;
import com.floralquafloral.registries.states.character.ParsedCharacter;
import com.floralquafloral.registries.states.powerup.ParsedPowerUp;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

@Mixin(Entity.class)
public class EntityMixin {
	@Inject(at = @At("HEAD"), method = "setSwimming(Z)V", cancellable = true)
	private void setSwimming(boolean swimming, CallbackInfo ci) {
		Entity entity = (Entity) (Object) this;
		if(swimming && entity instanceof PlayerEntity player && MarioDataManager.getMarioData(player).isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(at = @At("HEAD"), method = "isInSneakingPose", cancellable = true)
	private void isInSneakingPose(CallbackInfoReturnable<Boolean> cir) {
		if((Entity) (Object) this instanceof PlayerEntity player) {
			if(MarioDataManager.getMarioData(player).isSneakProhibited())
				cir.setReturnValue(false);
		}
	}

	@Inject(method = "setPose", at = @At("HEAD"), cancellable = true)
	private void preventSettingSneakPose(EntityPose pose, CallbackInfo ci) {
		if((Entity) (Object) this instanceof PlayerEntity player && pose == EntityPose.CROUCHING) {
//			MarioQuaMario.LOGGER.info("setPose called on player! Pose == {}", pose);
			if(MarioDataManager.getMarioData(player).isSneakProhibited()) {

				if(player.getPose() == EntityPose.CROUCHING)
					player.setPose(EntityPose.STANDING);
				ci.cancel();
			}
		}
	}

	@Inject(method = "playStepSounds", at = @At("HEAD"), cancellable = true)
	private void preventStepSounds(BlockPos pos, BlockState state, CallbackInfo ci) {
		if(((Entity) (Object) this) instanceof PlayerEntity player) {
			MarioPlayerData data = MarioDataManager.getMarioData(player);
			if(!data.getAction().SLIDING_STATUS.doFootsteps())
				ci.cancel();
		}
	}

	@Unique public final double CLIPPING_LENIENCY = 0.2;

	@WrapMethod(method = "move")
	private void jumpBlockEdgeClipping(MovementType movementType, Vec3d movement, Operation<Void> original) {
		if((Entity) (Object) this instanceof PlayerEntity mario && (movementType == MovementType.SELF || movementType == MovementType.PLAYER)) {
			MarioPlayerData data = MarioDataManager.getMarioData(mario);

			if(movement.y > 0 && data.useMarioPhysics()) {
				// If Mario's horizontal velocity is responsible for him clipping a ceiling, then just cancel his horizontal movement
				if(
						(movement.x != 0 || movement.z != 0)
								&& mario.getWorld().isSpaceEmpty(mario, mario.getBoundingBox().offset(movement.x, 0, movement.z))
								&& !mario.getWorld().isSpaceEmpty(mario, mario.getBoundingBox().offset(movement))) {
					mario.move(movementType, new Vec3d(0, movement.y, 0));
					return;
				}

				if(!mario.getWorld().isSpaceEmpty(mario, mario.getBoundingBox().offset(0, movement.y, 0))) {
					Box stretchedBox = mario.getBoundingBox().stretch(0, movement.y, 0);
					if(mario.getWorld().isSpaceEmpty(mario, stretchedBox.offset(CLIPPING_LENIENCY, 0, 0)))
						mario.move(MovementType.SELF, new Vec3d(CLIPPING_LENIENCY, 0, 0));
					if(mario.getWorld().isSpaceEmpty(mario, stretchedBox.offset(-CLIPPING_LENIENCY, 0, 0)))
						mario.move(MovementType.SELF, new Vec3d(-CLIPPING_LENIENCY, 0, 0));
					if(mario.getWorld().isSpaceEmpty(mario, stretchedBox.offset(0, 0, CLIPPING_LENIENCY)))
						mario.move(MovementType.SELF, new Vec3d(0, 0, CLIPPING_LENIENCY));
					if(mario.getWorld().isSpaceEmpty(mario, stretchedBox.offset(0, 0, -CLIPPING_LENIENCY)))
						mario.move(MovementType.SELF, new Vec3d(0, 0, -CLIPPING_LENIENCY));
				}
			}
		}
		original.call(movementType, movement);
	}

	@Unique
	private static boolean shouldStompHook = true;

	@Inject(method = "move", at = @At("HEAD"), cancellable = true)
	private void executeStompsOnServer(MovementType movementType, Vec3d movement, CallbackInfo ci) {
		if((Entity) (Object) this instanceof ServerPlayerEntity mario && shouldStompHook) {
			MarioPlayerData data = MarioDataManager.getMarioData(mario);
			if(data.useMarioPhysics()) {
				ParsedAction action = data.getAction();
				if(action.STOMP != null) {
					shouldStompHook = false;
					if(action.STOMP.attempt((MarioServerData) data, movement)) ci.cancel();
					shouldStompHook = true;
				}
			}
		}
	}

	@WrapOperation(method = "findCollisionsForMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/lang/Iterable;"))
	private static Iterable<VoxelShape> bumpBlocksOnCollision(World world, Entity entity, Box movingEntityBoundingBox, Operation<Iterable<VoxelShape>> original) {
		// This doesn't work! Fix it!
		if(false && entity instanceof ClientPlayerEntity) {
			MarioMainClientData data = MarioMainClientData.getInstance();
			if(data != null) {
				Vec3d marioImmutableVelocity = data.getMario().getVelocity(); // is this adequate?
				Vector3d marioVelocity = new Vector3d(marioImmutableVelocity.x, marioImmutableVelocity.y, marioImmutableVelocity.z);

				boolean bumpCeilings = true || marioVelocity.y != 0 && data.getAction().BUMPING_RULE.CEILINGS != 0;
				boolean bumpFloors = true || marioVelocity.y != 0 && data.getAction().BUMPING_RULE.FLOORS != 0;
				boolean bumpWalls = true || (marioVelocity.x != 0 || marioVelocity.z != 0) && data.getAction().BUMPING_RULE.WALLS != 0;
				Iterable<Pair<BlockPos, VoxelShape>> blockCollisions =
						() -> new BlockCollisionSpliterator<>(world, entity, movingEntityBoundingBox, false, (pos, voxelShape) -> new Pair<>(pos, voxelShape));

				EnumMap<Direction, Set<BlockPos>> bumpBlocks = new EnumMap<>(Direction.class);
				for(Direction direction : Direction.values()) {
					bumpBlocks.put(direction, new HashSet<>());
				}

				Box marioBoundingBox = entity.getBoundingBox();

				for(Pair<BlockPos, VoxelShape> collidedBlock : blockCollisions) {
					if((bumpCeilings || bumpFloors) && collidedBlock.getRight().calculateMaxDistance(Direction.Axis.Y, marioBoundingBox, marioVelocity.y) != 0) {
						if(bumpCeilings && marioVelocity.y > 0) {
							// Mario was moving up and bumped this block on the Y axis!
							bumpBlocks.get(Direction.UP).add(collidedBlock.getLeft().toImmutable());
						}
						else if(bumpFloors && marioVelocity.y < 0) {
							// Mario was moving down and bumped this block on the Y axis!
							bumpBlocks.get(Direction.DOWN).add(collidedBlock.getLeft().toImmutable());
						}
					}
					else if(bumpWalls) {
						boolean xAxisFirst = Math.abs(marioVelocity.x) > Math.abs(marioVelocity.z);
						if(xAxisFirst && collidedBlock.getRight().calculateMaxDistance(Direction.Axis.X, marioBoundingBox, marioVelocity.x) != 0) {
							MarioQuaMario.LOGGER.info("Collided along X axis: {}", collidedBlock.getRight().calculateMaxDistance(Direction.Axis.X, marioBoundingBox, marioVelocity.x));
							if(marioVelocity.x > 0)
								bumpBlocks.get(Direction.EAST).add(collidedBlock.getLeft().toImmutable());
							else
								bumpBlocks.get(Direction.WEST).add(collidedBlock.getLeft().toImmutable());
						}
						else if(collidedBlock.getRight().calculateMaxDistance(Direction.Axis.Z, marioBoundingBox, marioVelocity.z) != 0) {
							if(marioVelocity.z > 0)
								bumpBlocks.get(Direction.SOUTH).add(collidedBlock.getLeft().toImmutable());
							else
								bumpBlocks.get(Direction.NORTH).add(collidedBlock.getLeft().toImmutable());
						}
						else if(!xAxisFirst && collidedBlock.getRight().calculateMaxDistance(Direction.Axis.X, marioBoundingBox, marioVelocity.x) != 0) {
							MarioQuaMario.LOGGER.info("Collided along X axis: {}", collidedBlock.getRight().calculateMaxDistance(Direction.Axis.X, marioBoundingBox, marioVelocity.x));
							if(marioVelocity.x > 0)
								bumpBlocks.get(Direction.EAST).add(collidedBlock.getLeft().toImmutable());
							else
								bumpBlocks.get(Direction.WEST).add(collidedBlock.getLeft().toImmutable());
						}
					}
				}

				for(Direction direction : Direction.values()) {
					Set<BlockPos> bumpBlocksInThisDirection = bumpBlocks.get(direction);
					if(!bumpBlocksInThisDirection.isEmpty())
						MarioQuaMario.LOGGER.info("BUMP {}:\t{}\t{}", direction.getName(), bumpBlocksInThisDirection.size(), bumpBlocksInThisDirection);
				}
			}
		}

		return original.call(world, entity, movingEntityBoundingBox);
	}

	//	@WrapOperation(method = "checkBlockCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onEntityCollision(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V"))
//	private void executeBumpsOnClient(BlockState instance, World world, BlockPos blockPos, Entity entity, Operation<Void> original) {
//		if(entity instanceof ServerPlayerEntity mario) {
////			world.breakBlock(blockPos, true, mario);
//		}
//	}

//	@Inject(method = "findCollisionsForMovement", at = @At("TAIL"))
//	private static void testasaur(@Nullable Entity entity, World world, List<VoxelShape> regularCollisions, Box movingEntityBoundingBox, CallbackInfoReturnable<List<VoxelShape>> cir) {
//		if(entity instanceof ClientPlayerEntity clientPlayer) {
//			MarioQuaMario.LOGGER.info("findCollisionsForMovement on Mario!");
//			for(VoxelShape shape : cir.getReturnValue()) {
//				MarioQuaMario.LOGGER.info("Uwu: " + shape.);
//			}
//		}
//	}

	@Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("HEAD"))
	private void setMountedAction(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
		if((Entity) (Object) this instanceof PlayerEntity mario) {
			MarioPlayerData data = MarioDataManager.getMarioData(mario);

			data.attemptDismount = false;
			data.setActionTransitionless(RegistryManager.ACTIONS.get(Identifier.of("qua_mario:mounted")));
		}
	}

	@Unique private final String MOD_DATA_NAME = MarioQuaMario.MOD_ID + ".data";

	@Inject(method = "writeNbt", at = @At("HEAD"))
	protected void writeMarioData(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
		if((Entity) (Object) this instanceof ServerPlayerEntity player) {
			NbtCompound persistentMarioData = new NbtCompound();
			MarioPlayerData data = MarioDataManager.getMarioData(player);

			persistentMarioData.putBoolean("Enabled", data.isEnabled());
			persistentMarioData.putString("Character", data.getCharacterID().toString());
			persistentMarioData.putString("PowerUp", data.getPowerUpID().toString());


			MarioQuaMario.LOGGER.info("Writing player NBT"
					+ "\nEnabled: " + persistentMarioData.getBoolean("Enabled")
					+ "\nCharacter: " + persistentMarioData.getString("Character")
					+ "\nCharacterID: " + Identifier.of(persistentMarioData.getString("Character"))
					+ "\nParsedCharacter: " + RegistryManager.CHARACTERS.get(Identifier.of(persistentMarioData.getString("Character")))
			);

			nbt.put(MOD_DATA_NAME, persistentMarioData);
		}
	}

	@Inject(method = "readNbt", at = @At("HEAD"))
	protected void readMarioData(NbtCompound nbt, CallbackInfo ci) {
		if((Entity) (Object) this instanceof ServerPlayerEntity player) {
			MarioQuaMario.LOGGER.info("Reading player NBT!!!"
					+ "\nContains?: " + nbt.contains(MOD_DATA_NAME, NbtElement.COMPOUND_TYPE)
			);
			if(nbt.contains(MOD_DATA_NAME, NbtElement.COMPOUND_TYPE)) {
				NbtCompound persistentMarioData = nbt.getCompound(MOD_DATA_NAME);
				MarioQuaMario.LOGGER.info("Reading player NBT 2"
						+ "\nEnabled: " + persistentMarioData.getBoolean("Enabled")
						+ "\nCharacter: " + persistentMarioData.getString("Character")
						+ "\nCharacterID: " + Identifier.of(persistentMarioData.getString("Character"))
						+ "\nParsedCharacter: " + RegistryManager.CHARACTERS.get(Identifier.of(persistentMarioData.getString("Character")))
				);

				MarioServerData data = (MarioServerData) MarioDataManager.getMarioData(player);
//				if(data.getMario().networkHandler != null) {
//					data.setEnabled(persistentMarioData.getBoolean("Enabled"));
//					data.setPowerUp(persistentMarioData.getString("PowerUp"));
//					data.setCharacter(persistentMarioData.getString("Character"));
//				}

				data.setEnabledInternal(persistentMarioData.getBoolean("Enabled"));
				ParsedCharacter character = RegistryManager.CHARACTERS.get(Identifier.of(persistentMarioData.getString("Character")));
				if(character != null) data.setCharacter(character);
				else data.setCharacter(data.getCharacter());

				ParsedPowerUp powerUp = RegistryManager.POWER_UPS.get(Identifier.of(persistentMarioData.getString("PowerUp")));
				if(powerUp != null) data.setPowerUp(powerUp);
				else data.setPowerUp(data.getPowerUp());
			}
			else {

			}
		}
	}
}

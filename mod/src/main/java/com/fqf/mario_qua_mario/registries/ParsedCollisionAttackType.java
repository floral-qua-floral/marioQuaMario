package com.fqf.mario_qua_mario.registries;

import com.fqf.mario_qua_mario_api.definitions.CollisionAttackTypeDefinition;
import com.fqf.mario_qua_mario_api.interfaces.CollisionAttackResult;
import com.fqf.mario_qua_mario_api.interfaces.CollisionAttackable;
import com.fqf.mario_qua_mario.mariodata.*;
import com.fqf.mario_qua_mario.packets.MarioPackets;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.util.ItemStackArmorReader;
import com.fqf.mario_qua_mario.util.CollisionAttackDamageSource;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import it.unimi.dsi.fastutil.floats.FloatFloatImmutablePair;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ParsedCollisionAttackType extends ParsedMarioThing {
	private final CollisionAttackTypeDefinition DEFINITION;

	private final boolean MOUNTING;
	private final CollisionAttackTypeDefinition.PainfulCollisionResponse PAINFUL_COLLISION_RESPONSE;
	private final @Nullable EquipmentSlot USE_EQUIPMENT_SLOT;
	private final @NotNull RegistryKey<DamageType> DAMAGE_TYPE;
	private final EnumMap<CollisionAttackResult.ExecutableResult, @Nullable AbstractParsedAction> POST_COLLISION_ACTIONS;

	public ParsedCollisionAttackType(@NotNull CollisionAttackTypeDefinition definition) {
		super(definition.getID());

		this.DEFINITION = definition;

		this.MOUNTING = definition.shouldAttemptMounting();
		this.PAINFUL_COLLISION_RESPONSE = definition.painfulCollisionResponse();
		this.USE_EQUIPMENT_SLOT = definition.getEquipmentSlot();
		this.DAMAGE_TYPE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, definition.getDamageType());
		this.POST_COLLISION_ACTIONS = new EnumMap<>(CollisionAttackResult.ExecutableResult.class);
	}

	public void populatePostCollisionActions() {
		this.populatePostCollisionActions(CollisionAttackResult.ExecutableResult.MOUNT);
		this.populatePostCollisionActions(CollisionAttackResult.ExecutableResult.PAINFUL);
		this.populatePostCollisionActions(CollisionAttackResult.ExecutableResult.NORMAL);
		this.populatePostCollisionActions(CollisionAttackResult.ExecutableResult.GLANCING);
		this.populatePostCollisionActions(CollisionAttackResult.ExecutableResult.RESISTED);
	}
	private void populatePostCollisionActions(CollisionAttackResult.ExecutableResult result) {
		AbstractParsedAction targetAction;
		Identifier targetActionID = this.DEFINITION.getPostCollisionActions(result);
		if(targetActionID == null) targetAction = null;
		else targetAction = Objects.requireNonNull(RegistryManager.ACTIONS.get(targetActionID),
				"Collision attack type " + this.ID + " transitions into action " + targetActionID
				+ " when a collision occurs with result " + result + ", however that action isn't registered!!!");
		this.POST_COLLISION_ACTIONS.put(result, targetAction);
	}

	public Vec3d moveHook(MarioServerPlayerData data, Vec3d movement) {
		ServerPlayerEntity mario = data.getMario();

		List<Entity> possibleTargets = mario.getWorld().getOtherEntities(mario, this.DEFINITION.tweakMarioBoundingBox(data, mario.getBoundingBox()).stretch(movement));
		possibleTargets.removeIf(entity -> !entity.isAlive());
		this.DEFINITION.filterPotentialTargets(possibleTargets, mario, movement);

		if(possibleTargets.isEmpty()) return movement;

		Vec3d targetPos = hitEntitiesAndGetTargetPos(data, possibleTargets, mario.getPos().add(movement));

		if(targetPos == null) return movement;
		else return targetPos.subtract(mario.getPos());
	}

	private void registerCollidedEntity(EnumMap<CollisionAttackResult.ExecutableResult, Set<Entity>> collidedEntities, Entity entity, CollisionAttackResult.ExecutableResult result) {
		collidedEntities.putIfAbsent(result, new HashSet<>());
		collidedEntities.get(result).add(entity);
	}

	public Vec3d hitEntitiesAndGetTargetPos(MarioServerPlayerData data, List<Entity> entities, @Nullable Vec3d goingToPos) {
		ServerPlayerEntity mario = data.getMario();
		ItemStack collisionEquipment = mario.getEquippedStack(this.USE_EQUIPMENT_SLOT);
		FloatFloatImmutablePair equipmentArmor = ItemStackArmorReader.read(collisionEquipment, this.USE_EQUIPMENT_SLOT);
		float collisionDamageAmount = this.DEFINITION.calculateDamage(data, collisionEquipment, equipmentArmor.leftFloat(), equipmentArmor.rightFloat());
		float collisionDamagePiercing = Math.min(collisionDamageAmount, this.DEFINITION.calculatePiercing(data, collisionEquipment, equipmentArmor.leftFloat(), equipmentArmor.rightFloat()));
		DamageSource collisionDamageSource = new CollisionAttackDamageSource(mario.getServerWorld(), this.DAMAGE_TYPE, mario, collisionDamagePiercing, collisionEquipment);

		EnumMap<CollisionAttackResult.ExecutableResult, Set<Entity>> collidedEntities = new EnumMap<>(CollisionAttackResult.ExecutableResult.class);
		boolean canMount = this.MOUNTING && !mario.isSneaking();
		for(Entity target : entities) {
			CollisionAttackResult result = ((CollisionAttackable) target).mqm$processCollisionAttack(data, canMount, collisionDamageAmount, collisionDamageSource);
			if(result == CollisionAttackResult.PAINFUL) {
				result = switch(this.PAINFUL_COLLISION_RESPONSE) {
					case INJURY -> CollisionAttackResult.PAINFUL;
					case MUTUALLY_HARMLESS -> CollisionAttackResult.GLANCING;
					case IMMUNE -> {
						if(target.damage(collisionDamageSource, collisionDamageAmount)) {
							mario.onAttacking(target);
							yield CollisionAttackResult.NORMAL;
						}
						else yield CollisionAttackResult.RESISTED;
					}
				};
			}

			switch(result) {
				case MOUNT -> {
					canMount = false;
					registerCollidedEntity(collidedEntities, target, CollisionAttackResult.ExecutableResult.MOUNT);
				}
				case PAINFUL -> registerCollidedEntity(collidedEntities, target, CollisionAttackResult.ExecutableResult.PAINFUL);
				case NORMAL -> registerCollidedEntity(collidedEntities, target, CollisionAttackResult.ExecutableResult.NORMAL);
				case GLANCING -> registerCollidedEntity(collidedEntities, target, CollisionAttackResult.ExecutableResult.GLANCING);
				case RESISTED -> registerCollidedEntity(collidedEntities, target, CollisionAttackResult.ExecutableResult.RESISTED);
			}
		}

		boolean affectingMario = true;
		boolean canHurtMario = true;
		@Nullable Vec3d targetPos = null;
		for (Map.Entry<CollisionAttackResult.ExecutableResult, Set<Entity>> executableResultSetEntry : collidedEntities.entrySet()) {
			CollisionAttackResult.ExecutableResult currentCollisionResult = executableResultSetEntry.getKey();
			for(Entity affectEntity : executableResultSetEntry.getValue()) {
				Vec3d providedTargetPos = this.executeServerAndNetwork(data, affectEntity, currentCollisionResult, goingToPos, affectingMario);
				if(affectingMario) {
					affectingMario = false;
					targetPos = providedTargetPos;
				}
				if(currentCollisionResult == CollisionAttackResult.ExecutableResult.PAINFUL && canHurtMario) {
					canHurtMario = false;
					mario.damage(mario.getDamageSources().thorns(affectEntity), 4);
				}
			}
		}

		return targetPos;
	}

	public void transitionAction(MarioPlayerData data, CollisionAttackResult.ExecutableResult result) {
		AbstractParsedAction targetAction = this.POST_COLLISION_ACTIONS.get(result);
		if(targetAction != null) data.setActionTransitionless(targetAction);
	}

	public Vec3d executeServerAndNetwork(
			MarioServerPlayerData data,
			Entity target,
			CollisionAttackResult.ExecutableResult result,
			Vec3d targetPos,
			boolean affectMario
	) {
		this.DEFINITION.executeServer(data, data.getMario().getEquippedStack(this.USE_EQUIPMENT_SLOT), target, result, affectMario);
		if(affectMario) this.transitionAction(data, result);

		MarioPackets.stompS2C(data.getMario(), this, target, result, affectMario);
		return this.executeTravellersAndGetTargetPos(data, target, result, targetPos, affectMario);
	}

	public Vec3d executeTravellersAndGetTargetPos(
			MarioMoveableData data,
			Entity target,
			CollisionAttackResult.ExecutableResult result,
			Vec3d targetPos,
			boolean affectMario
	) {
		Vec3d value = this.DEFINITION.executeTravellersAndModifyTargetPos(data, data.getMario().getEquippedStack(this.USE_EQUIPMENT_SLOT), target, result, targetPos, affectMario);
		data.applyModifiedVelocity();
		return value;
	}

	public void executeClients(
			IMarioClientData data,
			Entity target,
			CollisionAttackResult.ExecutableResult result,
			boolean affectMario,
			long seed
	) {
		this.DEFINITION.executeClients(data, data.getMario().getEquippedStack(this.USE_EQUIPMENT_SLOT), target, result, affectMario, seed);
	}
}

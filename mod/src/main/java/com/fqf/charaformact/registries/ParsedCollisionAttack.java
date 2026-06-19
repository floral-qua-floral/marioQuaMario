package com.fqf.charaformact.registries;

import com.fqf.charaformact.util.DelayedRemovable;
import com.fqf.charaformact_api.definitions.CollisionAttackTypeDefinition;
import com.fqf.charaformact_api.interfaces.CollisionAttackResult;
import com.fqf.charaformact_api.interfaces.CollisionAttackable;
import com.fqf.charaformact.cfadata.*;
import com.fqf.charaformact.packets.CfaPackets;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact.util.ItemStackArmorReader;
import com.fqf.charaformact.util.CollisionAttackDamageSource;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import it.unimi.dsi.fastutil.floats.FloatFloatPair;
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

public class ParsedCollisionAttack extends ParsedCfaThing {
	private final CollisionAttackTypeDefinition DEFINITION;

	private final boolean MOUNTING;
	private final CollisionAttackTypeDefinition.PainfulCollisionResponse PAINFUL_COLLISION_RESPONSE;
	private final @Nullable EquipmentSlot USE_EQUIPMENT_SLOT;
	private final @NotNull RegistryKey<DamageType> DAMAGE_TYPE;
	private final EnumMap<CollisionAttackResult.ExecutableResult, @Nullable AbstractParsedAction> POST_COLLISION_ACTIONS;

	public ParsedCollisionAttack(@NotNull CollisionAttackTypeDefinition definition) {
		super(definition.getID());

		this.DEFINITION = definition;

		this.MOUNTING = definition.attemptsMounting();
		this.PAINFUL_COLLISION_RESPONSE = definition.definePainfulCollisionResponse();
		this.USE_EQUIPMENT_SLOT = definition.defineEquipmentSlot();
		this.DAMAGE_TYPE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, definition.defineDamageType());
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
		Identifier targetActionID = this.DEFINITION.definePostCollisionActions(result);
		if(targetActionID == null) targetAction = null;
		else targetAction = Objects.requireNonNull(RegistryManager.ACTIONS.get(targetActionID),
				"Collision attack type " + this.ID + " transitions into action " + targetActionID
				+ " when a collision occurs with result " + result + ", however that action isn't registered!!!");
		this.POST_COLLISION_ACTIONS.put(result, targetAction);
	}

	public Vec3d moveHook(CfaServerPlayerData data, Vec3d movement) {
		ServerPlayerEntity player = data.getPlayer();

		List<Entity> possibleTargets = player.getWorld().getOtherEntities(player, this.DEFINITION.mutatePlayerBoundingBox(data, player.getBoundingBox()).stretch(movement));
		possibleTargets.removeIf(entity -> !entity.isAlive());
		this.DEFINITION.filterPotentialTargets(possibleTargets, player, movement);

		if(possibleTargets.isEmpty()) return movement;

		Vec3d targetPos = hitEntitiesAndGetTargetPos(data, possibleTargets, player.getPos().add(movement));

		if(targetPos == null) return movement;
		else return targetPos.subtract(player.getPos());
	}

	private void registerCollidedEntity(EnumMap<CollisionAttackResult.ExecutableResult, Set<Entity>> collidedEntities, Entity entity, CollisionAttackResult.ExecutableResult result) {
		collidedEntities.putIfAbsent(result, new HashSet<>());
		collidedEntities.get(result).add(entity);
	}

	public Vec3d hitEntitiesAndGetTargetPos(CfaServerPlayerData data, List<Entity> entities, @Nullable Vec3d goingToPos) {
		ServerPlayerEntity player = data.getPlayer();
		ItemStack collisionEquipment = player.getEquippedStack(this.USE_EQUIPMENT_SLOT);
		FloatFloatPair equipmentArmor = ItemStackArmorReader.getArmorAndToughness(collisionEquipment, this.USE_EQUIPMENT_SLOT);
		float collisionDamageAmount = this.DEFINITION.calculateDamage(data, collisionEquipment, equipmentArmor.leftFloat(), equipmentArmor.rightFloat());
		float collisionDamagePiercing = Math.min(collisionDamageAmount, this.DEFINITION.calculatePiercing(data, collisionEquipment, equipmentArmor.leftFloat(), equipmentArmor.rightFloat()));
		DamageSource collisionDamageSource = new CollisionAttackDamageSource(player.getServerWorld(), this.DAMAGE_TYPE, player, collisionDamagePiercing, collisionEquipment);

		EnumMap<CollisionAttackResult.ExecutableResult, Set<Entity>> collidedEntities = new EnumMap<>(CollisionAttackResult.ExecutableResult.class);
		boolean canMount = this.MOUNTING && !player.isSneaking();

		try {
			for(Entity target : entities) {
				((DelayedRemovable) target).cfa$startDelayedRemoval();

				CollisionAttackResult result = ((CollisionAttackable) target).cfa$processCollisionAttack(data, canMount, collisionDamageAmount, collisionDamageSource);
				if(result == CollisionAttackResult.PAINFUL) {
					result = switch(this.PAINFUL_COLLISION_RESPONSE) {
						case INJURY -> CollisionAttackResult.PAINFUL;
						case MUTUALLY_HARMLESS -> CollisionAttackResult.GLANCING;
						case IMMUNE -> {
							if(target.damage(collisionDamageSource, collisionDamageAmount)) {
								player.onAttacking(target);
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

			boolean affectingAttacker = true;
			boolean canHurtAttacker = true;
			@Nullable Vec3d targetPos = null;
			for (Map.Entry<CollisionAttackResult.ExecutableResult, Set<Entity>> executableResultSetEntry : collidedEntities.entrySet()) {
				CollisionAttackResult.ExecutableResult currentCollisionResult = executableResultSetEntry.getKey();
				for(Entity affectEntity : executableResultSetEntry.getValue()) {
					Vec3d providedTargetPos = this.executeServerAndNetwork(data, affectEntity, currentCollisionResult, goingToPos, affectingAttacker);
					if(affectingAttacker) {
						affectingAttacker = false;
						targetPos = providedTargetPos;
					}
					if(currentCollisionResult == CollisionAttackResult.ExecutableResult.PAINFUL && canHurtAttacker) {
						canHurtAttacker = false;
						player.damage(player.getDamageSources().thorns(affectEntity), 4);
					}
				}
			}

			return targetPos;
		}
		finally {
			// This has to be in a finally block to make sure that even if there's an exception, we don't leave the
			// target in a Delayed Removal state!! It'll probably crash anyways but just to be safe!
			for(Entity target : entities) ((DelayedRemovable) target).cfa$finishDelayedRemoval();
		}
	}

	public void transitionAction(CfaPlayerData data, CollisionAttackResult.ExecutableResult result) {
		AbstractParsedAction targetAction = this.POST_COLLISION_ACTIONS.get(result);
		if(targetAction != null) data.setActionTransitionless(targetAction);
	}

	public Vec3d executeServerAndNetwork(
			CfaServerPlayerData data,
			Entity target,
			CollisionAttackResult.ExecutableResult result,
			Vec3d targetPos,
			boolean affectAttacker
	) {
		this.DEFINITION.executeServer(data, data.getPlayer().getEquippedStack(this.USE_EQUIPMENT_SLOT), target, result, affectAttacker);
		if(affectAttacker) this.transitionAction(data, result);

		CfaPackets.collisionAttackS2C(data.getPlayer(), this, target, result, affectAttacker);
		return this.executeTravellersAndGetTargetPos(data, target, result, targetPos, affectAttacker);
	}

	public Vec3d executeTravellersAndGetTargetPos(
			CfaMoveableData data,
			Entity target,
			CollisionAttackResult.ExecutableResult result,
			Vec3d targetPos,
			boolean affectAttacker
	) {
		Vec3d value = this.DEFINITION.executeTravellersAndModifyTargetPos(data, data.getPlayer().getEquippedStack(this.USE_EQUIPMENT_SLOT), target, result, targetPos, affectAttacker);
		data.applyModifiedVelocity();
		return value;
	}

	public void executeClients(
			CfaClientData data,
			Entity target,
			CollisionAttackResult.ExecutableResult result,
			boolean affectAttacker,
			long seed
	) {
		this.DEFINITION.executeClients(data, data.getPlayer().getEquippedStack(this.USE_EQUIPMENT_SLOT), target, result, affectAttacker, seed);
	}
}

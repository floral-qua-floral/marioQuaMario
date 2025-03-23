package com.fqf.mario_qua_mario.registries;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.definitions.StompTypeDefinition;
import com.fqf.mario_qua_mario.interfaces.StompResult;
import com.fqf.mario_qua_mario.interfaces.Stompable;
import com.fqf.mario_qua_mario.mariodata.*;
import com.fqf.mario_qua_mario.packets.MarioPackets;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.util.ItemStackArmorReader;
import com.fqf.mario_qua_mario.util.StompDamageSource;
import it.unimi.dsi.fastutil.floats.FloatFloatImmutablePair;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ParsedStompType extends ParsedMarioThing {
	private final StompTypeDefinition DEFINITION;

	private final boolean MOUNTING;
	private final StompTypeDefinition.PainfulStompResponse PAINFUL_STOMP_RESPONSE;
	private final @Nullable EquipmentSlot USE_EQUIPMENT_SLOT;
	private final @NotNull RegistryKey<DamageType> DAMAGE_TYPE;
	private final EnumMap<StompResult.ExecutableResult, @Nullable AbstractParsedAction> POST_STOMP_ACTIONS;

	public ParsedStompType(@NotNull StompTypeDefinition definition) {
		super(definition.getID());

		this.DEFINITION = definition;

		this.MOUNTING = definition.shouldAttemptMounting();
		this.PAINFUL_STOMP_RESPONSE = definition.painfulStompResponse();
		this.USE_EQUIPMENT_SLOT = definition.getEquipmentSlot();
		this.DAMAGE_TYPE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, definition.getDamageType());
		this.POST_STOMP_ACTIONS = new EnumMap<>(StompResult.ExecutableResult.class);
	}

	public void populatePostStompActions() {
		this.populatePostStompActions(StompResult.ExecutableResult.MOUNT);
		this.populatePostStompActions(StompResult.ExecutableResult.PAINFUL);
		this.populatePostStompActions(StompResult.ExecutableResult.NORMAL);
		this.populatePostStompActions(StompResult.ExecutableResult.GLANCING);
		this.populatePostStompActions(StompResult.ExecutableResult.RESISTED);
	}
	private void populatePostStompActions(StompResult.ExecutableResult result) {
		AbstractParsedAction targetAction;
		Identifier targetActionID = this.DEFINITION.getPostStompActions(result);
		if(targetActionID == null) targetAction = null;
		else targetAction = Objects.requireNonNull(RegistryManager.ACTIONS.get(targetActionID),
				"Stomp type " + this.ID + " transitions into action " + targetActionID
				+ " when a stomp occurs with result " + result + ", however that action isn't registered!!!");
		this.POST_STOMP_ACTIONS.put(result, targetAction);
	}

	public Vec3d moveHook(MarioServerPlayerData data, Vec3d movement) {
		ServerPlayerEntity mario = data.getMario();

		List<Entity> possibleTargets = mario.getWorld().getOtherEntities(mario, this.DEFINITION.tweakMarioBoundingBox(data, mario.getBoundingBox()).stretch(movement));
		this.DEFINITION.filterPotentialTargets(possibleTargets, mario, movement);

		if(possibleTargets.isEmpty()) return movement;

		Vec3d targetPos = stompEntitiesAndGetTargetPos(data, possibleTargets, mario.getPos().add(movement));

		if(targetPos == null) return movement;
		else return targetPos.subtract(mario.getPos());
	}

	private void registerStompedEntity(EnumMap<StompResult.ExecutableResult, Set<Entity>> stompedEntities, Entity entity, StompResult.ExecutableResult result) {
		stompedEntities.putIfAbsent(result, new HashSet<>());
		stompedEntities.get(result).add(entity);
	}

	public Vec3d stompEntitiesAndGetTargetPos(MarioServerPlayerData data, List<Entity> entities, @Nullable Vec3d goingToPos) {
		ServerPlayerEntity mario = data.getMario();
		ItemStack stompEquipment = mario.getEquippedStack(this.USE_EQUIPMENT_SLOT);
		FloatFloatImmutablePair equipmentArmor = ItemStackArmorReader.read(stompEquipment, this.USE_EQUIPMENT_SLOT);
		float stompDamageAmount = this.DEFINITION.calculateDamage(data, stompEquipment, equipmentArmor.leftFloat(), equipmentArmor.rightFloat());
		float stompDamagePiercing = Math.min(stompDamageAmount, this.DEFINITION.calculatePiercing(data, stompEquipment, equipmentArmor.leftFloat(), equipmentArmor.rightFloat()));
		DamageSource stompDamageSource = new StompDamageSource(mario.getServerWorld(), this.DAMAGE_TYPE, mario, stompDamagePiercing, stompEquipment);

		EnumMap<StompResult.ExecutableResult, Set<Entity>> stompedEntities = new EnumMap<>(StompResult.ExecutableResult.class);
		boolean canMount = this.MOUNTING;
		for(Entity target : entities) {
			StompResult result = ((Stompable) target).mqm$stomp(data, canMount, stompDamageAmount, stompDamageSource);
			if(result == StompResult.PAINFUL) {
				result = switch(this.PAINFUL_STOMP_RESPONSE) {
					case INJURY -> StompResult.PAINFUL;
					case BOUNCE -> StompResult.GLANCING;
					case STOMP -> target.damage(stompDamageSource, stompDamageAmount) ? StompResult.NORMAL : StompResult.RESISTED;
				};
			}

			switch(result) {
				case MOUNT -> {
					canMount = false;
					registerStompedEntity(stompedEntities, target, StompResult.ExecutableResult.MOUNT);
				}
				case PAINFUL -> registerStompedEntity(stompedEntities, target, StompResult.ExecutableResult.PAINFUL);
				case NORMAL -> registerStompedEntity(stompedEntities, target, StompResult.ExecutableResult.NORMAL);
				case GLANCING -> registerStompedEntity(stompedEntities, target, StompResult.ExecutableResult.GLANCING);
				case RESISTED -> registerStompedEntity(stompedEntities, target, StompResult.ExecutableResult.RESISTED);
			}
		}

		boolean affectingMario = true;
		boolean canHurtMario = true;
		@Nullable Vec3d targetPos = null;
		for (Map.Entry<StompResult.ExecutableResult, Set<Entity>> executableResultSetEntry : stompedEntities.entrySet()) {
			StompResult.ExecutableResult currentStompResult = executableResultSetEntry.getKey();
			for(Entity affectEntity : executableResultSetEntry.getValue()) {
				Vec3d providedTargetPos = this.executeServerAndNetwork(data, affectEntity, currentStompResult, goingToPos, affectingMario);
				if(affectingMario) {
					affectingMario = false;
					targetPos = providedTargetPos;
				}
				if(currentStompResult == StompResult.ExecutableResult.PAINFUL && canHurtMario) {
					canHurtMario = false;
					mario.damage(mario.getDamageSources().thorns(affectEntity), 4);
				}
			}
		}

		return targetPos;
	}

	public void transitionAction(MarioPlayerData data, StompResult.ExecutableResult result) {
		AbstractParsedAction targetAction = this.POST_STOMP_ACTIONS.get(result);
		if(targetAction != null) data.setActionTransitionless(targetAction);
	}

	public Vec3d executeServerAndNetwork(
			MarioServerPlayerData data,
			Entity target,
			StompResult.ExecutableResult result,
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
			StompResult.ExecutableResult result,
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
			StompResult.ExecutableResult result,
			boolean affectMario,
			long seed
	) {
		this.DEFINITION.executeClients(data, data.getMario().getEquippedStack(this.USE_EQUIPMENT_SLOT), target, result, affectMario, seed);
	}
}

package com.floralquafloral.registries.stomp;

import com.floralquafloral.mariodata.MarioClientSideDataImplementation;
import com.floralquafloral.mariodata.moveable.MarioMoveableData;
import com.floralquafloral.mariodata.moveable.MarioServerData;
import com.floralquafloral.registries.RegistryManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.RandomSeed;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ParsedStomp {
	public final Identifier ID;
	private final StompDefinition DEFINITION;

	private final boolean MUST_FALL_ON_TARGET;
	private final StompDefinition.PainfulStompResponse PAINFUL_STOMP_RESPONSE;
	private final boolean SHOULD_ATTEMPT_MOUNTING;
	private final boolean HITS_NONLIVING_ENTITIES;

	private final RegistryKey<DamageType> DAMAGE_TYPE;
	private final SoundEvent SOUND_EVENT;
	public final Identifier POST_STOMP_ACTION;

	public ParsedStomp(StompDefinition definition) {
		this.ID = definition.getID();
		this.DEFINITION = definition;

		this.MUST_FALL_ON_TARGET = definition.mustFallOnTarget();
		this.PAINFUL_STOMP_RESPONSE = definition.getPainfulStompResponse();
		this.SHOULD_ATTEMPT_MOUNTING = definition.shouldAttemptMounting();
		this.HITS_NONLIVING_ENTITIES = definition.canHitNonLiving();

		this.DAMAGE_TYPE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, definition.getDamageType());
		this.SOUND_EVENT = definition.getSoundEvent();
		this.POST_STOMP_ACTION = definition.getPostStompAction();
	}

	public boolean executeServer(MarioServerData data, Entity target, boolean affectMario, boolean harmless, long seed) {
		ServerPlayerEntity mario = data.getMario();
//		DamageSource damageSource = makeDamageSource(mario.getServerWorld(), this.DAMAGE_TYPE, mario);
		StompDamageSource stompDamageSource = new StompDamageSource(mario.getServerWorld(), this.DAMAGE_TYPE, mario);
		boolean useLegsItem = stompDamageSource.isIn(StompHandler.USES_LEGS_ITEM_TAG);

		ItemStack attackingArmor = mario.getEquippedStack(useLegsItem ? EquipmentSlot.LEGS : EquipmentSlot.FEET);

		float armor = 0.0F;
		float toughness = 0.0F;
		boolean attributeModifierFound = false;

		for(AttributeModifiersComponent.Entry entry : attackingArmor.getOrDefault(
				DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT).modifiers()) {
			attributeModifierFound = true;
			if(
					entry.modifier().operation() == EntityAttributeModifier.Operation.ADD_VALUE &&
					(entry.slot() == AttributeModifierSlot.ANY
					|| entry.slot() == AttributeModifierSlot.ARMOR
					|| entry.slot() == (useLegsItem ? AttributeModifierSlot.LEGS : AttributeModifierSlot.FEET))
			) {
				if (entry.attribute().value().equals(EntityAttributes.GENERIC_ARMOR.value()))
					armor += (float) entry.modifier().value();
				else if (entry.attribute().value().equals(EntityAttributes.GENERIC_ARMOR_TOUGHNESS.value()))
					toughness += (float) entry.modifier().value();
			}
		}

		if(!attributeModifierFound && attackingArmor.getItem() instanceof ArmorItem trueArmor) {
			armor = trueArmor.getProtection();
			toughness = trueArmor.getToughness();
		}

		float damage = this.DEFINITION.calculateDamage(data, mario, attackingArmor, armor, target);
		stompDamageSource.piercing = 2.0F * toughness;

		if(target.damage(stompDamageSource, Math.max(1.0F, damage - 0.6F * stompDamageSource.piercing))) {
			if(affectMario) {
				mario.fallDistance = 0;
				this.DEFINITION.executeTravellers(data, target, harmless);
				if(this.POST_STOMP_ACTION != null)
					data.setActionTransitionless(Objects.requireNonNull(RegistryManager.ACTIONS.get(this.POST_STOMP_ACTION)));
				StompHandler.networkStomp(data.getMario(), target, this, harmless, seed);
			}
			return true;
		}
		return false;
	}
	public void executeClient(MarioClientSideDataImplementation data, boolean isSelf, Entity target, boolean harmless, long seed) {
		data.getMario().fallDistance = 0;
		if(this.SOUND_EVENT != null) {
			data.playSoundEvent(
					this.SOUND_EVENT,
					SoundCategory.PLAYERS,
					data.getMario().getX(),
					target.getY() + target.getHeight(),
					data.getMario().getZ(),
					1.0F,
					1.0F,
					seed
			);
		}

		this.DEFINITION.executeClients(data, isSelf, target, harmless, seed);
		if(data instanceof MarioMoveableData moveableData) {
			moveableData.getTimers().jumpCapped = false;
			this.DEFINITION.executeTravellers(moveableData, target, harmless);
			if(this.POST_STOMP_ACTION != null)
				moveableData.setActionTransitionless(Objects.requireNonNull(RegistryManager.ACTIONS.get(this.POST_STOMP_ACTION)));
			moveableData.applyModifiedVelocity();
		}

//		if(data.getMario().isMainPlayer()) AirborneActionDefinition.jumpCapped = false;
	}

	public boolean attempt(MarioServerData data, Vec3d movement) {
		ServerPlayerEntity mario = data.getMario();
		List<Entity> targets = mario.getWorld().getOtherEntities(mario, mario.getBoundingBox().stretch(movement.multiply(1, 2, 1)));

		boolean affectMario = true;
		long seed = RandomSeed.getSeed();
		for(Entity target : targets) {
			if(attemptOnTarget(mario, data, target, affectMario, seed)) affectMario = false;
		}
		return false;
	}

	private boolean attemptOnTarget(ServerPlayerEntity mario, MarioServerData data, Entity target, boolean affectMario, long seed) {
		if(target.getType().isIn(StompHandler.UNSTOMPABLE_TAG)) return false;

		if(this.MUST_FALL_ON_TARGET && target.getY() + target.getHeight() > mario.getY()) return false;

		if(this.SHOULD_ATTEMPT_MOUNTING) {
			if((target instanceof Saddleable saddleableTarget && saddleableTarget.isSaddled())
					|| target instanceof VehicleEntity) {
				if(mario.startRiding(target)) return true;
			}
		}

		if(!(target instanceof LivingEntity livingTarget && !livingTarget.isDead())) return false;
		if(!this.DEFINITION.canStompTarget(data, target)) return false;
		livingTarget.isDead();

		boolean targetHurtsToStomp = target.getType().isIn(StompHandler.HURTS_TO_STOMP_TAG);
		boolean harmless = false;
		if(targetHurtsToStomp) {
			if(this.PAINFUL_STOMP_RESPONSE == StompDefinition.PainfulStompResponse.INJURY) {
				// Hurt Mario
				mario.damage(makeDamageSource(mario.getServerWorld(), DamageTypes.THORNS, target), 2.8F);
				return false;
			}
			else if(this.PAINFUL_STOMP_RESPONSE == StompDefinition.PainfulStompResponse.BOUNCE)
				harmless = true;

		}

		return executeServer(data, target, affectMario, harmless, seed);
	}

	private static DamageSource makeDamageSource(ServerWorld world, RegistryKey<DamageType> key, Entity attacker) {
		return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key), attacker);
	}

	public static class StompDamageSource extends DamageSource {
		private float piercing;

		public StompDamageSource(ServerWorld world, RegistryKey<DamageType> key, @Nullable Entity attacker) {
			super(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key), attacker);
		}

		public float getPiercing() {
			return this.piercing;
		}
	}
}

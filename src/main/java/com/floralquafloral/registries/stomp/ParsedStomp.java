package com.floralquafloral.registries.stomp;

import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioDataPackets;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.moveable.MarioMoveableData;
import com.floralquafloral.mariodata.moveable.MarioServerData;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.util.ClientSoundPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.vehicle.VehicleEntity;
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

import java.util.List;

public class ParsedStomp {
	public final Identifier ID;
	private final StompDefinition DEFINITION;

	private final boolean MUST_FALL_ON_TARGET;
	private final StompDefinition.PainfulStompResponse PAINFUL_STOMP_RESPONSE;
	private final boolean SHOULD_ATTEMPT_MOUNTING;
	private final boolean HITS_NONLIVING_ENTITIES;

	private final RegistryKey<DamageType> DAMAGE_TYPE;
	private final SoundEvent SOUND_EVENT;
	private final Identifier POST_STOMP_ACTION;

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

	public void executeServer(MarioServerData data, Entity target, boolean harmless, long seed) {
		ServerPlayerEntity mario = data.getMario();
		DamageSource damageSource = makeDamageSource(mario.getServerWorld(), this.DAMAGE_TYPE, mario);
		boolean useLegsItem = damageSource.isIn(StompHandler.USES_LEGS_ITEM_TAG);

		ItemStack attackingArmor = mario.getEquippedStack(useLegsItem ? EquipmentSlot.LEGS : EquipmentSlot.FEET);


		float damage = this.DEFINITION.calculateDamage(data, mario, attackingArmor, 0, 0, target);
//		for(RegistryEntry<Enchantment> ick : attackingArmor.getEnchantments().getEnchantments()) {
//			int level = attackingArmor.getEnchantments().getLevel(ick);
////			ick.
//		}

		target.damage(damageSource, damage);

		this.DEFINITION.executeTravellers(data, target, harmless);
	}
	public void executeClient(MarioClientSideData data, boolean isSelf, Entity target, boolean harmless, long seed) {
		if(this.SOUND_EVENT != null) {
			ClientSoundPlayer.playSound(
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
			this.DEFINITION.executeTravellers(moveableData, target, harmless);
			moveableData.applyModifiedVelocity();
		}
	}

	public boolean attempt(MarioServerData data, Vec3d movement) {
		ServerPlayerEntity mario = data.getMario();
		List<Entity> targets = mario.getWorld().getOtherEntities(mario, mario.getBoundingBox().stretch(movement));

		long seed = RandomSeed.getSeed();
		for(Entity target : targets) {
			if(attemptOnTarget(mario, data, target, seed)) return true;
		}
		return false;
	}

	private boolean attemptOnTarget(ServerPlayerEntity mario, MarioServerData data, Entity target, long seed) {
		if(target.getType().isIn(StompHandler.UNSTOMPABLE_TAG)) return false;

		if(this.MUST_FALL_ON_TARGET && target.getY() + target.getHeight() > mario.getY()) return false;

		if(this.SHOULD_ATTEMPT_MOUNTING) {
			if((target instanceof Saddleable saddleableTarget && saddleableTarget.isSaddled())
					|| target instanceof VehicleEntity) {
				if(mario.startRiding(target)) {
					MarioDataPackets.forceSetMarioAction(mario, RegistryManager.ACTIONS.get(Identifier.of("qua_mario:basic")));
					return true;
				}
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

		StompHandler.networkStomp(mario, target, this, harmless, seed);
		executeServer(data, target, true, seed);

		return true;
	}

	private static DamageSource makeDamageSource(ServerWorld world, RegistryKey<DamageType> key, Entity attacker) {
		return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key), attacker);
	}
}

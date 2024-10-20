package com.floralquafloral.registries.stomp;

import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioPlayerData;
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
import net.minecraft.util.Identifier;
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
	private final Identifier POST_STOMP_ACTION;

	public ParsedStomp(StompDefinition definition) {
		this.ID = definition.getID();
		this.DEFINITION = definition;

		this.MUST_FALL_ON_TARGET = definition.mustFallOnTarget();
		this.PAINFUL_STOMP_RESPONSE = definition.getPainfulStompResponse();
		this.SHOULD_ATTEMPT_MOUNTING = definition.shouldAttemptMounting();
		this.HITS_NONLIVING_ENTITIES = definition.canHitNonLiving();

		this.DAMAGE_TYPE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, definition.getDamageType());
		this.POST_STOMP_ACTION = definition.getPostStompAction();
	}

	public void executeServer(MarioPlayerData data, Entity target, boolean harmless, long seed) {
		this.DEFINITION.executeServer(data, target, harmless, seed);
	}
	public void executeClient(MarioPlayerData data, boolean isSelf, Entity target, boolean harmless, long seed) {
		this.DEFINITION.executeClient(data, isSelf, target, harmless, seed);
	}

	public void attempt(MarioData data) {
		ServerPlayerEntity mario = (ServerPlayerEntity) data.getMario();
		double yVel = data.getYVel();
		if(this.MUST_FALL_ON_TARGET && yVel > 0) return;

		List<Entity> targets = mario.getWorld().getOtherEntities(mario, mario.getBoundingBox());

		boolean enteredStompAction = false;
		long seed = RandomSeed.getSeed();
		for(Entity target : targets) {

			if(target.getType().isIn(StompHandler.UNSTOMPABLE_TAG)) continue;

			if(this.MUST_FALL_ON_TARGET && target.getY() + target.getHeight() > mario.getHeight() - yVel) continue;


			if(this.SHOULD_ATTEMPT_MOUNTING) {
				if((target instanceof Saddleable saddleableTarget && saddleableTarget.isSaddled())
						|| target instanceof VehicleEntity) {
					if(mario.startRiding(target)) return;
				}
			}

			if(!(target instanceof LivingEntity)) continue;
			if(!this.DEFINITION.canStompTarget(data, target)) continue;

			boolean targetHurtsToStomp = target.getType().isIn(StompHandler.HURTS_TO_STOMP_TAG);
			if(this.PAINFUL_STOMP_RESPONSE == StompDefinition.PainfulStompResponse.INJURY && targetHurtsToStomp) {
				// Hurt Mario
				mario.damage(makeDamageSource(mario.getServerWorld(), DamageTypes.THORNS, target), 2.8F);
				return;
			}

			DamageSource damageSource = makeDamageSource(mario.getServerWorld(), this.DAMAGE_TYPE, mario);
			boolean useLegsItem = damageSource.isIn(StompHandler.USES_LEGS_ITEM_TAG);

			ItemStack attackingArmor = mario.getEquippedStack(useLegsItem ? EquipmentSlot.LEGS : EquipmentSlot.FEET);


			float damage = this.DEFINITION.calculateDamage(data, mario, attackingArmor, 0, 0, target);

			target.damage(damageSource, damage);

			boolean harmless = targetHurtsToStomp && this.PAINFUL_STOMP_RESPONSE == StompDefinition.PainfulStompResponse.BOUNCE;
			StompHandler.networkStomp(mario, target, this, harmless, seed);
			executeServer((MarioPlayerData) data, target, true, seed);

			return;
		}
	}

	private static DamageSource makeDamageSource(ServerWorld world, RegistryKey<DamageType> key, Entity attacker) {
		return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key), attacker);
	}
}

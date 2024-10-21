package com.floralquafloral.registries.stomp;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.registries.RegistryManager;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
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
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.world.World;

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
		MarioQuaMario.LOGGER.info("Parsing:"
				+ "\ndef damagetype: " + definition.getDamageType()
				+ "\ndamagetype: " + this.DAMAGE_TYPE
		);
		this.POST_STOMP_ACTION = definition.getPostStompAction();
	}

	public void executeServer(MarioPlayerData data, Entity target, boolean harmless, long seed) {
		ServerPlayerEntity mario = (ServerPlayerEntity) data.getMario();
		DamageSource damageSource = makeDamageSource(mario.getServerWorld(), this.DAMAGE_TYPE, mario);
		boolean useLegsItem = damageSource.isIn(StompHandler.USES_LEGS_ITEM_TAG);

		ItemStack attackingArmor = mario.getEquippedStack(useLegsItem ? EquipmentSlot.LEGS : EquipmentSlot.FEET);


		float damage = this.DEFINITION.calculateDamage(data, mario, attackingArmor, 0, 0, target);
//		for(RegistryEntry<Enchantment> ick : attackingArmor.getEnchantments().getEnchantments()) {
//			int level = attackingArmor.getEnchantments().getLevel(ick);
////			ick.
//		}

		target.damage(damageSource, damage);

		this.DEFINITION.executeServer(target.getWorld(), data, target, harmless, seed);
	}
	public void executeClient(World world, MarioPlayerData data, boolean isSelf, Entity target, boolean harmless, long seed) {
		this.DEFINITION.executeClient(world, data, isSelf, target, harmless, seed);
		data.applyModifiedVelocity();
	}

	public void attempt(MarioData data, Vec3d movement) {
		ServerPlayerEntity mario = (ServerPlayerEntity) data.getMario();
		List<Entity> targets = mario.getWorld().getOtherEntities(mario, mario.getBoundingBox().stretch(movement));

		long seed = RandomSeed.getSeed();
		for(Entity target : targets) {
			if(attemptOnTarget(mario, data, target, seed)) break;
		}
	}

	private boolean attemptOnTarget(ServerPlayerEntity mario, MarioData data, Entity target, long seed) {
		if(target.getType().isIn(StompHandler.UNSTOMPABLE_TAG)) return false;

		if(this.MUST_FALL_ON_TARGET && target.getY() + target.getHeight() > mario.getY()) return false;

		if(this.SHOULD_ATTEMPT_MOUNTING) {
			if((target instanceof Saddleable saddleableTarget && saddleableTarget.isSaddled())
					|| target instanceof VehicleEntity) {
				if(mario.startRiding(target)) return false;
			}
		}

		if(!(target instanceof LivingEntity livingTarget && !livingTarget.isDead())) return false;
		if(!this.DEFINITION.canStompTarget(data, target)) return false;
		livingTarget.isDead();

		boolean targetHurtsToStomp = target.getType().isIn(StompHandler.HURTS_TO_STOMP_TAG);
		MarioQuaMario.LOGGER.info("hurts:"
				+ "\ntarget type: " + target.getType()
				+ "\ntag: " + StompHandler.HURTS_TO_STOMP_TAG
				+ "\ninTag: " + target.getType().isIn(StompHandler.HURTS_TO_STOMP_TAG)
				+ "\ninVanillaTag: " + target.getType().isIn(EntityTypeTags.AQUATIC)
				+ "\nresponse: " + this.PAINFUL_STOMP_RESPONSE
		);
		if(this.PAINFUL_STOMP_RESPONSE == StompDefinition.PainfulStompResponse.INJURY && targetHurtsToStomp) {
			// Hurt Mario
			mario.damage(makeDamageSource(mario.getServerWorld(), DamageTypes.THORNS, target), 2.8F);
			return false;
		}



		boolean harmless = targetHurtsToStomp && this.PAINFUL_STOMP_RESPONSE == StompDefinition.PainfulStompResponse.BOUNCE;
		StompHandler.networkStomp(mario, target, this, harmless, seed);
		executeServer((MarioPlayerData) data, target, true, seed);

		return true;
	}

	private static DamageSource makeDamageSource(ServerWorld world, RegistryKey<DamageType> key, Entity attacker) {
		return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key), attacker);
	}
}

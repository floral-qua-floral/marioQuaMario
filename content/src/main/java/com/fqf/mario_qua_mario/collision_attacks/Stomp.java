package com.fqf.mario_qua_mario.collision_attacks;

import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.CollisionAttackTypeDefinition;
import com.fqf.charaformact_api.interfaces.CollisionAttackResult;
import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.charaformact_api.util.CfaTags;
import com.fqf.charaformact_api.util.StatCategory;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.BonkAir;
import com.fqf.mario_qua_mario.actions.airborne.StompBounce;
import com.fqf.mario_qua_mario.util.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static com.fqf.charaformact_api.util.StatCategory.DAMAGE;
import static com.fqf.charaformact_api.util.StatCategory.COLLISION_ATTACK;

public class Stomp implements CollisionAttackTypeDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("stomp");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public boolean shouldAttemptMounting() {
		return true;
	}
	@Override public @NotNull CollisionAttackTypeDefinition.PainfulCollisionResponse painfulCollisionResponse() {
		return PainfulCollisionResponse.INJURY;
	}
	@Override public @Nullable EquipmentSlot getEquipmentSlot() {
		return EquipmentSlot.FEET;
	}
	@Override public @NotNull Identifier getDamageType() {
		return MarioQuaMario.makeResID("stomp");
	}
	@Override public @Nullable Identifier getPostCollisionActions(CollisionAttackResult.ExecutableResult result) {
		return switch(result) {
			case PAINFUL -> BonkAir.ID;
			case NORMAL, GLANCING, RESISTED -> StompBounce.ID;
			default -> null;
		};
	}

	@Override public Box tweakPlayerBoundingBox(CfaData data, Box box) {
		return box.stretch(0, -0.05, 0);
	}

	public static boolean collidingFromTop(Entity entity, ServerPlayerEntity mario, double marioY, Vec3d motion, boolean allowRisingStomp) {
		double entityHeadY = entity.getY() + entity.getHeight() - 0.026;
		double marioDestinationY = marioY + motion.y;

		return (marioY > entityHeadY && marioDestinationY < entityHeadY) || (
				allowRisingStomp
				&& mario.getWorld().getGameRules().getBoolean(MQMGamerules.ALLOW_RISING_STOMPS)
				&& (marioY < entityHeadY && marioDestinationY > entityHeadY)
		);
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override
	public void filterPotentialTargets(List<Entity> potentialTargets, ServerPlayerEntity attacker, Vec3d motion) {
		potentialTargets.removeIf(entity -> {
			// Mario cannot stomp on entities that are solid to him, unless they're vehicles (boats).
			// This should prevent stomps on Shulkers and also modded platform entities.
			if(attacker.collidesWith(entity) && !(entity instanceof VehicleEntity)) return true;

			// Mario must be able to hit the entity, unless it's a trident.
			if(!entity.canHit() && !(entity instanceof TridentEntity)) return true;

			boolean canRisingStomp;
			if(entity.getType().isIn(CfaTags.HARMS_COLLISION_ATTACKERS)) // No rising stomp on pointy things!
				canRisingStomp = false;
			else if(entity instanceof Monster) // Mario can do a rising stomp on monsters
				canRisingStomp = true;
			else if(entity.getType().isIn(MQMTags.RISING_STOMPABLE_NONMONSTERS)) // Mario can do a rising stomp on entities in the tag
				canRisingStomp = true;
			else // Against all other things - animals, vehicles, etc - Mario cannot do a rising stomp, and must be falling.
				canRisingStomp = false;

			// Mario must be either entering or exiting the top of the entity's hitbox.
			return !collidingFromTop(entity, attacker, attacker.getY(), motion, canRisingStomp);
		});
	}

	public static final CfaStat BASE_DAMAGE = new CfaStat(4.5, COLLISION_ATTACK, DAMAGE);

	public static final Identifier PULVERIZING_ID = MarioQuaMario.makeResID("pulverizing");
	public static final Identifier BOUNDING_ID = MarioQuaMario.makeResID("bounding");

	public static int getPulverizingLevel(ItemStack item, CfaData data) {
		return getEnchantmentLevel(item, data.getPlayer().getWorld(), PULVERIZING_ID);
	}

	public static int getEnchantmentLevel(ItemStack item, World world, Identifier enchantmentID) {
		Optional<RegistryEntry.Reference<Enchantment>> pulverizingEntry =
				world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(enchantmentID);
		assert pulverizingEntry.isPresent();
		return item.getEnchantments().getLevel(pulverizingEntry.get());
	}

	@Override
	public float calculateDamage(CfaData data, ItemStack equipment, float equipmentArmor, float equipmentToughness) {
		int pulverizingLevel = Stomp.getPulverizingLevel(equipment, data);
		float pulverizingDamage = pulverizingLevel * 0.5F + (pulverizingLevel > 0 ? 0.5F : 0);
		return (float) (new CfaStat(4.5F + equipmentArmor * 2.25F, DAMAGE, COLLISION_ATTACK).get(data)) + pulverizingDamage;
	}

	@Override
	public float calculatePiercing(CfaData data, ItemStack equipment, float equipmentArmor, float equipmentToughness) {
		return equipmentToughness * 2;
	}

	@Override
	public void executeServer(CfaAuthoritativeData data, ItemStack equipment, Entity target, CollisionAttackResult.ExecutableResult result, boolean affectAttacker) {
		if(affectAttacker && data.hasPower(Powers.STOMP_GUARD)) {
			data.retrieveStateData(MarioVars.class).stompGuardMinHeight = target.getY() + target.getHeight() + 0.15;
			data.retrieveStateData(MarioVars.class).stompGuardRemainingTicks = 4;
		}
	}

	public static Vec3d bounceMarioAndGetTargetPos(CfaTravelData data, ItemStack equipment, Entity target, CollisionAttackResult.ExecutableResult result, Vec3d movingToPos, boolean affectMario) {
		return switch(result) {
			case PAINFUL -> null;
			case NORMAL, GLANCING, RESISTED -> {
				if(affectMario) {
					data.refreshJumpCapping();
					data.setYVel(StompBounce.BOUNCE_VEL.get(data) + 0.2F * getEnchantmentLevel(equipment, data.getPlayer().getWorld(), BOUNDING_ID));
				}
				yield movingToPos.withAxis(Direction.Axis.Y, target.getY() + target.getHeight());
			}
			default -> null;
		};
	}

	@Override
	public @Nullable Vec3d executeTravellersAndModifyTargetPos(CfaTravelData data, ItemStack equipment, Entity target, CollisionAttackResult.ExecutableResult result, Vec3d movingToPos, boolean affectAttacker) {
		return bounceMarioAndGetTargetPos(data, equipment, target, result, movingToPos, affectAttacker);
	}

	public static void visuallySquashOnClient(Entity target, CollisionAttackResult.ExecutableResult result) {
		if(result != CollisionAttackResult.ExecutableResult.MOUNT && target instanceof Squashable squashableTarget)
			squashableTarget.cfa$squash();
	}

	@Override
	public void executeClients(CfaClientData data, ItemStack equipment, Entity target, CollisionAttackResult.ExecutableResult result, boolean affectAttacker, long seed) {
		visuallySquashOnClient(target, result);
		SoundEvent stompSound = switch(result) {
			case MOUNT, PAINFUL -> null;
			case NORMAL, GLANCING -> target.isAlive() ? MarioSFX.STOMP : MarioSFX.LAST;
			case RESISTED -> MarioSFX.HARMLESS;
		};
		if(stompSound == null) return;
		data.playSound(stompSound, seed);
	}
}

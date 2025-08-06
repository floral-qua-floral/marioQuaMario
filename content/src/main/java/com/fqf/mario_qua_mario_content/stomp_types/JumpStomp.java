package com.fqf.mario_qua_mario_content.stomp_types;

import com.fqf.mario_qua_mario_api.definitions.StompTypeDefinition;
import com.fqf.mario_qua_mario_api.interfaces.StompResult;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.actions.airborne.StompBounce;
import com.fqf.mario_qua_mario_content.util.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static com.fqf.mario_qua_mario_api.util.StatCategory.DAMAGE;
import static com.fqf.mario_qua_mario_api.util.StatCategory.STOMP;

public class JumpStomp implements StompTypeDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("stomp");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public boolean shouldAttemptMounting() {
		return true;
	}
	@Override public @NotNull PainfulStompResponse painfulStompResponse() {
		return PainfulStompResponse.INJURY;
	}
	@Override public @Nullable EquipmentSlot getEquipmentSlot() {
		return EquipmentSlot.FEET;
	}
	@Override public @NotNull Identifier getDamageType() {
		return MarioQuaMarioContent.makeResID("stomp");
	}
	@Override public @Nullable Identifier getPostStompActions(StompResult.ExecutableResult result) {
		return switch(result) {
			case PAINFUL -> null; // Later: Replace this with Bonk
			case NORMAL, GLANCING, RESISTED -> StompBounce.ID;
			default -> null;
		};
	}

	@Override public Box tweakMarioBoundingBox(IMarioData data, Box box) {
		return box.stretch(0, -0.05, 0);
	}

	public static boolean collidingFromTop(Entity entity, ServerPlayerEntity mario, double marioY, Vec3d motion, boolean allowRisingStomp) {
		double entityHeadY = entity.getY() + entity.getHeight() - 0.026;
		double marioDestinationY = marioY + motion.y;

		return (marioY > entityHeadY && marioDestinationY < entityHeadY) || (
				allowRisingStomp
				&& mario.getWorld().getGameRules().getBoolean(MarioContentGamerules.ALLOW_RISING_STOMPS)
				&& (marioY < entityHeadY && marioDestinationY > entityHeadY)
		);
	}

	public static void filterStompTargets(List<Entity> potentialTargets, ServerPlayerEntity mario, Vec3d motion) {
		potentialTargets.removeIf(entity -> !(
				(entity.canHit() || entity instanceof TridentEntity) // Mario can only stomp on things he can hit w/ crosshair (& Tridents)
						&& collidingFromTop(entity, mario, mario.getY(), motion,
						(
								entity instanceof Monster // Mario can do rising stomps against monsters
										|| entity.getType().isIn(MQMContentTags.RISING_STOMPABLE_NONMONSTERS) // And off of armor stands
						))
		));
	}

	@Override
	public void filterPotentialTargets(List<Entity> potentialTargets, ServerPlayerEntity mario, Vec3d motion) {
		filterStompTargets(potentialTargets, mario, motion);
	}

	public static final CharaStat BASE_DAMAGE = new CharaStat(4.5, STOMP, DAMAGE);

	public static final Identifier PULVERIZING_ID = MarioQuaMarioContent.makeResID("pulverizing");
	public static final Identifier BOUNDING_ID = MarioQuaMarioContent.makeResID("bounding");

	public static int getPulverizingLevel(ItemStack item, IMarioData data) {
		return getEnchantmentLevel(item, data.getMario().getWorld(), PULVERIZING_ID);
	}

	public static int getEnchantmentLevel(ItemStack item, World world, Identifier enchantmentID) {
		Optional<RegistryEntry.Reference<Enchantment>> pulverizingEntry =
				world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(enchantmentID);
		assert pulverizingEntry.isPresent();
		return item.getEnchantments().getLevel(pulverizingEntry.get());
	}

	@Override
	public float calculateDamage(IMarioData data, ItemStack equipment, float equipmentArmor, float equipmentToughness) {
		int pulverizingLevel = JumpStomp.getPulverizingLevel(equipment, data);
		return ((float) BASE_DAMAGE.get(data)) + equipmentArmor * 2.25F + pulverizingLevel * 0.5F + (pulverizingLevel > 0 ? 0.5F : 0);
	}

	@Override
	public float calculatePiercing(IMarioData data, ItemStack equipment, float equipmentArmor, float equipmentToughness) {
		return equipmentToughness * 2;
	}

	@Override
	public void executeServer(IMarioAuthoritativeData data, ItemStack equipment, Entity target, StompResult.ExecutableResult result, boolean affectMario) {
		if(affectMario && data.hasPower(Powers.STOMP_GUARD)) {
			data.getVars(MarioVars.class).stompGuardMinHeight = target.getY() + target.getHeight() + 0.15;
			data.getVars(MarioVars.class).stompGuardRemainingTicks = 4;
		}
	}

	public static Vec3d stompETAMTS(IMarioTravelData data, ItemStack equipment, Entity target, StompResult.ExecutableResult result, Vec3d movingToPos, boolean affectMario) {
		return switch(result) {
			case PAINFUL -> null; // Replace once Bonk implemented: Give Mario backwards momentum
			case NORMAL, GLANCING, RESISTED -> {
				if(affectMario) {
					data.refreshJumpCapping();
					data.setYVel(StompBounce.BOUNCE_VEL.get(data) + 0.2F * getEnchantmentLevel(equipment, data.getMario().getWorld(), BOUNDING_ID));
				}
				yield movingToPos.withAxis(Direction.Axis.Y, target.getY() + target.getHeight());
			}
			default -> null;
		};
	}

	@Override
	public @Nullable Vec3d executeTravellersAndModifyTargetPos(IMarioTravelData data, ItemStack equipment, Entity target, StompResult.ExecutableResult result, Vec3d movingToPos, boolean affectMario) {
		return stompETAMTS(data, equipment, target, result, movingToPos, affectMario);
	}

	@Override
	public void executeClients(IMarioClientData data, ItemStack equipment, Entity target, StompResult.ExecutableResult result, boolean affectMario, long seed) {
		data.playSound(MarioContentSFX.STOMP, seed);
	}
}

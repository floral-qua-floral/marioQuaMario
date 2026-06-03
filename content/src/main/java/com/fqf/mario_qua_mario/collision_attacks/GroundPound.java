package com.fqf.mario_qua_mario.collision_attacks;

import com.fqf.charaformact_api.definitions.CollisionAttackTypeDefinition;
import com.fqf.charaformact_api.interfaces.CollisionAttackResult;
import com.fqf.charaformact_api.cfadata.*;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.BonkAir;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.fqf.charaformact_api.util.StatCategory.DAMAGE;
import static com.fqf.charaformact_api.util.StatCategory.COLLISION_ATTACK;

public class GroundPound implements CollisionAttackTypeDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("ground_pound");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	public boolean shouldAttemptMounting() {
		return true;
	}

	@Override
	public @NotNull CollisionAttackTypeDefinition.PainfulCollisionResponse painfulCollisionResponse() {
		return PainfulCollisionResponse.INJURY;
	}

	@Override
	public @Nullable EquipmentSlot getEquipmentSlot() {
		return EquipmentSlot.LEGS;
	}

	@Override
	public @NotNull Identifier getDamageType() {
		return MarioQuaMario.makeResID("ground_pound");
	}

	@Override
	public @Nullable Identifier getPostCollisionActions(CollisionAttackResult.ExecutableResult result) {
		if(result == CollisionAttackResult.ExecutableResult.PAINFUL) return BonkAir.ID;
		return null;
	}

	@Override
	public Box tweakPlayerBoundingBox(CfaData data, Box box) {
		return box.stretch(0, -0.05, 0);
	}

	@Override
	public void filterPotentialTargets(List<Entity> potentialTargets, ServerPlayerEntity attacker, Vec3d motion) {
		potentialTargets.removeIf(entity -> !entity.canHit());
	}

	public static final CfaStat BASE_DAMAGE = new CfaStat(7, COLLISION_ATTACK, DAMAGE);

	@Override
	public float calculateDamage(CfaData data, ItemStack equipment, float equipmentArmor, float equipmentToughness) {
		int pulverizingLevel = Stomp.getPulverizingLevel(equipment, data);
		return ((float) BASE_DAMAGE.get(data)) + equipmentArmor * 2.25F + pulverizingLevel * 0.5F + (pulverizingLevel > 0 ? 1 : 0);
	}

	@Override
	public float calculatePiercing(CfaData data, ItemStack equipment, float equipmentArmor, float equipmentToughness) {
		return equipmentToughness * 2.5F;
	}

	@Override
	public void executeServer(CfaAuthoritativeData data, ItemStack equipment, Entity target, CollisionAttackResult.ExecutableResult result, boolean affectAttacker) {

	}

	@Override
	public @Nullable Vec3d executeTravellersAndModifyTargetPos(CfaTravelData data, ItemStack equipment, Entity target, CollisionAttackResult.ExecutableResult result, Vec3d movingToPos, boolean affectAttacker) {
		return null;
	}

	@Override
	public void executeClients(CfaClientData data, ItemStack equipment, Entity target, CollisionAttackResult.ExecutableResult result, boolean affectAttacker, long seed) {
		if(result == CollisionAttackResult.ExecutableResult.RESISTED || result == CollisionAttackResult.ExecutableResult.PAINFUL) return;
		MarioQuaMario.LOGGER.info("Result: {}", result);
		data.playSound(MarioSFX.KICK, seed);
	}
}

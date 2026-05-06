package com.fqf.mario_qua_mario.collision_attacks;

import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.CollisionAttackTypeDefinition;
import com.fqf.charaformact_api.interfaces.CollisionAttackResult;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.aquatic.Submerged;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.fqf.charaformact_api.util.StatCategory.DAMAGE;
import static com.fqf.charaformact_api.util.StatCategory.COLLISION_ATTACK;

public class AquaticGroundPound extends JumpStomp implements CollisionAttackTypeDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("aquatic_ground_pound");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	public boolean shouldAttemptMounting() {
		return false;
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
		return Submerged.ID;
	}

	public static final CfaStat BASE_DAMAGE = new CfaStat(4.5, COLLISION_ATTACK, DAMAGE);

	public static final Identifier DEPTH_CHARGE_ID = MarioQuaMario.makeResID("depth_charge");

	@Override
	public float calculateDamage(CfaData data, ItemStack equipment, float equipmentArmor, float equipmentToughness) {
		int pulverizingLevel = JumpStomp.getPulverizingLevel(equipment, data);
		float pulverizingBonus = pulverizingLevel * 0.25F + (pulverizingLevel > 0 ? 0.5F : 0);
		int depthChargeLevel = getDepthChargeLevel(equipment, data);
		float depthChargeBonus = depthChargeLevel * 0.5F + (depthChargeLevel > 0 ? 1 : 0);
		return ((float) BASE_DAMAGE.get(data)) + equipmentArmor * 2.25F + pulverizingBonus + depthChargeBonus;
	}

	public static int getDepthChargeLevel(ItemStack item, CfaData data) {
		return JumpStomp.getEnchantmentLevel(item, data.getPlayer().getWorld(), DEPTH_CHARGE_ID);
	}

	@Override
	public float calculatePiercing(CfaData data, ItemStack equipment, float equipmentArmor, float equipmentToughness) {
		return equipmentToughness * 0.5F;
	}

	@Override
	public @Nullable Vec3d executeTravellersAndModifyTargetPos(CfaTravelData data, ItemStack equipment, Entity target, CollisionAttackResult.ExecutableResult result, Vec3d movingToPos, boolean affectAttacker) {
		return super.executeTravellersAndModifyTargetPos(data, equipment, target, result, movingToPos, affectAttacker);
	}
}

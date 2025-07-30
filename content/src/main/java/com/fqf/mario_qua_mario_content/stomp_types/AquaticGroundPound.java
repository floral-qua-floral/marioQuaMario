package com.fqf.mario_qua_mario_content.stomp_types;

import com.fqf.mario_qua_mario_api.definitions.StompTypeDefinition;
import com.fqf.mario_qua_mario_api.interfaces.StompResult;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.actions.aquatic.Submerged;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.fqf.mario_qua_mario_api.util.StatCategory.DAMAGE;
import static com.fqf.mario_qua_mario_api.util.StatCategory.STOMP;

public class AquaticGroundPound extends JumpStomp implements StompTypeDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("aquatic_ground_pound");
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
		return MarioQuaMarioContent.makeResID("ground_pound");
	}

	@Override
	public @Nullable Identifier getPostStompActions(StompResult.ExecutableResult result) {
		return Submerged.ID;
	}

	public static final CharaStat BASE_DAMAGE = new CharaStat(4.5, STOMP, DAMAGE);

	public static final Identifier DEPTH_CHARGE_ID = MarioQuaMarioContent.makeResID("depth_charge");

	@Override
	public float calculateDamage(IMarioData data, ItemStack equipment, float equipmentArmor, float equipmentToughness) {
		int pulverizingLevel = JumpStomp.getPulverizingLevel(equipment, data);
		float pulverizingBonus = pulverizingLevel * 0.25F + (pulverizingLevel > 0 ? 0.5F : 0);
		int depthChargeLevel = getDepthChargeLevel(equipment, data);
		float depthChargeBonus = depthChargeLevel * 0.5F + (depthChargeLevel > 0 ? 1 : 0);
		return ((float) BASE_DAMAGE.get(data)) + equipmentArmor * 2.25F + pulverizingBonus + depthChargeBonus;
	}

	public static int getDepthChargeLevel(ItemStack item, IMarioData data) {
		return JumpStomp.getEnchantmentLevel(item, data.getMario().getWorld(), DEPTH_CHARGE_ID);
	}

	@Override
	public float calculatePiercing(IMarioData data, ItemStack equipment, float equipmentArmor, float equipmentToughness) {
		return equipmentToughness * 0.5F;
	}

	@Override
	public @Nullable Vec3d executeTravellersAndModifyTargetPos(IMarioTravelData data, ItemStack equipment, Entity target, StompResult.ExecutableResult result, Vec3d movingToPos, boolean affectMario) {
		return super.executeTravellersAndModifyTargetPos(data, equipment, target, result, movingToPos, affectMario);
	}
}

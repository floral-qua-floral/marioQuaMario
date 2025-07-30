package com.fqf.mario_qua_mario_content.stomp_types;

import com.fqf.mario_qua_mario_api.definitions.StompTypeDefinition;
import com.fqf.mario_qua_mario_api.interfaces.StompResult;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.util.MarioContentSFX;
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

public class GroundPound implements StompTypeDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("ground_pound");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	public boolean shouldAttemptMounting() {
		return true;
	}

	@Override
	public @NotNull PainfulStompResponse painfulStompResponse() {
		return PainfulStompResponse.INJURY;
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
		return null;
	}

	@Override
	public Box tweakMarioBoundingBox(IMarioData data, Box box) {
		return box.stretch(0, -0.05, 0);
	}

	@Override
	public void filterPotentialTargets(List<Entity> potentialTargets, ServerPlayerEntity mario, Vec3d motion) {
		potentialTargets.removeIf(entity -> !(entity.canHit() && entity instanceof LivingEntity));
	}

	public static final CharaStat BASE_DAMAGE = new CharaStat(7, STOMP, DAMAGE);

	@Override
	public float calculateDamage(IMarioData data, ItemStack equipment, float equipmentArmor, float equipmentToughness) {
		int pulverizingLevel = JumpStomp.getPulverizingLevel(equipment, data);
		return ((float) BASE_DAMAGE.get(data)) + equipmentArmor * 2.25F + pulverizingLevel * 0.5F + (pulverizingLevel > 0 ? 1 : 0);
	}

	@Override
	public float calculatePiercing(IMarioData data, ItemStack equipment, float equipmentArmor, float equipmentToughness) {
		return equipmentToughness * 2.5F;
	}

	@Override
	public void executeServer(IMarioAuthoritativeData data, ItemStack equipment, Entity target, StompResult.ExecutableResult result, boolean affectMario) {

	}

	@Override
	public @Nullable Vec3d executeTravellersAndModifyTargetPos(IMarioTravelData data, ItemStack equipment, Entity target, StompResult.ExecutableResult result, Vec3d movingToPos, boolean affectMario) {
		return null;
	}

	@Override
	public void executeClients(IMarioClientData data, ItemStack equipment, Entity target, StompResult.ExecutableResult result, boolean affectMario, long seed) {
		if(result == StompResult.ExecutableResult.RESISTED || result == StompResult.ExecutableResult.PAINFUL) return;
		MarioQuaMarioContent.LOGGER.info("Result: {}", result);
		data.playSound(MarioContentSFX.KICK, seed);
	}
}

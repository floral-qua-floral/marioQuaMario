package com.fqf.mario_qua_mario.definitions;

import com.fqf.mario_qua_mario.interfaces.StompResult;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
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

public interface StompTypeDefinition {
	@NotNull Identifier getID();

	boolean shouldAttemptMounting();
	@NotNull PainfulStompResponse painfulStompResponse();
	@Nullable EquipmentSlot getEquipmentSlot();
	@NotNull Identifier getDamageType();
	@Nullable Identifier getPostStompActions(StompResult.ExecutableResult result);

	Box tweakMarioBoundingBox(IMarioData data, Box box);
	void filterPotentialTargets(List<Entity> potentialTargets, ServerPlayerEntity mario, Vec3d motion);

	float calculateDamage(IMarioData data, ItemStack equipment, float equipmentArmor, float equipmentToughness);
	float calculatePiercing(IMarioData data, ItemStack equipment, float equipmentArmor, float equipmentToughness);

	void executeServer(IMarioAuthoritativeData data, ItemStack equipment, Entity target, StompResult.ExecutableResult result, boolean affectMario);
	@Nullable Vec3d executeTravellersAndModifyTargetPos(IMarioTravelData data, ItemStack equipment, Entity target, StompResult.ExecutableResult result, Vec3d movingToPos, boolean affectMario);
	void executeClients(IMarioClientData data, ItemStack equipment, Entity target, StompResult.ExecutableResult result, boolean affectMario, long seed);

	enum PainfulStompResponse {
		INJURY,
		BOUNCE, // Mario bounces off and neither entity takes damage. Like the Spin Jump from Super Mario World.
		STOMP // Mario damages the entity and isn't harmed himself. Like the Goomba's Shoe.
	}
}

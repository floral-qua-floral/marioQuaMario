package com.fqf.charapoweract_api.definitions;

import com.fqf.charapoweract_api.interfaces.CollisionAttackResult;
import com.fqf.charapoweract_api.cpadata.*;
import com.fqf.charapoweract_api.cpadata.ICPAAuthoritativeData;
import com.fqf.charapoweract_api.cpadata.ICPAData;
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

public interface CollisionAttackTypeDefinition {
	@NotNull Identifier getID();

	boolean shouldAttemptMounting();
	@NotNull CollisionAttackTypeDefinition.PainfulCollisionResponse painfulCollisionResponse();
	@Nullable EquipmentSlot getEquipmentSlot();
	@NotNull Identifier getDamageType();
	@Nullable Identifier getPostCollisionActions(CollisionAttackResult.ExecutableResult result);

	Box tweakPlayerBoundingBox(ICPAData data, Box box);
	void filterPotentialTargets(List<Entity> potentialTargets, ServerPlayerEntity attacker, Vec3d motion);

	float calculateDamage(ICPAData data, ItemStack equipment, float equipmentArmor, float equipmentToughness);
	float calculatePiercing(ICPAData data, ItemStack equipment, float equipmentArmor, float equipmentToughness);

	void executeServer(ICPAAuthoritativeData data, ItemStack equipment, Entity target, CollisionAttackResult.ExecutableResult result, boolean affectAttacker);
	@Nullable Vec3d executeTravellersAndModifyTargetPos(ICPATravelData data, ItemStack equipment, Entity target, CollisionAttackResult.ExecutableResult result, Vec3d movingToPos, boolean affectAttacker);
	void executeClients(ICPAClientData data, ItemStack equipment, Entity target, CollisionAttackResult.ExecutableResult result, boolean affectAttacker, long seed);

	enum PainfulCollisionResponse {
		INJURY,
		MUTUALLY_HARMLESS, // Player bounces off and neither entity takes damage. Like the Spin Jump from Super Mario World.
		IMMUNE // Player damages the entity and isn't harmed herself. Like the Goomba's Shoe.
	}
}

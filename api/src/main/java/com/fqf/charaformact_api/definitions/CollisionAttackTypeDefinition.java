package com.fqf.charaformact_api.definitions;

import com.fqf.charaformact_api.interfaces.CollisionAttackResult;
import com.fqf.charaformact_api.cfadata.*;
import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaData;
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

	boolean attemptsMounting();
	default @NotNull PainfulCollisionResponse definePainfulCollisionResponse() {
		return PainfulCollisionResponse.INJURY;
	}
	@Nullable EquipmentSlot defineEquipmentSlot();
	@NotNull Identifier defineDamageType();
	@Nullable Identifier definePostCollisionActions(CollisionAttackResult.ExecutableResult result);

	Box mutatePlayerBoundingBox(CfaData data, Box box);
	void filterPotentialTargets(List<Entity> potentialTargets, ServerPlayerEntity attacker, Vec3d motion);

	float calculateDamage(CfaData data, ItemStack equipment, float equipmentArmor, float equipmentToughness);
	float calculatePiercing(CfaData data, ItemStack equipment, float equipmentArmor, float equipmentToughness);

	void executeServer(CfaAuthoritativeData data, ItemStack equipment, Entity target, CollisionAttackResult.ExecutableResult result, boolean affectAttacker);
	@Nullable Vec3d executeTravellersAndModifyTargetPos(CfaTravelData data, ItemStack equipment, Entity target, CollisionAttackResult.ExecutableResult result, Vec3d movingToPos, boolean affectAttacker);
	void executeClients(CfaClientData data, ItemStack equipment, Entity target, CollisionAttackResult.ExecutableResult result, boolean affectAttacker, long seed);

	enum PainfulCollisionResponse {
		INJURY,
		MUTUALLY_HARMLESS, // Player bounces off and neither entity takes damage. Like the Spin Jump from Super Mario World.
		IMMUNE // Player damages the entity and isn't harmed herself. Like the Goomba's Shoe.
	}
}

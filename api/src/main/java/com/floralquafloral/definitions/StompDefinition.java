package com.floralquafloral.definitions;

import com.floralquafloral.StompableEntity;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioTravelData;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface StompDefinition {
	@NotNull Identifier getID();

	boolean mustFallOnTarget();
	@NotNull PainfulStompResponse getPainfulStompResponse();
	boolean shouldAttemptMounting();
	boolean canHitNonLiving();

	@NotNull Identifier getDamageType();

	@Nullable Identifier getPostStompAction();

	boolean canStompTarget(MarioData data, Entity target);

	float calculateDamage(MarioData data, ServerPlayerEntity mario, ItemStack equipment, float equipmentArmorValue, Entity target);

	void executeTravellers(MarioTravelData data, Entity target, StompableEntity.StompResult result);
	void executeClients(MarioClientSideData data, boolean isSelf, Entity target, StompableEntity.StompResult result, long seed);

	enum PainfulStompResponse {
		INJURY,
		BOUNCE,
		STOMP
	}
}

package com.fqf.mario_qua_mario.definitions;

import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.mariodata.IMarioReadableMotionData;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface StompTypeDefinition {
	@NotNull Identifier getID();

	boolean hitsFromAbove();
	boolean shouldAttemptMounting();
	PainfulStompResponse painfulStompResponse();

	void filterPotentialTargets(List<Entity> potentialTargets, ServerPlayerEntity mario, Vec3d motion);

//	float calculateDamage(IMarioData data,)

	enum PainfulStompResponse {
		INJURY,
		BOUNCE, // Mario bounces off and neither entity takes damage. Like the Spin Jump from Super Mario World.
		STOMP // Mario damages the entity and isn't harmed himself. Like the Goomba's Shoe.
	}
}

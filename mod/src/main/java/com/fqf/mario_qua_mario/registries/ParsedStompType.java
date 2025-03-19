package com.fqf.mario_qua_mario.registries;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.definitions.StompTypeDefinition;
import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ParsedStompType extends ParsedMarioThing {
	private final StompTypeDefinition DEFINITION;

	public ParsedStompType(@NotNull StompTypeDefinition definition) {
		super(definition.getID());

		this.DEFINITION = definition;
	}

	public Vec3d moveHook(MarioServerPlayerData data, Vec3d movement) {
		ServerPlayerEntity mario = data.getMario();

		List<Entity> possibleTargets = mario.getWorld().getOtherEntities(mario, mario.getBoundingBox().stretch(movement).expand(0.05));
		this.DEFINITION.filterPotentialTargets(possibleTargets, mario, movement);



		if(!possibleTargets.isEmpty()) mario.requestTeleportOffset(0, 10, 0);
		return movement;
	}
}

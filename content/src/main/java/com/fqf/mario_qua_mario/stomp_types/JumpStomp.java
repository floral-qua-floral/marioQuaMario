package com.fqf.mario_qua_mario.stomp_types;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.StompTypeDefinition;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.util.MarioContentGamerules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JumpStomp implements StompTypeDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("stomp");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public boolean hitsFromAbove() {
		return true;
	}

	@Override public boolean shouldAttemptMounting() {
		return true;
	}

	@Override public PainfulStompResponse painfulStompResponse() {
		return PainfulStompResponse.INJURY;
	}

	public static boolean collidingFromTop(Entity entity, ServerPlayerEntity mario, Vec3d motion, boolean allowRisingStomp) {
		double marioY = mario.getY();
		double entityHeadY = entity.getY() + entity.getHeight();

		return mario.getY() > entityHeadY || (
				allowRisingStomp
				&& mario.getWorld().getGameRules().getBoolean(MarioContentGamerules.ALLOW_RISING_STOMPS)
				&& marioY + motion.y > entityHeadY
		);
	}

	@Override
	public void filterPotentialTargets(List<Entity> potentialTargets, ServerPlayerEntity mario, Vec3d motion) {
		potentialTargets.removeIf(entity -> !(
				(entity.canHit() || entity instanceof TridentEntity) // Mario can only stomp on things he can hit w/ crosshair (& Tridents)
				&& collidingFromTop(entity, mario, motion, entity instanceof Monster)
		));
	}
}

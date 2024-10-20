package com.floralquafloral.registries.stomp.basestomptypes;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.registries.stomp.StompDefinition;
import com.floralquafloral.registries.stomp.StompHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JumpStomp implements StompDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "stomp");
	}

	@Override public boolean mustFallOnTarget() {
		return true;
	}

	@Override public @NotNull PainfulStompResponse getPainfulStompResponse() {
		return PainfulStompResponse.INJURY;
	}

	@Override public boolean shouldAttemptMounting() {
		return true;
	}

	@Override public boolean canHitNonLiving() {
		return false;
	}

	@Override public @NotNull Identifier getDamageType() {
		return Identifier.of(MarioQuaMario.MOD_ID, "basic");
	}

	@Override public @Nullable Identifier getPostStompAction() {
		return null;
	}

	@Override
	public boolean canStompTarget(MarioData data, Entity target) {
		return !target.getType().isIn(StompHandler.IMMUNE_TO_BASIC_STOMP_TAG);
	}

	@Override
	public float calculateDamage(MarioData data, ServerPlayerEntity mario, ItemStack equipment, double equipmentArmor, double equipmentToughness, Entity target) {
		return 1000;
	}

	@Override
	public void executeServer(MarioPlayerData data, Entity target, boolean harmless, long seed) {
		executeCommon(data, target);
	}

	@Override
	public void executeClient(MarioPlayerData data, boolean isSelf, Entity target, boolean harmless, long seed) {
		executeCommon(data, target);
	}

	private void executeCommon(MarioPlayerData data, Entity target) {
		data.getMario().move(MovementType.PISTON, new Vec3d(data.getMario().getX(), target.getY() + target.getHeight(), data.getMario().getZ()));
		data.setYVel(1.0);
	}
}

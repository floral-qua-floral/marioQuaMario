package com.floralquafloral.registries.stomp.basestomptypes;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import com.floralquafloral.registries.stomp.StompDefinition;
import com.floralquafloral.registries.stomp.StompHandler;
import com.floralquafloral.util.MarioSFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
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
		return Identifier.of(MarioQuaMario.MOD_ID, "stomp");
	}
	@Override public @Nullable SoundEvent getSoundEvent() {
		return MarioSFX.STOMP;
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
	public void executeTravellers(MarioTravelData data, Entity target, boolean harmless) {
		double deltaY = data.getMario().getY() - (target.getY() - target.getHeight());
		data.getMario().move(MovementType.PISTON, new Vec3d(0, deltaY, 0));
		data.setYVel(1.0);
	}

	@Override
	public void executeClients(MarioClientSideData data, boolean isSelf, Entity target, boolean harmless, long seed) {

	}
}

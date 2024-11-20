package com.floralquafloral.registries.stomp.basestomptypes;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import com.floralquafloral.registries.stomp.StompDefinition;
import com.floralquafloral.registries.stomp.StompHandler;
import com.floralquafloral.stats.CharaStat;
import com.floralquafloral.stats.StatCategory;
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

	public final CharaStat BASE_DAMAGE = new CharaStat(4.5, StatCategory.STOMP_BASE_DAMAGE);
	public final CharaStat BOUNCE_VEL = new CharaStat(1.15, StatCategory.STOMP_BOUNCE);

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
		return Identifier.of(MarioQuaMario.MOD_ID, "stomp");
	}

	@Override public boolean canStompTarget(MarioData data, Entity target) {
		return !target.getType().isIn(StompHandler.IMMUNE_TO_BASIC_STOMP_TAG);
	}

	@Override public float calculateDamage(MarioData data, ServerPlayerEntity mario, ItemStack equipment, float equipmentArmorValue, Entity target) {
		return ((float) BASE_DAMAGE.get(data)) + equipmentArmorValue * 2.25F;
	}

	@Override public void executeTravellers(MarioTravelData data, Entity target, boolean harmless) {
		double deltaY = (target.getY() + target.getHeight()) - data.getMario().getY();
//		MarioQuaMario.LOGGER.info("executeTravellers 1:"
//				+ "\nTarget: " + target
//				+ "\nTargetY: " + (target.getY() + target.getHeight())
//				+ "\nMarioY: " + data.getMario().getY()
//				+ "\ndeltaY: " + (data.getMario().getY())
//		);
		data.getMario().move(MovementType.SELF, new Vec3d(0, deltaY, 0));
//		data.getMario().setPos(data.getMario().getX(), target.getY() + target.getHeight(), data.getMario().getZ());
		data.setYVel(BOUNCE_VEL.get(data));
	}

	@Override public void executeClients(MarioClientSideData data, boolean isSelf, Entity target, boolean harmless, long seed) {

	}
}

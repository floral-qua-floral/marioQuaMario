package com.floralquafloral.registries.stomp.basestomptypes;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioTravelData;
import com.floralquafloral.definitions.StompDefinition;
import com.floralquafloral.registries.stomp.StompHandler;
import com.floralquafloral.definitions.actions.CharaStat;
import com.floralquafloral.definitions.actions.StatCategory;
import com.floralquafloral.util.MarioSFX;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GroundPoundStomp implements StompDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "ground_pound");
	}

	public final CharaStat BASE_DAMAGE = new CharaStat(7, StatCategory.STOMP_BASE_DAMAGE);

	@Override public boolean mustFallOnTarget() {
		return false;
	}

	@Override public @NotNull PainfulStompResponse getPainfulStompResponse() {
		return PainfulStompResponse.INJURY;
	}

	@Override public boolean shouldAttemptMounting() {
		return true;
	}

	@Override public boolean canHitNonLiving() {
		return true;
	}

	@Override public @NotNull Identifier getDamageType() {
		return Identifier.of(MarioQuaMario.MOD_ID, "ground_pound");
	}
	@Override public @Nullable SoundEvent getSoundEvent() {
		return MarioSFX.KICK;
	}

	@Override public @Nullable Identifier getPostStompAction() {
		return null;
	}

	@Override public boolean canStompTarget(MarioData data, Entity target) {
		return !target.getType().isIn(StompHandler.IMMUNE_TO_BASIC_STOMP_TAG);
	}

	@Override public float calculateDamage(MarioData data, ServerPlayerEntity mario, ItemStack equipment, float equipmentArmorValue, Entity target) {
		return ((float) BASE_DAMAGE.get(data)) + equipmentArmorValue;
	}

	@Override public void executeTravellers(MarioTravelData data, Entity target, boolean harmless) {
		double deltaY = data.getMario().getY() - (target.getY() - target.getHeight());
	}

	@Override public void executeClients(MarioClientSideData data, boolean isSelf, Entity target, boolean harmless, long seed) {

	}
}

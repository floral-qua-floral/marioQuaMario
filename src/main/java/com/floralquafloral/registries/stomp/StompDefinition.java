package com.floralquafloral.registries.stomp;

import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioPlayerData;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface StompDefinition {
	@NotNull Identifier getID();

	boolean mustFallOnTarget();
	@NotNull PainfulStompResponse getPainfulStompResponse();
	boolean shouldAttemptMounting();
	boolean canHitNonLiving();

	@NotNull Identifier getDamageType();
	@Nullable SoundEvent getSoundEvent();
	@Nullable Identifier getPostStompAction();

	boolean canStompTarget(MarioData data, Entity target);

	float calculateDamage(MarioData data, ServerPlayerEntity mario, ItemStack equipment, double equipmentArmor, double equipmentToughness, Entity target);

	void executeServer(World world, MarioPlayerData data, Entity target, boolean harmless, long seed);
	void executeClient(World world, MarioPlayerData data, boolean isSelf, Entity target, boolean harmless, long seed);

	enum PainfulStompResponse {
		INJURY,
		BOUNCE,
		STOMP
	}
}

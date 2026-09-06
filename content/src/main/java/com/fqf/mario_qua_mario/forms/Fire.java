package com.fqf.mario_qua_mario.forms;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.entity.custom.MarioFireballProjectileEntity;
import com.fqf.mario_qua_mario.util.MQMTags;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Fire extends AbstractProjectileThrowingForm<MarioFireballProjectileEntity> {
	public static final Identifier ID = MarioQuaMario.makeID("fire");

	@Override
	protected boolean canDirectHitEntity(@Nullable EntityHitResult result) {
		return result == null || !(
				result.getEntity().isFireImmune()
				|| result.getEntity().getType().isIn(MQMTags.FIRE_MARIO_PUNCH_TARGETS)
		);
	}

	@Override
	protected SoundEvent getThrowSound() {
		return MarioSFX.FIREBALL;
	}

	@Override
	protected int getTotalCooldown() {
		return 12;
	}

	@Override
	protected @NotNull MarioFireballProjectileEntity instantiateProjectile(ServerWorld world, ServerPlayerEntity player) {
		return new MarioFireballProjectileEntity(world, player);
	}
}

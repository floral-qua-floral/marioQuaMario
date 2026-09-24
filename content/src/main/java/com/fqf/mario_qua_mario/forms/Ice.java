package com.fqf.mario_qua_mario.forms;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.entity.custom.MarioIceballProjectileEntity;
import com.fqf.mario_qua_mario.freezing.IceFlowerFreezable;
import com.fqf.mario_qua_mario.util.MQMTags;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class Ice extends AbstractProjectileThrowingForm<MarioIceballProjectileEntity> {
	public static final Identifier ID = MarioQuaMario.makeID("ice");

	@Override
	protected boolean canDirectHitEntity(@NotNull EntityHitResult result) {
		return !result.getEntity().getType().isIn(MQMTags.ICE_MARIO_PUNCH_TARGETS)
				&& ((IceFlowerFreezable) result.getEntity()).mqm$canBeEncased();
	}

	@Override
	protected SoundEvent getThrowSound() {
		return MarioSFX.ICEBALL;
	}

	@Override
	protected int getTotalCooldown() {
		return 12;
	}

	@Override
	protected @NotNull MarioIceballProjectileEntity instantiateProjectile(ServerWorld world, ServerPlayerEntity player) {
		return new MarioIceballProjectileEntity(world, player);
	}
}

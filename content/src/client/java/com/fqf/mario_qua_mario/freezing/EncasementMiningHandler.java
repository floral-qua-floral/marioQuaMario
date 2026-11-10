package com.fqf.mario_qua_mario.freezing;

import com.fqf.mario_qua_mario.entity.custom.IceEncasementEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class EncasementMiningHandler {
	private byte breakSoundCooldown;
	private @Nullable IceEncasementEntity target;

	public void handleEncasementBreaking(PlayerEntity player, boolean breaking, HitResult crosshairTarget) {
		assert player != null;
		if(
				breaking
				&& crosshairTarget instanceof EntityHitResult crosshairEntity
				&& crosshairEntity.getEntity() instanceof IceEncasementEntity encasement
		)
			this.handleEncasementBreaking(player, encasement, crosshairEntity.getPos());
		else
			this.cancelBreakingEncasedEntity();
	}

	private void handleEncasementBreaking(PlayerEntity player, IceEncasementEntity encasement, Vec3d hitPos) {
		if(this.target == null || !this.target.equals(encasement)) {
			this.target = encasement;

			ClientPlayNetworking.send(new MineEncasementC2SPayload(encasement.getId(), MineEncasementC2SPayload.START_MINING));
		}

		if(this.breakSoundCooldown++ >= 4) {
			this.breakSoundCooldown = 0;

			BlockSoundGroup iceSounds = Blocks.ICE.getDefaultState().getSoundGroup();
			encasement.getWorld().playSound(
					player, encasement.getX(), encasement.getY() + encasement.getHeight() / 2, encasement.getZ(),
					iceSounds.getHitSound(), SoundCategory.BLOCKS,
					(iceSounds.getVolume() + 1.0F) / 8.0F, iceSounds.getPitch() * 0.5F
			);
		}

		player.swingHand(Hand.MAIN_HAND);

		Direction hittingSide = Direction.getFacing(hitPos.subtract(encasement.getPos().add(0, encasement.getHeight() / 2, 0)));
		Box box = encasement.getBoundingBox();

		double area = switch(hittingSide.getAxis()) {
			case X -> box.getLengthY() * box.getLengthZ();
			case Y -> box.getLengthX() * box.getLengthZ();
			case Z -> box.getLengthX() * box.getLengthY();
		};

		double particleCount = MathHelper.clamp(area, 1, 5);

		while(particleCount-- > 1)
			this.spawnParticle(encasement, hittingSide);

		// Decimal portion of the particle count is treated as a percent chance of one extra particle.
		if(particleCount > 0 && encasement.getRandom().nextDouble() < particleCount)
			this.spawnParticle(encasement, hittingSide);
	}

	private void spawnParticle(Entity target, Direction side) {
		Box box = target.getBoundingBox();
		Random random = target.getRandom();
		double particleX = box.minX + random.nextDouble() * box.getLengthX();
		double particleY = box.minY + random.nextDouble() * box.getLengthY();
		double particleZ = box.minZ + random.nextDouble() * box.getLengthZ();

		switch(side) {
			case DOWN -> particleY = box.minY;
			case UP -> particleY = box.maxY;
			case NORTH -> particleZ = box.minZ;
			case SOUTH -> particleZ = box.maxZ;
			case WEST -> particleX = box.minX;
			case EAST -> particleX = box.maxX;
		}

		MinecraftClient.getInstance().particleManager.addParticle(new BlockDustParticle(
				MinecraftClient.getInstance().world, particleX, particleY, particleZ, 0.0, 0.0, 0.0, Blocks.ICE.getDefaultState()
		).move(0.2F).scale(0.6F));
	}

	private void cancelBreakingEncasedEntity() {
		if(this.target != null) {
			ClientPlayNetworking.send(new MineEncasementC2SPayload(target.getId(), MineEncasementC2SPayload.STOP_MINING));

			this.target = null;
		}
	}
}

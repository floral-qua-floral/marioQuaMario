package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.util.PersistentReflectable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileMixin extends ProjectileEntity implements PersistentReflectable {
	@Shadow protected boolean inGround;
	@Shadow private int life;

	@Shadow protected abstract void fall();

	@Shadow public abstract void setVelocity(double x, double y, double z, float power, float uncertainty);

	@Shadow protected int inGroundTime;
	@Unique private @Nullable Direction stickDirection = Direction.UP;
	@Unique private @NotNull Vec3d groundNormal = Vec3d.ZERO;

	public PersistentProjectileMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "onBlockHit", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;inGround:Z"))
	private void storeNormalVector(BlockHitResult blockHitResult, CallbackInfo ci) {
		this.stickDirection = blockHitResult.getSide();
		this.groundNormal = Vec3d.of(this.stickDirection.getVector());
	}

	@Override
	public boolean mqm$isInGround() {
		return this.inGround;
	}

	@Override
	public Vec3d mqm$getGroundNormal() {
		return this.groundNormal;
	}

	@Override
	public Direction mqm$getGroundStickFace() {
		return this.stickDirection;
	}

	@Override
	public void mqm$dislodge() {
		// This is a little wasteful, since fall() randomizes the motion and we're just gonna reset it.
		// But it's very reasonable that some mods might do mixins on fall(). Sable might, for instance? Not sure??
		// So we want to do it this way so we can be sure that we trigger modded projectile-dislodging functionality too.
		Vec3d velocity = this.getVelocity();
		this.fall();
		this.setVelocity(velocity);
	}

	@Inject(method = "fall", at = @At("HEAD"))
	private void clearGroundInformationOnDislodge(CallbackInfo ci) {
		this.stickDirection = Direction.UP;
		this.groundNormal = Vec3d.ZERO;
	}
}

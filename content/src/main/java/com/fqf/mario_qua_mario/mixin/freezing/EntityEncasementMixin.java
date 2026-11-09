package com.fqf.mario_qua_mario.mixin.freezing;

import com.fqf.mario_qua_mario.entity.custom.FatalityReceiver;
import com.fqf.mario_qua_mario.entity.custom.IceEncasementEntity;
import com.fqf.mario_qua_mario.freezing.IceFlowerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityEncasementMixin implements IceEncasementEntity.EncasementOwner {
	@Shadow public abstract World getWorld();

	@Shadow public abstract boolean damage(DamageSource source, float amount);

	@Unique private static final String ENCASEMENT_NBT_KEY = "mqm_ice_encasement";

	@Unique private @Nullable IceEncasementEntity encasement;

	@Override
	public @Nullable IceEncasementEntity mqm$getEncasement() {
		if(this.encasement != null &&  this.encasement.isRemoved())
			this.encasement = null;
		return this.encasement;
	}

	@Override
	public void mqm$setEncasement(@Nullable IceEncasementEntity encasement) {
		this.encasement = encasement;
		IceFlowerUtil.endIllegalEncasedInteractions((Entity) (Object) this);
	}

	@Inject(
			method = "writeNbt",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/Entity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V"
			)
	)
	private void writeEncasementToNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
		IceEncasementEntity encasement = this.mqm$getEncasement();
		if(encasement != null) {
			NbtCompound encasementNbt = new NbtCompound();
			if(encasement.saveSelfNbt(encasementNbt))
				nbt.put(ENCASEMENT_NBT_KEY, encasementNbt);
		}
	}

	@Inject(
			method = "readNbt",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/Entity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V"
			)
	)
	private void readFrozenStatusFromNbt(NbtCompound nbt, CallbackInfo ci) {
		IceEncasementEntity encasement = this.mqm$getEncasement();
		if(encasement != null) encasement.discard();

		NbtCompound encasementNbt = nbt.getCompound(ENCASEMENT_NBT_KEY);
		if(!encasementNbt.isEmpty()) {
			Entity me = (Entity) (Object) this;
			try {
				encasement = IceFlowerUtil.encase(me);
				if(encasement != null)
					encasement.readNbt(encasementNbt);
			} catch(RuntimeException e) {
				throw new RuntimeException("(Mario qua Mario) Failed to create Ice Encasement for " + this, e);
			}
		}
	}
}

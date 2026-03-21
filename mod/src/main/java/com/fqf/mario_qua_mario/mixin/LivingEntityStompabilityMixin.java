package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.util.MarioGamerules;
import com.fqf.mario_qua_mario_api.util.MQMTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityStompabilityMixin extends Entity {
	public LivingEntityStompabilityMixin(EntityType<?> type, World world) {
		super(type, world);
		throw new IllegalStateException("Trying to use mixin constructor??");
	}

	@Inject(method = "damage", at = @At("HEAD"), cancellable = true)
	private void conditionallyPreventCollisionDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if(source.isIn(MQMTags.COLLISION_ATTACKS)
				&& !this.getWorld().isClient()
				&& this.getWorld().getGameRules().getBoolean(MarioGamerules.PETS_AND_TEAMMATES_RESIST_COLLISION_ATTACKS)
				&& this.canIgnoreCollisionAttacksFrom(source.getAttacker())) {
			cir.setReturnValue(false);
		}
	}

	@Unique
	private boolean canIgnoreCollisionAttacksFrom(Entity collisionAttacker) {
		if(collisionAttacker == null) return false;

		if(this instanceof Tameable tameable && collisionAttacker.equals(tameable.getOwner()))
			return true;

		Team myTeam = this.getScoreboardTeam();
		Team otherTeam = collisionAttacker.getScoreboardTeam();

		if(myTeam != null && otherTeam != null)
			return myTeam == otherTeam;

		return false;
	}
}

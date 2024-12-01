package com.floralquafloral.mixin;

import com.floralquafloral.bumping.BumpManager;
import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.moveable.MarioServerData;
import com.floralquafloral.registries.states.action.ParsedAction;
import com.floralquafloral.util.DamageHelper;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.function.Predicate;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
	@Shadow public abstract ServerWorld getServerWorld();

	private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Unique private boolean shouldStompHook = true;

	@Override public void move(MovementType movementType, Vec3d movement) {
		if(shouldStompHook) {
			MarioPlayerData data = MarioDataManager.getMarioData(this);
			if(data.useMarioPhysics()) {
				ParsedAction action = data.getAction();
				if(action.STOMP != null) {
					shouldStompHook = false;
					if(action.STOMP.attempt((MarioServerData) data, movement)) return;
					shouldStompHook = true;
				}
				if(action.BUMPING_RULE != null && action.BUMPING_RULE.CEILINGS > 0 && movement.y > 0) {
					Predicate<Entity> predicate = EntityPredicates.EXCEPT_SPECTATOR.and(this::collidesWith);
					List<Entity> list = this.getWorld().getOtherEntities(this, this.getBoundingBox().stretch(0, movement.y, 0), predicate);
					for(Entity entity : list) {
						DamageHelper.damageEntity(data, 1F, getServerWorld(), entity, BumpManager.CEILING_BONK_DAMAGE, 10);
					}
				}
			}
		}
		super.move(movementType, movement);
	}
}

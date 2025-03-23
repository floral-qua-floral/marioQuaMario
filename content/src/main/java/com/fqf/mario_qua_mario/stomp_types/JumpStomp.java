package com.fqf.mario_qua_mario.stomp_types;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.actions.airborne.DoubleJump;
import com.fqf.mario_qua_mario.definitions.StompTypeDefinition;
import com.fqf.mario_qua_mario.interfaces.StompResult;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.util.CharaStat;
import com.fqf.mario_qua_mario.util.MarioContentGamerules;
import com.fqf.mario_qua_mario.util.MarioContentSFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.fqf.mario_qua_mario.util.StatCategory.*;

public class JumpStomp implements StompTypeDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("stomp");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public boolean shouldAttemptMounting() {
		return true;
	}
	@Override public @NotNull PainfulStompResponse painfulStompResponse() {
		return PainfulStompResponse.INJURY;
	}
	@Override public @Nullable EquipmentSlot getEquipmentSlot() {
		return EquipmentSlot.FEET;
	}
	@Override public @NotNull Identifier getDamageType() {
		return MarioQuaMarioContent.makeResID("stomp");
	}
	@Override public @Nullable Identifier getPostStompActions(StompResult.ExecutableResult result) {
		return DoubleJump.ID;
	}

	@Override public Box tweakMarioBoundingBox(IMarioData data, Box box) {
		return box.stretch(0, -0.05, 0);
	}

	public static boolean collidingFromTop(Entity entity, ServerPlayerEntity mario, Vec3d motion, boolean allowRisingStomp) {
		double marioY = mario.getY();
		double entityHeadY = entity.getY() + entity.getHeight() - 0.026;

		return mario.getY() > entityHeadY || (
				allowRisingStomp
				&& mario.getWorld().getGameRules().getBoolean(MarioContentGamerules.ALLOW_RISING_STOMPS)
				&& marioY + motion.y > entityHeadY
		);
	}

	@Override
	public void filterPotentialTargets(List<Entity> potentialTargets, ServerPlayerEntity mario, Vec3d motion) {
		potentialTargets.removeIf(entity -> !(
				(entity.canHit() || entity instanceof TridentEntity) // Mario can only stomp on things he can hit w/ crosshair (& Tridents)
				&& collidingFromTop(entity, mario, motion, entity instanceof Monster) // Mario can do rising stomps against monsters
		));
	}

	public final CharaStat BASE_DAMAGE = new CharaStat(4.5, STOMP, DAMAGE);
	public final CharaStat BOUNCE_VEL = new CharaStat(1.15, STOMP, JUMP_VELOCITY);

	@Override
	public float calculateDamage(IMarioData data, ItemStack equipment, float equipmentArmor, float equipmentToughness) {
		return ((float) BASE_DAMAGE.get(data)) + equipmentArmor * 2.25F;
	}

	@Override
	public float calculatePiercing(IMarioData data, ItemStack equipment, float equipmentArmor, float equipmentToughness) {
		return equipmentToughness * 2;
	}

	@Override
	public void executeServer(IMarioAuthoritativeData data, ItemStack equipment, Entity target, StompResult.ExecutableResult result, boolean affectMario) {

	}

	@Override
	public @Nullable Vec3d executeTravellersAndModifyTargetPos(IMarioTravelData data, ItemStack equipment, Entity target, StompResult.ExecutableResult result, Vec3d movingToPos, boolean affectMario) {
		if(affectMario) {
			data.refreshJumpCapping();
			data.setYVel(BOUNCE_VEL.get(data));
		}

		return movingToPos.withAxis(Direction.Axis.Y, target.getY() + target.getHeight());
	}

	@Override
	public void executeClients(IMarioClientData data, ItemStack equipment, Entity target, StompResult.ExecutableResult result, boolean affectMario, long seed) {
		data.playSound(MarioContentSFX.STOMP, seed);
	}
}

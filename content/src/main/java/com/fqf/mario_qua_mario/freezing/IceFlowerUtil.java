package com.fqf.mario_qua_mario.freezing;

import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

public class IceFlowerUtil {
	public static final RegistryKey<DamageType> ICEBALL_DAMAGE_TYPE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, MarioQuaMario.makeResID("iceball"));
	public static final RegistryKey<DamageType> SHATTER_DAMAGE_TYPE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, MarioQuaMario.makeResID("shatter"));
	public static final RegistryKey<DamageType> DIRECT_SHATTER_DAMAGE_TYPE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, MarioQuaMario.makeResID("shatter_direct"));

	public static DamageSource makeShatterSource(World world, Entity source, Entity attacker) {
		return new DamageSource(
				world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(
						attacker == null
								? SHATTER_DAMAGE_TYPE
								: DIRECT_SHATTER_DAMAGE_TYPE
				),
				source, attacker
		);
	}

	public static void doEntityTickInjection(Entity entity, CallbackInfo ci, BiConsumer<Entity, Entity> passengerTicker) {
		if(!((IceFlowerFreezable) entity).mqm$tickEncased()) {
			ci.cancel();
			entity.baseTick(); // Still do base tick because it seems important maybe i guess? :/
			if(entity.age == 0) entity.age = 1; // Prevent entities from getting stuck doing their 0-age behavior every frame
			for (Entity passenger : entity.getPassengerList()) {
				passengerTicker.accept(entity, passenger);
			}
		}
	}
}

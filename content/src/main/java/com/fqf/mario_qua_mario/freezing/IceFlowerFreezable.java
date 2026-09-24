package com.fqf.mario_qua_mario.freezing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.NotNull;

public interface IceFlowerFreezable extends BreakableEntity {
	boolean mqm$canBeEncased();

	boolean mqm$isEncased();

	// This gets fired each tick EVEN WHEN NOT ENCASED!!!!!!!!! If it returns true, the entity will tick as normal.
	boolean mqm$tickEncased();

	// Called on server only.
	boolean mqm$iceFlowerHit(@NotNull Entity source, @NotNull Entity attacker);

	// Called on server only.
	boolean mqm$encase();

	// Called on server only.
	boolean mqm$thaw();

	// Gets called every time an entity takes fatal damage, unless it's Shatter damage.
	boolean mqm$attemptFatalFreeze(DamageSource source);

	// Gets called every time an entity is damaged.
	boolean mqm$shatter(DamageSource source, float amount);

	boolean mqm$isShaking();
}

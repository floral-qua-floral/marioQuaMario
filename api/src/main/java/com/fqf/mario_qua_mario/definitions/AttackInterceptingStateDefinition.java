package com.fqf.mario_qua_mario.definitions;

import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AttackInterceptingStateDefinition extends MarioStateDefinition {
	List<AttackInterceptionDefinition> getUnarmedAttackInterceptions();

	interface AttackInterceptionDefinition {
		@Nullable Identifier getActionTarget();
		@Nullable Hand getHandToSwing();
		boolean shouldTriggerAttackCooldown();

		boolean shouldIntercept(

		);
	}
}

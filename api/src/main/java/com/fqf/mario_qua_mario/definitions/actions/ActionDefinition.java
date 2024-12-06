package com.fqf.mario_qua_mario.definitions.actions;

import com.fqf.mario_qua_mario.definitions.AttackInterceptingStateDefinition;
import com.fqf.mario_qua_mario.definitions.actions.util.BumpingRule;
import com.fqf.mario_qua_mario.definitions.actions.util.CameraAnimationSet;
import com.fqf.mario_qua_mario.definitions.actions.util.SneakingRule;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ActionDefinition extends AttackInterceptingStateDefinition {
	@Nullable String getAnimationName();
	@Nullable CameraAnimationSet getCameraAnimations();

	@NotNull SneakingRule getSneakingRule();

	boolean canSprint();

	@Nullable Identifier getStompType();

	@Nullable BumpingRule getBumpingRule();

}

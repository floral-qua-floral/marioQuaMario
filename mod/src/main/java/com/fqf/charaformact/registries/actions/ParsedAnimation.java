package com.fqf.charaformact.registries.actions;

import com.fqf.charaformact_api.cfadata.CfaAnimatingData;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.Arrangement;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.Posture;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ParsedAnimation {
	public final @NotNull AnimationDefinition DEFINITION;

	public final @Nullable Identifier ID;
	public final @NotNull EnumSet<AnimationFlag> FLAGS;
	public final boolean USE_DEGREES;

	public ParsedAnimation(@NotNull AnimationDefinition definition) {
		this.DEFINITION = definition;
		this.ID = definition.getID();
		this.FLAGS = definition.defineFlags();
		this.USE_DEGREES = !this.FLAGS.contains(AnimationFlag.USE_RADIANS);
	}

	public @NotNull EnumSet<AnimationFlag.Execution> getExecutionFlags(CfaAnimatingData data, Identifier prevAnimationID) {
		return this.DEFINITION.chooseExecutionFlags(data, prevAnimationID);
	}

	public void arrangeModel(Arrangement modelTranslation, CfaAnimatingData data, float animationTime) {
		this.DEFINITION.arrangeModel(modelTranslation, data, animationTime, AnimationHelperImpl.INSTANCE);
	}

	public void mutate(Posture posture, CfaAnimatingData data, float animationTime) {
		this.DEFINITION.mutatePosture(posture, data, animationTime, AnimationHelperImpl.INSTANCE);
	}
}

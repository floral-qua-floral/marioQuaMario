package com.floralquafloral.definitions;

import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioTravelData;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface MarioAttackInterceptingStateDefinition extends MarioStateDefinition {
	boolean interceptAttack(
			MarioData data, @Nullable MarioClientSideData clientData, @Nullable MarioTravelData travelData,
			@Nullable Entity entityTarget, @Nullable BlockPos blockTarget
	);
}

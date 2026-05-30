package com.fqf.charaformact.util;

import com.fqf.charaformact.cfadata.CfaMoveableData;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact.registries.actions.TransitionPhase;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.Arrangement;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.Posture;
import com.fqf.charaformact_api.interfaces.BapResult;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.function.Consumer;

public class CfaClientHelperManager {
	public static ClientHelper helper = null;
	public static ClientPacketSender packetSender = null;

	public interface ClientHelper {
		void mirrorAndAnimate(Posture posture, Arrangement part, Consumer<Arrangement> animator);
		void mirrorAndAnimate(Posture posture, Arrangement part, AnimationHelper.DualPartAnimator animator);
	}
	public interface ClientPacketSender {
		void setActionC2S(AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed, TransitionPhase phase);
		void transmitWallYawC2S(CfaMoveableData data, float wallYaw);
		void conditionallySaveTransitionToReplayMod(AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed);
		void bapBlockC2S(BlockPos pos, Direction direction, AbstractParsedAction action);
		void conditionallySaveBapToReplayMod(BlockPos pos, Direction direction, int strength, BapResult result, Entity bapper);
	}
}

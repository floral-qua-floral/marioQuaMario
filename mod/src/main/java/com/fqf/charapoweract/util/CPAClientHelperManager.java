package com.fqf.charapoweract.util;

import com.fqf.charapoweract.bapping.AbstractBapInfo;
import com.fqf.charapoweract.cpadata.CPAMoveableData;
import com.fqf.charapoweract.registries.actions.AbstractParsedAction;
import com.fqf.charapoweract.registries.actions.TransitionPhase;
import com.fqf.charapoweract_api.interfaces.BapResult;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class CPAClientHelperManager {
	public static ClientHelper helper = null;
	public static ClientPacketSender packetSender = null;

	public interface ClientHelper {
		void clientBap(AbstractBapInfo info);
	}
	public interface ClientPacketSender {
		void setActionC2S(AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed, TransitionPhase phase);
		void transmitWallYawC2S(CPAMoveableData data, float wallYaw);
		void conditionallySaveTransitionToReplayMod(AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed);
		void bapBlockC2S(BlockPos pos, Direction direction, AbstractParsedAction action);
		void conditionallySaveBapToReplayMod(BlockPos pos, Direction direction, int strength, BapResult result, Entity bapper);
	}
}

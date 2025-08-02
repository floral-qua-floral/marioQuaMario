package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.bapping.AbstractBapInfo;
import com.fqf.mario_qua_mario.mariodata.MarioMoveableData;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario_api.interfaces.BapResult;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class MarioClientHelperManager {
	public static ClientHelper helper = null;
	public static ClientPacketSender packetSender = null;

	public interface ClientHelper {
		void clientBap(AbstractBapInfo info);
	}
	public interface ClientPacketSender {
		void setActionC2S(AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed);
		void transmitWallYawC2S(MarioMoveableData data, float wallYaw);
		void conditionallySaveTransitionToReplayMod(AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed);
		void bapBlockC2S(BlockPos pos, Direction direction, AbstractParsedAction action);
		void conditionallySaveBapToReplayMod(BlockPos pos, Direction direction, int strength, BapResult result, Entity bapper);
	}
}

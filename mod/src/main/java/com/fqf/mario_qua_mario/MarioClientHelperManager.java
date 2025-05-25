package com.fqf.mario_qua_mario;

import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;

public class MarioClientHelperManager {
	public static ClientHelper helper = null;
	public static ClientPacketSender packetSender = null;

	public interface ClientHelper {

	}
	public interface ClientPacketSender {
		void setActionC2S(AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed);
		void conditionallySaveTransitionToReplayMod(AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed);
	}
}

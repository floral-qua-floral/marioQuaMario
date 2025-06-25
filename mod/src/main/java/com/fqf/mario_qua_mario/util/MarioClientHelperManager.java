package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.bapping.AbstractBapInfo;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;

public class MarioClientHelperManager {
	public static ClientHelper helper = null;
	public static ClientPacketSender packetSender = null;

	public interface ClientHelper {
		void clientBap(AbstractBapInfo info);
	}
	public interface ClientPacketSender {
		void setActionC2S(AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed);
		void conditionallySaveTransitionToReplayMod(AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed);
	}
}

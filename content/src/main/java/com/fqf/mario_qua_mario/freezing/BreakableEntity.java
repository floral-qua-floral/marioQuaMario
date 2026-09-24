package com.fqf.mario_qua_mario.freezing;

import net.minecraft.server.network.ServerPlayerEntity;

public interface BreakableEntity {
	boolean mqm$canMine();

	float mqm$getMiningProgress();
	void mqm$updateMiningProgress(ServerPlayerEntity miner);
	void mqm$addMiner(ServerPlayerEntity miner);
	void mqm$stopMining(ServerPlayerEntity miner);
	void mqm$resetMiningProgress();
}

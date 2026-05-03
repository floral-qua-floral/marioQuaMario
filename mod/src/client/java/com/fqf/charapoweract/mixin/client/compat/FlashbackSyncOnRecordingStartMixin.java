package com.fqf.charapoweract.mixin.client.compat;

import com.fqf.charapoweract.CharaPowerAct;
import com.fqf.charapoweract.packets.CPAClientPacketHelper;
import com.moulberry.flashback.record.Recorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = Recorder.class, remap = false)
public class FlashbackSyncOnRecordingStartMixin {
//	@Inject(method = "startRecordingReplay()V", at = @At("RETURN"))
//	private static void injectSyncMarioDataAtRecordingStart(CallbackInfo ci) {
//		CharaPowerAct.LOGGER.info("Syncing MarioData of tracked players in world to newly started replay...");
//		CPAClientPacketHelper.syncCPADatasToReplay();
//	}
//
//	@Inject(method = "pauseRecordingReplay", at = @At("RETURN"))
//	private static void injectSyncMarioDataAtRecordingUnpause(boolean pause, CallbackInfo ci) {
//		CharaPowerAct.LOGGER.info("Syncing MarioData of tracked players in world to resumed replay...");
//		if(!pause) CPAClientPacketHelper.syncCPADatasToReplay();
//	}

	// TODO: Figure out what to do about the player model :/

	@Inject(method = "writeSnapshot", at = @At("RETURN"))
	private void syncAfterSnapshot(boolean asActualSnapshot, CallbackInfo ci) {
		CharaPowerAct.LOGGER.info("Syncing MarioData of tracked players after snapshot recorded...");
		CPAClientPacketHelper.syncCPADatasToReplay();
	}
}

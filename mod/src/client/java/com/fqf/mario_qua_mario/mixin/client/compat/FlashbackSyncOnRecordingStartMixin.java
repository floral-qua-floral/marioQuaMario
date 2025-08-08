package com.fqf.mario_qua_mario.mixin.client.compat;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.packets.MarioClientPacketHelper;
import com.moulberry.flashback.Flashback;
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
//		MarioQuaMario.LOGGER.info("Syncing MarioData of tracked players in world to newly started replay...");
//		MarioClientPacketHelper.syncMarioDatasToReplay();
//	}
//
//	@Inject(method = "pauseRecordingReplay", at = @At("RETURN"))
//	private static void injectSyncMarioDataAtRecordingUnpause(boolean pause, CallbackInfo ci) {
//		MarioQuaMario.LOGGER.info("Syncing MarioData of tracked players in world to resumed replay...");
//		if(!pause) MarioClientPacketHelper.syncMarioDatasToReplay();
//	}

	@Inject(method = "writeSnapshot", at = @At("RETURN"))
	private void syncAfterSnapshot(boolean asActualSnapshot, CallbackInfo ci) {
		MarioQuaMario.LOGGER.info("Syncing MarioData of tracked players after snapshot recorded...");
		MarioClientPacketHelper.syncMarioDatasToReplay();
	}
}

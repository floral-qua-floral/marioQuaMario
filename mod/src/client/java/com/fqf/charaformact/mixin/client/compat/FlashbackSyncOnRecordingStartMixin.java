package com.fqf.charaformact.mixin.client.compat;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.packets.CfaClientPacketHelper;
import com.moulberry.flashback.record.Recorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = Recorder.class, remap = false)
public class FlashbackSyncOnRecordingStartMixin {
	@Inject(method = "writeSnapshot", at = @At("RETURN"))
	private void syncAfterSnapshot(boolean asActualSnapshot, CallbackInfo ci) {
		CharaFormAct.LOGGER.info("Syncing CfaData of tracked players after snapshot recorded...");
		CfaClientPacketHelper.syncCfaDatasToReplay();
	}
}

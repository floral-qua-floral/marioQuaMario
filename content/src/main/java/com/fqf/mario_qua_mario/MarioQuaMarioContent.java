package com.fqf.mario_qua_mario;

import com.fqf.mario_qua_mario.util.MarioContentEventListeners;
import com.fqf.mario_qua_mario.util.MarioContentGamerules;
import com.fqf.mario_qua_mario.util.MarioContentSFX;
import net.fabricmc.api.ModInitializer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarioQuaMarioContent implements ModInitializer {
	public static final String MOD_ID = "mario_qua_mario_content";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	protected static ContentClientHelper clientHelper;
	public static ContentClientHelper getClientHelper() {
		return clientHelper;
	}

//	public static final EntityType<MarioFireballEntity> MARIO_FIREBALL = Registry.register(
//			Registries.ENTITY_TYPE, makeID("mario_fireball"),
//			EntityType.Builder.<MarioFireballEntity>create(MarioFireballEntity::new, SpawnGroup.MISC)
//					.dimensions(0.3125F, 0.3125F)
//					.maxTrackingRange(4)
//					.trackingTickInterval(10)
//					.build()
//	);

	@Override
	public void onInitialize() {
		LOGGER.info("Mario qua Mario Content initializing...");

		MarioContentSFX.staticInitialize();
		MarioContentGamerules.register();
		MarioContentEventListeners.register();
	}

	public static Identifier makeID(String path) {
		return Identifier.of("mqm", path);
	}
	public static Identifier makeResID(String path) {
		return Identifier.of("mario_qua_mario", path);
	}

	public static class ContentClientHelper {
		public Text getBackflipDismountText() {
			return Text.of("If you're seeing this, something's gone wrong! :(");
		}
	}
}
package com.fqf.mario_qua_mario_content;

import com.fqf.mario_qua_mario_content.entity.ModEntities;
import com.fqf.mario_qua_mario_content.item.ModItems;
import com.fqf.mario_qua_mario_content.util.MQMContentConfig;
import com.fqf.mario_qua_mario_content.util.MarioContentEventListeners;
import com.fqf.mario_qua_mario_content.util.MarioContentGamerules;
import com.fqf.mario_qua_mario_content.util.MarioContentSFX;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
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

	public static final MQMContentConfig CONFIG;
	static {
		AutoConfig.register(MQMContentConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(MQMContentConfig.class).getConfig();
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Mario qua Mario Content initializing...");

		ModEntities.registerModEntities();
		ModItems.registerModItems();
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
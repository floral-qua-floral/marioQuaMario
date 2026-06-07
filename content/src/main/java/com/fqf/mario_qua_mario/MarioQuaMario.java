package com.fqf.mario_qua_mario;

import com.fqf.mario_qua_mario.entity.ModEntities;
import com.fqf.mario_qua_mario.item.ModItems;
import com.fqf.mario_qua_mario.util.CustomToadUtil;
import com.fqf.mario_qua_mario.util.MQMEventListeners;
import com.fqf.mario_qua_mario.util.MQMGamerules;
import com.fqf.mario_qua_mario.util.MarioSFX;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarioQuaMario implements ModInitializer {
	public static final String MOD_ID = "mario_qua_mario";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	protected static ContentClientHelper clientHelper;
	public static ContentClientHelper getClientHelper() {
		return clientHelper;
	}

	public static final MQMConfig CONFIG;
	static {
		AutoConfig.register(MQMConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(MQMConfig.class).getConfig();
	}

	@Override
	public void onInitialize() {
		LOGGER.info("It's-a me!");

		ModEntities.registerModEntities();
		ModItems.registerModItems();
		MarioSFX.staticInitialize();
		MQMGamerules.register();
		MQMEventListeners.register();

		CustomToadUtil.registerCommand();
	}

	public static Identifier makeID(String path) {
		return Identifier.of("mario_qua_mario", path);
	}
	public static Identifier makeResID(String path) {
		return Identifier.of("mario_qua_mario", path);
	}

	public static abstract class ContentClientHelper {
		public abstract MutableText getBackflipDismountText();
	}
}
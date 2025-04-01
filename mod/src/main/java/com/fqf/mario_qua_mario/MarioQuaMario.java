package com.fqf.mario_qua_mario;

import com.fqf.mario_qua_mario.packets.MarioPackets;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.util.MQMConfig;
import com.fqf.mario_qua_mario.util.MarioEventListeners;
import com.fqf.mario_qua_mario.util.MarioGamerules;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarioQuaMario implements ModInitializer {
	public static final String MOD_ID = "mario_qua_mario";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final MQMConfig CONFIG;
	static {
		AutoConfig.register(MQMConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(MQMConfig.class).getConfig();
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Mario qua Mario initializing...");

//		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new PlayermodelListener());

		RegistryManager.registerAll();

		MarioCommand.registerMarioCommand();

		MarioPackets.register();

		MarioGamerules.register();
		MarioEventListeners.register();
	}

	public static Identifier makeID(String path) {
		return Identifier.of("mqm", path);
	}
	public static Identifier makeResID(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
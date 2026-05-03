package com.fqf.charapoweract;

import com.fqf.charapoweract.packets.CPAPackets;
import com.fqf.charapoweract.registries.RegistryManager;
import com.fqf.charapoweract.util.CPAGamerules;
import com.fqf.charapoweract.util.HelperGetterImplementation;
import com.fqf.charapoweract.util.CPAEventListeners;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharaPowerAct implements ModInitializer {
	public static final String MOD_ID = "mario_qua_mario";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final CharaPowerActConfig CONFIG;
	static {
		AutoConfig.register(CharaPowerActConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(CharaPowerActConfig.class).getConfig();
	}

	@Override
	public void onInitialize() {
		LOGGER.info("CharaPowerAct initializing...");

		HelperGetterImplementation.staticInitialize(); // Make sure the helpers are ready for action registration

		RegistryManager.registerAll();

		CharaPowerActCommand.registerMarioCommand();

		CPAPackets.register();

		CPAGamerules.register();
		CPAEventListeners.register();

	}

	public static Identifier makeID(String path) {
		return Identifier.of("mqm", path);
	}
	public static Identifier makeResID(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
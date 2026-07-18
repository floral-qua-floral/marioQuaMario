package com.fqf.charaformact;

import com.fqf.charaformact.appearance.CommonAppearanceCollector;
import com.fqf.charaformact.packets.CfaPackets;
import com.fqf.charaformact.registries.RegistryManager;
import com.fqf.charaformact.util.CfaGamerules;
import com.fqf.charaformact.util.HelperGetterImplementation;
import com.fqf.charaformact.util.CfaEventListeners;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharaFormAct implements ModInitializer {
	public static final String MOD_ID = "charaformact";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	static ClientHelper clientHelper;

	public static final CharaFormActConfig CONFIG;
	static {
		AutoConfig.register(CharaFormActConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(CharaFormActConfig.class).getConfig();
	}

	@Override
	public void onInitialize() {
		LOGGER.info("CharaFormAct initializing...");

		HelperGetterImplementation.staticInitialize(); // Make sure the helpers are ready for action registration

		RegistryManager.registerAll();

		CharaFormActCommand.registerCharaFormActCommand();

		CfaPackets.register();

		CfaGamerules.register();
		CfaEventListeners.register();

		CommonAppearanceCollector.INSTANCE.collect();
	}

	public static Identifier makeID(String path) {
		return Identifier.of("charaformact", path);
	}

	public static ClientHelper getClientHelper() {
		return clientHelper;
	}

	public static abstract class ClientHelper {
		public abstract ObjectIntPair<String> getAppearanceCoveringInformation();
		public abstract void prepareKeybindTexts();
	}
}
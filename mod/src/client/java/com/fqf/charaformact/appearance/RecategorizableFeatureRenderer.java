package com.fqf.charaformact.appearance;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact_api.appearance.equipment.EquipmentFeatureCategory;
import com.fqf.charaformact_api.appearance.equipment.EquipmentCategoryProvider;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.fqf.charaformact_api.appearance.equipment.EquipmentFeatureCategory.*;

public interface RecategorizableFeatureRenderer {
	boolean LOG_AUTO_CONTEXT_CHECKS = CharaFormAct.CONFIG.logFeatureContexts();
	Set<String> FEATURES_LOGGED = new HashSet<>();

	@NotNull EquipmentFeatureCategory cfa$getMutableCategory();

	default void cfa$recategorize(@NotNull EquipmentFeatureCategory newCategory) {
		throw new UnsupportedOperationException("Cannot override the Feature Transformation Context of feature {}! Did you do something weird??");
	}

	static @NotNull EquipmentFeatureCategory getInitialCategory(Object renderer) {
		if(renderer instanceof EquipmentCategoryProvider categoryProvider)
			return categoryProvider.cfa$defineEquipmentCategory();

		Class<?> clazz = renderer.getClass();

		String name = clazz.getSimpleName();
		if(
				checkContains(name, "back", true, false)
				|| checkContains(name, "elytra", false, false)
				|| checkContains(name, "cape", false, false)
				|| checkContains(name, "glove", false, false)
				|| checkContains(name, "hat", true, true)
		)
			return SPECIAL;

		// Belts are transformed the same way as chausses! Maybe remove this if inflation correction proves problematic.
		if(
				checkContains(name, "belt", false, false)
		)
			return ARMOR_INNER;

		// Modded shoes, non-armor boots, and skates are transformed the same way as armor boots!
		if(
				checkContains(name, "shoe", false, false)
				|| checkContains(name, "boot", false, false)
				|| checkContains(name, "skate", false, false)
		)
			return ARMOR_OUTER;



		if(LOG_AUTO_CONTEXT_CHECKS && FEATURES_LOGGED.add(name))
			CharaFormAct.LOGGER.info("Could not find a match that would put {} in a definitive transformation context," +
					"so its context will be UNKNOWN.", name);
		return UNKNOWN;
	}
	private static boolean checkContains(String name, String substring, boolean mustOpenWord, boolean mustTerminateWord) {
		Pattern pattern = Pattern.compile(substring, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(name);

		while(matcher.find()) {
			char firstCharacter = name.charAt(matcher.start());
			if(
					!mustOpenWord
					|| Character.isUpperCase(firstCharacter)
			) {
				int nextCharacterIndex = matcher.end();
				if (
						!mustTerminateWord
						|| nextCharacterIndex >= name.length()
						|| Character.isUpperCase(name.charAt(nextCharacterIndex))
						|| Character.isDigit(name.charAt(nextCharacterIndex))
				) {
					if(LOG_AUTO_CONTEXT_CHECKS && FEATURES_LOGGED.add(name)) {
						CharaFormAct.LOGGER.info("The feature renderer {} contains substring {}, and as such has been" +
								" identified as belonging to the SPECIAL transformation context.",
								name, substring);
					}
					return true;
				}
			}
		}

		return false;
	}
}

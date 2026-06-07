package com.fqf.charaformact.appearance;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.util.TransformationContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.fqf.charaformact.util.TransformationContext.*;

public interface FeatureRendererWithContext {
	boolean LOG_AUTO_CONTEXT_CHECKS = CharaFormAct.CONFIG.logFeatureContexts();
	Set<String> FEATURES_PARSED = new HashSet<>();

	@NotNull TransformationContext cfa$getContext();

	default void cfa$setContext(@NotNull TransformationContext newContext) {
		throw new UnsupportedOperationException("Cannot override the Feature Transformation Context of feature {}! Did you do something weird??");
	}

	static @NotNull TransformationContext getAssumedContext(Class<?> clazz) {
		String name = clazz.getSimpleName();
		if(
				checkContains(name, "back", true, false)
				|| checkContains(name, "elytra", false, false)
				|| checkContains(name, "glove", false, false)
				|| checkContains(name, "hat", true, true)
		)
			return SPECIAL;

		if(LOG_AUTO_CONTEXT_CHECKS && FEATURES_PARSED.add(name)) CharaFormAct.LOGGER.info("Could not find a match that would put {} in the Special" +
				" transformation context, so its context will be UNKNOWN.", name);
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
					if(LOG_AUTO_CONTEXT_CHECKS && FEATURES_PARSED.add(name)) {
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

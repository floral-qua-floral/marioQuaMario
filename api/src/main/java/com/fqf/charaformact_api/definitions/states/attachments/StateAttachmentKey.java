package com.fqf.charaformact_api.definitions.states.attachments;

import com.fqf.charaformact_api.cfadata.CfaData;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/// Used to attach arbitrary, mutable data to a player depending on their current state. Attachment keys are used to
/// retrieve that data, and must always contain a "Default Assigner" function. This function is used to create and store
/// a default value if the player has not already received that Attachment.
///
/// The player will automatically clear out certain Attachments after undergoing a state change matching those
/// Attachment's category. For example, upon doing an Action Transition, all Attachments in the ACTION category will be
/// reset. The next time they're accessed, they'll use their Default Assigner.
///
/// Here's a simple example of how these can be used:
/// ```
/// public static final StateAttachmentKey<MutableInt> TIMER = StateAttachmentKey.action(() -> new MutableInt());
///
/// @Override
/// public void commonTick(CfaData data) {
/// 	if(data.get(TIMER).getAndIncrement() % 4 == 0)
/// 		MyMod.LOGGER.info("Beep!");
/// }
/// ```
///
/// However, there's nothing stopping you from accessing Attachments from a State that doesn't match their Category.
/// An example use case could be if you wanted
///
/// Note that changing Character will also always reset the player's Form and Action, and so clears out every single
/// Attachment regardless of its category.
public final class StateAttachmentKey<T> {
	public static <T2> StateAttachmentKey<T2> of(
			AttachmentCategory category,
			Function<CfaData, T2> defaultAssigner,
			StateAttachmentFlag... flags
	) {
		return new StateAttachmentKey<>(category, defaultAssigner, flags);
	}
	public static <T2> StateAttachmentKey<T2> of(
			AttachmentCategory category,
			Supplier<T2> defaultAssigner,
			StateAttachmentFlag... flags
	) {
		return of(category, data -> defaultAssigner.get(), flags);
	}

	public static <T2> StateAttachmentKey<T2> action(Function<CfaData, T2> defaultAssigner, StateAttachmentFlag... flags) {
		return of(AttachmentCategory.ACTION, defaultAssigner, flags);
	}
	public static <T2> StateAttachmentKey<T2> action(Supplier<T2> defaultAssigner, StateAttachmentFlag... flags) {
		return of(AttachmentCategory.ACTION, defaultAssigner, flags);
	}
	public static <T2> StateAttachmentKey<T2> form(Function<CfaData, T2> defaultAssigner, StateAttachmentFlag... flags) {
		return of(AttachmentCategory.FORM, defaultAssigner, flags);
	}
	public static <T2> StateAttachmentKey<T2> form(Supplier<T2> defaultAssigner, StateAttachmentFlag... flags) {
		return of(AttachmentCategory.FORM, defaultAssigner, flags);
	}
	public static <T2> StateAttachmentKey<T2> character(Function<CfaData, T2> defaultAssigner, StateAttachmentFlag... flags) {
		return of(AttachmentCategory.CHARACTER, defaultAssigner, flags);
	}
	public static <T2> StateAttachmentKey<T2> character(Supplier<T2> defaultAssigner, StateAttachmentFlag... flags) {
		return of(AttachmentCategory.CHARACTER, defaultAssigner, flags);
	}

	public final AttachmentCategory CATEGORY;
	public final Function<CfaData, T> DEFAULT_ASSIGNER;

	// None of the code below this comment pertains at all to developers making addons using CharaFormAct.

	public final Set<StateAttachmentFlag> FLAGS;
	public final int INDEX;

	private StateAttachmentKey(AttachmentCategory category, Function<CfaData, T> defaultAssigner, StateAttachmentFlag... flags) {
		if(isFrozen) {
			throw new IllegalStateException("State Attachment registered at an inappropriate time! This is probably" +
					" caused by not statically initializing the Attachment Key while registering your CharaFormAct" +
					" content. This can be most easily remedied by making sure that your Key is stored as a static" +
					" final field of any Character, Action, or Form, or on the class you made extending" +
					" CharaFormActAddon.");
		}

		this.CATEGORY = category;
		this.DEFAULT_ASSIGNER = defaultAssigner;

		List<StateAttachmentFlag> flagsList = Arrays.asList(flags);
		this.FLAGS = Collections.unmodifiableSet(flagsList.isEmpty()
				? EnumSet.noneOf(StateAttachmentFlag.class)
				: EnumSet.copyOf(flagsList)
		);
		this.INDEX = NEXT_INDICES.computeIfAbsent(this.CATEGORY, cat -> new MutableInt(0)).getAndIncrement();
	}

	private static final EnumMap<AttachmentCategory, MutableInt> NEXT_INDICES = new EnumMap<>(AttachmentCategory.class);
	private static boolean isFrozen;
	public static int countInitializedAttachments(AttachmentCategory category) {
		return NEXT_INDICES.getOrDefault(category, new MutableInt()).intValue();
	}
	public static void freeze() {
		isFrozen = true;
	}
}

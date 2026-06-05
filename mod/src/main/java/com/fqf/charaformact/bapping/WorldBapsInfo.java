package com.fqf.charaformact.bapping;

import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableShort;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WorldBapsInfo {
	public final Map<BlockPos, AbstractBapInfo> ALL_BAPS = new HashMap<>();
	public final Set<BlockPos> HIDDEN = new HashSet<>();
	public final Set<LingeringInfo> HIDDEN_LINGERING = new HashSet<>();
	public final Set<BlockPos> BRITTLE = new HashSet<>();
	public final Set<BlockPos> POWERED = new HashSet<>();

	public static class LingeringInfo {
		public final BlockPos POS;
		public final boolean IS_BRITTLE;
		public int framesRemaining;

		public LingeringInfo(BlockPos pos, boolean isBrittle, int framesRemaining) {
			POS = pos;
			IS_BRITTLE = isBrittle;
			this.framesRemaining = framesRemaining;
		}
	}
}

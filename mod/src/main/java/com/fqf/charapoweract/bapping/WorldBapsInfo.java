package com.fqf.charapoweract.bapping;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WorldBapsInfo {
	public final Map<BlockPos, AbstractBapInfo> ALL_BAPS = new HashMap<>();
	public final Set<BlockPos> HIDDEN = new HashSet<>();
	public final Set<BlockPos> HIDDEN_LINGERING = new HashSet<>();
	public final Set<BlockPos> BRITTLE = new HashSet<>();
	public final Set<BlockPos> POWERED = new HashSet<>();
}

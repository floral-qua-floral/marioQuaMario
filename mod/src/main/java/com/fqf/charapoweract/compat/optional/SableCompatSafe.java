package com.fqf.charapoweract.compat.optional;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.*;

public class SableCompatSafe {
	public static final boolean SABLE_PRESENT = FabricLoader.getInstance().isModLoaded("sable");

	public static Triple<Vec3d, Vector3dc, Quaternionf> getPosAndOrientation(World world, BlockPos pos) {
		Vec3d posAsDoubles = Vec3d.of(pos);
		SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(world, pos);

		if (subLevelAccess != null) {
			Pose3dc pose = subLevelAccess.logicalPose();

			// Transform the position to global space
			return new ImmutableTriple<>(pose.transformPosition(posAsDoubles), pose.scale(), new Quaternionf(pose.orientation()));
		}

		return new ImmutableTriple<>(posAsDoubles, new Vector3d(1), new Quaternionf());
	}
}

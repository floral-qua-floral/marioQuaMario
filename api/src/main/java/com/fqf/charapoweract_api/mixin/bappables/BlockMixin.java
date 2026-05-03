package com.fqf.charapoweract_api.mixin.bappables;

import com.fqf.charapoweract_api.interfaces.Bappable;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public class BlockMixin implements Bappable {
}

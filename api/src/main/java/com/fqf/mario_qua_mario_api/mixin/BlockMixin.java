package com.fqf.mario_qua_mario_api.mixin;

import com.fqf.mario_qua_mario_api.interfaces.Bappable;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public class BlockMixin implements Bappable {
}

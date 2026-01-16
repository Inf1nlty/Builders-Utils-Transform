package net.dravigen.creative_tools.mixin;

import api.world.BlockPos;
import net.dravigen.creative_tools.api.ToolHelper;
import net.minecraft.src.*;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.dravigen.creative_tools.api.ToolHelper.getBlockPlayerIsLooking;
import static net.dravigen.creative_tools.api.ToolHelper.sendEditMsg;

@Mixin(EntityClientPlayerMP.class)
public abstract class EntityClientPlayerMPMixin extends EntityPlayer {
	
	@Unique
	boolean pressed = false;
	
	public EntityClientPlayerMPMixin(World par1World, String par2Str) {
		super(par1World, par2Str);
	}
	
	@Unique
	private static @NotNull String getBlockInfos(BlockPos pos, String s) {
		return String.format(s, pos.x, pos.y, pos.z);
	}
	
	@Inject(method = "onUpdate", at = @At("HEAD"))
	private void update(CallbackInfo ci) {
		MovingObjectPosition block = getBlockPlayerIsLooking(this);
		
		ItemStack heldItem = this.getHeldItem();
		
		if (heldItem == null) return;
		
		if (heldItem.getItem() == Item.axeWood && this.capabilities.isCreativeMode) {
			String pos1 = StatCollector.translateToLocal("commands.prefix") +
					StatCollector.translateToLocal("commands.pos1");
			String pos2 = StatCollector.translateToLocal("commands.prefix") +
					StatCollector.translateToLocal("commands.pos2");
			
			if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1) || Mouse.isButtonDown(2)) {
				if (!pressed && Minecraft.getMinecraft().currentScreen == null) {
					if (block != null) {
						BlockPos blockPos = new BlockPos(block.blockX, block.blockY, block.blockZ);
						if (Mouse.isButtonDown(0)) {
							// left
							ToolHelper.pos1 = blockPos;
							sendEditMsg(this, getBlockInfos(blockPos, pos1));
						}
						else if (Mouse.isButtonDown(1)) {
							// right
							ToolHelper.pos2 = blockPos;
							sendEditMsg(this, getBlockInfos(blockPos, pos2));
						}
					}
					
					if (Mouse.isButtonDown(2)) {
						// middle
						BlockPos blockPos = new BlockPos(MathHelper.floor_double(this.posX),
														 MathHelper.floor_double(this.posY),
														 MathHelper.floor_double(this.posZ));
						if (this.isUsingSpecialKey()) {
							ToolHelper.pos2 = blockPos;
							sendEditMsg(this, getBlockInfos(blockPos, pos2));
						}
						else {
							ToolHelper.pos1 = blockPos;
							sendEditMsg(this, getBlockInfos(blockPos, pos1));
						}
					}
				}
				
				pressed = true;
			}
			else {
				pressed = false;
			}
		}
	}
}

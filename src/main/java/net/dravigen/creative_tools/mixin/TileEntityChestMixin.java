package net.dravigen.creative_tools.mixin;

import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityChest;
import net.minecraft.src.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TileEntityChest.class)
public abstract class TileEntityChestMixin extends TileEntity {
	
	@Redirect(method = "checkForAdjacentChests", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/World;getBlockTileEntity(III)Lnet/minecraft/src/TileEntity;", ordinal = 0))
	private TileEntity additionalCheck1(World instance, int var6, int var7, int var5) {
		TileEntity tile = instance.getBlockTileEntity(var6, var7, var5);
		
		return tile instanceof TileEntityChest ? tile : null;
	}
	
	@Redirect(method = "checkForAdjacentChests", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/World;getBlockTileEntity(III)Lnet/minecraft/src/TileEntity;", ordinal = 1))
	private TileEntity additionalCheck2(World instance, int var6, int var7, int var5) {
		TileEntity tile = instance.getBlockTileEntity(var6, var7, var5);
		
		return tile instanceof TileEntityChest ? tile : null;
	}
	
	@Redirect(method = "checkForAdjacentChests", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/World;getBlockTileEntity(III)Lnet/minecraft/src/TileEntity;", ordinal = 2))
	private TileEntity additionalCheck3(World instance, int var6, int var7, int var5) {
		TileEntity tile = instance.getBlockTileEntity(var6, var7, var5);
		
		return tile instanceof TileEntityChest ? tile : null;
	}
	
	@Redirect(method = "checkForAdjacentChests", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/World;getBlockTileEntity(III)Lnet/minecraft/src/TileEntity;", ordinal = 3))
	private TileEntity additionalCheck4(World instance, int var6, int var7, int var5) {
		TileEntity tile = instance.getBlockTileEntity(var6, var7, var5);
		
		return tile instanceof TileEntityChest ? tile : null;
	}
}

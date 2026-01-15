package net.dravigen.creative_tools.mixin;

import net.minecraft.src.CrashReportCategory;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TileEntityRenderer.class)
public class TileEntityRendererMixin {
	
	@Redirect(method = "renderTileEntityAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/TileEntity;func_85027_a(Lnet/minecraft/src/CrashReportCategory;)V"))
	private void preventCrashWithNullTile(TileEntity tile,
			CrashReportCategory par1CrashReportCategory) {
		if (tile == null || tile.getBlockType() == null) return;
		
		tile.func_85027_a(par1CrashReportCategory);
	}
}

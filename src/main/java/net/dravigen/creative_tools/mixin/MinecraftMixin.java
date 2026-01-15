package net.dravigen.creative_tools.mixin;

import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow
	public EntityClientPlayerMP thePlayer;
	
	@Shadow
	protected abstract void sendClickBlockToController(int par1, boolean par2);
	
	@Unique
	private boolean hasSelectionTool() {
		return thePlayer.capabilities.isCreativeMode &&
				thePlayer.getHeldItem() != null &&
				thePlayer.getHeldItem().getItem() == Item.axeWood;
	}
	
	@Inject(method = "clickMouse", at = @At("HEAD"), cancellable = true)
	private void preventClick(int par1, CallbackInfo ci) {
		if (hasSelectionTool()) ci.cancel();
	}
	
	@Inject(method = "clickMiddleMouseButton", at = @At("HEAD"), cancellable = true)
	private void preventMiddleClick(CallbackInfo ci) {
		if (hasSelectionTool()) ci.cancel();
	}
	
	@Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/Minecraft;sendClickBlockToController(IZ)V"))
	private void preventBreak(Minecraft instance, int var4, boolean var5) {
		if (!hasSelectionTool()) {
			this.sendClickBlockToController(var4, var5);
		}
	}
}

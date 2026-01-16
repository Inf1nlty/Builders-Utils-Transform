package net.dravigen.creative_tools.mixin;

import api.item.items.AxeItem;
import api.item.items.ToolItem;
import net.minecraft.src.EnumToolMaterial;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AxeItem.class)
public abstract class AxeItemMixin extends ToolItem {
	protected AxeItemMixin(int iITemID, int iBaseEntityDamage, EnumToolMaterial par3EnumToolMaterial) {
		super(iITemID, iBaseEntityDamage, par3EnumToolMaterial);
	}
	
	
}

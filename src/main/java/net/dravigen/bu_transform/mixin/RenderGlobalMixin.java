package net.dravigen.bu_transform.mixin;

import net.minecraft.src.ICamera;
import net.minecraft.src.RenderGlobal;
import net.minecraft.src.Vec3;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.dravigen.bu_transform.api.ToolHelper.*;

@Mixin(RenderGlobal.class)
public class RenderGlobalMixin {
	
	@Inject(method = "renderEntities", at = @At("HEAD"))
	private void renderSelection(Vec3 par1Vec3, ICamera par2ICamera, float par3, CallbackInfo ci) {
		if (pos1 != null && pos2 != null) {
			double playerRenderX = par1Vec3.xCoord;
			double playerRenderY = par1Vec3.yCoord;
			double playerRenderZ = par1Vec3.zCoord;
			GL11.glPushMatrix();
			GL11.glTranslated(-playerRenderX, -playerRenderY, -playerRenderZ);
			
			int minX = Math.min(pos1.x, pos2.x);
			int minY = Math.min(pos1.y, pos2.y);
			int minZ = Math.min(pos1.z, pos2.z);
			int maxX = Math.max(pos1.x, pos2.x);
			int maxY = Math.max(pos1.y, pos2.y);
			int maxZ = Math.max(pos1.z, pos2.z);
			
			
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_LIGHTING);
			
			SelectionD selection = new SelectionD(new BlockPosD(minX - 0.005, minY - 0.005, minZ - 0.005),
												  new BlockPosD(maxX + 1 + 0.005, maxY + 1 + 0.005, maxZ + 1 + 0.005));
			
			
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			
			GL11.glColor3d(100, 0, 0);
			GL11.glLineWidth(0.5f);
			
			drawBox(selection);
			
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			
			GL11.glColor3d(255, 255, 0);
			GL11.glLineWidth(5);
			drawBox(selection);
			
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_LIGHTING);
			
			GL11.glPopMatrix();
		}
	}
	
	
	@Unique
	private void drawBox(SelectionD selection) {
		BlockPosD pos1 = selection.pos1();
		BlockPosD pos2 = selection.pos2();
		GL11.glBegin(GL11.GL_LINES);
		
		BlockPosD pos3 = new BlockPosD(pos1.x(), pos2.y(), pos2.z());
		BlockPosD pos6 = new BlockPosD(pos2.x(), pos1.y(), pos1.z());
		BlockPosD pos7 = new BlockPosD(pos1.x(), pos2.y(), pos1.z());
		BlockPosD pos5 = new BlockPosD(pos1.x(), pos1.y(), pos2.z());
		BlockPosD pos4 = new BlockPosD(pos2.x(), pos1.y(), pos2.z());
		BlockPosD pos8 = new BlockPosD(pos2.x(), pos2.y(), pos1.z());
		
		drawLine(pos1, pos6);
		drawLine(pos1, pos7);
		drawLine(pos1, pos5);
		
		drawLine(pos5, pos4);
		drawLine(pos6, pos4);
		drawLine(pos6, pos8);
		
		drawLine(pos2, pos3);
		drawLine(pos2, pos4);
		drawLine(pos2, pos8);
		
		drawLine(pos8, pos7);
		drawLine(pos3, pos7);
		drawLine(pos3, pos5);
		
		
		GL11.glEnd();
	}
	
	@Unique
	private void drawLine(BlockPosD pos1, BlockPosD pos2) {
		GL11.glVertex3d(pos1.x(), pos1.y(), pos1.z());
		GL11.glVertex3d(pos2.x(), pos2.y(), pos2.z());
	}
}

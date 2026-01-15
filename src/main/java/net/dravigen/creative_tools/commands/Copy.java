package net.dravigen.creative_tools.commands;

import api.world.BlockPos;
import net.dravigen.creative_tools.api.HelperCommand;
import net.minecraft.src.*;

import java.util.List;

import static net.dravigen.creative_tools.api.ToolHelper.*;
import static net.dravigen.creative_tools.api.HelperCommand.*;

public class Copy extends CommandBase {
	
	@Override
	public String getCommandName() {
		return "copy";
	}
	
	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/copy [x1/y1/z1] [x2/y2/z2]";
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] strings) {
		MovingObjectPosition block = getBlockPlayerIsLooking(sender);
		
		if (block != null && strings.length < 3) {
			return getListOfStringsMatchingLastWord(strings, block.blockX + "/" + block.blockY + "/" + block.blockZ);
		}
		
		return null;
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] strings) {
		if (strings.length == 0 && (pos1 == null || pos2 == null)) {
			HelperCommand.sendErrorMsg(sender, StatCollector.translateToLocal("commands.error.selection3"));
			
			return;
		}
		
		copyBlockList.clear();
		copyEntityList.clear();
		
		World world = sender.getEntityWorld();
		EntityPlayer player = getPlayer(sender, sender.getCommandSenderName());
		
		int x1 = strings.length == 1 ? Integer.parseInt(strings[0].split("/")[0]) : pos1.x;
		int y1 = strings.length == 1 ? Integer.parseInt(strings[0].split("/")[1]) : pos1.y;
		int z1 = strings.length == 1 ? Integer.parseInt(strings[0].split("/")[2]) : pos1.z;
		int x2 = strings.length == 2 ? Integer.parseInt(strings[1].split("/")[0]) : pos2.x;
		int y2 = strings.length == 2 ? Integer.parseInt(strings[1].split("/")[1]) : pos2.y;
		int z2 = strings.length == 2 ? Integer.parseInt(strings[1].split("/")[2]) : pos2.z;
		
		int minX = Math.min(x1, x2);
		int minY = Math.min(y1, y2);
		int minZ = Math.min(z1, z2);
		int maxX = Math.max(x1, x2);
		int maxY = Math.max(y1, y2);
		int maxZ = Math.max(z1, z2);
		
		int blockNum = 0;
		int entityNum = 0;
		
		List<Entity> entitiesInSelection = world.getEntitiesWithinAABBExcludingEntity(player, new AxisAlignedBB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1));
		
		if (!entitiesInSelection.isEmpty()) {
			for (Entity entity : entitiesInSelection) {
				if (entity instanceof EntityPlayer) continue;
				NBTTagCompound nbt = new NBTTagCompound();
				entity.writeToNBT(nbt);
				copyEntityList.add(new EntityInfo(new LocAndAngle(entity.posX - minX,
															entity.posY - minY,
															entity.posZ - minZ,
															entity.rotationYaw,
															entity.rotationPitch), entity.getClass(), nbt));
				entityNum++;
			}
		}
		
		for (int y = minY; y <= maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					int id = world.getBlockId(x, y, z);
					int meta = world.getBlockMetadata(x, y, z);
					TileEntity tile = world.getBlockTileEntity(x, y, z);
					
					NBTTagCompound tileNBT = null;
					
					if (tile != null) {
						tileNBT = new NBTTagCompound();
						tile.writeToNBT(tileNBT);
						tileNBT.removeTag("x");
						tileNBT.removeTag("y");
						tileNBT.removeTag("z");
					}
					
					copyBlockList.add(blockNum, new BlockInfo(x - minX, y - minY, z - minZ, id, meta, tileNBT));
					
					blockNum++;
				}
			}
		}
		
		sendEditMsg(sender, StatCollector.translateToLocal("commands.prefix") + String.format(StatCollector.translateToLocal("commands.copy"), blockNum, entityNum));
	}
}
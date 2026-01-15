package net.dravigen.creative_tools.commands;

import api.world.BlockPos;
import net.minecraft.src.*;

import java.util.*;

import static net.dravigen.creative_tools.api.ToolHelper.*;
import static net.dravigen.creative_tools.api.HelperCommand.*;

public class Paste extends CommandBase {
	@Override
	public String getCommandName() {
		return "paste";
	}
	
	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/paste [x/y/z]";
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] strings) {
		MovingObjectPosition block = getBlockPlayerIsLooking(sender);
		
		if (block != null && strings.length < 2) {
			return getListOfStringsMatchingLastWord(strings, block.blockX + "/" + block.blockY + "/" + block.blockZ);
		}
		
		return null;
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] strings) {
		if (strings.length > 3) {
			sendErrorMsg(sender, "Just one spot to paste, mate.");
			
			return;
		}
		
		EntityPlayer player = getPlayer(sender, sender.getCommandSenderName());
		World world = sender.getEntityWorld();
		
		int xLoc = strings.length == 1 ? Integer.parseInt(strings[0].split("/")[0]) : MathHelper.floor_double(player.posX);
		int yLoc = strings.length == 1 ? Integer.parseInt(strings[0].split("/")[1]) : MathHelper.floor_double(player.posY);
		int zLoc = strings.length == 1 ? Integer.parseInt(strings[0].split("/")[2]) : MathHelper.floor_double(player.posZ);
		
		List<BlockInfo> pasteNonBlockList = new ArrayList<>();
		Queue<BlockInfo> pasteBlockList = new LinkedList<>();
		List<EntityInfo> entities = new ArrayList<>();
		Queue<BlockToRemoveInfo> blocksToRemove = new LinkedList<>();
		
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		
		
		if (!copyEntityList.isEmpty()) {
			for (EntityInfo entity : copyEntityList) {
				LocAndAngle locAndAngle = entity.locAndAngle();
				entities.add(new EntityInfo(new LocAndAngle(locAndAngle.x() + xLoc,
															locAndAngle.y() + yLoc,
															locAndAngle.z() + zLoc,
															locAndAngle.yaw(),
															locAndAngle.pitch()), entity.entityClass(), entity.nbt()));
			}
		}
		
		for (BlockInfo info : copyBlockList) {
			int x = info.x() + xLoc;
			int y = info.y() + yLoc;
			int z = info.z() + zLoc;
			
			minX = Math.min(minX, x);
			minY = Math.min(minY, y);
			minZ = Math.min(minZ, z);
			maxX = Math.max(maxX, x);
			maxY = Math.max(maxY, y);
			maxZ = Math.max(maxZ, z);
			
			BlockInfo pasteInfo = new BlockInfo(x, y, z, info.id(), info.meta(), info.tile());
			
			Block block = Block.blocksList[info.id()];
			
			if (block != null) {
				if ((!block.canPlaceBlockOnSide(world, 0, 254, 0, 1) ||
						block instanceof BlockFluid ||
						block.isFallingBlock() ||
						!block.canPlaceBlockAt(world, 0, 254, 0))) {
					pasteNonBlockList.add(pasteInfo);
				}
				else {
					pasteBlockList.add(pasteInfo);
				}
			}
			else {
				pasteBlockList.add(pasteInfo);
			}
		}

		editList.add(new QueueInfo(new Selection(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ)), pasteNonBlockList, pasteBlockList, new LinkedList<>(), entities, blocksToRemove, yLoc, 0, player));
	}
}

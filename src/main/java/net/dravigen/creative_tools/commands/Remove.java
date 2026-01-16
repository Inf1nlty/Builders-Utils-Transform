package net.dravigen.creative_tools.commands;

import api.world.BlockPos;
import net.dravigen.creative_tools.api.HelperCommand;
import net.minecraft.src.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static net.dravigen.creative_tools.api.HelperCommand.sendEditMsg;
import static net.dravigen.creative_tools.api.ToolHelper.*;
import static net.dravigen.creative_tools.api.ToolHelper.SAVED_NUM;

public class Remove extends CommandBase {
	@Override
	public String getCommandName() {
		return "remove";
	}
	
	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/remove [x1/y1/z1] [x2/y2/z2]";
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
			HelperCommand.sendErrorMsg(sender, StatCollector.translateToLocal("commands.error.selection2"));
			
			return;
		}
		
		redoList.clear();
		World world = sender.getEntityWorld();
		EntityPlayer player = getPlayer(sender, sender.getCommandSenderName());
		
		int x1 = strings.length == 2 ? Integer.parseInt(strings[0].split("/")[0]) : pos1.x;
		int y1 = strings.length == 2 ? Integer.parseInt(strings[0].split("/")[1]) : pos1.y;
		int z1 = strings.length == 2 ? Integer.parseInt(strings[0].split("/")[2]) : pos1.z;
		int x2 = strings.length == 2 ? Integer.parseInt(strings[1].split("/")[0]) : pos2.x;
		int y2 = strings.length == 2 ? Integer.parseInt(strings[1].split("/")[1]) : pos2.y;
		int z2 = strings.length == 2 ? Integer.parseInt(strings[1].split("/")[2]) : pos2.z;
		
		int minX = Math.min(x1, x2);
		int minY = Math.min(y1, y2);
		int minZ = Math.min(z1, z2);
		int maxX = Math.max(x1, x2);
		int maxY = Math.max(y1, y2);
		int maxZ = Math.max(z1, z2);
		
		Selection selection = new Selection(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
		
		Queue<BlockToRemoveInfo> blocksToRemove = new LinkedList<>();
		
		for (int y = minY; y <= maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					blocksToRemove.add(new BlockToRemoveInfo(x, y, z, world.blockHasTileEntity(x, y, z)));
				}
			}
		}
		
		sendEditMsg(sender,
					StatCollector.translateToLocal("commands.prefix") +
							StatCollector.translateToLocal("commands.remove"));
		SavedLists edit = new SavedLists(new ArrayList<>(),
										 new LinkedList<>(),
										 new LinkedList<>(),
										 new ArrayList<>(),
										 new LinkedList<>(blocksToRemove));
		editList.add(new QueueInfo("remove",
								   selection,
								   edit,
								   createEmptySavedList(),
								   duplicateSavedList(edit),
								   minY,
								   new int[SAVED_NUM],
								   player,
								   false));
	}
}

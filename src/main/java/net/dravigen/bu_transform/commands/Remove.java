package net.dravigen.bu_transform.commands;

import api.world.BlockPos;
import net.minecraft.src.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static net.dravigen.bu_transform.api.ToolHelper.*;

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
	public void processCommand(ICommandSender sender, String[] strings) {
		try {
			BlockPos pos1 = pos1PlayersMap.get(getPlayer(sender, sender.getCommandSenderName()));
			BlockPos pos2 = pos2PlayersMap.get(getPlayer(sender, sender.getCommandSenderName()));
			
			if (strings.length != 2 && (pos1 == null || pos2 == null)) {
				sendErrorMsg(sender, "bu.transform.commands.error.selection2");
				
				return;
			}
			
			if (strings.length == 2 && (strings[0].split("/").length != 3 || strings[1].split("/").length != 3)) {
				sendErrorMsg(sender, "bu.transform.commands.error.format");
				
				return;
			}
			
			redoPlayersMap.get(sender).clear();
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
			List<Selection> selections = new ArrayList<>();
			selections.add(selection);
			
			Queue<BlockToRemoveInfo> blocksToRemove = new LinkedList<>();
			List<BlockInfo> undoNonBlock = new ArrayList<>();
			Queue<BlockInfo> undoBlock = new LinkedList<>();
			List<EntityInfo> undoEntity = new ArrayList<>();
			
			for (int y = minY; y <= maxY; y++) {
				for (int x = minX; x <= maxX; x++) {
					for (int z = minZ; z <= maxZ; z++) {
						blocksToRemove.add(new BlockToRemoveInfo(x, y, z, world.blockHasTileEntity(x, y, z)));
						
						saveBlockReplaced(world, x, y, z, undoBlock, undoNonBlock);
					}
				}
			}
			
			saveReplacedEntities(world, player, selection, undoEntity);
			
			sendEditMsg(sender, "bu.transform.commands.remove");
			SavedLists edit = new SavedLists(new ArrayList<>(),
											 new LinkedList<>(),
											 new LinkedList<>(),
											 new ArrayList<>(),
											 new LinkedList<>(blocksToRemove));
			SavedLists undo = new SavedLists(new ArrayList<>(undoNonBlock),
											 new LinkedList<>(undoBlock),
											 new LinkedList<>(),
											 new ArrayList<>(undoEntity),
											 new LinkedList<>());
			
			editList.add(new QueueInfo("remove",
									   selections,
									   edit,
									   undo,
									   duplicateSavedList(edit),
									   new int[SAVED_NUM],
									   player));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] strings) {
		MovingObjectPosition block = getBlockSenderIsLooking(sender);
		
		if (block != null && strings.length < 3) {
			return getListOfStringsMatchingLastWord(strings, block.blockX + "/" + block.blockY + "/" + block.blockZ);
		}
		
		return null;
	}
}

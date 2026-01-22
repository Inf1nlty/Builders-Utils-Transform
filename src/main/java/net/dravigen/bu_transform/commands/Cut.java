package net.dravigen.bu_transform.commands;

import api.world.BlockPos;
import net.minecraft.src.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static net.dravigen.bu_transform.api.ToolHelper.*;

public class Cut extends CommandBase {
	@Override
	public String getCommandName() {
		return "cut";
	}
	
	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/cut [x1/y1/z1] [x2/y2/z2]";
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] strings) {
		if (strings.length == 0 && (pos1 == null || pos2 == null)) {
			sendErrorMsg(sender, StatCollector.translateToLocal("commands.error.selection2"));
			
			return;
		}
		
		redoList.clear();
		copyBlockList.clear();
		copyEntityList.clear();
		
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
		
		List<Entity> entitiesInSelection = world.getEntitiesWithinAABBExcludingEntity(player,
																					  new AxisAlignedBB(minX,
																										minY,
																										minZ,
																										maxX + 1,
																										maxY + 1,
																										maxZ + 1));
		Queue<BlockToRemoveInfo> blocksToRemove = new LinkedList<>();
		List<BlockInfo> undoNonBlock = new ArrayList<>();
		Queue<BlockInfo> undoBlock = new LinkedList<>();
		List<EntityInfo> undoEntity = new ArrayList<>();
		
		copyEntityInSelection(entitiesInSelection, copyEntityList, minX, minY, minZ, undoEntity);
		
		copyRemoveBlockSelection(minY,
								 maxY,
								 minX,
								 maxX,
								 minZ,
								 maxZ,
								 world,
								 undoNonBlock,
								 undoBlock,
								 copyBlockList,
								 blocksToRemove);
		sendEditMsg(sender,
					StatCollector.translateToLocal("commands.prefix") + StatCollector.translateToLocal("commands.cut"));
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
		
		editList.add(new QueueInfo("cut",
								   selections,
								   edit,
								   undo,
								   duplicateSavedList(edit),
								   new int[SAVED_NUM],
								   player));
		
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] strings) {
		MovingObjectPosition block = getBlockPlayerIsLooking(sender);
		
		if (block != null && strings.length < 3) {
			return getListOfStringsMatchingLastWord(strings, block.blockX + "/" + block.blockY + "/" + block.blockZ);
		}
		
		return null;
	}
}

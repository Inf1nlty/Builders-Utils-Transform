package net.dravigen.bu_transform.commands;

import api.world.BlockPos;
import net.dravigen.bu_transform.api.PacketUtils;
import net.minecraft.src.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static net.dravigen.bu_transform.api.ToolHelper.*;

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
	public void processCommand(ICommandSender sender, String[] strings) {
		try {
			Queue<BlockInfo> copyBlocks = copyBlockPlayersMap.get(sender);
			List<EntityInfo> copyEntity = copyEntityPlayersMap.get(sender);
			
			if (copyBlocks.isEmpty() && copyEntity.isEmpty()) {
				sendErrorMsg(sender, "bu.transform.commands.error.copy/paste");
				
				return;
			}
			
			if (strings.length == 1 && strings[0].split("/").length != 3) {
				sendErrorMsg(sender, "bu.transform.commands.error.format");
				
				return;
			}
			
			redoPlayersMap.get(sender).clear();
			EntityPlayer player = getPlayer(sender, sender.getCommandSenderName());
			World world = sender.getEntityWorld();
			
			int xLoc = strings.length == 1
					   ? Integer.parseInt(strings[0].split("/")[0])
					   : MathHelper.floor_double(player.posX);
			int yLoc = strings.length == 1
					   ? Integer.parseInt(strings[0].split("/")[1])
					   : MathHelper.floor_double(player.posY);
			int zLoc = strings.length == 1
					   ? Integer.parseInt(strings[0].split("/")[2])
					   : MathHelper.floor_double(player.posZ);
			
			List<BlockInfo> nonBlockList = new ArrayList<>();
			Queue<BlockInfo> blockList = new LinkedList<>();
			List<EntityInfo> entities = new ArrayList<>();
			
			Queue<BlockInfo> undoBlock = new LinkedList<>();
			List<BlockInfo> undoNonBlock = new ArrayList<>();
			List<EntityInfo> undoEntity = new ArrayList<>();
			
			int minX = Integer.MAX_VALUE;
			int minY = Integer.MAX_VALUE;
			int minZ = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE;
			int maxY = Integer.MIN_VALUE;
			int maxZ = Integer.MIN_VALUE;
			
			saveEntitiesToPlace(copyEntity, entities, xLoc, yLoc, zLoc);
			
			for (BlockInfo info : copyBlocks) {
				int x = info.x() + xLoc;
				int y = info.y() + yLoc;
				int z = info.z() + zLoc;
				
				minX = Math.min(minX, x);
				minY = Math.min(minY, y);
				minZ = Math.min(minZ, z);
				maxX = Math.max(maxX, x);
				maxY = Math.max(maxY, y);
				maxZ = Math.max(maxZ, z);
				
				saveBlockToPlace(info, x, y, z, world, nonBlockList, blockList);
				saveBlockReplaced(world, x, y, z, undoBlock, undoNonBlock);
			}
			
			Selection selection = new Selection(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
			List<Selection> selections = new ArrayList<>();
			selections.add(selection);
			
			saveReplacedEntities(world, player, selection, undoEntity);
			
			SavedLists edit = new SavedLists(new ArrayList<>(nonBlockList),
											 new LinkedList<>(blockList),
											 new LinkedList<>(),
											 new ArrayList<>(entities),
											 new LinkedList<>());
			SavedLists undo = new SavedLists(new ArrayList<>(undoNonBlock),
											 new LinkedList<>(undoBlock),
											 new LinkedList<>(),
											 new ArrayList<>(undoEntity),
											 new LinkedList<>());
			Result result = new Result(edit, undo);
			
			pos1PlayersMap.put(sender, selection.pos1());
			pos2PlayersMap.put(sender, selection.pos2());
			PacketUtils.sendPosUpdate(1, (EntityPlayerMP) sender);
			PacketUtils.sendPosUpdate(2, (EntityPlayerMP) sender);
			
			editList.add(new QueueInfo("paste",
									   selections,
									   result.edit(),
									   result.undo(),
									   duplicateSavedList(result.edit()),
									   new int[SAVED_NUM],
									   player));
			
			sendEditMsg(sender, "bu.transform.commands.paste");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] strings) {
		MovingObjectPosition block = getBlockSenderIsLooking(sender);
		
		if (block != null && strings.length < 2) {
			return getListOfStringsMatchingLastWord(strings, block.blockX + "/" + block.blockY + "/" + block.blockZ);
		}
		
		return null;
	}
	
	private record Result(SavedLists edit, SavedLists undo) {}
}

package net.dravigen.creative_tools.commands;

import api.world.BlockPos;
import net.minecraft.src.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static net.dravigen.creative_tools.api.ToolHelper.*;

public class Move extends CommandBase {
	@Override
	public String getCommandName() {
		return "move";
	}
	
	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/move <add|to> [x/y/z]";
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] strings) {
		try {
			if (pos1 == null || pos2 == null) {
				sendErrorMsg(sender, StatCollector.translateToLocal("commands.error.selection2"));
				
				return;
			}
			
			redoList.clear();
			World world = sender.getEntityWorld();
			EntityPlayer player = getPlayer(sender, sender.getCommandSenderName());
			
			int x1 = pos1.x;
			int y1 = pos1.y;
			int z1 = pos1.z;
			int x2 = pos2.x;
			int y2 = pos2.y;
			int z2 = pos2.z;
			
			int minX = Math.min(x1, x2);
			int minY = Math.min(y1, y2);
			int minZ = Math.min(z1, z2);
			int maxX = Math.max(x1, x2);
			int maxY = Math.max(y1, y2);
			int maxZ = Math.max(z1, z2);
			
			int x3 = Integer.parseInt(strings[1].split("/")[0]) + (strings[0].equalsIgnoreCase("add") ? minX : 0);
			int y3 = Integer.parseInt(strings[1].split("/")[1]) + (strings[0].equalsIgnoreCase("add") ? minY : 0);
			int z3 = Integer.parseInt(strings[1].split("/")[2]) + (strings[0].equalsIgnoreCase("add") ? minZ : 0);
			
			Selection selection1 = new Selection(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
			
			
			List<EntityInfo> entities = new ArrayList<>();
			List<Entity> entitiesInSelection = world.getEntitiesWithinAABBExcludingEntity(player,
																						  new AxisAlignedBB(minX,
																											minY,
																											minZ,
																											maxX + 1,
																											maxY + 1,
																											maxZ + 1));
			Queue<BlockToRemoveInfo> blocksToRemove = new LinkedList<>();
			Queue<BlockInfo> moveBlockList = new LinkedList<>();
			
			List<BlockInfo> undoNonBlock1 = new ArrayList<>();
			Queue<BlockInfo> undoBlock1 = new LinkedList<>();
			List<EntityInfo> undoEntity1 = new ArrayList<>();
			List<BlockInfo> undoNonBlock = new ArrayList<>();
			Queue<BlockInfo> undoBlock = new LinkedList<>();
			List<EntityInfo> undoEntity = new ArrayList<>();
			
			
			for (Entity entity : entitiesInSelection) {
				if (entity instanceof EntityPlayer) continue;
				
				NBTTagCompound nbt = new NBTTagCompound();
				entity.writeToNBT(nbt);
				entities.add(new EntityInfo(new LocAndAngle(entity.posX - minX + x3,
															entity.posY - minY + y3,
															entity.posZ - minZ + z3,
															entity.rotationYaw,
															entity.rotationPitch), entity.getClass(), nbt));
				
				undoEntity1.add(new EntityInfo(new LocAndAngle(entity.posX,
															   entity.posY,
															   entity.posZ,
															   entity.rotationYaw,
															   entity.rotationPitch), entity.getClass(), nbt));
			}
			
			
			copyRemoveBlockSelection(minY,
									 maxY,
									 minX,
									 maxX,
									 minZ,
									 maxZ,
									 world,
									 undoNonBlock1,
									 undoBlock1,
									 moveBlockList,
									 blocksToRemove);
			
			List<BlockInfo> nonBlockList = new ArrayList<>();
			Queue<BlockInfo> blockList = new LinkedList<>();
			
			minX = Integer.MAX_VALUE;
			minY = Integer.MAX_VALUE;
			minZ = Integer.MAX_VALUE;
			maxX = Integer.MIN_VALUE;
			maxY = Integer.MIN_VALUE;
			maxZ = Integer.MIN_VALUE;
			
			for (BlockInfo info : moveBlockList) {
				int x = info.x() + x3;
				int y = info.y() + y3;
				int z = info.z() + z3;
				
				minX = Math.min(minX, x);
				minY = Math.min(minY, y);
				minZ = Math.min(minZ, z);
				maxX = Math.max(maxX, x);
				maxY = Math.max(maxY, y);
				maxZ = Math.max(maxZ, z);
				
				saveBlockToPlace(info, x, y, z, world, nonBlockList, blockList);
				saveBlockReplaced(world, x, y, z, undoNonBlock, undoBlock);
			}
			
			pos1 = new BlockPos(minX, minY, minZ);
			pos2 = new BlockPos(maxX, maxY, maxZ);
			
			Selection selection2 = new Selection(pos1, pos2);
			
			saveReplacedEntities(world, player, selection2, undoEntity);
			
			undoBlock.addAll(undoBlock1);
			undoNonBlock.addAll(undoNonBlock1);
			undoEntity.addAll(undoEntity1);
			
			SavedLists edit = new SavedLists(new ArrayList<>(nonBlockList),
											 new LinkedList<>(blockList),
											 new LinkedList<>(),
											 new ArrayList<>(entities),
											 new LinkedList<>(blocksToRemove));
			SavedLists undo = new SavedLists(new ArrayList<>(undoNonBlock),
											 new LinkedList<>(undoBlock),
											 new LinkedList<>(),
											 new ArrayList<>(undoEntity),
											 new LinkedList<>());
			
			List<Selection> selections = new ArrayList<>();
			selections.add(selection1);
			selections.add(selection2);
			
			editList.add(new QueueInfo("move",
									   selections,
									   edit,
									   undo,
									   duplicateSavedList(edit),
									   new int[SAVED_NUM],
									   player));
			
			sendEditMsg(sender,
						StatCollector.translateToLocal("commands.prefix") +
								StatCollector.translateToLocal("commands.move"));
		} catch (Exception e) {
			sendErrorMsg(sender, StatCollector.translateToLocal("commands.error.error"));
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] strings) {
		MovingObjectPosition block = getBlockPlayerIsLooking(sender);
		if (strings.length == 1) {
			return getListOfStringsMatchingLastWord(strings, "to", "add");
		}
		if (block != null && strings.length == 2 && strings[0].equalsIgnoreCase("to")) {
			return getListOfStringsMatchingLastWord(strings, block.blockX + "/" + block.blockY + "/" + block.blockZ);
		}
		
		return null;
	}
}


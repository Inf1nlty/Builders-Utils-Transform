package net.dravigen.bu_transform.commands;

import api.world.BlockPos;
import net.minecraft.src.*;

import java.util.*;

import static net.dravigen.bu_transform.api.ToolHelper.*;

public class Stack extends CommandBase {
	String[] dir = new String[]{"+x", "-x", "+y", "-y", "+z", "-z"};
	
	@Override
	public String getCommandName() {
		return "stack";
	}
	
	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/stack [+x|-x|+y|-y|+z|-z] [number of stack]";
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] strings) {
		try {
			BlockPos pos1 = pos1PlayersMap.get(getPlayer(sender, sender.getCommandSenderName()));
			BlockPos pos2 = pos2PlayersMap.get(getPlayer(sender, sender.getCommandSenderName()));
			
			if (pos1 == null || pos2 == null) {
				sendErrorMsg(sender, "bu.transform.commands.error.selectionArea");
				
				return;
			}
			
			if (strings.length == 1 && Arrays.stream(dir).noneMatch(s -> s.equalsIgnoreCase(strings[0]))) {
				try {
					Integer.parseInt(strings[0]);
				} catch (NumberFormatException e) {
					sendErrorMsg(sender, "bu.transform.commands.error.stack");
					
					return;
				}
			}
			
			redoPlayersMap.get(sender).clear();
			World world = sender.getEntityWorld();
			EntityPlayer player = getPlayer(sender, sender.getCommandSenderName());
			
			int x1 = pos1.x;
			int y1 = pos1.y;
			int z1 = pos1.z;
			int x2 = pos2.x;
			int y2 = pos2.y;
			int z2 = pos2.z;
			
			String direction = strings.length == 0 ? "eye" : strings[0];
			
			if (strings.length == 1) {
				try {
					Integer.parseInt(strings[0]);
					
					direction = "eye";
				} catch (Exception ignored) {
				}
			}
			
			int stackNum = direction.equals("eye") && strings.length == 1
						   ? Integer.parseInt(strings[0])
						   : strings.length == 2 ? Integer.parseInt(strings[1]) : 1;
			
			if (direction.equals("eye")) {
				Vec3 eye = player.getLookVec();
				double x = eye.xCoord;
				double y = eye.yCoord;
				double z = eye.zCoord;
				
				double absX = Math.abs(x);
				double absY = Math.abs(y);
				double absZ = Math.abs(z);
				
				if (absX >= absY && absX >= absZ) {
					direction = (x > 0) ? "+x" : "-x";
				}
				else if (absY >= absX && absY >= absZ) {
					direction = (y > 0) ? "+y" : "-y";
				}
				else {
					direction = (z > 0) ? "+z" : "-z";
				}
			}
			
			boolean xP = direction.equalsIgnoreCase("+x");
			boolean xN = direction.equalsIgnoreCase("-x");
			boolean yP = direction.equalsIgnoreCase("+y");
			boolean yN = direction.equalsIgnoreCase("-y");
			boolean zP = direction.equalsIgnoreCase("+z");
			boolean zN = direction.equalsIgnoreCase("-z");
			
			
			int minX = Math.min(x1, x2);
			int minY = Math.min(y1, y2);
			int minZ = Math.min(z1, z2);
			int maxX = Math.max(x1, x2);
			int maxY = Math.max(y1, y2);
			int maxZ = Math.max(z1, z2);
			
			List<Entity> entitiesInSelection = world.getEntitiesWithinAABBExcludingEntity(player,
																						  new AxisAlignedBB(minX,
																											minY,
																											minZ,
																											maxX + 1,
																											maxY + 1,
																											maxZ + 1));
			List<EntityInfo> entities = new ArrayList<>();
			Queue<BlockInfo> moveBlockList = new LinkedList<>();
			List<BlockInfo> undoNonBlock = new ArrayList<>();
			Queue<BlockInfo> undoBlock = new LinkedList<>();
			List<EntityInfo> undoEntity = new ArrayList<>();
			
			if (!entitiesInSelection.isEmpty()) {
				for (Entity entity : entitiesInSelection) {
					if (entity instanceof EntityPlayer) continue;
					NBTTagCompound nbt = new NBTTagCompound();
					entity.writeToNBT(nbt);
					entities.add(new EntityInfo(new LocAndAngle(entity.posX,
																entity.posY,
																entity.posZ,
																entity.rotationYaw,
																entity.rotationPitch), entity.getClass(), nbt));
				}
			}
			
			for (int y = minY; y <= maxY; y++) {
				for (int x = minX; x <= maxX; x++) {
					for (int z = minZ; z <= maxZ; z++) {
						getBlocksInfo result = getGetBlocksInfo(world, x, y, z);
						int id = result.id();
						int meta = result.meta();
						TileEntity tile = result.tile();
						
						NBTTagCompound tileNBT = null;
						
						if (tile != null) {
							tileNBT = new NBTTagCompound();
							tile.writeToNBT(tileNBT);
							tileNBT.removeTag("x");
							tileNBT.removeTag("y");
							tileNBT.removeTag("z");
						}
						
						BlockInfo info = new BlockInfo(x, y, z, id, meta, tileNBT);
						
						Block block = Block.blocksList[id];
						
						if (block != null) {
							if ((!block.canPlaceBlockOnSide(world, 0, 254, 0, 1) ||
									block instanceof BlockFluid ||
									block.isFallingBlock() ||
									!block.canPlaceBlockAt(world, 0, 254, 0))) {
								undoNonBlock.add(info);
							}
							else {
								undoBlock.add(info);
							}
						}
						else {
							undoBlock.add(info);
						}
						
						moveBlockList.add(info);
					}
				}
			}
			
			List<BlockInfo> nonBlockList = new ArrayList<>();
			Queue<BlockInfo> blockList = new LinkedList<>();
			List<EntityInfo> entitiesToPaste = new ArrayList<>();
			
			int xDist = minX - maxX;
			int yDist = minY - maxY;
			int zDist = minZ - maxZ;
			
			minX = Integer.MAX_VALUE;
			minY = Integer.MAX_VALUE;
			minZ = Integer.MAX_VALUE;
			maxX = Integer.MIN_VALUE;
			maxY = Integer.MIN_VALUE;
			maxZ = Integer.MIN_VALUE;
			
			for (int i = 1; i <= stackNum; i++) {
				int x3 = (xP ? (-xDist + 1) * i : xN ? (xDist - 1) * i : 0);
				int y3 = (yP ? (-yDist + 1) * i : yN ? (yDist - 1) * i : 0);
				int z3 = (zP ? (-zDist + 1) * i : zN ? (zDist - 1) * i : 0);
				
				saveEntitiesToPlace(entities, entitiesToPaste, x3, y3, z3);
				
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
					saveBlockReplaced(world, x, y, z, undoBlock);
				}
			}
			
			
			Selection selection = new Selection(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
			List<Selection> selections = new ArrayList<>();
			selections.add(selection);
			
			saveReplacedEntities(world, player, selection, undoEntity);
			
			SavedLists edit = new SavedLists(new ArrayList<>(nonBlockList),
											 new LinkedList<>(blockList),
											 new LinkedList<>(),
											 new ArrayList<>(entitiesToPaste),
											 new LinkedList<>());
			SavedLists undo = new SavedLists(new ArrayList<>(undoNonBlock),
											 new LinkedList<>(undoBlock),
											 new LinkedList<>(),
											 new ArrayList<>(undoEntity),
											 new LinkedList<>());
			
			editList.add(new QueueInfo("stack",
									   selections,
									   edit,
									   undo,
									   duplicateSavedList(edit),
									   new int[SAVED_NUM],
									   player));
			
			sendEditMsg(sender, "bu.transform.commands.stack", stackNum, direction);
		} catch (NumberFormatException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] strings) {
		return getListOfStringsMatchingLastWord(strings, dir);
	}
}

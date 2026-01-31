package net.dravigen.bu_transform.commands;

import api.world.BlockPos;
import net.dravigen.bu_transform.api.PacketUtils;
import net.minecraft.src.*;

import java.util.*;

import static net.dravigen.bu_transform.api.ToolHelper.*;

public class Rotate extends CommandBase {
	@Override
	public String getCommandName() {
		return "rotate";
	}
	
	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/rotate [x1/y1/z1] [x2/y2/z2] [true|false]";
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
			
			List<EntityInfo> entities = new ArrayList<>();
			List<Entity> entitiesInSelection = world.getEntitiesWithinAABBExcludingEntity(player,
																						  new AxisAlignedBB(minX,
																											minY,
																											minZ,
																											maxX + 1,
																											maxY + 1,
																											maxZ + 1));
			Queue<BlockInfo> moveBlockList = new LinkedList<>();
			
			List<EntityInfo> undoEntity = new ArrayList<>();
			Queue<BlockInfo> undoBlock = new LinkedList<>();
			List<BlockInfo> undoNonBlock = new ArrayList<>();
			Queue<BlockToRemoveInfo> blocksToRemove = new LinkedList<>();
			
			boolean clockwise = Arrays.stream(strings).anyMatch(s -> s.equalsIgnoreCase("false"));
			
			for (Entity entity : entitiesInSelection) {
				if (entity instanceof EntityPlayer) continue;
				
				double centerX = (maxX + 1 + minX) / 2d;
				double centerZ = (maxZ + 1 + minZ) / 2d;
				
				double relX = entity.posX - centerX;
				double relZ = entity.posZ - centerZ;
				
				double x3 = -relZ + centerX;
				double z3 = relX + centerZ;
				
				NBTTagCompound nbt = new NBTTagCompound();
				entity.writeToNBT(nbt);
				
				entities.add(new EntityInfo(new LocAndAngle(x3,
															entity.posY,
															z3,
															entity.rotationYaw,
															entity.rotationPitch), entity.getClass(), nbt));
				
				undoEntity.add(new EntityInfo(new LocAndAngle(entity.posX,
															  entity.posY,
															  entity.posZ,
															  entity.rotationYaw,
															  entity.rotationPitch), entity.getClass(), nbt));
			}
			
			for (int y = minY; y <= maxY; y++) {
				for (int x = minX; x <= maxX; x++) {
					for (int z = minZ; z <= maxZ; z++) {
						BlockInfoNoTile result = getGetBlocksInfo(world, x, y, z);
						int id = result.id();
						int meta = result.meta();
						TileEntity tile = result.tile();
						
						NBTTagCompound nbt = null;
						
						if (tile != null) {
							nbt = new NBTTagCompound();
							tile.writeToNBT(nbt);
							nbt.removeTag("x");
							nbt.removeTag("y");
							nbt.removeTag("z");
						}
						
						BlockInfo blockInfo = new BlockInfo(x, y, z, id, meta, nbt);
						
						//undoBlock.add(blockInfo);
						
						addBlockOrNonBlock(world, undoBlock, undoNonBlock, blockInfo);
						
						double centerX = (maxX + minX) / 2d;
						double centerZ = (maxZ + minZ) / 2d;
						
						double relX = x - centerX;
						double relZ = z - centerZ;
						
						int x3 = MathHelper.floor_double(-relZ + centerX);
						int z3 = MathHelper.floor_double(relX + centerZ);
						
						int i = rotateBlock(id, meta, clockwise, tile, nbt);
						
						moveBlockList.add(new BlockInfo(x3, y, z3, id, i, nbt));
						
						blocksToRemove.add(new BlockToRemoveInfo(x, y, z, tile != null));
					}
				}
			}
			
			List<BlockInfo> nonBlockList = new ArrayList<>();
			Queue<BlockInfo> blockList = new LinkedList<>();
			
			minX = Integer.MAX_VALUE;
			minY = Integer.MAX_VALUE;
			minZ = Integer.MAX_VALUE;
			maxX = Integer.MIN_VALUE;
			maxY = Integer.MIN_VALUE;
			maxZ = Integer.MIN_VALUE;
			
			for (BlockInfo info : moveBlockList) {
				int x = info.x();
				int y = info.y();
				int z = info.z();
				
				minX = Math.min(minX, x);
				minY = Math.min(minY, y);
				minZ = Math.min(minZ, z);
				maxX = Math.max(maxX, x);
				maxY = Math.max(maxY, y);
				maxZ = Math.max(maxZ, z);
				
				saveBlockToPlace(info, x, y, z, world, nonBlockList, blockList);
				saveBlockReplaced(world, x, y, z, undoBlock, undoNonBlock);
			}
			
			Selection selection1 = new Selection(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
			Selection selection2 = new Selection(pos1, pos2);
			
			pos1PlayersMap.put(sender, selection1.pos1());
			pos2PlayersMap.put(sender, selection1.pos2());
			PacketUtils.sendPosUpdate(1, (EntityPlayerMP) sender);
			PacketUtils.sendPosUpdate(2, (EntityPlayerMP) sender);
			
			
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
			
			editList.add(new QueueInfo("rotate",
									   selections,
									   edit,
									   undo,
									   duplicateSavedList(edit),
									   new int[SAVED_NUM],
									   player));
			
			sendEditMsg(sender, "bu.transform.commands.move");
			
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
		
		if (strings.length == 2) {
			return getListOfStringsMatchingLastWord(strings, "true", "false");
		}
		
		return null;
	}
}

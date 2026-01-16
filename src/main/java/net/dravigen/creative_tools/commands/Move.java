package net.dravigen.creative_tools.commands;

import api.world.BlockPos;
import net.dravigen.creative_tools.api.HelperCommand;
import net.minecraft.src.*;

import java.util.*;

import static net.dravigen.creative_tools.api.HelperCommand.sendEditMsg;
import static net.dravigen.creative_tools.api.ToolHelper.*;

public class Move extends CommandBase {
	@Override
	public String getCommandName() {
		return "move";
	}
	
	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/move [x1/y1/z1] [x2/y2/z2] [x3/y3/z3]";
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] strings) {
		MovingObjectPosition block = getBlockPlayerIsLooking(sender);
		
		if (block != null && strings.length < 4) {
			return getListOfStringsMatchingLastWord(strings, block.blockX + "/" + block.blockY + "/" + block.blockZ);
		}
		
		return null;
	}
	
	
	@Override
	public void processCommand(ICommandSender sender, String[] strings) {
		try {
			if (strings.length != 3 && (pos1 == null || pos2 == null)) {
				HelperCommand.sendErrorMsg(sender, StatCollector.translateToLocal("commands.error.selection3"));
				
				return;
			}
			
			World world = sender.getEntityWorld();
			EntityPlayer player = getPlayer(sender, sender.getCommandSenderName());
			
			int x1 = strings.length == 3 ? Integer.parseInt(strings[0].split("/")[0]) : pos1.x;
			int y1 = strings.length == 3 ? Integer.parseInt(strings[0].split("/")[1]) : pos1.y;
			int z1 = strings.length == 3 ? Integer.parseInt(strings[0].split("/")[2]) : pos1.z;
			int x2 = strings.length == 3 ? Integer.parseInt(strings[1].split("/")[0]) : pos2.x;
			int y2 = strings.length == 3 ? Integer.parseInt(strings[1].split("/")[1]) : pos2.y;
			int z2 = strings.length == 3 ? Integer.parseInt(strings[1].split("/")[2]) : pos2.z;
			int x3 = strings.length == 3
					 ? Integer.parseInt(strings[2].split("/")[0])
					 : strings.length == 1
					   ? Integer.parseInt(strings[0].split("/")[0])
					   : MathHelper.floor_double(player.posX);
			int y3 = strings.length == 3
					 ? Integer.parseInt(strings[2].split("/")[1])
					 : strings.length == 1
					   ? Integer.parseInt(strings[0].split("/")[1])
					   : MathHelper.floor_double(player.posY);
			int z3 = strings.length == 3
					 ? Integer.parseInt(strings[2].split("/")[2])
					 : strings.length == 1
					   ? Integer.parseInt(strings[0].split("/")[2])
					   : MathHelper.floor_double(player.posZ);
			
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
																											maxX,
																											maxY,
																											maxZ));
			Queue<BlockToRemoveInfo> blocksToRemove = new LinkedList<>();
			Queue<BlockInfo> moveBlockList = new LinkedList<>();
			
			List<BlockInfo> undoNonBlock = new ArrayList<>();
			Queue<BlockInfo> undoBlock = new LinkedList<>();
			List<EntityInfo> undoEntity = new ArrayList<>();
			
			if (!entitiesInSelection.isEmpty()) {
				for (Entity entity : entitiesInSelection) {
					if (entity instanceof EntityPlayer) continue;
					
					NBTTagCompound nbt = new NBTTagCompound();
					entity.writeToNBT(nbt);
					entities.add(new EntityInfo(new LocAndAngle(entity.posX - minX + x3,
																entity.posY - minY + y3,
																entity.posZ - minZ + z3,
																entity.rotationYaw,
																entity.rotationPitch), entity.getClass(), nbt));
					
					undoEntity.add(new EntityInfo(new LocAndAngle(entity.posX,
																  entity.posY,
																  entity.posZ,
																  entity.rotationYaw,
																  entity.rotationPitch), entity.getClass(), nbt));
				}
			}
			
			for (int y = minY; y <= maxY; y++) {
				for (int x = minX; x <= maxX; x++) {
					for (int z = minZ; z <= maxZ; z++) {
						int id = world.getBlockId(x, y, z);
						int meta = world.getBlockMetadata(x, y, z);
						TileEntity tile = world.getBlockTileEntity(x, y, z);
						
						NBTTagCompound nbt = null;
						
						if (tile != null) {
							nbt = new NBTTagCompound();
							tile.writeToNBT(nbt);
							nbt.removeTag("x");
							nbt.removeTag("y");
							nbt.removeTag("z");
						}
						
						BlockInfo pasteInfo = new BlockInfo(x, y, z, id, meta, nbt);
						
						Block block = Block.blocksList[id];
						
						if (block != null) {
							if ((!block.canPlaceBlockOnSide(world, 0, 254, 0, 1) ||
									block instanceof BlockFluid ||
									block.isFallingBlock() ||
									!block.canPlaceBlockAt(world, 0, 254, 0))) {
								undoNonBlock.add(pasteInfo);
							}
							else {
								undoBlock.add(pasteInfo);
							}
						}
						else {
							undoBlock.add(pasteInfo);
						}
						
						moveBlockList.add(new BlockInfo(x - minX, y - minY, z - minZ, id, meta, nbt));
						
						blocksToRemove.add(new BlockToRemoveInfo(x, y, z, tile != null));
					}
				}
			}
			
			List<BlockInfo> nonBlockList = new ArrayList<>();
			Queue<BlockInfo> blockList = new LinkedList<>();
			
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
				
				BlockInfo pasteInfo = new BlockInfo(x, y, z, info.id(), info.meta(), info.tile());
				
				Block block = Block.blocksList[info.id()];
				
				if (block != null) {
					if ((!block.canPlaceBlockOnSide(world, 0, 254, 0, 1) ||
							block instanceof BlockFluid ||
							block.isFallingBlock() ||
							!block.canPlaceBlockAt(world, 0, 254, 0))) {
						nonBlockList.add(pasteInfo);
					}
					else {
						blockList.add(pasteInfo);
					}
				}
				else {
					blockList.add(pasteInfo);
				}
			}
			
			Selection selection = new Selection(new BlockPos(minX, minY, minZ),
												new BlockPos(maxX, maxY, maxZ));
			editList.add(new QueueInfo(selection,
									   nonBlockList,
									   blockList,
									   new LinkedList<>(),
									   entities,
									   blocksToRemove,
									   minY,
									   new int[SAVED_NUM],
									   player,
									   false,
									   new QueueInfo(selection,
													 undoNonBlock,
													 undoBlock,
													 new LinkedList<>(),
													 undoEntity,
													 new LinkedList<>(),
													 minY,
													 new int[SAVED_NUM],
													 player,
													 true,
													 null)));
			
			sendEditMsg(sender,
						StatCollector.translateToLocal("commands.prefix") +
								StatCollector.translateToLocal("commands.move"));
		}
		catch (Exception e) {
			HelperCommand.sendErrorMsg(sender, StatCollector.translateToLocal("commands.error.error"));
			throw new RuntimeException(e);
		}
	}
}


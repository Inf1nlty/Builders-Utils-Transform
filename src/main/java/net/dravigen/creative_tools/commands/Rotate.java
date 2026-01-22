package net.dravigen.creative_tools.commands;

import api.world.BlockPos;
import net.minecraft.src.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static net.dravigen.creative_tools.api.ToolHelper.*;

public class Rotate extends CommandBase {
	@Override
	public String getCommandName() {
		return "rotate";
	}
	
	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/rotate";
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] strings) {
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
		
		Selection selection1 = new Selection(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
		
		
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
		List<BlockInfo> undoNonBlock = new ArrayList<>();
		Queue<BlockInfo> undoBlock = new LinkedList<>();
		Queue<BlockToRemoveInfo> blocksToRemove = new LinkedList<>();
		
		boolean clockwise = true;
		
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
					getBlocksInfo result = getGetBlocksInfo(world, x, y, z);
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
					
					undoBlock.add(new BlockInfo(x, y, z, id, meta, nbt));
					
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
			saveBlockReplaced(world, x, y, z, undoNonBlock, undoBlock);
		}
		
		pos1 = new BlockPos(minX, minY, minZ);
		pos2 = new BlockPos(maxX, maxY, maxZ);
		
		Selection selection2 = new Selection(pos1, pos2);
		
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
		
		sendEditMsg(sender,
					StatCollector.translateToLocal("commands.prefix") +
							StatCollector.translateToLocal("commands.move"));
		
		/*
		for (SavedBlock block : solidBlockEndList) {
			int newX = block.x() + Math.min(x1, x2);
			int newY = block.y() + Math.min(y1, y2);
			int newZ = block.z() + Math.min(z1, z2);
			
			Block currentBlock = Block.blocksList[block.id()];
			
			if (currentBlock != null) {
				undoList.add(new SavedBlock(newX,
											newY,
											newZ,
											world.getBlockId(newX, newY, newZ),
											world.getBlockMetadata(newX, newY, newZ)));
				
				if (currentBlock instanceof ButtonBlock || currentBlock instanceof BlockLever) {
					int i = block.meta();
					int i2 = i & 8;
					i &= 7;
					i = clockwise
						? (i == 1 ? 3 : i == 3 ? 2 : i == 2 ? 4 : 1)
						: i == 1 ? 4 : i == 4 ? 2 : i == 2 ? 3 : 1;
					if (!(currentBlock instanceof ButtonBlock)) i |= i2;
					
					world.setBlock(newX, newY, newZ, block.id(), i, flag);
					
				}
				else if (currentBlock instanceof BlockSign sign) {
					int i = block.meta();
					if (sign.isFreestanding) i = i + (clockwise ? 4 : -4) & 15;
					else i = clockwise
							 ? (i == 2 ? 5 : i == 5 ? 3 : i == 3 ? 4 : 2)
							 : i == 2 ? 4 : i == 4 ? 3 : i == 3 ? 5 : 2;
					
					world.setBlock(newX, newY, newZ, block.id(), i, flag);
					
				}
				else if (currentBlock instanceof BlockSkull skull) {
					world.setBlock(newX, newY, newZ, block.id(), block.meta(), flag);
					
					TileEntity tileEnt = world.getBlockTileEntity(newX, newY, newZ);
					if (tileEnt instanceof TileEntitySkull skullEnt) {
						int iSkullFacing = skullEnt.getSkullRotationServerSafe();
						if (clockwise) {
							if ((iSkullFacing += 4) > 15) {
								iSkullFacing -= 16;
							}
						}
						else if ((iSkullFacing -= 4) < 0) {
							iSkullFacing += 16;
						}
						skullEnt.setSkullRotation(iSkullFacing);
						world.markBlockForUpdate(newX, newY, newZ);
					}
				}
				else {
					int iMetadata = block.meta();
					int iNewMetadata = currentBlock.rotateMetadataAroundYAxis(iMetadata, clockwise);
					
					world.setBlock(newX, newY, newZ, block.id(), iNewMetadata, flag);
					
				}
			}
		}*/
	}
}

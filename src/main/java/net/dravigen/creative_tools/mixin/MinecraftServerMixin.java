package net.dravigen.creative_tools.mixin;

import api.world.BlockPos;
import net.dravigen.creative_tools.api.HelperCommand;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Queue;

import static net.dravigen.creative_tools.api.ToolHelper.*;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
	double SPEED = 50;
	double REMOVE_SPEED = 500;
	
	
	
	/// TODO: fix /move and cut to remove copied area
	
	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(CallbackInfo ci) {
		for (int j = 0; j < editList.size(); j++) {
			QueueInfo queueInfo = editList.get(j);
			
			int currentY = queueInfo.minY();
			
			World world = queueInfo.player().getEntityWorld();
			
			List<BlockInfo> nonBlockList = queueInfo.nonBlockList();
			Queue<BlockInfo> blockList = queueInfo.blockList();
			Queue<BlockInfo> allBlocks = queueInfo.allBlocks();
			
			boolean isBlockEmpty = blockList == null || blockList.isEmpty();
			boolean isNonBlockEmpty = nonBlockList == null || nonBlockList.isEmpty();
			
			Selection selection = queueInfo.selection();
			/*
			if (!removeList.isEmpty()) {
				for (int i = 0; i < REMOVE_SPEED; i++) {
					BlockToRemoveInfo info = removeList.peek();
					
					int x = info.x();
					int y = info.y();
					int z = info.z();
					boolean hasTile = info.hasTile();
					
					if (y < currentY )
					
					removeBlock(world, x, z, y, hasTile);
					
					if (removeList.isEmpty()) break;
				}
			}
			*/
			
			List<EntityInfo> entities = queueInfo.entities();
			boolean isEntitiesEmpty = entities == null || entities.isEmpty();
			
			int blockNum = queueInfo.num();
			
			if (!isBlockEmpty) {
				for (int i = 0; i < SPEED; i++) {
					BlockInfo block = blockList.poll();
					
					if (block == null) continue;
					
					allBlocks.add(block);
					
					int x = block.x();
					int y = block.y();
					int z = block.z();
					
					if (world.getBlockId(x, y, z) == block.id() && world.getBlockMetadata(x, y, z) == block.meta() && !world.blockHasTileEntity(x, y, z)) {
						i--;
						continue;
					}
					
					try {
						if (world.getBlockTileEntity(x, y, z) != null) {
							removeBlock(world, x, z, y, true);
						}
						
						world.setBlock(x, y, z, block.id(), block.meta(), 2);
						blockNum++;
						pasteTile(block, world, x, y, z);
					}
					catch (Exception e) {
					
					}
					
					if (blockList.isEmpty()) break;
				}
			}
			else if (!isNonBlockEmpty) {
				for (BlockInfo block : nonBlockList) {
					int x = block.x();
					int y = block.y();
					int z = block.z();
					
					allBlocks.add(block);
					
					if (world.getBlockId(x, y, z) == block.id() && world.getBlockMetadata(x, y, z) == block.meta() && !world.blockHasTileEntity(x, y, z)) {
						continue;
					}
					
					try {
						if (world.getBlockTileEntity(x, y, z) != null) {
							removeBlock(world, x, z, y, true);
						}
						
						blockNum++;
						world.setBlock(x, y, z, block.id(), block.meta(), 2);
						
						pasteTile(block, world, x, y, z);
					}
					catch (Exception e) {
					
					}
				}
				
				nonBlockList.clear();
			}
			
			if (isNonBlockEmpty && isBlockEmpty) {
				for (int i = 0; i < SPEED * 5; i++) {
					BlockInfo block = allBlocks.poll();
					
					if (block == null) continue;
					
					int x = block.x();
					int y = block.y();
					int z = block.z();
					
					if (world.getBlockId(x, y, z) == block.id() && world.getBlockMetadata(x, y, z) == block.meta()) {
						i--;
						continue;
					}
					
					try {
						world.setBlock(x, y, z, block.id(), block.meta(), 2);
						
						pasteTile(block, world, x, y, z);
					}
					catch (Exception e) {
					
					}
					if (allBlocks.isEmpty()) break;
				}
			}
			
			editList.set(editList.indexOf(queueInfo), new QueueInfo(selection, queueInfo.nonBlockList(), queueInfo.blockList(), allBlocks, queueInfo.entities(), queueInfo.blocksToRemove(), currentY, blockNum, queueInfo.player()));
			
			if (isNonBlockEmpty && isBlockEmpty && allBlocks.isEmpty()) {
				int entityNum = 0;
				List<Entity> entitiesInSelection = world.getEntitiesWithinAABBExcludingEntity(queueInfo.player(), new AxisAlignedBB(selection.pos1().x, selection.pos1().y, selection.pos1().z, selection.pos2().x + 1, selection.pos2().y + 1, selection.pos2().z + 1));
				
				for (Entity entity : entitiesInSelection) {
					if (entity instanceof EntityPlayer) continue;
					
					entity.setDead();
					entityNum++;
				}
				
				if (!isEntitiesEmpty) {
					for (EntityInfo info : entities) {
						Entity entity = EntityList.createEntityByID(EntityList.getEntityIDFromClass(info.entityClass()),
																	world);
						LocAndAngle locAndAngle = info.locAndAngle();
						entity.readFromNBT(info.nbt());
						entity.setLocationAndAngles(locAndAngle.x(),
													locAndAngle.y(),
													locAndAngle.z(),
													locAndAngle.yaw(),
													locAndAngle.pitch());
						
						world.spawnEntityInWorld(entity);
						entityNum++;
					}
					
					entities.clear();
				}
				
				for (int y = selection.pos1().y; y < selection.pos2().y; y++) {
					for (int x = selection.pos1().x; x < selection.pos2().x; x++) {
						for (int z = selection.pos1().z; z < selection.pos2().z; z++) {
							world.markBlockForUpdate(x, y, z);
						}
					}
				}
				
				HelperCommand.sendEditMsg(queueInfo.player(), StatCollector.translateToLocal("commands.prefix") + String.format(StatCollector.translateToLocal("commands.paste"), blockNum, entityNum));
				editList.remove(queueInfo);
			}
		}
	}
	
	@Unique
	private static void removeBlock(World world, int x, int z, int y, boolean hasTile) {
		if (world.isAirBlock(x, y, z)) return;
		
		Chunk chunk = world.getChunkFromChunkCoords(x >> 4, z >> 4);
		
		try {
			ExtendedBlockStorage storage = ((ExtendedBlockStorage[]) storageArraysField.get(chunk))[y >> 4];
			
			storage.setExtBlockID(x & 0xF, y & 0xF, z & 0xF, 0);
			world.markBlockForUpdate(x, y, z);
			
			if (hasTile) world.removeBlockTileEntity(x, y, z);
		}
		catch (Exception e) {
			world.setBlock(x, y, z, 0, 0, 2);
			
			if (hasTile) world.removeBlockTileEntity(x, y, z);
		}
	}
	
	@Unique
	private static void pasteTile(BlockInfo blockInfo, World world, int x, int y, int z) {
		if (blockInfo.tile() == null) return;
		
		NBTTagCompound tileInfo = blockInfo.tile();
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		
		if (tile == null) {
			tile = TileEntity.createAndLoadEntity(tileInfo);
			world.setBlockTileEntity(x, y, z, tile);
		}
		
		if (tile == null) return;
		
		if (tile instanceof IInventory inv) {
			deleteInventory(inv);
		}
		
		NBTTagCompound pasteNbt = (NBTTagCompound) tileInfo.copy();
		
		pasteNbt.setInteger("x", x);
		pasteNbt.setInteger("y", y);
		pasteNbt.setInteger("z", z);
		
		tile.readFromNBT(pasteNbt);
	}
}

package net.dravigen.creative_tools.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Queue;

import static net.dravigen.creative_tools.api.ToolHelper.*;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
	@Shadow
	public abstract boolean isSinglePlayer();
	
	@Unique
	int SPEED = 100;
	@Unique
	int SPEED_UNDO = 1000;
	@Unique
	int REMOVE_SPEED = 500;
	
	@Unique
	private static void removeBlock(World world, int x, int z, int y, boolean hasTile) {
		if (world.isAirBlock(x, y, z)) return;
		
		Chunk chunk = world.getChunkFromChunkCoords(x >> 4, z >> 4);
		
		try {
			ExtendedBlockStorage storage = ((ExtendedBlockStorage[]) storageArraysField.get(chunk))[y >> 4];
			
			storage.setExtBlockID(x & 0xF, y & 0xF, z & 0xF, 0);
			//world.updateAllLightTypes(x, y, z);
			world.markBlockForUpdate(x, y, z);
			
			int var7 = chunk.heightMap[z & 0xF << 4 | x & 0xF];
			
			Method relightBlock = chunk.getClass().getDeclaredMethod("relightBlock", int.class, int.class, int.class);
			relightBlock.setAccessible(true);
			
			Method propagateSkylightOcclusion = chunk.getClass()
					.getDeclaredMethod("propagateSkylightOcclusion", int.class, int.class);
			propagateSkylightOcclusion.setAccessible(true);
			
			if (y == var7 - 1) {
				relightBlock.invoke(chunk, x & 0xF, y, z & 0xF);
			}
			propagateSkylightOcclusion.invoke(chunk, x & 0xF, z & 0xF);
			
			if (hasTile) world.removeBlockTileEntity(x, y, z);
		} catch (Exception e) {
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
	
	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(CallbackInfo ci) {
		if (!this.isSinglePlayer()) return;
		
		for (int j = 0; j < editList.size(); j++) {
			QueueInfo queueInfo = editList.get(j);
			int speed = SPEED;
			if (queueInfo.id().equals("redo") || queueInfo.id().equals("undo")) {
				speed = SPEED_UNDO;
			}
			
			World world = queueInfo.player().getEntityWorld();
			
			SavedLists edit = queueInfo.editList();
			SavedLists undo = queueInfo.undoList();
			
			List<BlockInfo> nonBlockList = edit.nonBlockList();
			Queue<BlockInfo> blockList = edit.blockList();
			Queue<BlockInfo> allBlocks = edit.allBlocks();
			Queue<BlockToRemoveInfo> removeList = edit.blocksToRemove();
			List<EntityInfo> entities = edit.entities();
			
			boolean isEntitiesEmpty = entities == null || entities.isEmpty();
			boolean isBlockEmpty = blockList == null || blockList.isEmpty();
			boolean isNonBlockEmpty = nonBlockList == null || nonBlockList.isEmpty();
			boolean isAllBlocksEmpty = allBlocks == null || allBlocks.isEmpty();
			List<Selection> selections = queueInfo.selection();
			
			int[] num = queueInfo.num();
			
			for (Selection selection : selections) {
				for (Object o : world.getEntitiesWithinAABBExcludingEntity(queueInfo.player(),
																		   new AxisAlignedBB(selection.pos1().x,
																							 selection.pos1().y,
																							 selection.pos1().z,
																							 selection.pos2().x + 1,
																							 selection.pos2().y + 1,
																							 selection.pos2().z + 1))) {
					Entity entity = (Entity) o;
					if (entity instanceof EntityPlayer) continue;
					
					entity.setDead();
					num[3]++;
				}
			}
			
			if (!removeList.isEmpty()) {
				for (int i = 0; i < REMOVE_SPEED; i++) {
					if (removeList.isEmpty()) break;
					BlockToRemoveInfo info = removeList.poll();
					
					int x = info.x();
					int y = info.y();
					int z = info.z();
					boolean hasTile = info.hasTile();
					
					if (world.isAirBlock(x, y, z)) {
						i--;
						continue;
					}
					
					removeBlock(world, x, z, y, hasTile);
					num[2]++;
				}
			}
			else {
				if (!isBlockEmpty) {
					for (int i = 0; i < speed; i++) {
						if (blockList.isEmpty()) break;
						
						BlockInfo block = blockList.poll();
						
						if (allBlocks != null) {
							allBlocks.add(block);
						}
						
						int x = block.x();
						int y = block.y();
						int z = block.z();
						
						if (world.getBlockId(x, y, z) == block.id() &&
								world.getBlockMetadata(x, y, z) == block.meta() &&
								!world.blockHasTileEntity(x, y, z)) {
							i--;
							continue;
						}
						
						try {
							if (world.blockHasTileEntity(x, y, z)) {
								removeBlock(world, x, z, y, true);
							}
							
							if (block.id() == 0) num[2]++;
							else num[0]++;
							
							world.setBlock(x, y, z, block.id(), block.meta(), 2);
							pasteTile(block, world, x, y, z);
						} catch (Exception ignored) {
						
						}
					}
				}
				else if (!isNonBlockEmpty) {
					for (BlockInfo block : nonBlockList) {
						int x = block.x();
						int y = block.y();
						int z = block.z();
						
						if (allBlocks != null) {
							allBlocks.add(block);
						}
						
						if (world.getBlockId(x, y, z) == block.id() &&
								world.getBlockMetadata(x, y, z) == block.meta() &&
								!world.blockHasTileEntity(x, y, z)) {
							continue;
						}
						
						try {
							if (world.getBlockTileEntity(x, y, z) != null) {
								removeBlock(world, x, z, y, true);
							}
							
							num[0]++;
							world.setBlock(x, y, z, block.id(), block.meta(), 2);
							
							pasteTile(block, world, x, y, z);
						} catch (Exception ignored) {
						
						}
					}
					
					nonBlockList.clear();
				}
				
				if (isNonBlockEmpty && isBlockEmpty && allBlocks != null) {
					for (int i = 0; i < REMOVE_SPEED; i++) {
						if (allBlocks.isEmpty()) break;
						
						BlockInfo block = allBlocks.poll();
						
						int x = block.x();
						int y = block.y();
						int z = block.z();
						
						if (world.getBlockId(x, y, z) == block.id() &&
								world.getBlockMetadata(x, y, z) == block.meta()) {
							i--;
							continue;
						}
						
						try {
							if (block.id() == 0) num[2]++;
							else num[0]++;
							
							world.setBlock(x, y, z, block.id(), block.meta(), 2);
							pasteTile(block, world, x, y, z);
						} catch (Exception ignored) {
						
						}
					}
				}
			}
			
			editList.set(editList.indexOf(queueInfo),
						 new QueueInfo(queueInfo.id(),
									   selections,
									   edit,
									   undo,
									   queueInfo.redoList(),
									   num,
									   queueInfo.player()));
			
			if (removeList.isEmpty() && isNonBlockEmpty && isBlockEmpty && isAllBlocksEmpty) {
				if (!isEntitiesEmpty) {
					for (Selection selection : selections) {
						for (Object o : world.getEntitiesWithinAABBExcludingEntity(queueInfo.player(),
																				   new AxisAlignedBB(selection.pos1().x,
																									 selection.pos1().y,
																									 selection.pos1().z,
																									 selection.pos2().x +
																											 1,
																									 selection.pos2().y +
																											 1,
																									 selection.pos2().z +
																											 1))) {
							Entity entity = (Entity) o;
							if (entity instanceof EntityPlayer) continue;
							
							entity.setDead();
							num[3]++;
						}
					}
					
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
						num[1]++;
					}
					
					entities.clear();
				}
				
				for (Selection selection : selections) {
					int x1 = selection.pos1().x;
					int y1 = selection.pos1().y;
					int z1 = selection.pos1().z;
					int x2 = selection.pos2().x;
					int y2 = selection.pos2().y;
					int z2 = selection.pos2().z;
					
					int minX = Math.min(x1, x2);
					int minY = Math.min(y1, y2);
					int minZ = Math.min(z1, z2);
					int maxX = Math.max(x1, x2);
					int maxY = Math.max(y1, y2);
					int maxZ = Math.max(z1, z2);
					
					for (int y = minY; y <= maxY; y++) {
						for (int x = minX; x <= maxX; x++) {
							for (int z = minZ; z <= maxZ; z++) {
								world.markBlockForUpdate(x, y, z);
							}
						}
					}
				}
				
				if (num[0] + num[1] + num[2] + num[3] > 0) {
					if (!queueInfo.id().equals("undo")) {
						undoList.add(new QueueInfo("undo",
												   selections,
												   duplicateSavedList(undo),
												   createEmptySavedList(),
												   duplicateSavedList(queueInfo.redoList()),
												   new int[SAVED_NUM],
												   queueInfo.player()));
					}
					
					sendEditMsg(queueInfo.player(),
								String.format(StatCollector.translateToLocal("commands.edit"),
											  num[0] + num[2],
											  num[1] + num[3],
											  num[0],
											  num[2],
											  num[1],
											  num[3]));
				}
				else {
					sendErrorMsg(queueInfo.player(),
								 String.format(StatCollector.translateToLocal("commands.error.edit")));
				}
				
				editList.remove(queueInfo);
			}
		}
	}
}

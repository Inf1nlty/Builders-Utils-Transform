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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static net.dravigen.creative_tools.api.ToolHelper.*;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
	@Unique
	double SPEED = 50;
	@Unique
	double REMOVE_SPEED = 250;
	
	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(CallbackInfo ci) {
		for (int j = 0; j < editList.size(); j++) {
			QueueInfo queueInfo = editList.get(j);
			
			World world = queueInfo.player().getEntityWorld();
			
			List<BlockInfo> nonBlockList = queueInfo.nonBlockList();
			Queue<BlockInfo> blockList = queueInfo.blockList();
			Queue<BlockInfo> allBlocks = queueInfo.allBlocks();
			Queue<BlockToRemoveInfo> removeList = queueInfo.blocksToRemove();
			List<EntityInfo> entities = queueInfo.entities();
			boolean isEntitiesEmpty = entities == null || entities.isEmpty();
			boolean isBlockEmpty = blockList == null || blockList.isEmpty();
			boolean isNonBlockEmpty = nonBlockList == null || nonBlockList.isEmpty();
			Selection selection = queueInfo.selection();
			boolean savedUndo = queueInfo.savedUndo();
			
			int[] num = queueInfo.num();
			
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
				
				if (!savedUndo) {
					NBTTagCompound nbt = new NBTTagCompound();
					entity.writeToNBT(nbt);
					queueInfo.undo().entities().add(new EntityInfo(new LocAndAngle(entity.posX,
																	  entity.posY,
																	  entity.posZ,
																	  entity.rotationYaw,
																	  entity.rotationPitch), entity.getClass(), nbt));
				}
				
				entity.setDead();
				num[3]++;
			}
			
			if (!savedUndo) {
				if (!removeList.isEmpty()) {
					for (BlockToRemoveInfo info : removeList) {
						int x = info.x();
						int y = info.y();
						int z = info.z();
						
						if (world.isAirBlock(x, y, z)) {
							continue;
						}
						
						int id = world.getBlockId(x, y, z);
						int meta = world.getBlockMetadata(x, y, z);
						TileEntity tile = world.getBlockTileEntity(x, y, z);
						
						NBTTagCompound tileNBT = null;
						
						if (tile != null) {
							tileNBT = new NBTTagCompound();
							tile.writeToNBT(tileNBT);
							tileNBT.removeTag("x");
							tileNBT.removeTag("y");
							tileNBT.removeTag("z");
						}
						
						BlockInfo pasteInfo = new BlockInfo(x, y, z, id, meta, tileNBT);
						
						Block block = Block.blocksList[id];
						
						if (block != null) {
							if ((!block.canPlaceBlockOnSide(world, 0, 254, 0, 1) ||
									block instanceof BlockFluid ||
									block.isFallingBlock() ||
									!block.canPlaceBlockAt(world, 0, 254, 0))) {
								queueInfo.undo().nonBlockList().add(pasteInfo);
							}
							else {
								queueInfo.undo().blockList().add(pasteInfo);
							}
						}
						else {
							queueInfo.undo().blockList().add(pasteInfo);
						}
					}
				}
				
				for (int y = selection.pos1().y; y <= selection.pos2().y; y++) {
					for (int x = selection.pos1().x; x <= selection.pos2().x; x++) {
						for (int z = selection.pos1().z; z <= selection.pos2().z; z++) {
							int id = world.getBlockId(x, y, z);
							int meta = world.getBlockMetadata(x, y, z);
							TileEntity tile = world.getBlockTileEntity(x, y, z);
							
							NBTTagCompound tileNBT = null;
							
							if (tile != null) {
								tileNBT = new NBTTagCompound();
								tile.writeToNBT(tileNBT);
								tileNBT.removeTag("x");
								tileNBT.removeTag("y");
								tileNBT.removeTag("z");
							}
							
							BlockInfo pasteInfo = new BlockInfo(x, y, z, id, meta, tileNBT);
							
							Block block = Block.blocksList[id];
							
							if (block != null) {
								if ((!block.canPlaceBlockOnSide(world, 0, 254, 0, 1) ||
										block instanceof BlockFluid ||
										block.isFallingBlock() ||
										!block.canPlaceBlockAt(world, 0, 254, 0))) {
									queueInfo.undo().nonBlockList().add(pasteInfo);
								}
								else {
									queueInfo.undo().blockList().add(pasteInfo);
								}
							}
							else {
								queueInfo.undo().blockList().add(pasteInfo);
							}
						}
					}
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
					for (int i = 0; i < SPEED; i++) {
						if (blockList.isEmpty()) break;
						
						BlockInfo block = blockList.poll();
						
						allBlocks.add(block);
						
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
							if (world.getBlockTileEntity(x, y, z) != null) {
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
						
						allBlocks.add(block);
						
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
				
				if (isNonBlockEmpty && isBlockEmpty) {
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
			
			editList.set(editList.indexOf(queueInfo), new QueueInfo(selection, queueInfo.nonBlockList(), queueInfo.blockList(), allBlocks, queueInfo.entities(), queueInfo.blocksToRemove(),
																	queueInfo.minY(), num, queueInfo.player(), true, queueInfo.undo()));
			
			if (removeList.isEmpty() && isNonBlockEmpty && isBlockEmpty && allBlocks.isEmpty()) {
				if (!isEntitiesEmpty) {
					for (Object o : world.getEntitiesWithinAABBExcludingEntity(queueInfo.player(),
																			   new AxisAlignedBB(
																					   selection.pos1().x,
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
				
				for (int y = selection.pos1().y; y <= selection.pos2().y; y++) {
					for (int x = selection.pos1().x; x <= selection.pos2().x; x++) {
						for (int z = selection.pos1().z; z <= selection.pos2().z; z++) {
							world.markBlockForUpdate(x, y, z);
						}
					}
				}
				
				if ((num[0] + num[1] + num[2] + num[3]) > 0) {
					if (queueInfo.undo() != null) {
						undoList.add(queueInfo.undo());
					}
					
					HelperCommand.sendEditMsg(queueInfo.player(), StatCollector.translateToLocal("commands.prefix") + String.format(StatCollector.translateToLocal("commands.edit"), num[0] + num[2], num[1] + num[3], num[0], num[2], num[1], num[3]));
				}
				else {
					HelperCommand.sendErrorMsg(queueInfo.player(), String.format(StatCollector.translateToLocal("commands.error.edit")));
				}
				
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
			//world.updateAllLightTypes(x, y, z);
			world.markBlockForUpdate(x, y, z);
			
			int var7 = chunk.heightMap[z & 0xF << 4 | x & 0xF];
			
			Method relightBlock = chunk.getClass().getDeclaredMethod("relightBlock", int.class, int.class, int.class);
			relightBlock.setAccessible(true);
			
			Method propagateSkylightOcclusion = chunk.getClass().getDeclaredMethod("propagateSkylightOcclusion", int.class, int.class);
			propagateSkylightOcclusion.setAccessible(true);
			
			if (y == var7 - 1) {
				relightBlock.invoke(chunk, x & 0xF, y, z & 0xF);
			}
			propagateSkylightOcclusion.invoke(chunk, x & 0xF, z & 0xF);
			
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

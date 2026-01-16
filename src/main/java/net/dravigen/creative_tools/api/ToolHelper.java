package net.dravigen.creative_tools.api;

import api.world.BlockPos;
import net.minecraft.src.*;

import java.lang.reflect.Field;
import java.util.*;

import static net.minecraft.src.CommandBase.getPlayer;

public class ToolHelper {
	public record BlockInfo(int x, int y, int z, int id, int meta, NBTTagCompound tile) {}
	public record BlockToRemoveInfo(int x, int y, int z, boolean hasTile) {}
	public record QueueInfo(String id, Selection selection, SavedLists editList, SavedLists undoList, SavedLists redoList, int minY, int[] num, EntityPlayer player, boolean savedUndo) {}
	public record EntityInfo(LocAndAngle locAndAngle, Class entityClass, NBTTagCompound nbt) {}
	public record LocAndAngle(double x, double y, double z, float yaw, float pitch) {}
	public record Selection(BlockPos pos1, BlockPos pos2) {}
	public record SavedLists(List<BlockInfo> nonBlockList, Queue<BlockInfo> blockList, Queue<BlockInfo> allBlocks, List<EntityInfo> entities, Queue<BlockToRemoveInfo> blocksToRemove) {}
	
	public static SavedLists duplicateSavedList(SavedLists old) {
		return new SavedLists(new ArrayList<>(old.nonBlockList), new LinkedList<>(old.blockList), new LinkedList<>(old.allBlocks), new ArrayList<>(old.entities), new LinkedList<>(old.blocksToRemove));
	}
	
	public static SavedLists createEmptySavedList() {
		return new SavedLists(new ArrayList<>(), new LinkedList<>(), new LinkedList<>(), new ArrayList<>(), new LinkedList<>());
	}
	
	public static int SAVED_NUM = 4;
	
	public static BlockPos pos1 = null;
	public static BlockPos pos2 = null;
	
	public static List<BlockInfo> copyBlockList = new ArrayList<>();
	public static List<EntityInfo> copyEntityList = new ArrayList<>();
	public static List<QueueInfo> editList = new ArrayList<>();
	public static List<QueueInfo> undoList = new ArrayList<>();
	public static List<QueueInfo> redoList = new ArrayList<>();
	
	public static final Field storageArraysField;
	
	static {
		Field f = null;
		
		try {
			try {
				f = Chunk.class.getDeclaredField("storageArrays");
				f.setAccessible(true);
			} catch (NoSuchFieldException ignored) {
			}
		} catch (Exception e) {
			System.err.println("Failed to hook into Chunk.storageArrays!");
			e.printStackTrace();
		}
		
		storageArraysField = f;
	}
	
	public static MovingObjectPosition getBlockPlayerIsLooking(ICommandSender sender) {
		EntityPlayer player = getPlayer(sender, sender.getCommandSenderName());
		Vec3 var3 = player.getPosition(1);
		var3.yCoord += player.getEyeHeight();
		Vec3 var4 = player.getLookVec();
		int reach = 512;
		Vec3 var5 = var3.addVector(var4.xCoord * reach, var4.yCoord * reach, var4.zCoord * reach);
		return player.worldObj.clip(var3, var5);
	}
	
	public static void deleteInventory(IInventory inv) {
		for (int k = 0; k < inv.getSizeInventory(); k++) {
			inv.setInventorySlotContents(k, null);
		}
	}
	
	public static SavedLists createUndoList(World world, Selection selection, Queue<BlockToRemoveInfo> removeList) {
		List<BlockInfo> nonBlockList = new ArrayList<>();
		Queue<BlockInfo> blockList = new LinkedList<>();
		
		if (!removeList.isEmpty()) {
			for (BlockToRemoveInfo info : removeList) {
				int x = info.x();
				int y = info.y();
				int z = info.z();
				
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
			}
		}
	}
	
	
	/*
	public static void removeBlock(World world, int x, int z, int y, boolean hasTile) {
		Chunk chunk = world.getChunkFromChunkCoords(x >> 4, z >> 4);
		
		try {
			ExtendedBlockStorage storage = ((ExtendedBlockStorage[]) storageArraysField.get(chunk))[y >> 4];
			
			storage.setExtBlockID(x & 0xF, y & 0xF, z & 0xF, 0);
			world.markBlockForUpdate(x, y, z);

			if (hasTile) world.removeBlockTileEntity(x, y, z);
		} catch (Exception e) {
			world.setBlock(x, y, z, 0, 0, 2);
		}
	}*/
}

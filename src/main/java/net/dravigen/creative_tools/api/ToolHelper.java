package net.dravigen.creative_tools.api;

import api.world.BlockPos;
import net.minecraft.src.*;

import java.lang.reflect.Field;
import java.util.*;

import static net.minecraft.src.CommandBase.getPlayer;

public class ToolHelper {
	public record BlockInfo(int x, int y, int z, int id, int meta, NBTTagCompound tile) {}
	public record BlockToRemoveInfo(int x, int y, int z, boolean hasTile) {}
	public record QueueInfo(Selection selection, List<BlockInfo> nonBlockList, Queue<BlockInfo> blockList, Queue<BlockInfo> allBlocks, List<EntityInfo> entities, Queue<BlockToRemoveInfo> blocksToRemove, int minY, int[] num, EntityPlayer player, boolean savedUndo, QueueInfo undo) {}
	public record EntityInfo(LocAndAngle locAndAngle, Class entityClass, NBTTagCompound nbt) {}
	public record LocAndAngle(double x, double y, double z, float yaw, float pitch) {}
	public record Selection(BlockPos pos1, BlockPos pos2) {}
	
	public static int SAVED_NUM = 4;
	
	public static BlockPos pos1 = null;
	public static BlockPos pos2 = null;
	
	public static List<BlockInfo> copyBlockList = new ArrayList<>();
	public static List<EntityInfo> copyEntityList = new ArrayList<>();
	public static List<QueueInfo> editList = new ArrayList<>();
	public static List<QueueInfo> undoList = new ArrayList<>();
	
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

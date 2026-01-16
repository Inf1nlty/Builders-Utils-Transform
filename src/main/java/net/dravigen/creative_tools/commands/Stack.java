package net.dravigen.creative_tools.commands;

import api.world.BlockPos;
import net.dravigen.creative_tools.api.HelperCommand;
import net.minecraft.src.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static net.dravigen.creative_tools.api.HelperCommand.sendEditMsg;
import static net.dravigen.creative_tools.api.ToolHelper.*;

public class Stack extends CommandBase {
	@Override
	public String getCommandName() {
		return "stack";
	}
	
	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/stack [+x|-x|+y|-y|+z|-z] [number of stack]";
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] strings) {
		return getListOfStringsMatchingLastWord(strings, "+x", "-x", "+y", "-y", "+z", "-z");
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] strings) {
		if (pos1 == null || pos2 == null) {
			HelperCommand.sendErrorMsg(sender, StatCollector.translateToLocal("commands.error.selection2"));
			
			return;
		}
		
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
			} catch (Exception e) {
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
		
		for (int i = 0; i <= stackNum; i++) {
			int x3 = (xP ? (-xDist + 1) * i : xN ? (xDist - 1) * i : 0);
			int y3 = (yP ? (-yDist + 1) * i : yN ? (yDist - 1) * i : 0);
			int z3 = (zP ? (-zDist + 1) * i : zN ? (zDist - 1) * i : 0);
			
			for (EntityInfo entity : entities) {
				LocAndAngle locAndAngle = entity.locAndAngle();
				entitiesToPaste.add(new EntityInfo(new LocAndAngle(locAndAngle.x() + x3,
																   locAndAngle.y() + y3,
																   locAndAngle.z() + z3,
																   locAndAngle.yaw(),
																   locAndAngle.pitch()),
												   entity.entityClass(),
												   entity.nbt()));
			}
			
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
		}
		
		Selection selection = new Selection(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
		editList.add(new QueueInfo(selection,
								   nonBlockList,
								   blockList,
								   new LinkedList<>(),
								   entitiesToPaste,
								   new LinkedList<>(),
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
	
		sendEditMsg(sender, StatCollector.translateToLocal("commands.prefix") + String.format(StatCollector.translateToLocal("commands.stack"), stackNum, direction));
	}
}

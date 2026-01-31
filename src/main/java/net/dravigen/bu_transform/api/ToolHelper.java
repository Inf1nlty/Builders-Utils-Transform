package net.dravigen.bu_transform.api;

import api.world.BlockPos;
import btw.block.BTWBlocks;
import btw.block.blocks.ButtonBlock;
import net.minecraft.src.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.minecraft.src.CommandBase.getPlayer;

public class ToolHelper {
	public static int SAVED_NUM = 4;
	public static BlockPos pos1 = null;
	public static BlockPos pos2 = null;
	public static List<QueueInfo> editList = new ArrayList<>();
	
	public static Map<ICommandSender, Queue<BlockInfo>> copyBlockPlayersMap = new HashMap<>();
	public static Map<ICommandSender, List<EntityInfo>> copyEntityPlayersMap = new HashMap<>();
	public static Map<ICommandSender, BlockPos> pos1PlayersMap = new HashMap<>();
	public static Map<ICommandSender, BlockPos> pos2PlayersMap = new HashMap<>();
	public static Map<ICommandSender, List<QueueInfo>> undoPlayersMap = new HashMap<>();
	public static Map<ICommandSender, List<QueueInfo>> redoPlayersMap = new HashMap<>();
	
	public static SavedLists duplicateSavedList(SavedLists old) {
		return new SavedLists(new ArrayList<>(old.nonBlockList),
							  new LinkedList<>(old.blockList),
							  null,
							  new ArrayList<>(old.entities),
							  new LinkedList<>(old.blocksToRemove));
	}
	
	public static SavedLists createEmptySavedList() {
		return new SavedLists(new ArrayList<>(),
							  new LinkedList<>(),
							  new LinkedList<>(),
							  new ArrayList<>(),
							  new LinkedList<>());
	}
	
	public static void saveReplacedEntities(World world, EntityPlayer player, Selection selection,
			List<EntityInfo> undoEntity) {
		for (Object o : world.getEntitiesWithinAABBExcludingEntity(player,
																   new AxisAlignedBB(selection.pos1().x,
																					 selection.pos1().y,
																					 selection.pos1().z,
																					 selection.pos2().x + 1,
																					 selection.pos2().y + 1,
																					 selection.pos2().z + 1))) {
			Entity entity = (Entity) o;
			if (entity instanceof EntityPlayer) continue;
			
			NBTTagCompound nbt = new NBTTagCompound();
			entity.writeToNBT(nbt);
			undoEntity.add(new EntityInfo(new LocAndAngle(entity.posX,
														  entity.posY,
														  entity.posZ,
														  entity.rotationYaw,
														  entity.rotationPitch), entity.getClass(), nbt));
		}
	}
	
	public static void saveEntitiesToPlace(List<EntityInfo> entities, List<EntityInfo> entitiesToPaste, int x3, int y3,
			int z3) {
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
	}
	
	public static void copyRemoveBlockSelection(int minY, int maxY, int minX, int maxX, int minZ, int maxZ, World world,
			Queue<BlockInfo> undoBlock, List<BlockInfo> undoNonBlock, Queue<BlockInfo> moveBlockList,
			Queue<BlockToRemoveInfo> blocksToRemove) {
		for (int y = minY; y <= maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					BlockInfoNoTile info = getGetBlocksInfo(world, x, y, z);
					NBTTagCompound nbt = null;
					
					if (info.tile() != null) {
						nbt = new NBTTagCompound();
						info.tile().writeToNBT(nbt);
						nbt.removeTag("x");
						nbt.removeTag("y");
						nbt.removeTag("z");
					}
					
					BlockInfo blockInfo = new BlockInfo(x, y, z, info.id(), info.meta(), nbt);
					
					//undoBlock.add(blockInfo);
					
					
					addBlockOrNonBlock(world, undoBlock, undoNonBlock, blockInfo);
					
					moveBlockList.add(new BlockInfo(x - minX, y - minY, z - minZ, info.id(), info.meta(), nbt));
					
					blocksToRemove.add(new BlockToRemoveInfo(x, y, z, info.tile() != null));
				}
			}
		}
	}
	
	public static void addBlockOrNonBlock(World world, Queue<BlockInfo> blockList, List<BlockInfo> nonBlockList,
			BlockInfo blockInfo) {
		Block block = Block.blocksList[blockInfo.id()];
		
		if (block != null) {
			
			if ((!block.canPlaceBlockOnSide(world, 0, 254, 0, 1) ||
					block instanceof BlockFluid ||
					block.isFallingBlock() ||
					!block.canPlaceBlockAt(world, 0, 254, 0))) {
				nonBlockList.add(blockInfo);
			}
			//if (block instanceof BlockFluid || block.isFallingBlock()) nonBlockList.add(blockInfo);
			else blockList.add(blockInfo);
		}
		else blockList.add(blockInfo);
	}
	
	public static @NotNull ToolHelper.BlockInfoNoTile getGetBlocksInfo(World world, int x, int y, int z) {
		int id = world.getBlockId(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		
		if (id == BTWBlocks.axlePowerSource.blockID) {
			id = BTWBlocks.axle.blockID;
		}
		
		return new BlockInfoNoTile(id, meta, tile);
	}
	
	public static int rotateBlock(int id, int meta, boolean clockwise, TileEntity tile, NBTTagCompound nbt) {
		Block currentBlock = Block.blocksList[id];
		int i = meta;
		
		if (currentBlock != null) {
			if (currentBlock instanceof ButtonBlock || currentBlock instanceof BlockLever) {
				int i2 = i & 8;
				i &= 7;
				
				i = switch (i) {
					case 1 -> clockwise ? 3 : 4;
					case 2 -> clockwise ? 4 : 3;
					case 3 -> clockwise ? 2 : 1;
					default -> clockwise ? 1 : 2;
				};
				
				if (!(currentBlock instanceof ButtonBlock)) i |= i2;
			}
			else if (currentBlock instanceof BlockSign sign) {
				if (sign.isFreestanding) i = i + (clockwise ? 4 : -4) & 15;
				else {
					i = switch (i) {
						case 2 -> clockwise ? 5 : 4;
						case 3 -> clockwise ? 4 : 5;
						case 4 -> clockwise ? 2 : 3;
						default -> clockwise ? 3 : 2;
					};
				}
				
			}
			else if (currentBlock instanceof BlockSkull) {
				if (tile instanceof TileEntitySkull skullEnt) {
					if (meta != 1) {
						i = switch (i) {
							case 2 -> clockwise ? 5 : 4;
							case 3 -> clockwise ? 4 : 5;
							case 4 -> clockwise ? 2 : 3;
							default -> clockwise ? 3 : 2;
						};
					}
					else {
						int skullFace = skullEnt.getSkullRotationServerSafe();
						
						if (clockwise) {
							if ((skullFace += 4) > 15) {
								skullFace -= 16;
							}
						}
						else if ((skullFace -= 4) < 0) {
							skullFace += 16;
						}
						
						nbt.setByte("Rot", (byte) (skullFace & 0xFF));
					}
				}
			}
			else if (currentBlock instanceof BlockTrapDoor) {
				int prevI = i;
				i &= 0xFC;
				int i1 = prevI & 3;
				
				i |= switch (i1) {
					case 0 -> clockwise ? 3 : 2;
					case 1 -> clockwise ? 2 : 3;
					case 2 -> clockwise ? 0 : 1;
					default -> clockwise ? 1 : 0;
				};
			}
			else if (currentBlock instanceof BlockPistonBase || currentBlock instanceof BlockPistonExtension) {
				int i1 = i & 7;
				int i2 = i & 8;
				
				i = switch (i1) {
					case 2 -> clockwise ? 5 : 4;
					case 3 -> clockwise ? 4 : 5;
					case 4 -> clockwise ? 2 : 3;
					default -> clockwise ? 3 : 2;
				};
				
				i |= i2;
			}
			else if (currentBlock instanceof BlockDoor) {
				int i1 = i & 8;
				
				i = switch (i) {
					case 0 -> clockwise ? 1 : 3;
					case 1 -> clockwise ? 2 : 0;
					case 2 -> clockwise ? 3 : 1;
					case 3 -> clockwise ? 0 : 2;
					case 4 -> clockwise ? 5 : 7;
					case 5 -> clockwise ? 6 : 4;
					case 6 -> clockwise ? 7 : 5;
					default -> clockwise ? 4 : 6;
				};
				
				i |= i1;
			}
			else {
				i = currentBlock.rotateMetadataAroundYAxis(i, !clockwise);
			}
		}
		return i;
	}
	
	public static void copyEntityInSelection(List<Entity> entitiesInSelection, List<EntityInfo> entities, int minX,
			int minY, int minZ, List<EntityInfo> undoEntity) {
		for (Entity entity : entitiesInSelection) {
			if (entity instanceof EntityPlayer) continue;
			
			NBTTagCompound nbt = new NBTTagCompound();
			entity.writeToNBT(nbt);
			entities.add(new EntityInfo(new LocAndAngle(entity.posX - minX,
														entity.posY - minY,
														entity.posZ - minZ,
														entity.rotationYaw,
														entity.rotationPitch), entity.getClass(), nbt));
			
			undoEntity.add(new EntityInfo(new LocAndAngle(entity.posX,
														  entity.posY,
														  entity.posZ,
														  entity.rotationYaw,
														  entity.rotationPitch), entity.getClass(), nbt));
		}
	}
	
	public static void saveBlockReplaced(World world, int x, int y, int z, Queue<BlockInfo> undoBlock,
			List<BlockInfo> undoNonBlock) {
		BlockInfoNoTile info = getGetBlocksInfo(world, x, y, z);
		NBTTagCompound nbt = null;
		
		if (info.tile() != null) {
			nbt = new NBTTagCompound();
			info.tile().writeToNBT(nbt);
			nbt.removeTag("x");
			nbt.removeTag("y");
			nbt.removeTag("z");
		}
		
		BlockInfo blockInfo = new BlockInfo(x, y, z, info.id(), info.meta(), nbt);
		
		//undoBlock.add(blockInfo);
		
		addBlockOrNonBlock(world, undoBlock, undoNonBlock, blockInfo);
	}
	
	public static void saveBlockToPlace(BlockInfo info, int x, int y, int z, World world, List<BlockInfo> nonBlockList,
			Queue<BlockInfo> blockList) {
		BlockInfo blockInfo = new BlockInfo(x, y, z, info.id(), info.meta(), info.tile());
		
		//blockList.add(blockInfo);
		
		addBlockOrNonBlock(world, blockList, nonBlockList, blockInfo);
	}
	
	public static MovingObjectPosition getBlockSenderIsLooking(ICommandSender sender) {
		EntityPlayer player = getPlayer(sender, sender.getCommandSenderName());
		
		return getBlockPlayerIsLooking(player);
	}
	
	public static MovingObjectPosition getBlockPlayerIsLooking(EntityPlayer player) {
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
	
	public static void sendErrorMsg(ICommandSender sender, String msg) {
		sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(msg).setColor(EnumChatFormatting.RED));
	}
	
	public static void sendEditMsg(ICommandSender sender, String msg) {
		ChatMessageComponent component = ChatMessageComponent.createFromTranslationKey("bu.transform.commands.prefix");
		
		component.addKey(msg);
		
		sender.sendChatToPlayer(component.setColor(EnumChatFormatting.LIGHT_PURPLE));
	}
	
	public static void sendEditMsg(ICommandSender sender, String msg, Object... args) {
		ChatMessageComponent component = ChatMessageComponent.createFromTranslationKey("bu.transform.commands.prefix");
		
		ChatMessageComponent core = ChatMessageComponent.createFromTranslationWithSubstitutions(msg, args);
		
		ChatMessageComponent combined = ChatMessageComponent.createFromTranslationWithSubstitutions("%s%s",
																									component,
																									core);
		sender.sendChatToPlayer(combined.setColor(EnumChatFormatting.LIGHT_PURPLE));
	}
	
	public record BlockInfo(int x, int y, int z, int id, int meta, NBTTagCompound tile) {}
	
	public record BlockToRemoveInfo(int x, int y, int z, boolean hasTile) {}
	
	public record QueueInfo(String id, List<Selection> selection, SavedLists editList, SavedLists undoList,
							SavedLists redoList, int[] num, EntityPlayer player) {}
	
	public record EntityInfo(LocAndAngle locAndAngle, Class entityClass, NBTTagCompound nbt) {}
	
	public record LocAndAngle(double x, double y, double z, float yaw, float pitch) {}
	
	public record Selection(BlockPos pos1, BlockPos pos2) {}
	
	public record SelectionD(BlockPosD pos1, BlockPosD pos2) {}
	
	public record SavedLists(List<BlockInfo> nonBlockList, Queue<BlockInfo> blockList, Queue<BlockInfo> allBlocks,
							 List<EntityInfo> entities, Queue<BlockToRemoveInfo> blocksToRemove) {}
	
	public record BlockPosD(double x, double y, double z) {}
	
	public record BlockInfoNoTile(int id, int meta, TileEntity tile) {}
}

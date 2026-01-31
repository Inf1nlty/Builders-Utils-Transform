package net.dravigen.bu_transform.commands;

import api.world.BlockPos;
import net.dravigen.bu_transform.api.PacketUtils;
import net.minecraft.src.CommandBase;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.MovingObjectPosition;

import java.util.List;

import static net.dravigen.bu_transform.api.ToolHelper.*;

public class PosAll extends CommandBase {
	@Override
	public String getCommandName() {
		return "posAll";
	}
	
	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/posAll [x1/y1/z1] [x2/y2/z2]";
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] strings) {
		try {
			if (strings.length != 2) {
				sendErrorMsg(sender, "bu.transform.commands.error.selection2");
				
				return;
			}
			
			if (strings[0].split("/").length != 3 || strings[1].split("/").length != 3) {
				sendErrorMsg(sender, "bu.transform.commands.error.format");
				
				return;
			}
			
			int x1 = Integer.parseInt(strings[0].split("/")[0]);
			int y1 = Integer.parseInt(strings[0].split("/")[1]);
			int z1 = Integer.parseInt(strings[0].split("/")[2]);
			int x2 = Integer.parseInt(strings[1].split("/")[0]);
			int y2 = Integer.parseInt(strings[1].split("/")[1]);
			int z2 = Integer.parseInt(strings[1].split("/")[2]);
			
			pos1PlayersMap.put(sender, new BlockPos(x1, y1, z1));
			pos2PlayersMap.put(sender, new BlockPos(x2, y2, z2));
			PacketUtils.sendPosUpdate(1, (EntityPlayerMP) sender);
			PacketUtils.sendPosUpdate(2, (EntityPlayerMP) sender);
			
			//pos1.set(x1, y1, z1);
			//pos2.set(x2, y2, z2);
			sendEditMsg(sender, "bu.transform.commands.posAll", x1, y1, z1, x2, y2, z2);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] strings) {
		MovingObjectPosition block = getBlockSenderIsLooking(sender);
		
		if (block != null && strings.length < 2) {
			return getListOfStringsMatchingLastWord(strings, block.blockX + "/" + block.blockY + "/" + block.blockZ);
		}
		
		return null;
	}
}

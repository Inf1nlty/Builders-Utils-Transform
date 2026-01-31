package net.dravigen.bu_transform.commands;

import api.world.BlockPos;
import net.dravigen.bu_transform.api.PacketUtils;
import net.minecraft.src.*;

import java.util.List;

import static net.dravigen.bu_transform.api.ToolHelper.*;

public class Pos2 extends CommandBase {
	@Override
	public String getCommandName() {
		return "pos2";
	}
	
	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/pos2 [x/y/z]";
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] strings) {
		try {
			if (strings.length == 1 && strings[0].split("/").length != 3) {
				sendErrorMsg(sender, "bu.transform.commands.error.format");
				
				return;
			}
			
			EntityPlayer player = getPlayer(sender, sender.getCommandSenderName());
			int x = strings.length == 1
					? Integer.parseInt(strings[0].split("/")[0])
					: MathHelper.floor_double(player.posX);
			int y = strings.length == 1
					? Integer.parseInt(strings[0].split("/")[1])
					: MathHelper.floor_double(player.posY);
			int z = strings.length == 1
					? Integer.parseInt(strings[0].split("/")[2])
					: MathHelper.floor_double(player.posZ);
			
			pos2PlayersMap.put(player, new BlockPos(x, y, z));
			PacketUtils.sendPosUpdate(2, (EntityPlayerMP) sender);
			//pos2.set(x, y, z);
			sendEditMsg(sender, "bu.transform.commands.pos2", x, y, z);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] strings) {
		MovingObjectPosition block = getBlockSenderIsLooking(sender);
		
		if (block != null && strings.length < 1) {
			return getListOfStringsMatchingLastWord(strings, block.blockX + "/" + block.blockY + "/" + block.blockZ);
		}
		
		return null;
	}
}

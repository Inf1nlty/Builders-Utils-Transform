package net.dravigen.creative_tools.commands;

import net.minecraft.src.*;

import java.util.List;

import static net.dravigen.creative_tools.api.ToolHelper.*;

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
		EntityPlayer player = getPlayer(sender, sender.getCommandSenderName());
		int x = strings.length == 1 ? Integer.parseInt(strings[0].split("/")[0]) : MathHelper.floor_double(player.posX);
		int y = strings.length == 1 ? Integer.parseInt(strings[0].split("/")[1]) : MathHelper.floor_double(player.posY);
		int z = strings.length == 1 ? Integer.parseInt(strings[0].split("/")[2]) : MathHelper.floor_double(player.posZ);
		
		pos2.set(x, y, z);
		sendEditMsg(sender,
					StatCollector.translateToLocal("commands.prefix") +
							String.format(StatCollector.translateToLocal("commands.pos2"), x, y, z));
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] strings) {
		MovingObjectPosition block = getBlockPlayerIsLooking(sender);
		
		if (block != null && strings.length < 1) {
			return getListOfStringsMatchingLastWord(strings, block.blockX + "/" + block.blockY + "/" + block.blockZ);
		}
		
		return null;
	}
}

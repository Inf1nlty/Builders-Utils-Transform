package net.dravigen.creative_tools.commands;

import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.StatCollector;

import java.util.List;

import static net.dravigen.creative_tools.api.HelperCommand.sendEditMsg;
import static net.dravigen.creative_tools.api.ToolHelper.getBlockPlayerIsLooking;
import static net.dravigen.creative_tools.api.ToolHelper.pos1;

public class Pos1 extends CommandBase {
	@Override
	public String getCommandName() {
		return "pos1";
	}
	
	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/pos1 [x/y/z]";
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] strings) {
		MovingObjectPosition block = getBlockPlayerIsLooking(sender);
		
		if (block != null && strings.length < 1) {
			return getListOfStringsMatchingLastWord(strings, block.blockX + "/" + block.blockY + "/" + block.blockZ);
		}
		
		return null;
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] strings) {
		int x = strings.length == 1 ? Integer.parseInt(strings[0].split("/")[0]) : pos1.x;
		int y = strings.length == 1 ? Integer.parseInt(strings[0].split("/")[1]) : pos1.y;
		int z = strings.length == 1 ? Integer.parseInt(strings[0].split("/")[2]) : pos1.z;
		
		pos1.set(x, y, z);
		sendEditMsg(sender, StatCollector.translateToLocal("commands.prefix") + String.format(StatCollector.translateToLocal("commands.pos1"), x, y, z));
	}
}

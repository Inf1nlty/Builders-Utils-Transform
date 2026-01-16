package net.dravigen.creative_tools.commands;

import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.StatCollector;

import java.util.List;

import static net.dravigen.creative_tools.api.HelperCommand.sendEditMsg;
import static net.dravigen.creative_tools.api.ToolHelper.getBlockPlayerIsLooking;
import static net.dravigen.creative_tools.api.ToolHelper.pos2;

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
	public List addTabCompletionOptions(ICommandSender sender, String[] strings) {
		MovingObjectPosition block = getBlockPlayerIsLooking(sender);
		
		if (block != null && strings.length < 1) {
			return getListOfStringsMatchingLastWord(strings, block.blockX + "/" + block.blockY + "/" + block.blockZ);
		}
		
		return null;
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] strings) {
		int x = strings.length == 1 ? Integer.parseInt(strings[0].split("/")[0]) : pos2.x;
		int y = strings.length == 1 ? Integer.parseInt(strings[0].split("/")[1]) : pos2.y;
		int z = strings.length == 1 ? Integer.parseInt(strings[0].split("/")[2]) : pos2.z;
		
		pos2.set(x, y, z);
		sendEditMsg(sender, StatCollector.translateToLocal("commands.prefix") + String.format(StatCollector.translateToLocal("commands.pos2"), x, y, z));
	}
}

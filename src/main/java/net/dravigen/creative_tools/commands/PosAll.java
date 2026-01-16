package net.dravigen.creative_tools.commands;

import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.StatCollector;

import java.util.List;

import static net.dravigen.creative_tools.api.ToolHelper.*;

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
		int x1 = strings.length == 2 ? Integer.parseInt(strings[0].split("/")[0]) : pos1.x;
		int y1 = strings.length == 2 ? Integer.parseInt(strings[0].split("/")[1]) : pos1.y;
		int z1 = strings.length == 2 ? Integer.parseInt(strings[0].split("/")[2]) : pos1.z;
		int x2 = strings.length == 2 ? Integer.parseInt(strings[1].split("/")[0]) : pos2.x;
		int y2 = strings.length == 2 ? Integer.parseInt(strings[1].split("/")[1]) : pos2.y;
		int z2 = strings.length == 2 ? Integer.parseInt(strings[1].split("/")[2]) : pos2.z;
		
		pos1.set(x1, y1, z1);
		pos2.set(x2, y2, z2);
		sendEditMsg(sender,
					StatCollector.translateToLocal("commands.prefix") +
							String.format(StatCollector.translateToLocal("commands.posAll"), x1, y1, z1, x2, y2, z2));
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] strings) {
		MovingObjectPosition block = getBlockPlayerIsLooking(sender);
		
		if (block != null && strings.length < 2) {
			return getListOfStringsMatchingLastWord(strings, block.blockX + "/" + block.blockY + "/" + block.blockZ);
		}
		
		return null;
	}
}

package net.dravigen.creative_tools.commands;

import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.StatCollector;

import static net.dravigen.creative_tools.api.HelperCommand.sendEditMsg;
import static net.dravigen.creative_tools.api.HelperCommand.sendErrorMsg;
import static net.dravigen.creative_tools.api.ToolHelper.*;

public class Undo extends CommandBase {
	@Override
	public String getCommandName() {
		return "undo";
	}
	
	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/undo [number]";
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] strings) {
		int num = strings.length == 1 ? Integer.parseInt(strings[0]) : 1;
		
		for (int i = 0; i < num; i++) {
			if (!undoList.isEmpty()) {
				editList.add(undoList.get(undoList.size() - 1));
				undoList.remove(undoList.size() - 1);
				
				sendEditMsg(sender,
							StatCollector.translateToLocal("commands.prefix") +
									String.format(StatCollector.translateToLocal("commands.undo")));
			}
			else {
				sendErrorMsg(sender, StatCollector.translateToLocal("commands.error.undo"));
			}
		}
	}
}

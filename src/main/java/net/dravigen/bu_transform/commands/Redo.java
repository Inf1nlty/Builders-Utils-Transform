package net.dravigen.bu_transform.commands;

import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.StatCollector;

import java.util.List;

import static net.dravigen.bu_transform.api.ToolHelper.*;

public class Redo extends CommandBase {
	@Override
	public String getCommandName() {
		return "redo";
	}
	
	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/redo";
	}
	
	
	@Override
	public void processCommand(ICommandSender sender, String[] strings) {
		if (!redoList.isEmpty()) {
			for (QueueInfo queueInfo : editList) {
				if (queueInfo.id().equals("undo") || queueInfo.id().equals("redo")) {
					sendErrorMsg(sender, StatCollector.translateToLocal("commands.error.process"));
					
					return;
				}
			}
			
			QueueInfo queueInfo = redoList.get(redoList.size() - 1);
			redoList.remove(redoList.size() - 1);
			editList.add(queueInfo);
			
			List<Selection> selection = queueInfo.selection();
			
			if (selection.size() > 1) {
				pos1 = selection.get(1).pos1();
				pos2 = selection.get(1).pos2();
			}
			
			sendEditMsg(sender,
						StatCollector.translateToLocal("commands.prefix") +
								String.format(StatCollector.translateToLocal("commands.redo")));
		}
		else {
			sendErrorMsg(sender, StatCollector.translateToLocal("commands.error.redo"));
		}
	}
}

package net.dravigen.creative_tools.commands;

import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.StatCollector;

import java.util.ArrayList;
import java.util.List;

import static net.dravigen.creative_tools.api.ToolHelper.*;

public class Undo extends CommandBase {
	@Override
	public String getCommandName() {
		return "undo";
	}
	
	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/undo";
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] strings) {
		if (!undoList.isEmpty()) {
			for (QueueInfo queueInfo : editList) {
				if (queueInfo.id().equals("undo") || queueInfo.id().equals("redo")) {
					sendErrorMsg(sender, StatCollector.translateToLocal("commands.error.process"));
					
					return;
				}
			}
			
			
			QueueInfo queueInfo = undoList.get(undoList.size() - 1);
			undoList.remove(undoList.size() - 1);
			editList.add(queueInfo);
			
			List<Selection> selection = queueInfo.selection();
			
			if (selection.size() > 1) {
				pos1 = selection.get(0).pos1();
				pos2 = selection.get(0).pos2();
			}
			
			redoList.add(new QueueInfo("redo",
									   new ArrayList<>(selection),
									   duplicateSavedList(queueInfo.redoList()),
									   duplicateSavedList(queueInfo.editList()),
									   duplicateSavedList(queueInfo.redoList()),
									   new int[SAVED_NUM],
									   queueInfo.player()));
			
			sendEditMsg(sender,
						StatCollector.translateToLocal("commands.prefix") +
								String.format(StatCollector.translateToLocal("commands.undo")));
		}
		else {
			sendErrorMsg(sender, StatCollector.translateToLocal("commands.error.undo"));
		}
	}
}

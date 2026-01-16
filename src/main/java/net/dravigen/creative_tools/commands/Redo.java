package net.dravigen.creative_tools.commands;

import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.StatCollector;

import java.util.ArrayList;

import static net.dravigen.creative_tools.api.ToolHelper.*;

public class Redo extends CommandBase {
	@Override
	public String getCommandName() {
		return "redo";
	}
	
	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/redo [number]";
	}
	
	
	
	/// TODO: fix that to allow multiple redo at once
	@Override
	public void processCommand(ICommandSender sender, String[] strings) {
		int num = strings.length == 1 ? Integer.parseInt(strings[0]) : 1;
		
		if (!redoList.isEmpty()) {
			QueueInfo queueInfo = redoList.get(redoList.size() - 1);
			redoList.remove(redoList.size() - 1);
			
			if (num > 1) {
				for (int i = 0; i < num; i++) {
					if (!redoList.isEmpty()) {
						QueueInfo toMerge = redoList.get(redoList.size() - 1);
						queueInfo = mergeQueue(toMerge, queueInfo);
						redoList.remove(redoList.size() - 1);
					}
				}
				
				editList.add(queueInfo);
				
				sendEditMsg(sender,
							StatCollector.translateToLocal("commands.prefix") +
									String.format(StatCollector.translateToLocal("commands.redoMul"), num));
			}
			else {
				editList.add(queueInfo);
				
				sendEditMsg(sender,
							StatCollector.translateToLocal("commands.prefix") +
									String.format(StatCollector.translateToLocal("commands.redo")));
			}
			
			
		}
		else {
			sendErrorMsg(sender, StatCollector.translateToLocal("commands.error.redo"));
		}
	}
}

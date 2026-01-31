package net.dravigen.bu_transform.commands;

import net.dravigen.bu_transform.api.PacketUtils;
import net.minecraft.src.CommandBase;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ICommandSender;

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
		try {
			List<QueueInfo> redoList = redoPlayersMap.get(sender);
			
			if (!redoList.isEmpty()) {
				for (QueueInfo queueInfo : editList) {
					if (queueInfo.id().equals(sender.getCommandSenderName() + "|undo") ||
							queueInfo.id().equals(sender.getCommandSenderName() + "|redo")) {
						sendErrorMsg(sender, "bu.transform.commands.error.process");
						
						return;
					}
				}
				
				QueueInfo queueInfo = redoList.get(redoList.size() - 1);
				redoList.remove(redoList.size() - 1);
				editList.add(queueInfo);
				
				List<Selection> selections = queueInfo.selection();
				
				pos1PlayersMap.put(sender, selections.get(0).pos1());
				pos2PlayersMap.put(sender, selections.get(0).pos2());
				PacketUtils.sendPosUpdate(1, (EntityPlayerMP) sender);
				PacketUtils.sendPosUpdate(2, (EntityPlayerMP) sender);
				
				sendEditMsg(sender, "bu.transform.commands.redo");
			}
			else {
				sendErrorMsg(sender, "bu.transform.commands.error.redo");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

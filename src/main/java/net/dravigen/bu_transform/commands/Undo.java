package net.dravigen.bu_transform.commands;

import net.dravigen.bu_transform.api.PacketUtils;
import net.minecraft.src.CommandBase;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ICommandSender;

import java.util.ArrayList;
import java.util.List;

import static net.dravigen.bu_transform.api.ToolHelper.*;

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
		try {
			List<QueueInfo> redoList = redoPlayersMap.get(sender);
			List<QueueInfo> undoList = undoPlayersMap.get(sender);
			
			if (!undoList.isEmpty()) {
				for (QueueInfo queueInfo : editList) {
					if (queueInfo.id().equals(sender.getCommandSenderName() + "|undo") ||
							queueInfo.id().equals(sender.getCommandSenderName() + "|redo")) {
						sendErrorMsg(sender, "bu.transform.commands.error.process");
						
						return;
					}
				}
				
				
				QueueInfo queueInfo = undoList.get(undoList.size() - 1);
				undoList.remove(undoList.size() - 1);
				editList.add(queueInfo);
				
				List<Selection> selection = queueInfo.selection();
				
				if (selection.size() > 1) {
					pos1PlayersMap.put(sender, selection.get(1).pos1());
					pos2PlayersMap.put(sender, selection.get(1).pos2());
					PacketUtils.sendPosUpdate(1, (EntityPlayerMP) sender);
					PacketUtils.sendPosUpdate(2, (EntityPlayerMP) sender);
				}
				
				redoList.add(new QueueInfo("redo",
										   new ArrayList<>(selection),
										   duplicateSavedList(queueInfo.redoList()),
										   duplicateSavedList(queueInfo.editList()),
										   duplicateSavedList(queueInfo.redoList()),
										   new int[SAVED_NUM],
										   queueInfo.player()));
				
				sendEditMsg(sender, "bu.transform.commands.undo");
			}
			else {
				sendErrorMsg(sender, "bu.transform.commands.error.undo");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

package net.dravigen.bu_transform.commands;

import net.dravigen.bu_transform.BU_Transform;
import net.dravigen.bu_transform.api.ConfigUpdater;
import net.dravigen.bu_transform.api.ToolHelper;
import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;

public class EditSpeed extends CommandBase {
	@Override
	public String getCommandName() {
		return "editSpeed";
	}
	
	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/editSpeed <speed>";
	}
	
	@Override
	public void processCommand(ICommandSender iCommandSender, String[] strings) {
		try {
			if (strings.length == 0) {
				ToolHelper.sendEditMsg(iCommandSender, "bu.transform.commands.editSpeed", BU_Transform.SPEED);
				
				return;
			}
			
			int value = Integer.parseInt(strings[0]);
			ConfigUpdater.updateValue(BU_Transform.instance.addonConfig, "bu_tr.editSpeed", value);
			BU_Transform.SPEED = value;
			
			ToolHelper.sendEditMsg(iCommandSender, "bu.transform.commands.editSpeed", value);
		} catch (NumberFormatException e) {
			throw new RuntimeException(e);
		}
	}
}

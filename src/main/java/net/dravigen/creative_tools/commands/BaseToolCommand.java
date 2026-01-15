package net.dravigen.creative_tools.commands;

import net.minecraft.src.*;

import java.util.List;

import static net.dravigen.creative_tools.api.HelperCommand.sendErrorMsg;

public class BaseToolCommand extends CommandBase {
	@Override
	public String getCommandName() {
		return "tools";
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "";
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {
		return super.addTabCompletionOptions(par1ICommandSender, par2ArrayOfStr);
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] command) {
		sendErrorMsg(sender, "Oy mate, chuck a command in would ya ?");
	}
	
	
	
	
	/*** Useful commands:
	 *
	 * CommandEffect.notifyAdmins(sender, "", getPlayer(sender, sender.getCommandSenderName()).getEntityName());
	 *
	 */
}

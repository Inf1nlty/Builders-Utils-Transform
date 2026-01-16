package net.dravigen.creative_tools.commands;

import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;

public class Redo extends CommandBase {
	@Override
	public String getCommandName() {
		return "redo";
	}
	
	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/redo [number]";
	}
	
	@Override
	public void processCommand(ICommandSender iCommandSender, String[] strings) {
	
	}
}

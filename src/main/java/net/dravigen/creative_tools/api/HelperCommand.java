package net.dravigen.creative_tools.api;

import net.minecraft.src.ChatMessageComponent;
import net.minecraft.src.ICommandSender;

public class HelperCommand {
	public static void sendMsg(ICommandSender sender, String msg) {
		sender.sendChatToPlayer(ChatMessageComponent.createFromText(msg));
	}
	
	public static void sendErrorMsg(ICommandSender sender, String msg) {
		sender.sendChatToPlayer(ChatMessageComponent.createFromText("§c" + msg));
	}
	
	public static void sendEditMsg(ICommandSender sender, String msg) {
		sender.sendChatToPlayer(ChatMessageComponent.createFromText("§d" + msg));
	}
	
	
}

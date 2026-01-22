package net.dravigen.creative_tools;

import api.AddonHandler;
import api.BTWAddon;
import net.dravigen.creative_tools.commands.*;

public class CreativeTools extends BTWAddon {
	private static CreativeTools instance;
	
	public CreativeTools() {
		super();
		instance = this;
	}
	
	@Override
	public void initialize() {
		AddonHandler.logMessage(this.getName() + " Version " + this.getVersionString() + " Initializing...");
		
		initCommands();
	}
	
	private void initCommands() {
		registerAddonCommand(new Copy());
		registerAddonCommand(new Paste());
		registerAddonCommand(new Move());
		registerAddonCommand(new Cut());
		registerAddonCommand(new Stack());
		registerAddonCommand(new Remove());
		registerAddonCommand(new Pos1());
		registerAddonCommand(new Pos2());
		registerAddonCommand(new PosAll());
		registerAddonCommand(new Undo());
		registerAddonCommand(new Redo());
		registerAddonCommand(new Rotate());
	}
}
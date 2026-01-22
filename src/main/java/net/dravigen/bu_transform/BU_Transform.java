package net.dravigen.bu_transform;

import api.AddonHandler;
import api.BTWAddon;
import net.dravigen.bu_transform.commands.*;

public class BU_Transform extends BTWAddon {
	private static BU_Transform instance;
	
	public BU_Transform() {
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
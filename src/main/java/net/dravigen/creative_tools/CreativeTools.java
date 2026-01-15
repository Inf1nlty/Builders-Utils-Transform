package net.dravigen.creative_tools;

import api.AddonHandler;
import api.BTWAddon;
import net.dravigen.creative_tools.commands.Copy;
import net.dravigen.creative_tools.commands.Cut;
import net.dravigen.creative_tools.commands.Move;
import net.dravigen.creative_tools.commands.Paste;

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
	}
}
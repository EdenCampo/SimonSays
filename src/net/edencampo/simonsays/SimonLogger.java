package net.edencampo.simonsays;

import net.edencampo.simonsays.SimonSays.SimonGame;

import org.bukkit.plugin.PluginDescriptionFile;

public class SimonLogger 
{
	SimonSays plugin;
	
	public SimonLogger(SimonSays instance)
	{
		plugin = instance;
	}
	
	public void logSevereError(String msg)
	{
		PluginDescriptionFile pdFile = plugin.getDescription();
		plugin.getLogger().severe(pdFile.getName() + " " + pdFile.getVersion() + ": " + msg);
	}
	
	public void logWarning(String msg)
	{
		PluginDescriptionFile pdFile = plugin.getDescription();
		plugin.getLogger().warning(pdFile.getName() + " " + pdFile.getVersion() + ": " + msg);
	}
	
	public void logInfo(String msg)
	{	
		PluginDescriptionFile pdFile = plugin.getDescription();
		plugin.getLogger().info(pdFile.getName() + " " + pdFile.getVersion() + ": " + msg);
	}

	public void logEnum(SimonGame result)
	{
		PluginDescriptionFile pdFile = plugin.getDescription();
		plugin.getLogger().info(pdFile.getName() + " " + pdFile.getVersion() + ": " + result);
		
	}
}

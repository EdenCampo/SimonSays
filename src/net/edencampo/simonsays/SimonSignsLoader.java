package net.edencampo.simonsays;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class SimonSignsLoader
{
	SimonSays plugin;
	
	public SimonSignsLoader(SimonSays instance)
	{
		plugin = instance;
	}
	
	private FileConfiguration customConfig = null;
	private File customConfigFile = null;
	
	public void reloadSignsConfig() 
	{
	    if (customConfigFile == null) 
	    {
	    	customConfigFile = new File(plugin.getDataFolder(), "SavedArenaSigns.yml");
	    }
	    customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
	 
	    InputStream defConfigStream = plugin.getResource("SavedArenaSigns.yml");
	    if (defConfigStream != null) 
	    {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        customConfig.setDefaults(defConfig);
	    }
	}
	
	
	public FileConfiguration getSignsConfig() 
	{
	    if (customConfig == null) 
	    {
	        return null;
	    }
	    
	    return customConfig;
	}
	
	public void saveSignsConfig() 
	{
	    if (customConfig == null || customConfigFile == null)
	    {
	        return;
	    }
	    
	    try 
	    {
	    	getSignsConfig().save(customConfigFile);
	    } 
	    catch (IOException ex)
	    {
	        plugin.getLogger().log(Level.WARNING, "Could not save config to " + customConfigFile, ex);
	    }
	}
	
	public void saveDefaultSignsConfig() 
	{
	    if (customConfigFile == null) 
	    {
	        customConfigFile = new File(plugin.getDataFolder(), "SavedArenaSigns.yml");
	    }
	    if (!customConfigFile.exists()) 
	    {            
	         plugin.saveResource("SavedArenaSigns.yml", false);
	    }
	}
	
	public void CFGaddSignArena(String arenatoconnect, String location)
	{
		String ArenasConnected = plugin.SimonSignsM.getSignsConfig().getString("ArenasConnected");
		String SignLocs = plugin.SimonSignsM.getSignsConfig().getString("SignLocations");
		
		if(ArenasConnected != null)
		{
			plugin.SimonSignsM.getSignsConfig().set("ArenasConnected", ArenasConnected + " | " + arenatoconnect);
			plugin.SimonSignsM.getSignsConfig().set("SignLocations", SignLocs + " | " + location);
		}
		else
		{
			plugin.SimonSignsM.getSignsConfig().set("ArenasConnected", arenatoconnect);
			plugin.SimonSignsM.getSignsConfig().set("SignLocations", location);
		}
		
		saveSignsConfig();
		reloadSignsConfig();
		
		plugin.SimonLog.logInfo("Successfully linked sign to arena: " + arenatoconnect);
	}
	
	public void CFGlinkSignsToArenas()
	{
		String ConnectedArenas = plugin.SimonSignsM.getSignsConfig().getString("ArenasConnected");
		String SignLocations = plugin.SimonSignsM.getSignsConfig().getString("SignLocations");

		if(ConnectedArenas == null)
		{
			plugin.SimonLog.logInfo("SavedArenaSigns.yml seems empty.. skipping!");
			return;
		}
		
		String[] Names = null;
		String[] Locations = null;
		
		if(ConnectedArenas != null)
		{
			Names = ConnectedArenas.split(" | ");
		}
		
		if(SignLocations != null)
		{
			Locations = SignLocations.split(" | ");
		}
		
		int id = 0;
		while(id < 1)
		{	
			Block block = Bukkit.getServer().getWorld("world").getBlockAt(SimonGameArenaManager.getGameManager().deserializeLoc(Locations[0]));
			
			Sign arenasign = (Sign) block.getState();
			
			SimonGameArenaManager.getGameManager().getArena(Names[id]).setSign(arenasign);
			
			plugin.SimonLog.logInfo("Successfully linked " + Names[id] + " with sign at " + Locations[id]);
			
			id++;
		}
	}
}

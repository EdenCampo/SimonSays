package net.edencampo.simonsays.Dataloaders;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import net.edencampo.simonsays.SimonSays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
	
	public String getSQLSignsCount()
	{
		if(plugin.UsingMySQL() == true)
		{
			try 
			{
				Connection connection = plugin.sql.getConnection();
				
				Statement loadsigns = connection.createStatement();
				
				ResultSet res = loadsigns.executeQuery("SELECT SignID FROM SimonSays_SignLinks;");
				
				if(res.last())
				{
					String signcount = res.getString("SignID");
					
					res.close();
					
					return signcount+1;	
				}
			}
			catch (SQLException e) 
			{
				plugin.SimonLog.logSevereError("Failed to get sign-count (MySQL)! Error: " + e.getMessage());
			}
		}
		
		return "0";
	}
	
	
	public void SQLlinkSignsToArenas()
	{	
		String signcount = getSQLSignsCount();
		
		int signs = Integer.parseInt(signcount);
		
		if(signs == 0)
		{
			plugin.SimonLog.logInfo("Can't find signs to load... Skipping!");
			return;
		}
		
		
		try 
		{
			int id = 1;
			while(id < signs)
			{
				Connection connection = plugin.sql.getConnection();
				
				Statement loadsigns = connection.createStatement();
					
				ResultSet res = loadsigns.executeQuery("SELECT ArenaConnected,SignLocation FROM SimonSays_SignLinks WHERE SignID = '" + id + "';");
				
				String ArenaConnected = "";
				String SignLoc = "";
				
				
				if(res.next())
				{
					ArenaConnected = res.getString("ArenaConnected");
					SignLoc = res.getString("SignLocation");
					res.close();
				}
				else
				{
					id++;
					continue;
				}
				
				Block block = Bukkit.getServer().getWorld("world").getBlockAt(plugin.SimonAM.deserializeLoc(SignLoc));
				
				if(block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST)
				{
					block.setType(Material.SIGN);
				}
				
				Sign arenasign = (Sign) block.getState();
				
				plugin.SimonAM.getArena(ArenaConnected).setSign(arenasign);
				
				plugin.SimonLog.logInfo("Successfully linked '" + ArenaConnected + "' with sign at " + SignLoc);
				
				id++;
			}
			
			plugin.SimonLog.logInfo("Finished loading all signs! (MySQL)");
		}
		catch (SQLException e) 
		{
			plugin.SimonLog.logSevereError(e.getMessage());
		}
	}
	
	public void SQLaddSignArena(String arenatoconnect, String location)
	{
		if(plugin.UsingMySQL() == true)
		{	
			try 
			{	
				Connection connection = plugin.sql.getConnection();
				
				Statement createtables = connection.createStatement();
				
				createtables.executeUpdate("INSERT INTO SimonSays_SignLinks (`ArenaConnected`, `SignLocation`) VALUES ('" + arenatoconnect + "', '" + location +"');");
				plugin.SimonLog.logInfo("Success! Added new arena:" + arenatoconnect + " (MySQL) ");
			} 
			catch (SQLException e) 
			{
				plugin.SimonLog.logSevereError(ChatColor.RED + "INSERT INTO SimonSays_SignLinks (`ArenaConnected`, `SignLocation`) VALUES ('" + arenatoconnect + "', '" + location +"'); FAILED. Sign not added!");
				//e.printStackTrace();
			}
		 }
	}
	
	public void SQLremoveSignArena(String arena)
	{
		if(plugin.UsingMySQL() == true)
		{
			try 
			{
				Connection connection = plugin.sql.getConnection();
				
				Statement deletearena = connection.createStatement();
				
				deletearena.executeUpdate("DELETE FROM SimonSays_SignLinks WHERE ArenaConnected = '" + arena + "';");
				plugin.SimonLog.logInfo("Successfully removed sign connection: " + arena);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
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
		
		plugin.SimonLog.logInfo("Successfully linked sign to arena: '" + arenatoconnect + "'");
	}
	
	public void CFGlinkSignsToArenas()
	{
		String ArenaNames = plugin.SimonSignsM.getSignsConfig().getString("ArenasConnected");
		String ArenaLocs = plugin.SimonSignsM.getSignsConfig().getString("SignLocations");

		if(ArenaNames == null)
		{
			plugin.SimonLog.logInfo("Can't find arenas to load... Skipping!");
			return;
		}
		
		String[] Names = null;
		String[] Locations = null;
		
		if(ArenaNames != null)
		{
			Names = ArenaNames.split(" | ");
		}
		
		if(ArenaLocs != null)
		{
			Locations = ArenaLocs.split(" | ");
		}
		
		int id = 0;
		while(id < Names.length)
		{	
			if(Names[id].equals("|") || Locations[id].equals("|") || Locations[id].isEmpty() || Names[id].equals("DELETED"))
			{
				id++;
				continue;
			}
			
			plugin.SimonLog.logInfo("Trying to link arena '" + Names[id] + "' with sign at " + Locations[id]);
			
			Block block;
			
			Sign arenasign;
				
			block = Bukkit.getServer().getWorld(plugin.SimonAM.getLocWorld(Locations[id])).getBlockAt(plugin.SimonAM.deserializeLoc(Locations[id]));
				
			if(block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST)
			{
				plugin.SimonLog.logInfo("Failed to link arena '" + Names[id] + "' with sign at " + Locations[id] + " couldn't find a sign there");
				return;
			}
				
			arenasign = (Sign) block.getState();

			plugin.SimonAM.getArena(Names[id]).setSign(arenasign);
				
			plugin.SimonLog.logInfo("Successfully linked '" + Names[id] + "' with sign at " + Locations[id]);
			
			id++;
			
			id++;
		}
		
		plugin.SimonLog.logInfo("Finished loading all signs! (config)");
	}
	
	
	public void CFGRemoveSign(String GameArena, String location)
	{
		String ArenaNames = plugin.SimonSignsM.getSignsConfig().getString("ArenasConnected");
		String ArenaLocs = plugin.SimonSignsM.getSignsConfig().getString("SignLocations");
		
		if(ArenaNames == null || ArenaLocs == null)
		{
			plugin.SimonLog.logWarning("Attempted to delete an arena from an empty cfg! Canceled!");
		}
		else
		{	
			plugin.SimonSignsM.getSignsConfig().set("ArenasConnected", CFGremoveFromList(ArenaNames, GameArena));
			plugin.SimonSignsM.getSignsConfig().set("SignLocations", CFGremoveFromList(ArenaLocs, location));
			
			plugin.SimonLog.logInfo("Deleting process finished for arena: " + GameArena);
			
			plugin.SimonSignsM.saveSignsConfig();
			plugin.SimonSignsM.reloadSignsConfig();
			
			plugin.SimonSignsM.CFGlinkSignsToArenas();
		}
	}
	
	
    public String CFGremoveFromList(String list, String toRemove)
    {
        String[] split = list.split(" | ");
        StringBuilder sb = new StringBuilder();
 
        for (String s : split)
        {
            if (!s.equals("|") && !s.equals(toRemove))
                sb.append(s + " | ");
        }
        
        return sb.toString().contains("|") ? sb.toString().substring(0, sb.lastIndexOf(" | ")) : sb.toString();
    }
	
	/*
	public void CFGremoveSignArena(String GameArena)
	{
		String ArenaNames = plugin.SimonSignsM.getSignsConfig().getString("ArenasConnected");
		String ArenaLocs = plugin.SimonSignsM.getSignsConfig().getString("SignLocations");
		
		if(ArenaNames == null || ArenaLocs == null)
		{
			plugin.SimonLog.logWarning("Attempted to delete a sign from an empty cfg! Canceled!");
		}
		else
		{
			String[] Names = ArenaNames.split(" | ");
			String[] Locs = ArenaLocs.split(" | ");
			
			int id = 0;
			int correctid = 0;
			while(id < Names.length)
			{			
				if(!ArenaNames.contains(GameArena))
				{
					plugin.SimonLog.logWarning("Attempted to delete an non-existing sign! Canceled!");
					return;
				}
				
				if(Names[id].contains(GameArena))
				{	
					correctid = id+2;
					break;
				}
				
				id++;
			}
			
			id = -1;
			while(id < correctid)
			{			
				if(correctid == 2 && id == -1 && Names.length == 1)
				{
					plugin.SimonSignsM.getSignsConfig().set("ArenasConnected", null);
					plugin.SimonSignsM.getSignsConfig().set("SignLocations", null);
					
					File signFile = new File(plugin.getDataFolder(), "SavedArenaSigns.yml");
					signFile.delete();
					
					plugin.SimonSignsM.saveDefaultSignsConfig();
					plugin.SimonSignsM.reloadSignsConfig();
					
					break;
				}
				
				if(id == correctid-1)
				{
					String NewArenaNames = ArenaNames.replace(Names[correctid], "DELETED");
					String NewArenaLocs = ArenaLocs.replace(Locs[correctid], "DELETED");
					
					plugin.SimonSignsM.getSignsConfig().set("ArenasConnected", NewArenaNames);
					plugin.SimonSignsM.getSignsConfig().set("SignLocations", NewArenaLocs);
					
					id++;
					continue;
				}
				
				if(id == -1)
				{
					id++;
					continue;
				}
				
				id++;
			}
			
			plugin.SimonSignsM.saveSignsConfig();
			plugin.SimonSignsM.reloadSignsConfig();
			
			plugin.SimonSignsM.CFGlinkSignsToArenas();
			
			plugin.SimonLog.logInfo("Deleting sign link process finished for arena: " + GameArena);
		}
	}*/
}
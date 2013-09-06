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
				//e.printStackTrace();
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
			plugin.SimonSignsM.getSignsConfig().set("ArenasConnected", ArenasConnected + " |" + arenatoconnect);
			plugin.SimonSignsM.getSignsConfig().set("SignLocations", SignLocs + " |" + location);
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
			plugin.SimonLog.logInfo("Can't find signs to load... Skipping!");
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
			if(Names[id].equals("|DELETED"))
			{
				id++;
				continue;
			}
			
			Block block;
			
			Sign arenasign;
			
			if(id > 1)
			{
				String CorrectLocation = Locations[id].replace("|", "");
				
				block = Bukkit.getServer().getWorld(plugin.SimonAM.getLocWorld(CorrectLocation)).getBlockAt(plugin.SimonAM.deserializeLoc(CorrectLocation));
				
				if(block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST)
				{
					block.setType(Material.SIGN);
				}
				
				arenasign = (Sign) block.getState();
				
				String CorrectName = Names[id].replace("|", "");
				plugin.SimonAM.getArena(CorrectName).setSign(arenasign);
				
				plugin.SimonLog.logInfo("Successfully linked '" + CorrectName + "' with sign at " + CorrectLocation);
			}
			else
			{
				String CorrectLocation = Locations[id].replace("|", "");
				
				block = Bukkit.getServer().getWorld(plugin.SimonAM.getLocWorld(CorrectLocation)).getBlockAt(plugin.SimonAM.deserializeLoc(CorrectLocation));
				
				if(block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST)
				{
					block.setType(Material.SIGN);
				}
				
				arenasign = (Sign) block.getState();
				
				
				String CorrectName = Names[id].replace("|", "");
				plugin.SimonAM.getArena(CorrectName).setSign(arenasign);
				
				plugin.SimonLog.logInfo("Successfully linked '" + CorrectName + "' with sign at " + CorrectLocation);
			}
			id++;
		}
	}
	
	public void CFGremoveSignArena(String GameArena)
	{
		String ArenaNames = plugin.SimonSignsM.getSignsConfig().getString("ArenasConnected");
		String ArenaLocs = plugin.SimonSignsM.getSignsConfig().getString("SignLocations");
		
		if(ArenaNames == null || ArenaLocs == null)
		{
			plugin.SimonLog.logWarning("Attempted to delete an sign from an empty cfg! Canceled!");
		}
		else
		{
			String[] Names = ArenaNames.split(" | ");
			String[] Locs = ArenaLocs.split(" | ");
			
			int id = 0;
			int correctid = 0;
			while(id < Names.length)
			{	
				//plugin.SimonLog.logWarning("ID = " + id + " " + "CorrectID = " + correctid + " " + "Length = " + Names.length);
				
				if(!ArenaNames.contains(GameArena))
				{
					plugin.SimonLog.logWarning("Attempted to delete an non-existing arena! Canceled!");
					return;
				}
				
				if(ArenaNames.contains(GameArena))
				{	
					correctid = id+2;
					
					plugin.SimonLog.logInfo("Deleting " + Names[correctid-1]);
					plugin.SimonLog.logInfo("Deleting " + Locs[correctid-1]);
					
					plugin.SimonLog.logInfo("Found split id = " + correctid);
					break;
				}
				
				id++;
			}
			
			id = -1;
			while(id < correctid)
			{
				if(correctid == 0 && id == 0)
				{
					plugin.SimonSignsM.getSignsConfig().set("ArenasConnected", "");
					plugin.SimonSignsM.getSignsConfig().set("SignLocations", "");
					
					plugin.SimonLog.logInfo("Found only single arena(ID = " + id + ")");
					break;
				}
				
				plugin.SimonLog.logInfo("Looping until " + correctid + " Current ID = " + id);
				
				if(id == correctid-1)
				{
					plugin.SimonLog.logInfo("Skipping " + Names[correctid-1]);
					plugin.SimonLog.logInfo("Skipping " + Locs[correctid-1]);
					
					String NewArenaNames = ArenaNames.replace(Names[correctid-1], "|DELETED");
					String NewArenaLocs = ArenaLocs.replace(Locs[correctid-1], "|DELETED");
					
					plugin.SimonSignsM.getSignsConfig().set("ArenasConnected", NewArenaNames);
					plugin.SimonSignsM.getSignsConfig().set("SignLocations", NewArenaLocs);
					
					id++;
					continue;
				}
				
				if(id == -1)
				{
					plugin.SimonLog.logInfo("Skipping loop for ID -1");
					id++;
					continue;
				}
							
				plugin.SimonLog.logInfo("Writing " + Names[id]);
				plugin.SimonLog.logInfo("Writing " + Locs[id]);
				
				id++;
			}
			
			plugin.SimonSignsM.saveSignsConfig();
			plugin.SimonSignsM.reloadSignsConfig();
			
			//plugin.SimonSignsM.CFGlinkSignsToArenas();
			
			plugin.SimonLog.logInfo("Removed link for: " + GameArena + " with a sign!");
		}
	}
}

//plugin.SimonLog.logInfo("Removed link for: " + GameArena + " with a sign!");
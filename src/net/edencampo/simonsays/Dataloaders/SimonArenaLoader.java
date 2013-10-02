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
import net.edencampo.simonsays.ArenaManagers.GameArena;
import net.edencampo.simonsays.ArenaManagers.SimonSpectateArenaManager;
import net.edencampo.simonsays.ArenaManagers.SpectateArena;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class SimonArenaLoader
{
	SimonSays plugin;
	
	public SimonArenaLoader(SimonSays instance)
	{
		plugin = instance;
	}
	
	private FileConfiguration customConfig = null;
	private File customConfigFile = null;
	
	public void reloadArenaConfig() 
	{
	    if (customConfigFile == null) 
	    {
	    	customConfigFile = new File(plugin.getDataFolder(), "SavedArenas.yml");
	    }
	    customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
	 
	    InputStream defConfigStream = plugin.getResource("SavedArenas.yml");
	    if (defConfigStream != null) 
	    {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        customConfig.setDefaults(defConfig);
	    }
	}
	
	
	public FileConfiguration getArenaConfig() 
	{
	    if (customConfig == null) 
	    {
	        return null;
	    }
	    
	    return customConfig;
	}
	
	public void saveArenaConfig() 
	{
	    if (customConfig == null || customConfigFile == null)
	    {
	        return;
	    }
	    
	    try 
	    {
	    	getArenaConfig().save(customConfigFile);
	    } 
	    catch (IOException ex)
	    {
	        plugin.getLogger().log(Level.WARNING, "Could not save config to " + customConfigFile, ex);
	    }
	}
	
	public void saveDefaultArenaConfig() 
	{
	    if (customConfigFile == null) 
	    {
	        customConfigFile = new File(plugin.getDataFolder(), "SavedArenas.yml");
	    }
	    if (!customConfigFile.exists()) 
	    {            
	         plugin.saveResource("SavedArenas.yml", false);
	    }
	}
	
	public void AddArenaToSQL(String location, String arenaname, String type, String relatedarena)
	{
		// types: 0-regular 1-spec
		
		if(plugin.UsingMySQL() == true)
		{	
			try 
			{	
				Connection connection = plugin.sql.getConnection();
				
				Statement createtables = connection.createStatement();
				
				createtables.executeUpdate("INSERT INTO SimonSays_Arenas (`ArenaName`, `ArenaLocation`, `ArenaType`, `RelatedArena`) VALUES ('" + arenaname + "', '" + location +"', '" + type + "', '" + relatedarena +"');");
				plugin.SimonLog.logInfo("Success! Added new arena:" + arenaname + " (MySQL) ");
			} 
			catch (SQLException e) 
			{
				plugin.SimonLog.logSevereError(ChatColor.RED + "WARNING: Adding new arena: " + arenaname + " FAILED. Arena was not added!");
				e.printStackTrace();
			}
		 }
	}
	
	
	
	public String getSQLGameArenasCount()
	{
		if(plugin.UsingMySQL() == true)
		{
			try 
			{
				Connection connection = plugin.sql.getConnection();
				
				Statement loadarenas = connection.createStatement();
				
				ResultSet res = loadarenas.executeQuery("SELECT ArenaID FROM SimonSays_Arenas;");
				
				if(res.last())
				{
					String arenacount = res.getString("ArenaID");
					
					res.close();
					
					return arenacount+1;	
				}
			}
			catch (SQLException e) 
			{
				plugin.SimonLog.logSevereError("Failed to get arena-count (MySQL)! Error: " + e.getMessage());
			}
		}
		
		return "0";
	}
	
	public void SQLLoadGameArenas()
	{
		String arenacount = getSQLGameArenasCount();
		
		int arenas = Integer.parseInt(arenacount);
		
		if(arenas == 0)
		{
			plugin.SimonLog.logInfo("Can't find arenas to load... Skipping!");
			return;
		}
		
		try 
		{
			int id = 1;
			while(id < arenas)
			{
				if(id == 0)
				{
					id++;
					continue;
				}
					
				Connection connection = plugin.sql.getConnection();
				
				Statement loadarenas = connection.createStatement();
				
				String ArenaType = "";
				String ArenaName = "";
				String ArenaLocation = "";
				
				ResultSet res = loadarenas.executeQuery("SELECT ArenaType,ArenaName,ArenaLocation FROM SimonSays_Arenas WHERE ArenaID = '" + id + "';");
				
				if(res.next())
				{
					ArenaType = res.getString("ArenaType");
					ArenaName = res.getString("ArenaName");
					ArenaLocation = res.getString("ArenaLocation");
					res.close();
				}
				else
				{
					id++;
					continue;
				}
				
				if(ArenaType.equals("0"))
				{
					GameArena a = new GameArena(plugin.SimonAM.deserializeLoc(ArenaLocation), ArenaName, plugin);
					plugin.SimonAM.arenas.add(a);
					plugin.SimonAM.getArena(ArenaName).spawn = plugin.SimonAM.deserializeLoc(ArenaLocation);
						
					plugin.SimonLog.logInfo("Successfully loaded game arena:" + " " +  ArenaName + " at " + ArenaLocation + " " +  "type: GameArena");
				}
				else if(ArenaType.equals("1"))
				{
					SpectateArena a =  new SpectateArena(SimonSpectateArenaManager.getSpecManager().deserializeLoc(ArenaLocation), ArenaName);
					SimonSpectateArenaManager.getSpecManager().arenas.add(a);
					SimonSpectateArenaManager.getSpecManager().getArena(ArenaName).spawn = plugin.SimonAM.deserializeLoc(ArenaLocation);
						
					plugin.SimonLog.logInfo("Successfully loaded spec arena:" + " " +  ArenaName + " at " + ArenaLocation + " " +  "type: SpectateArena");
				}
					
				id++;
			}
			
			plugin.SimonLog.logInfo("Finished loading all arenas! (MySQL)");
		} 
		catch (SQLException e) 
		{
			//e.printStackTrace();
			plugin.SimonLog.logSevereError(e.getMessage());
		}
}
	
	public String SQLGetRelatedGameArena(String GameArena)
	{
		if(plugin.UsingMySQL() == true)
		{		
			try 
			{
				Connection connection = plugin.sql.getConnection();
				
				Statement loadarenas = connection.createStatement();
				
				ResultSet res = loadarenas.executeQuery("SELECT RelatedArena FROM SimonSays_Arenas WHERE ArenaName = '" + GameArena + "';");
				res.next();
				
				String RelatedArena = res.getString("RelatedArena");
				
				return RelatedArena;
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		
		}
		return "none";
	}
	
	public void SQLRemoveArena(String Arena)
	{
		if(plugin.UsingMySQL() == true)
		{
			try 
			{
				Connection connection = plugin.sql.getConnection();
				
				Statement deletearena = connection.createStatement();
				
				deletearena.executeUpdate("DELETE FROM SimonSays_Arenas WHERE ArenaName = '" + Arena + "';");
				plugin.SimonLog.logInfo("Successfully removed arena: " + Arena);
			}
			catch (SQLException e)
			{
				plugin.SimonLog.logSevereError("Failed to remove arena " + Arena + " (MySQL)! Error: " + e.getMessage());
			}
		}
	}
	
	public void CFGLoadGameArenas()
	{
		String ArenaNames = plugin.SimonArenasM.getArenaConfig().getString("ArenaNames");
		String ArenaLocs = plugin.SimonArenasM.getArenaConfig().getString("ArenaLocations");
		String ArenaTypes = plugin.SimonArenasM.getArenaConfig().getString("ArenaTypes");

		if(ArenaNames == null)
		{
			plugin.SimonLog.logInfo("Can't find arenas to load... Skipping!");
			return;
		}
		
		String[] Names = null;
		String[] Locations = null;
		String[] Types = null;
		
		if(ArenaNames != null)
		{
			Names = ArenaNames.split(" | ");
		}
		
		if(ArenaLocs != null)
		{
			Locations = ArenaLocs.split(" | ");
		}
		
		if(ArenaTypes != null)
		{
			Types = ArenaTypes.split(" | ");
		}
		
		int id = 0;
		while(id < Names.length)
		{	
			try
			{
				if(Names[id].equals("DELETED"))
				{
					id++;
					continue;
				}
				
				if(Types[id].equals("0"))
				{
					GameArena a = new GameArena(plugin.SimonAM.deserializeLoc(Locations[id]), Names[id], plugin);
					plugin.SimonAM.arenas.add(a);
					plugin.SimonAM.getArena(Names[id]).spawn = plugin.SimonAM.deserializeLoc(Locations[id]);
					
					plugin.SimonLog.logInfo("Loaded arena:" + " '" +  Names[id] + "' at " + Locations[id] + " " +  "type: 'GameArena'");
				}
				else if(Types[id].equals("1"))
				{
					SpectateArena a =  new SpectateArena(SimonSpectateArenaManager.getSpecManager().deserializeLoc(Locations[id]), Names[id]);
					SimonSpectateArenaManager.getSpecManager().arenas.add(a);
					SimonSpectateArenaManager.getSpecManager().getArena(Names[id]).spawn = plugin.SimonAM.deserializeLoc(Locations[id]);
					
					plugin.SimonLog.logInfo("Loaded arena:" + " '" +  Names[id] + "' at '" + Locations[id] + "' " +  "type: 'SpectateArena'");
				}
			}
		    catch (IndexOutOfBoundsException ex )
		    {
		    	break;
		    }
			
			id++;
		}
		
		plugin.SimonLog.logInfo("Finished loading all arenas! (config)");
}
	public String CFGGetRelatedArena(String GameArena)
	{
		String ArenaNames = plugin.SimonArenasM.getArenaConfig().getString("ArenaNames");
		String RelatedArenas = plugin.SimonArenasM.getArenaConfig().getString("RelatedArena");
		
		if(RelatedArenas != null)
		{
			String[] Names = ArenaNames.split(" | ");
			String[] Relateds = RelatedArenas.split(" | ");
			
			int id = 0;
			while(id < Relateds.length)
			{	
				if(Names[id].equalsIgnoreCase(GameArena))
				{	
					plugin.SimonLog.logInfo("Found related arena " + Relateds[id] + " for arena " + GameArena);
					return Relateds[id];
				}
				
				id++;
			}	
		}
		
		return "none";
	}
	
	public void CFGAddArena(String location, String arenaname, String type, String relatedarena)
	{
		String ArenaNames = plugin.SimonArenasM.getArenaConfig().getString("ArenaNames");
		String ArenaLocs = plugin.SimonArenasM.getArenaConfig().getString("ArenaLocations");
		String ArenaTypes = plugin.SimonArenasM.getArenaConfig().getString("ArenaTypes");
		String RelatedArenas = plugin.SimonArenasM.getArenaConfig().getString("RelatedArena");
		
		if(ArenaNames != null)
		{
			plugin.SimonArenasM.getArenaConfig().set("ArenaNames", ArenaNames + " | " + arenaname);
			plugin.SimonArenasM.getArenaConfig().set("ArenaLocations", ArenaLocs + " | " + location);
			plugin.SimonArenasM.getArenaConfig().set("ArenaTypes", ArenaTypes + " | " + type);	
			plugin.SimonArenasM.getArenaConfig().set("RelatedArena", RelatedArenas + " | " + relatedarena);
		}
		else
		{
			plugin.SimonArenasM.getArenaConfig().set("ArenaNames", arenaname);
			plugin.SimonArenasM.getArenaConfig().set("ArenaLocations", location);
			plugin.SimonArenasM.getArenaConfig().set("ArenaTypes", type);	
			plugin.SimonArenasM.getArenaConfig().set("RelatedArena", relatedarena);	
		}
		
		plugin.SimonArenasM.saveArenaConfig();
		plugin.SimonArenasM.reloadArenaConfig();
		
		plugin.SimonLog.logInfo("Success! Added new arena:" + arenaname + " (config) ");
	}
	
	public void CFGRemoveArena(String GameArena, String location, String type, String related)
	{
		String ArenaNames = plugin.SimonArenasM.getArenaConfig().getString("ArenaNames");
		String ArenaLocs = plugin.SimonArenasM.getArenaConfig().getString("ArenaLocations");
		String RelatedArenas = plugin.SimonArenasM.getArenaConfig().getString("RelatedArena");
		
		if(ArenaNames == null || ArenaLocs == null ||  RelatedArenas == null)
		{
			plugin.SimonLog.logWarning("Attempted to delete an arena from an empty cfg! Canceled!");
		}
		else
		{	
			plugin.SimonArenasM.getArenaConfig().set("ArenaNames", CFGremoveFromList(ArenaNames, GameArena));
			plugin.SimonArenasM.getArenaConfig().set("ArenaLocations", CFGremoveFromList(ArenaLocs, location));
			plugin.SimonArenasM.getArenaConfig().set("RelatedArena", CFGremoveFromList(RelatedArenas, related));
			
			plugin.SimonLog.logInfo("Deleting process finished for arena: " + GameArena);
			
			plugin.SimonArenasM.saveArenaConfig();
			plugin.SimonArenasM.reloadArenaConfig();
			
			plugin.SimonArenasM.CFGLoadGameArenas();
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
}

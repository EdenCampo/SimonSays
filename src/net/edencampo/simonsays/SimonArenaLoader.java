package net.edencampo.simonsays;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

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
			} 
			catch (SQLException e) 
			{
				plugin.SimonLog.logSevereError(ChatColor.RED + "WARNING: Executing query: INSERT INTO SimonSays_Arenas (`ArenaName`, `ArenaLocation`, `ArenaType`, `RelatedArena`) VALUES ('" + arenaname + "', '" + location +"', '" + type + "', '" + relatedarena +"'); FAILED. Arena not added!");
				e.printStackTrace();
			}
			
			plugin.SimonLog.logInfo("Successfully executed AddArenaToSQL query!");
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
				res.last();
				
				String arenacount = res.getString("ArenaID");
				
				return arenacount+1;
			}
			catch (SQLException e) 
			{
				//e.printStackTrace();
				plugin.SimonLog.logSevereError(e.getMessage());
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
			return;
		}
		
		if(plugin.UsingMySQL() == true)
		{		
			try 
			{
				int id = 1;
				while(id < arenas)
				{
					if(id == 0)
					{
						id++;
						return;
					}
					
					Connection connection = plugin.sql.getConnection();
					
					Statement loadarenas = connection.createStatement();
					
					ResultSet res = loadarenas.executeQuery("SELECT ArenaType FROM SimonSays_Arenas WHERE ArenaID = '" + id + "';");
					res.next();
					
					String ArenaType = res.getString("ArenaType");
					
					ResultSet res1 = loadarenas.executeQuery("SELECT ArenaName FROM SimonSays_Arenas WHERE ArenaID = '" + id + "';");
					res1.next();
					
					String ArenaName = res1.getString("ArenaName");
					
					ResultSet res2 = loadarenas.executeQuery("SELECT ArenaLocation FROM SimonSays_Arenas WHERE ArenaID = '" + id + "';");
					res2.next();
			
					String ArenaLocation = res2.getString("ArenaLocation");
					
					if(ArenaType.equals("0"))
					{
						GameArena a = new GameArena(SimonGameArenaManager.getGameManager().deserializeLoc(ArenaLocation), ArenaName);
						SimonGameArenaManager.getGameManager().arenas.add(a);
						SimonGameArenaManager.getGameManager().getArena(ArenaName).spawn = SimonGameArenaManager.getGameManager().deserializeLoc(ArenaLocation);
						
						plugin.SimonLog.logInfo("Successfully loaded game arena:" + " " +  ArenaName + " at " + ArenaLocation + " " +  "type: GameArena");
					}
					else if(ArenaType.equals("1"))
					{
						SpectateArena a =  new SpectateArena(SimonSpectateArenaManager.getSpecManager().deserializeLoc(ArenaLocation), ArenaName);
						SimonSpectateArenaManager.getSpecManager().arenas.add(a);
						SimonSpectateArenaManager.getSpecManager().getArena(ArenaName).spawn = SimonGameArenaManager.getGameManager().deserializeLoc(ArenaLocation);
						
						plugin.SimonLog.logInfo("Successfully loaded spec arena:" + " " +  ArenaName + " at " + ArenaLocation + " " +  "type: SpectateArena");
					}
					
					id++;
				}
			} 
			catch (SQLException e) 
			{
				//e.printStackTrace();
				plugin.SimonLog.logSevereError(e.getMessage());
			}
			
			plugin.SimonLog.logInfo("Successfully executed loadSQLGameArenas query!");
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
				e.printStackTrace();
			}
		}
	}
	
	public void CFGLoadGameArenas()
	{
		String ArenaNames = plugin.SimonCFGM.getArenaConfig().getString("ArenaNames");
		String ArenaLocs = plugin.SimonCFGM.getArenaConfig().getString("ArenaLocations");
		String ArenaTypes = plugin.SimonCFGM.getArenaConfig().getString("ArenaTypes");

		if(ArenaNames == null)
		{
			plugin.SimonLog.logInfo("SavedArenas.yml seems empty.. skipping!");
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
			if(Types[id].equals("0"))
			{
				GameArena a = new GameArena(SimonGameArenaManager.getGameManager().deserializeLoc(Locations[id]), Names[id]);
				SimonGameArenaManager.getGameManager().arenas.add(a);
				SimonGameArenaManager.getGameManager().getArena(Names[id]).spawn = SimonGameArenaManager.getGameManager().deserializeLoc(Locations[id]);
				
				plugin.SimonLog.logInfo("Successfully loaded game arena:" + " " +  Names[id] + " at " + Locations[id] + " " +  "type: GameArena");
			}
			else if(Types[id].equals("1"))
			{
				SpectateArena a =  new SpectateArena(SimonSpectateArenaManager.getSpecManager().deserializeLoc(Locations[id]), Names[id]);
				SimonSpectateArenaManager.getSpecManager().arenas.add(a);
				SimonSpectateArenaManager.getSpecManager().getArena(Names[id]).spawn = SimonGameArenaManager.getGameManager().deserializeLoc(Locations[id]);
				
				plugin.SimonLog.logInfo("Successfully loaded spec arena:" + " " +  Names[id] + " at " + Locations[id] + " " +  "type: SpectateArena");
			}
			
			id++;
		}
	}
	
	@SuppressWarnings("unused")
	public String CFGGetRelatedArena(String GameArena)
	{
		String RelatedArenas = plugin.SimonCFGM.getArenaConfig().getString("RelatedArenas");
		
		String[] Relateds = RelatedArenas.split(" | ");
		
		return "none";
	}
	
	public void CFGAddArena(String location, String arenaname, String type)
	{
		String ArenaNames = plugin.SimonCFGM.getArenaConfig().getString("ArenaNames");
		String ArenaLocs = plugin.SimonCFGM.getArenaConfig().getString("ArenaLocations");
		String ArenaTypes = plugin.SimonCFGM.getArenaConfig().getString("ArenaTypes");
		
		if(ArenaNames != null)
		{
			plugin.SimonCFGM.getArenaConfig().set("ArenaNames", ArenaNames + " | " + arenaname);
			plugin.SimonCFGM.getArenaConfig().set("ArenaLocations", ArenaLocs + " | " + location);
			plugin.SimonCFGM.getArenaConfig().set("ArenaTypes", ArenaTypes + " | " + type);	
		}
		else
		{
			plugin.SimonCFGM.getArenaConfig().set("ArenaNames", arenaname);
			plugin.SimonCFGM.getArenaConfig().set("ArenaLocations", location);
			plugin.SimonCFGM.getArenaConfig().set("ArenaTypes", type);	
		}
		
		plugin.SimonCFGM.saveArenaConfig();
		plugin.SimonCFGM.reloadArenaConfig();
		
		plugin.SimonLog.logInfo("Successfully added cfg arena!");
	}
	
	public void CFGRemoveArena(String GameArena)
	{
		String ArenaNames = plugin.SimonCFGM.getArenaConfig().getString("ArenaNames");
		String ArenaLocs = plugin.SimonCFGM.getArenaConfig().getString("ArenaLocations");
		String ArenaTypes = plugin.SimonCFGM.getArenaConfig().getString("ArenaTypes");
		
		if(ArenaNames == null || ArenaLocs == null || ArenaTypes == null)
		{
			plugin.SimonLog.logWarning("Attempted to delete an arena from an empty cfg! Canceled!");
		}
		else
		{
			String[] Names = ArenaNames.split(" | ");
			String[] Locs = ArenaLocs.split(" | ");
			String[] Types = ArenaTypes.split(" | ");
			
			int id = 0;
			int correctid = 0;
			while(id < Names.length)
			{	
				plugin.SimonLog.logWarning("ID = " + id + " " + "CorrectID = " + correctid + " " + "Length = " + Names.length);
				
				if(!ArenaNames.contains(GameArena))
				{
					plugin.SimonLog.logWarning("Attempted to delete an non-existing arena! Canceled!");
					return;
				}
				
				if(ArenaNames.contains(GameArena))
				{
					correctid = id+1;
					
					plugin.SimonLog.logInfo("Deleting " + Names[correctid]);
					plugin.SimonLog.logInfo("Deleting " + Locs[correctid]);
					plugin.SimonLog.logInfo("Deleting " + Types[correctid]);
					
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
					plugin.SimonCFGM.getArenaConfig().set("ArenaNames", "");
					plugin.SimonCFGM.getArenaConfig().set("ArenaLocations", "");
					plugin.SimonCFGM.getArenaConfig().set("ArenaTypes", "");
					
					plugin.SimonLog.logInfo("Found only single arena(ID = " + id + ")");
					break;
				}
				
				plugin.SimonLog.logInfo("Looping until " + correctid + " Current ID = " + id);
				
				if(id == correctid)
				{
					plugin.SimonLog.logInfo("Skipping " + Names[id]);
					plugin.SimonLog.logInfo("Skipping " + Locs[id]);
					plugin.SimonLog.logInfo("Skipping " + Types[id]);
					
					id++;
					continue;
				}
				
				if(id == -1)
				{
					plugin.SimonLog.logInfo("Skipping loop for ID -1");
					id++;
					continue;
				}
				
				plugin.SimonCFGM.getArenaConfig().set("ArenaNames", Names[id]);
				plugin.SimonCFGM.getArenaConfig().set("ArenaLocations", Locs[id]);
				plugin.SimonCFGM.getArenaConfig().set("ArenaTypes", Types[id]);
				
				plugin.SimonLog.logInfo("Writing " + Names[id]);
				plugin.SimonLog.logInfo("Writing " + Locs[id]);
				plugin.SimonLog.logInfo("Writing " + Types[id]);
				
				id++;
			}
			
			plugin.SimonCFGM.saveArenaConfig();
			plugin.SimonCFGM.reloadArenaConfig();
			
			plugin.SimonCFGM.CFGLoadGameArenas();
		}
	}
}

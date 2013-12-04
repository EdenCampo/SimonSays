package net.edencampo.simonsays;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.edencampo.simonsays.ArenaManagers.SimonArena;
import net.edencampo.simonsays.ArenaManagers.SimonArena.ARENA_STAGE;
import net.edencampo.simonsays.ArenaManagers.SimonArenaManager;
import net.edencampo.simonsays.Dataloaders.SimonArenaLoader;
import net.edencampo.simonsays.Dataloaders.SimonSignsLoader;
import net.edencampo.simonsays.GameplayManagers.SimonCountdown;
import net.edencampo.simonsays.GameplayManagers.SimonGameChooser;
import net.edencampo.simonsays.GameplayManagers.SimonGameEvents;
import net.edencampo.simonsays.GameplayManagers.SimonGameManager;
import net.edencampo.simonsays.GameplayManagers.SimonGameStagesManager;
import net.edencampo.simonsays.GameplayManagers.SimonScoreManager;
import net.edencampo.simonsays.utils.Metrics;
import net.edencampo.simonsays.utils.MySQL;
import net.edencampo.simonsays.utils.Updater;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * @author Eden.Campo
 * C:\Users\чофе\workspace\SimonSays\bin\net\edencampo\simonsays
 */

public class SimonSays extends JavaPlugin implements Listener
{	
	/*
	 * TODO: Finish all debugging messages
	 * TODO: Points system
	 * TODO: More SimonGames...!
	 */
	
	
	public enum SimonGame
	{
		SGAME_NONE,
		SGAME_DONTMOVE,
		SGAME_SNEAK,
		SGAME_JUMP,
		SGAME_ATTACKPLAYER,
		SGAME_PUNCHBLOCK,
		SGAME_SPRINT,
		SGAME_WALK,
		SGAME_PLACEBLOCK,
		SGAME_FAKEDONTMOVE,
		SGAME_FAKESNEAK,
		SGAME_FAKEJUMP,
		SGAME_FAKEATTACKPLAYER,
		SGAME_FAKEPUNCHBLOCK,
		SGAME_FAKESPRINT,
		SGAME_FAKEWALK,
		SGAME_FAKEPLACEBLOCK
	}
	
	public SimonLogger SimonLog = new SimonLogger(this);
	public SimonGameChooser SimonSGC = new SimonGameChooser(this);
	public SimonGameManager SimonSGM = new SimonGameManager(this);
	public SimonArenaLoader SimonArenasM = new SimonArenaLoader(this);
	public SimonSignsLoader SimonSignsM = new SimonSignsLoader(this);
	public SimonGameStagesManager SimonGSM = new SimonGameStagesManager(this);
	public SimonCountdown SimonCD = new SimonCountdown(this);
	public SimonArenaManager SimonAM = new SimonArenaManager(this);
	public SimonGameEvents SimonGE = new SimonGameEvents(this);
	public SimonScoreManager SimonScore = new SimonScoreManager(this);
	
	// TODO: Make static
	public String SimonTag = ChatColor.BLACK + "[" + ChatColor.GREEN + "SimonSays" + ChatColor.BLACK + "]" + " " + ChatColor.WHITE;
	
	public MySQL sql;
	
	int SimonSGCTask;
	int SimonSGMTask;
	
	boolean usingLocalConfig;
	
	public void onEnable()
	{
		SimonLog.logDebug("Trying to activate SimonSays!");
		
		try 
		{
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		    
		    SimonLog.logDebug("Metrics activation success.");
		} 
		catch (IOException e) 
		{
			SimonLog.logInfo("SimonSays failed to start usage tracking :(");
		}
		
		String osname = System.getProperty("os.name");
		
		saveDefaultConfig();
		reloadConfig();
		
		if(IsFirstStartup())
		{
			SimonLog.logDebug("Found first startup! Wait, debug mode is on in first startup? Ahh you messed up with my config! (:");
			
			SimonLog.logInfo("Thank you for downloading SimonSays!");
			
			SimonLog.logInfo("Please read the installation and setup field at: http://dev.bukkit.org/bukkit-plugins/simon-says/");
			
			SimonLog.logInfo("As it's your first startup, SimonSays will not check for updates.");
			
		    SimonLog.logInfo("Note that SimonSays utilizes PluginMetrics for usage tracking.");
		    
		    SimonLog.logInfo("If you don't want usage tracking disable that in /plugins/PluginMetrics/config.yml");
		    
		    getConfig().set("firstStartup", "no");
		    
		    saveConfig();
		    reloadConfig();
		}
		
		if(isConfigUptoDate())
		{
			SimonLog.logInfo("Configuration file is up to date (:");
		}
		else
		{
			SimonLog.logInfo("Configuration file is outdated, updating...");
			
			SimonLog.logDebug("Gathering OLD config data...");
			
			String autoUpdate = getConfig().getString("autoUpdate");
			
			String useMySQL = getConfig().getString("useMySQL");
			
			String host = getConfig().getString("host");
			String port = getConfig().getString("port");
			String db = getConfig().getString("database");
			String user = getConfig().getString("user");
			String pw = getConfig().getString("password");
			
			File configFile = new File(getDataFolder(), "config.yml");
			configFile.delete();
			
			saveDefaultConfig();
			
			reloadConfig();
			
			SimonLog.logDebug("autoUpdate: " + autoUpdate);
			
			SimonLog.logDebug("useMySQL: " + useMySQL);
			
			SimonLog.logDebug("host: " + host);
			SimonLog.logDebug("port: " + port);
			SimonLog.logDebug("database: " + db);
			SimonLog.logDebug("user: " + user);
			SimonLog.logDebug("password: " + "******");
			
			getConfig().set("autoUpdate", autoUpdate);
			
			getConfig().set("useMySQL", useMySQL);
			
			getConfig().set("host", host);
			getConfig().set("port", port);
			getConfig().set("database", db);
			getConfig().set("user", user);
			getConfig().set("password", pw);
			
			getConfig().set("configVersion", getDescription().getVersion());
			
			getConfig().set("firstStartup", "no");
			
			saveConfig();
			reloadConfig();
			
			SimonLog.logInfo("Successfully updated config to version v" + getDescription().getVersion());
		}
		
		if(UsingMySQL() == true)
		{	
			SimonLog.logDebug("Connection attempt to MySQL started");
			
			SimonLog.logInfo("Detected MySQL usage, connecting..");
			
			usingLocalConfig = false;
			
			String sqlHost = this.getConfig().getString("host");
			String sqlPort = this.getConfig().getString("port");
			String sqlDb = this.getConfig().getString("database");
			String sqlUser = this.getConfig().getString("user");
			String sqlPw = this.getConfig().getString("password");
			
			SimonLog.logDebug("host: " + sqlHost);
			SimonLog.logDebug("port: " + sqlPort);
			SimonLog.logDebug("database: " + sqlDb);
			SimonLog.logDebug("user: " + sqlUser);
			SimonLog.logDebug("password: " + "******");
			
			SimonLog.logInfo("Attempting to connect MySQL (" + sqlHost + ") using database " + sqlDb);
			
			sql = new MySQL(this, sqlHost, sqlPort, sqlDb, sqlUser, sqlPw);		
			
			try 
			{
				SimonLog.logDebug("Opening connection to MySQL server.");
				
				sql.openConnection();
				
				SimonLog.logDebug("Connection opened!");
				
				Statement createtables = sql.getConnection().createStatement();
				createtables.executeUpdate("CREATE TABLE IF NOT EXISTS SimonSays_Arenas(ArenaName varchar(255) NOT NULL, ArenaLocation varchar(255) NOT NULL, ArenaType varchar(255) NOT NULL, RelatedArena varchar(255) NOT NULL, ArenaID int(255) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`ArenaID`))");
				createtables.executeUpdate("CREATE TABLE IF NOT EXISTS SimonSays_SignLinks(ArenaConnected varchar(255) NOT NULL, SignLocation varchar(255) NOT NULL, SignID int(255) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`SignID`));");
				SimonLog.logDebug("Executed: CREATE TABLE IF NOT EXISTS SimonSays_Arenas(ArenaName varchar(255) NOT NULL, ArenaLocation varchar(255) NOT NULL, ArenaType varchar(255) NOT NULL, RelatedArena varchar(255) NOT NULL, ArenaID int(255) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`ArenaID`))");
				SimonLog.logDebug("Executed: CREATE TABLE IF NOT EXISTS SimonSays_SignLinks(ArenaConnected varchar(255) NOT NULL, SignLocation varchar(255) NOT NULL, SignID int(255) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`SignID`));");
			} 
			catch (SQLException e) 
			{
				SimonLog.logSevereError(ChatColor.RED + "Failed to connect MySQL! Expect errors...");
			}
			
			SimonArenasM.SQLLoadGameArenas();
			SimonSignsM.SQLlinkSignsToArenas();
			
			SimonLog.logInfo("Loaded with MySQL support! System: (" + osname + ")");
		}
		else
		{
			SimonLog.logInfo("Using local-config, loading data...");
			usingLocalConfig = true;
			
			SimonArenasM.saveDefaultArenaConfig();
			SimonArenasM.saveArenaConfig();
			SimonArenasM.reloadArenaConfig();
			
			SimonSignsM.saveDefaultSignsConfig();
			SimonSignsM.saveSignsConfig();
			SimonSignsM.reloadSignsConfig();
			
			SimonArenasM.CFGLoadGameArenas();
			
			SimonSignsM.CFGlinkSignsToArenas();
			
			SimonLog.logInfo("Loaded with local-config support! System: (" + osname + ")");
		}
		
		if(!IsFirstStartup())
		{
			CheckUpdate();
			
			SimonLog.logDebug("Checked for updates.");
		}
		
		for(SimonArena a : SimonAM.simonArenas)
		{		
			a.arenaStage = ARENA_STAGE.SGAMESTAGE_WAITINGPLAYERS;
			
			SimonLog.logDebug("SimonArena " + a + " is now SGAMESTAGE_WAITINGPLAYERS");
		}
		
		Bukkit.getPluginManager().registerEvents(SimonGE, this);
		
		SimonSGCTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, SimonSGC, 0L, 75L);
		
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, SimonGSM, 0L, 20L);
	}
	
	public void onDisable()
	{		
		SimonLog.logInfo("Successfully unloaded!");
	}
	
	public boolean IsFirstStartup()
	{
		if(getConfig().getString("firstStartup").equalsIgnoreCase("yes") || getConfig().getString("firstStartup").equalsIgnoreCase("true"))
		{
			return true;
		}
		
		return false;
	}
	
	public boolean isConfigUptoDate()
	{
		PluginDescriptionFile pdFile = this.getDescription();
		
		if(!getConfig().getString("configVersion").equals(pdFile.getVersion()))
		{
			return false;
		}
		
		return true;
	}
	
	protected void CheckUpdate()
	{
		String update = this.getConfig().getString("autoUpdate");
		
		if(update.equalsIgnoreCase("true") || update.equalsIgnoreCase("yes"))
		{
			Updater updater = new Updater(this, 64788, this.getFile(), Updater.UpdateType.DEFAULT, true);
			
	        Updater.UpdateResult upresult = updater.getResult();
	        
	        switch(upresult)
	        {
	            case SUCCESS:
	            	SimonLog.logInfo("SimonSays will be updated on next reload!");
	                break;
	            case FAIL_DOWNLOAD:
	            	SimonLog.logInfo("Download Failed: The auto-updater found an update, but was unable to download SimonSays.");
	                break;
	            case FAIL_DBO:
	            	SimonLog.logInfo("dev.bukkit.org Failed: for some reason, the updater was unable to contact DBO to download the file.");
	        }
		}
		else
		{
			SimonLog.logDebug("Skipped update-checking...");
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args) 
	{
		if(sender instanceof Player)
		{
			Player player = (Player) sender;
			
			if(cmd.getName().equalsIgnoreCase("simonsays"))
			{
				if(args.length == 0)
				{
					showSimonHelp(player);
					return true;
				}
				
				if(args[0].equalsIgnoreCase("join"))
				{
					if(!player.hasPermission("SimonSays.command.join"))
					{
						player.sendMessage(SimonTag + ChatColor.RED + "Access denied");
						return true;
					}
					
					if(args.length != 2)
					{
						showSimonHelp(player);
						return true;
					}
					
					String arenaname = args[1];
		    		
					if(SimonAM.getArena(arenaname) == null)
					{
						player.sendMessage(SimonTag + ChatColor.RED + "Invalid Arena: " + arenaname);
						return true;
					}
					
		    		if(SimonAM.getArena(arenaname).needsPlayers())
		    		{
			    		SimonAM.addPlayer(player, arenaname);
			    		player.setGameMode(GameMode.SURVIVAL);	
		    		}
		    		else
		    		{
		    			player.sendMessage(SimonTag + "Woops! Game is already in progress!");
		    		}
		    		
					return true;
				}
				else if(args[0].equalsIgnoreCase("leave"))
				{
					if(!player.hasPermission("SimonSays.command.leave"))
					{
						player.sendMessage(SimonTag + ChatColor.RED + "Access denied");
						return true;
					}
					
					String GameArena = SimonAM.getArenaIn(player);
					
					if(GameArena.equals("none"))
					{
						player.sendMessage(SimonTag + "Woops! I can't find in what arena you are :(");
						return true;
					}
					
					String RelatedArena = "none";
					
					if(UsingMySQL() == true)
					{
						RelatedArena = SimonArenasM.SQLGetRelatedGameArena(GameArena);
					}
					else
					{
						RelatedArena = SimonArenasM.CFGGetRelatedArena(GameArena);
					}
					
		    		SimonAM.removePlayer(player);
		    		
		    		SimonAM.specPlayer(player, RelatedArena);
		    		
		    		player.sendMessage(SimonTag + "Successfully left " + GameArena + " !");
		    		
					return true;
				}
				
				
				else if(args[0].equalsIgnoreCase("arena"))
				{
					if(args[1].equalsIgnoreCase("creategame"))
					{
						if(!player.hasPermission("SimonSays.command.create"))
						{
							player.sendMessage(SimonTag + ChatColor.RED + "Access denied");
							return true;
						}
						
						if(args.length < 3)
						{
							showSimonHelp(player);
							return true;
						}
						
						String arenaname = args[2];
						String relatedarena = args[3];
						
				  		SimonAM.createArena(player.getLocation(), arenaname);
				  		
				  		if(UsingMySQL() == true)
				  		{
				  			SimonArenasM.AddArenaToSQL(SimonAM.serializeLoc(player.getLocation()), arenaname, "0", relatedarena);
				  			SimonArenasM.SQLLoadGameArenas();
				  		}
				  		else
				  		{
				  			SimonArenasM.CFGAddArena(SimonAM.serializeLoc(player.getLocation()), arenaname, "0", relatedarena);
				  			SimonArenasM.saveArenaConfig();
				  			SimonArenasM.reloadArenaConfig();
				  		}
				  		
			    		player.sendMessage(SimonTag + "Created " + arenaname + " at your location");
			    		
			    		SimonLog.logInfo(player.getName() + " Created game arena " + arenaname);
			    		
			    		SimonAM.getArena(arenaname).arenaStage = ARENA_STAGE.SGAMESTAGE_WAITINGPLAYERS;
			    		
						return true;
					}
					else if(args[1].equalsIgnoreCase("createspec"))
					{
						if(!player.hasPermission("SimonSays.command.create"))
						{
							player.sendMessage(SimonTag + ChatColor.RED + "Access denied");
							return true;
						}
						
						if(args.length < 2)
						{
							showSimonHelp(player);
							return true;
						}
						
						String arenaname = args[2];
						
				  		SimonAM.createArena(player.getLocation(), arenaname);
				  		
				  		if(UsingMySQL() == true)
				  		{
				  			SimonArenasM.AddArenaToSQL(SimonAM.serializeLoc(player.getLocation()), arenaname, "1", "none");
				  			SimonArenasM.SQLLoadGameArenas();
				  		}
				  		else
				  		{
				  			SimonArenasM.CFGAddArena(SimonAM.serializeLoc(player.getLocation()), arenaname, "1", "none");
				  			SimonArenasM.CFGLoadGameArenas();
				  		}
				  		
			    		player.sendMessage(SimonTag + "Created spectator arena " + arenaname + " ar your location");
			    		SimonLog.logInfo(player.getName() + " Created spectator arena " + arenaname);
			    		
						return true;
					}
					
					else if(args[1].equalsIgnoreCase("delete"))
					{
						if(!player.hasPermission("SimonSays.command.delete"))
						{
							player.sendMessage(SimonTag + ChatColor.RED + "Access denied");
							return true;
						}
						
						
						if(args.length < 2)
						{
							showSimonHelp(player);
							return true;
						}
						
						String arenaname = args[2];
						
						if(UsingMySQL() == true)
						{
							SimonArenasM.SQLRemoveArena(arenaname);
							player.sendMessage(SimonTag + "Successfully removed arena: " + arenaname);
						}
						else
						{
							if(arenaname.equalsIgnoreCase("clearall") || arenaname.equalsIgnoreCase("all") || arenaname.equalsIgnoreCase("deleteall"))
							{
								SimonArenasM.getArenaConfig().set("ArenaNames", "");
								SimonArenasM.getArenaConfig().set("ArenaLocations", "");
								SimonArenasM.getArenaConfig().set("ArenaTypes", "");
							
								SimonArenasM.saveArenaConfig();
								SimonArenasM.reloadArenaConfig();
								
								SimonArenasM.CFGLoadGameArenas();
								
								SimonLog.logInfo(player.getName() + " cleared all arenas!");
								
								player.sendMessage(SimonTag + "Successfully removed all arenas!");
							}
							else
							{		
								String location;
								
								String type;
								
								String RelatedArena = "none";
								
								if(SimonAM.getArena(arenaname) == null)
								{
									type = "1";
									
									location = SimonAM.serializeLoc(SimonAM.getArena(arenaname).arenaSpawn);
								}
								else
								{
									type = "0";
									
									RelatedArena = SimonArenasM.CFGGetRelatedArena(arenaname);
									
									location = SimonAM.serializeLoc(SimonAM.getArena(arenaname).arenaSpawn);
								}
								
								
								SimonArenasM.CFGRemoveArena(arenaname, location, type, RelatedArena);
								
								player.sendMessage(SimonTag + "Successfully removed arena: " + arenaname);
							}
						}
						
						return true;
					}
					
					else if(args[1].equalsIgnoreCase("list"))
					{
						if(UsingMySQL() == true)
						{
							String arenacount = SimonArenasM.getSQLGameArenasCount();
							
							int arenas = Integer.parseInt(arenacount);
							
							if(arenas == 0)
							{
								player.sendMessage(SimonTag + "Can't find any arenas to display...");
								return true;
							}
							
							player.sendMessage(SimonTag + "--- Listing current arenas ---");
							
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
										
									Connection connection = sql.getConnection();
									
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
									
									if(ArenaName.equals("|"))
									{
										id++;
										continue;
									}
									
									if(ArenaType.equals("0"))
									{
										player.sendMessage(SimonTag + "SimonArena (" + ArenaName + ") located at " + ArenaLocation + " type: GameArena");	
									}
									else
									{
										player.sendMessage(SimonTag + "SimonArena (" + ArenaName + ") located at " + ArenaLocation + " type: SpectatorArena");	
									}
									
									id++;
								}
							} 
							catch (SQLException e) 
							{
								SimonLog.logSevereError(e.getMessage());
							}
						}
						else
						{
							String ArenaNames = SimonArenasM.getArenaConfig().getString("ArenaNames");
							String ArenaLocs = SimonArenasM.getArenaConfig().getString("ArenaLocations");
							String ArenaTypes = SimonArenasM.getArenaConfig().getString("ArenaTypes");

							if(ArenaNames == null)
							{
								player.sendMessage(SimonTag + "Can't find any arenas to display...");
								return true;
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
									if(Names[id].equals("DELETED") || Names[id].equals("|"))
									{
										id++;
										continue;
									}
									
									if(Types[id].equals("0"))
									{
										player.sendMessage(SimonTag + "SimonArena [" + Names[id] + "] located at " + Locations[id] + " type: GameArena");	
									}
									else
									{
										player.sendMessage(SimonTag + "SimonArena [" + Names[id] + "] located at " + Locations[id] + " type: SpectatorArena");	
									}

								}
							    catch (IndexOutOfBoundsException ex )
							    {
							    	break;
							    }
								
								id++;
							}
						}
						
						return true;
					}
					
					return true;
				}
				else
				{
					showSimonHelp(player);
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	public int getSimonSGCTask()
	{
		return SimonSGCTask;
	}
	
	public int getSimonSGMTask()
	{
		return SimonSGCTask;
	}
	
	public boolean UsingMySQL()
	{
		String useMySQL = this.getConfig().getString("useMySQL");
		
		if(useMySQL.equalsIgnoreCase("true") || useMySQL.equalsIgnoreCase("yes"))
		{
			return true;
		}
		
		return false;
	}
	
	public void showSimonHelp(Player player)
	{
		player.sendMessage(SimonTag + "------ SimonSays v" + ChatColor.YELLOW + getDescription().getVersion() + ChatColor.WHITE + " ------");
		player.sendMessage(SimonTag + "Listing " + getDescription().getCommands().size() + " commands:");
		player.sendMessage(SimonTag + "/simonsays - SimonSays's help command.");
		player.sendMessage(SimonTag + "/simonsays arena creategame <ArenaName> <RelatedArena> - Creates a game arena linked with <RelatedArena> at your location.");
		player.sendMessage(SimonTag + "/simonsays arena createspec <ArenaName> - Creates a spectator arena linked with <RelatedArena> at your location.");
		player.sendMessage(SimonTag + "/simonsays arena delete <ArenaName> - Deletes <ArenaName>.");
		player.sendMessage(SimonTag + "/simonsays arena list - Lists all existing arenas.");
		player.sendMessage(SimonTag + "/simonsays join <ArenaName> - Joins <ArenaName>");
		player.sendMessage(SimonTag + "/simonsays leave - Leaves your current arena.");
	}
}
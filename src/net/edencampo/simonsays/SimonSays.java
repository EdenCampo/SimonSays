package net.edencampo.simonsays;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import net.edencampo.simonsays.ArenaManagers.GameArena;
import net.edencampo.simonsays.ArenaManagers.SimonGameArenaManager;
import net.edencampo.simonsays.ArenaManagers.SimonSpectateArenaManager;
import net.edencampo.simonsays.Dataloaders.SimonArenaLoader;
import net.edencampo.simonsays.Dataloaders.SimonSignsLoader;
import net.edencampo.simonsays.GameplayManagers.SimonCountdown;
import net.edencampo.simonsays.GameplayManagers.SimonGameChooser;
import net.edencampo.simonsays.GameplayManagers.SimonGameManager;
import net.edencampo.simonsays.GameplayManagers.SimonGameStagesManager;
import net.edencampo.simonsays.utils.Metrics;
import net.edencampo.simonsays.utils.MySQL;
import net.edencampo.simonsays.utils.Updater;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * @author Eden.Campo
 * C:\Users\чофе\workspace\SimonSays\bin\net\edencampo\simonsays
 */

public class SimonSays extends JavaPlugin implements Listener
{	
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
	public SimonArenaLoader SimonCFGM = new SimonArenaLoader(this);
	public SimonSignsLoader SimonSignsM = new SimonSignsLoader(this);
	public SimonGameStagesManager SimonGSM = new SimonGameStagesManager(this);
	public SimonCountdown SimonCD = new SimonCountdown(this);
	public SimonGameArenaManager SimonAM = new SimonGameArenaManager(this);
	
	public String SimonTag = ChatColor.BLACK + "[" + ChatColor.GREEN + "SimonSays" + ChatColor.BLACK + "]" + " " + ChatColor.WHITE;
	
	public MySQL sql;
	
	int SimonSGCTask;
	int SimonSGMTask;
	
	boolean usingLocalConfig;
	
	public void onEnable()
	{		
		saveDefaultConfig();
		reloadConfig();
		
		if(IsFirstStartup())
		{
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
			SimonLog.logInfo("Configuration file is not up to date, updated config!");
			
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
			
			getConfig().set("autoUpdate", autoUpdate);
			
			getConfig().set("useMySQL", useMySQL);
			
			getConfig().set("host", host);
			getConfig().set("port", port);
			getConfig().set("database", db);
			getConfig().set("user", user);
			getConfig().set("password", pw);
			
			getConfig().set("firstStartup", "no");
			
			saveConfig();
			reloadConfig();
			
			SimonLog.logInfo("Successfully updated config to version v" + getDescription().getVersion());
		}
		
		if(UsingMySQL() == true)
		{	
			SimonLog.logInfo("Detected MySQL usage, connecting..");
			
			usingLocalConfig = false;
			
			String sqlHost = this.getConfig().getString("host");
			String sqlPort = this.getConfig().getString("port");
			String sqlDb = this.getConfig().getString("database");
			String sqlUser = this.getConfig().getString("user");
			String sqlPw = this.getConfig().getString("password");
			
			SimonLog.logInfo("Attempting to connect MySQL (" + sqlHost + ") using database " + sqlDb);
			
			sql = new MySQL(this, sqlHost, sqlPort, sqlDb, sqlUser, sqlPw);
			
			sql.openConnection();
			
			try 
			{
				Statement createtables = sql.getConnection().createStatement();
				createtables.executeUpdate("CREATE TABLE IF NOT EXISTS SimonSays_Arenas(ArenaName varchar(255) NOT NULL, ArenaLocation varchar(255) NOT NULL, ArenaType varchar(255) NOT NULL, RelatedArena varchar(255) NOT NULL, ArenaID int(255) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`ArenaID`))");
				createtables.executeUpdate("CREATE TABLE IF NOT EXISTS SimonSays_SignLinks(ArenaConnected varchar(255) NOT NULL, SignLocation varchar(255) NOT NULL, SignID int(255) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`SignID`));");
				SimonLog.logInfo("Successfully executed onEnable() queries!");
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
				SimonLog.logSevereError(ChatColor.RED + "WARNING: Arenas will not be saved!");
			}
			
			SimonCFGM.SQLLoadGameArenas();
			SimonSignsM.SQLlinkSignsToArenas();
		}
		else
		{
			SimonLog.logInfo("Using local-config, loading data...");
			usingLocalConfig = true;
			
			SimonCFGM.saveDefaultArenaConfig();
			SimonCFGM.saveArenaConfig();
			SimonCFGM.reloadArenaConfig();
			
			SimonSignsM.saveDefaultSignsConfig();
			SimonSignsM.saveSignsConfig();
			SimonSignsM.reloadSignsConfig();
			
			SimonCFGM.CFGLoadGameArenas();
			SimonSignsM.CFGlinkSignsToArenas();
			
		}
		
		if(!IsFirstStartup())
		{
			CheckUpdate();
		}
		
		for(GameArena a : SimonAM.arenas)
		{		
			SimonGSM.arenagamestage.put(a, "SGAMESTAGE_WAITINGPLAYERS");
		}
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		SimonSGCTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, SimonSGC, 0L, 75L);
		
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, SimonGSM, 0L, 20L);
		
		try 
		{
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		    SimonLog.logInfo("Metrics Started");
		} 
		catch (IOException e) 
		{
			SimonLog.logInfo("SimonSays failed to start usage tracking :(");
		}
		
		String osname = System.getProperty("os.name");
		
		SimonLog.logInfo("Successfully loaded! System: (" + osname + ")");
	}
	
	public void onDisable()
	{
		SimonGSM.arenagamestage.clear();
		
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
			Updater updater = new Updater(this, "simon-says", this.getFile(), Updater.UpdateType.DEFAULT, true);
			
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
			SimonLog.logInfo("Skipped update-checking...");
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args) 
	{
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			
			if(cmd.getName().equalsIgnoreCase("simonhelp") || CommandLabel.equalsIgnoreCase("sh"))
			{
				player.sendMessage(SimonTag + "------------------- SimonSays v" + getDescription().getVersion() + "----------------");
				player.sendMessage(SimonTag + "Listing " + getDescription().getCommands().size() + "commands:");
				player.sendMessage(SimonTag + "TODO: Add all the commands here");
				return true;
			}
			else if(cmd.getName().equalsIgnoreCase("creategamearena"))
			{
				if(!player.hasPermission("SimonSays.command.create"))
				{
					player.sendMessage(SimonTag + ChatColor.RED + "Access denied");
					return true;
				}
				
				if(args.length != 2)
				{
					player.sendMessage(SimonTag + "Usage: /creategamearena <ArenaName> <RelatedArena>");
					return true;
				}
				
				String arenaname = args[0];
				String relatedarena = args[1];
				
		  		SimonAM.createArena(player.getLocation(), arenaname);
		  		
		  		if(UsingMySQL() == true)
		  		{
		  			SimonCFGM.AddArenaToSQL(SimonAM.serializeLoc(player.getLocation()), arenaname, "0", relatedarena);
		  			SimonCFGM.SQLLoadGameArenas();
		  		}
		  		else
		  		{
		  			SimonCFGM.CFGAddArena(SimonAM.serializeLoc(player.getLocation()), arenaname, "0", relatedarena);
		  			SimonCFGM.saveArenaConfig();
		  			SimonCFGM.reloadArenaConfig();
		  		}
		  		
	    		player.sendMessage(SimonTag + "Created " + arenaname + " at your location");
	    		
	    		SimonLog.logInfo(player.getName() + " Created game arena " + arenaname);
	    		
	    		this.SimonGSM.arenagamestage.put(SimonAM.getArena(arenaname), "SGAMESTAGE_WAITINGPLAYERS");
				return true;
			}
			else if(cmd.getName().equalsIgnoreCase("createspecarena"))
			{	
				if(!player.hasPermission("SimonSays.command.create"))
				{
					player.sendMessage(SimonTag + ChatColor.RED + "Access denied");
					return true;
				}
				
				if(args.length != 1)
				{
					player.sendMessage(SimonTag + "Usage: /createspecarena <ArenaName>");
					return true;
				}
				
				String arenaname = args[0];
				
		  		SimonSpectateArenaManager.getSpecManager().createArena(player.getLocation(), arenaname);
		  		
		  		if(UsingMySQL() == true)
		  		{
		  			SimonCFGM.AddArenaToSQL(SimonAM.serializeLoc(player.getLocation()), arenaname, "1", "none");
		  			SimonCFGM.SQLLoadGameArenas();
		  		}
		  		else
		  		{
		  			SimonCFGM.CFGAddArena(SimonAM.serializeLoc(player.getLocation()), arenaname, "1", "none");
		  			SimonCFGM.CFGLoadGameArenas();
		  		}
		  		
	    		player.sendMessage(SimonTag + "Created spectator arena " + arenaname + " ar your location");
	    		SimonLog.logInfo(player.getName() + " Created spectator arena " + arenaname);
				return true;
			}
			else if(cmd.getName().equalsIgnoreCase("deletearena"))
			{
				if(!player.hasPermission("SimonSays.command.delete"))
				{
					player.sendMessage(SimonTag + ChatColor.RED + "Access denied");
					return true;
				}
				
				
				if(args.length != 1)
				{
					player.sendMessage(SimonTag + "Usage: /deletearena <ArenaName>");
					return true;
				}
				
				String arenaname = args[0];
				
				if(UsingMySQL() == true)
				{
					SimonCFGM.SQLRemoveArena(arenaname);
					player.sendMessage(SimonTag + "Successfully removed arena: " + arenaname);
				}
				else
				{
					if(arenaname.equalsIgnoreCase("clearall") || arenaname.equalsIgnoreCase("all") || arenaname.equalsIgnoreCase("deleteall"))
					{
						SimonCFGM.getArenaConfig().set("ArenaNames", "");
						SimonCFGM.getArenaConfig().set("ArenaLocations", "");
						SimonCFGM.getArenaConfig().set("ArenaTypes", "");
					
						SimonCFGM.saveArenaConfig();
						SimonCFGM.reloadArenaConfig();
						
						SimonCFGM.CFGLoadGameArenas();
						
						SimonLog.logInfo(player.getName() + " cleared all arenas!");
						
						player.sendMessage(SimonTag + "Successfully removed all arenas!");
					}
					else
					{
						SimonCFGM.CFGRemoveArena(arenaname);
						player.sendMessage(SimonTag + "Successfully removed arena: " + arenaname);
					}
				}
				
				return true;
			}
			else if(cmd.getName().equalsIgnoreCase("simonjoin") || CommandLabel.equalsIgnoreCase("sj"))
			{
				if(!player.hasPermission("SimonSays.join"))
				{
					player.sendMessage(SimonTag + ChatColor.RED + "Access denied");
					return true;
				}
				
				if(args.length != 1)
				{
					player.sendMessage(SimonTag + "Usage: /simonjoin <ArenaName>");
					return true;
				}
				
				String arenaname = args[0];
	    		
	    		if(SimonAM.getArena(arenaname).needsPlayers())
	    		{
		    		SimonAM.addPlayer(player, arenaname);
		    		player.setGameMode(GameMode.SURVIVAL);	
	    		}
	    		else
	    		{
	    			player.sendMessage(SimonTag + "Woops! Game is already in progress! (or arena is invalid)");
	    		}
	    		
				return true;
			}
			
			else if(CommandLabel.equalsIgnoreCase("simonleave") || CommandLabel.equalsIgnoreCase("sl"))
			{
				if(!player.hasPermission("SimonSays.leave"))
				{
					player.sendMessage(SimonTag + ChatColor.RED + "Access denied");
					return true;
				}
				
				String GameArena = SimonAM.getArenaIn(player);
				
				String RelatedArena = "none";
				
				if(UsingMySQL() == true)
				{
					RelatedArena = SimonCFGM.SQLGetRelatedGameArena(GameArena);
				}
				else
				{
					RelatedArena = SimonCFGM.CFGGetRelatedArena(GameArena);
				}
				
				
	    		SimonAM.removePlayer(player);
	    		SimonSpectateArenaManager.getSpecManager().specPlayer(player, RelatedArena);
	    		player.sendMessage(SimonTag + "Successfully left the SimonArena!");
				return true;
			}
		}
		
		return false;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e)
	{
		SimonGame SimonGameType = this.SimonSGC.GetGame();
		
		Player p = e.getPlayer();
		
		String GameArena = SimonAM.getArenaIn(p);
		
		if(SimonAM.IsPlaying(p))
		{
			Location to = e.getTo();
			Location from = e.getFrom();
			
			switch(SimonGameType)
			{
				case SGAME_DONTMOVE:
				{
					if(to == from)
					{
						return;
					}
					
					String RelatedArena = "";
					
					if(UsingMySQL() == true)
					{
						RelatedArena = SimonCFGM.SQLGetRelatedGameArena(GameArena);
					}
					else
					{
						RelatedArena = SimonCFGM.CFGGetRelatedArena(GameArena);
					}
					
					p.sendMessage(SimonTag + "Ohhh, you moved! Abandoned Game!");
					SimonAM.removePlayer(p);
					SimonSpectateArenaManager.getSpecManager().specPlayer(p, RelatedArena);
					break;
				}
				
				case SGAME_WALK:
				{
					if(to != from)
					{
						SimonSGM.SimonActionSetDone(p);
						
						if(!SimonSGM.SimonMsgSent(p))
						{
							p.sendMessage(SimonTag + "Great job! Lets Continue!");
							SimonSGM.SimonSetMsgSent(p);
						}
					}
					break;
				}
				
				case SGAME_FAKEWALK:
				{
					String RelatedArena = "";
					
					if(UsingMySQL() == true)
					{
						RelatedArena = SimonCFGM.SQLGetRelatedGameArena(GameArena);
					}
					else
					{
						RelatedArena = SimonCFGM.CFGGetRelatedArena(GameArena);
					}
					
					p.sendMessage(SimonTag + "Ohhh, you walked! Abandoned Game!");
					SimonAM.removePlayer(p);
					SimonSpectateArenaManager.getSpecManager().specPlayer(p, RelatedArena);
					break;
				}
				
				case SGAME_JUMP:
				{
					if(to.getY() > from.getY())
					{
						Block block = e.getPlayer().getWorld().getBlockAt(new Location(e.getPlayer().getWorld(), e.getTo().getX(), e.getTo().getY()+2, e.getTo().getZ()));
						
						if(block.getType() == Material.AIR)
						{
							SimonSGM.SimonActionSetDone(p);
							
							if(!SimonSGM.SimonMsgSent(p))
							{
								p.sendMessage(SimonTag + "Great job! Lets Continue!");
								SimonSGM.SimonSetMsgSent(p);
							}
						}
					}
					
					break;
				}
				
				case SGAME_FAKEJUMP:
				{
					if(e.getTo().getY() > e.getFrom().getY())
					{
						Block block = e.getPlayer().getWorld().getBlockAt(new Location(e.getPlayer().getWorld(), e.getTo().getX(), e.getTo().getY()+2, e.getTo().getZ()));
						
						if(block.getType() == Material.AIR)
						{
							String RelatedArena = "";
							
							if(UsingMySQL() == true)
							{
								RelatedArena = SimonCFGM.SQLGetRelatedGameArena(GameArena);
							}
							else
							{
								RelatedArena = SimonCFGM.CFGGetRelatedArena(GameArena);
							}
							
							p.sendMessage(SimonTag + "Nope.. I didn't want you to jump! Abandoned Game!");
							SimonAM.removePlayer(p);
							SimonSpectateArenaManager.getSpecManager().specPlayer(p, RelatedArena);
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerSneak(PlayerToggleSneakEvent e)
	{
		SimonGame SimonGameType = this.SimonSGC.GetGame();
		
		Player p = e.getPlayer();
		
		String GameArena = SimonAM.getArenaIn(p);
		
		if(SimonAM.IsPlaying(p))
		{
			switch(SimonGameType)
			{
				case SGAME_SNEAK:
				{
					SimonSGM.SimonActionSetDone(p);
					
					if(!SimonSGM.SimonMsgSent(p))
					{
						p.sendMessage(SimonTag + "Great job! Lets Continue!");
						SimonSGM.SimonSetMsgSent(p);
					}
					
					
					break;
				}
				
				case SGAME_FAKESNEAK:
				{
					String RelatedArena = "";
					
					if(UsingMySQL() == true)
					{
						RelatedArena = SimonCFGM.SQLGetRelatedGameArena(GameArena);
					}
					else
					{
						RelatedArena = SimonCFGM.CFGGetRelatedArena(GameArena);
					}
					
					p.sendMessage(SimonTag + "Woops! You've mistaken! Abandoned Game!");
					SimonAM.removePlayer(p);
					SimonSpectateArenaManager.getSpecManager().specPlayer(p, RelatedArena);
				}
			
			}
		}
	}
	
	@EventHandler
	public void onPlayerSprint(PlayerToggleSprintEvent e)
	{
		SimonGame SimonGameType = this.SimonSGC.GetGame();
		
		Player p = e.getPlayer();
		Boolean Sprinting = e.isSprinting();
		
		String GameArena = SimonAM.getArenaIn(p);
		
		if(SimonAM.IsPlaying(p))
		{
			switch(SimonGameType)
			{
				case SGAME_SPRINT:
				{
					if(Sprinting == true)
					{
						SimonSGM.SimonActionSetDone(p);
						
						if(!SimonSGM.SimonMsgSent(p))
						{
							p.sendMessage(SimonTag + "Great job! Lets Continue!");
							SimonSGM.SimonSetMsgSent(p);
						}
					}
					break;
				}
				
				case SGAME_FAKESPRINT:
				{
					if(Sprinting == true)
					{		
						String RelatedArena = "";
						
						if(UsingMySQL() == true)
						{
							RelatedArena = SimonCFGM.SQLGetRelatedGameArena(GameArena);
						}
						else
						{
							RelatedArena = SimonCFGM.CFGGetRelatedArena(GameArena);
						}
						
						p.sendMessage(SimonTag + "Nope. Abandoned Game!");
						SimonAM.removePlayer(p);
						SimonSpectateArenaManager.getSpecManager().specPlayer(p, RelatedArena);
					}
				}
			
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e)
	{
		SimonGame SimonGameType = this.SimonSGC.GetGame();
		
		Player p = e.getPlayer();
		
		Block block = e.getClickedBlock();
		
		if(block != null)
		{
			BlockState state = block.getState();
			
			if(state instanceof Sign)
			{
				if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && block.getType() == Material.SIGN || e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && block.getType() == Material.SIGN_POST)
				{
					Sign sign = (Sign) state;
					
					String[] SignLine = sign.getLines();
					
					if(SignLine[0].contains("[SimonSays]"))
					{
						if(!SignLine[1].isEmpty())
						{	
							if(SimonAM.getArena(SignLine[1]) != null)
							{
								if(!p.hasPermission("SimonSays.sign.use"))
								{
									p.sendMessage(SimonTag + ChatColor.RED + "Access denied");
									return;
								}
								
					    		if(SimonAM.getArena(SignLine[1]).needsPlayers())
					    		{
						    		SimonAM.addPlayer(p, SignLine[1]);
						    		p.setGameMode(GameMode.SURVIVAL);	
					    		}
					    		else
					    		{
					    			p.sendMessage(SimonTag + "Woops! Game is already in progress! (or arena is invalid)");
					    		}
							}
							else
							{
								p.sendMessage(SimonTag + "Arena join attempt denied. Invalid Arena!");
							}
						}
					}
				}
			}
			
			String GameArena = SimonAM.getArenaIn(p);
			
			if(SimonAM.IsPlaying(p))
			{
				switch(SimonGameType)
				{
					case SGAME_PUNCHBLOCK:
					{
							
						SimonSGM.SimonActionSetDone(p);
						if(!SimonSGM.SimonMsgSent(p))
						{
							p.sendMessage(SimonTag + "Great job! Lets Continue!");
							SimonSGM.SimonSetMsgSent(p);
						}
							
						break;
					}
						
					case SGAME_FAKEPUNCHBLOCK:
					{
						String RelatedArena = "";
						
						if(UsingMySQL() == true)
						{
							RelatedArena = SimonCFGM.SQLGetRelatedGameArena(GameArena);
						}
						else
						{
							RelatedArena = SimonCFGM.CFGGetRelatedArena(GameArena);
						}
						
						p.sendMessage(SimonTag + "I confused you ah? Abandoned Game!");
						SimonAM.removePlayer(p);
						SimonSpectateArenaManager.getSpecManager().specPlayer(p, RelatedArena);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onHitPlayer(EntityDamageByEntityEvent e)
	{
		SimonGame SimonGameType = this.SimonSGC.GetGame();
		
		Entity entattacker = e.getDamager();
		
		if(entattacker instanceof Player)
		{
			Player p = (Player)entattacker;

			String GameArena = SimonAM.getArenaIn(p);
			
			if(SimonAM.IsPlaying(p))
			{
				switch(SimonGameType)
				{
					case SGAME_ATTACKPLAYER:
					{
						SimonSGM.SimonActionSetDone(p);
						
						if(!SimonSGM.SimonMsgSent(p))
						{
							p.sendMessage(SimonTag + "Ouch, Lets Continue!");
							SimonSGM.SimonSetMsgSent(p);
						}
						
						break;
					}
					
					case SGAME_FAKEATTACKPLAYER:
					{
						String RelatedArena = "";
						
						if(UsingMySQL() == true)
						{
							RelatedArena = SimonCFGM.SQLGetRelatedGameArena(GameArena);
						}
						else
						{
							RelatedArena = SimonCFGM.CFGGetRelatedArena(GameArena);
						}
						
						p.sendMessage(SimonTag + "Wrong move, mate. Abandoned Game!");
						SimonAM.removePlayer(p);
						SimonSpectateArenaManager.getSpecManager().specPlayer(p, RelatedArena);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onDamaged(EntityDamageEvent e)
	{
		Entity ent = e.getEntity();
		
		if(ent instanceof Player)
		{
			Player p = (Player)ent;
			
			if(SimonAM.IsPlaying(p))
			{
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onSignChanged(SignChangeEvent e)
	{
		Sign arenasign = (Sign) e.getBlock().getState();
		String[] SignLine = e.getLines();
		
		if(SignLine[0].contains("[SimonSays]"))
		{
			if(!SignLine[1].isEmpty())
			{
				if(!e.getPlayer().hasPermission("SimonSays.sign.create"))
				{
					e.getPlayer().sendMessage(SimonTag + ChatColor.RED + "Access denied");
					e.getBlock().breakNaturally();
					return;
				}
				
				e.setLine(0, ChatColor.GREEN + "[SimonSays]");
				
				if(SimonAM.getArena(SignLine[1]) == null)
				{
					e.setLine(2, ChatColor.DARK_AQUA + "Invalid Arena");
					e.setLine(3, ChatColor.DARK_AQUA + "ERROR...");
					return;
				}
				
				if(UsingMySQL() == true)
				{
					SimonSignsM.SQLaddSignArena(SignLine[1], SimonAM.serializeLoc(e.getBlock().getLocation()));
				}
				else
				{
					SimonSignsM.CFGaddSignArena(SignLine[1], SimonAM.serializeLoc(e.getBlock().getLocation()));
				}
				
				SimonAM.getArena(SignLine[1]).setSign(arenasign);
			}
		}
	}
	
	@EventHandler
	public void onBlockPlaced(BlockPlaceEvent e)
	{
		SimonGame SimonGameType = this.SimonSGC.GetGame();
		
		Material block = e.getBlock().getType();
		
		Player p = e.getPlayer();
		
		String GameArena = SimonAM.getArenaIn(p);
		
		if(SimonAM.IsPlaying(p))
		{
			switch(SimonGameType)
			{
				case SGAME_PLACEBLOCK:
				{	
					Material correctblock = SimonSGM.getBlockToPlace(p);
					
					if(correctblock.equals(block))
					{
						SimonSGM.SimonActionSetDone(p);
						
						if(!SimonSGM.SimonMsgSent(p))
						{
							p.sendMessage(SimonTag + "Good job! Lets Continue!");
							SimonSGM.SimonSetMsgSent(p);
							e.setCancelled(true);
						}
					}
					
					break;
				}
				
				case SGAME_FAKEPLACEBLOCK:
				{
					String RelatedArena = "";
					
					if(UsingMySQL() == true)
					{
						RelatedArena = SimonCFGM.SQLGetRelatedGameArena(GameArena);
					}
					else
					{
						RelatedArena = SimonCFGM.CFGGetRelatedArena(GameArena);
					}
					
					p.sendMessage(SimonTag + "You've mistaken! Abandoned Game!");
					SimonAM.removePlayer(p);
					SimonSpectateArenaManager.getSpecManager().specPlayer(p, RelatedArena);
					
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onSignBreak(BlockBreakEvent e)
	{
		if(e.getBlock().getState() instanceof Sign)
		{
			Sign arenasign = (Sign) e.getBlock().getState();
			
			String[] SignLines = arenasign.getLines();
			
			String SimonTag = SignLines[0];
			String arenaname = SignLines[1];
			
			if(SimonTag.contains("[SimonSays]"))
			{
				if(!e.getPlayer().hasPermission("SimonSays.sign.destory"))
				{
					e.getPlayer().sendMessage(SimonTag + ChatColor.RED + "Access denied");
					e.setCancelled(true);
					return;
				}
				
				if(UsingMySQL() == true)
				{
					if(SimonAM.getArena(arenaname) != null)
					{
						SimonSignsM.SQLremoveSignArena(arenaname);
						SimonSignsM.SQLlinkSignsToArenas();
						arenasign.update(false);
						
						SimonAM.getArena(arenaname).setSign(null);
					}
				}
				else
				{
					if(SimonAM.getArena(arenaname) != null)
					{
						SimonSignsM.CFGremoveSignArena(arenaname);
						SimonSignsM.CFGlinkSignsToArenas();
						arenasign.update(false);
						
						SimonAM.getArena(arenaname).setSign(null);
					}
				}
			}
		}
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
}

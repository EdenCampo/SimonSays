package net.edencampo.simonsays;

import java.sql.SQLException;
import java.sql.Statement;

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
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * @author Eden.Campo
 * C:\Users\чофе\workspace\SimonSays\bin\net\edencampo\simonsays
 */

public class SimonSays extends JavaPlugin implements Listener
{
	/*
	 * TODO: ADD: /simonhelp
	 * TODO: ADD: MySQL Support + Config
	 * TODO: ADD: Permissions
	 * TODO: FIX: Config Errors
	 * TODO: FINISH-POST: BukkitDev.org post
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
		SGAME_FAKEDONTMOVE,
		SGAME_FAKESNEAK,
		SGAME_FAKEJUMP,
		SGAME_FAKEATTACKPLAYER,
		SGAME_FAKEPUNCHBLOCK,
		SGAME_FAKESPRINT,
		SGAME_FAKEWALK
	}
	
	SimonLogger SimonLog = new SimonLogger(this);
	SimonGameChooser SimonSGC = new SimonGameChooser(this);
	SimonGameManager SimonSGM = new SimonGameManager(this);
	SimonArenaLoader SimonCFGM = new SimonArenaLoader(this);
	
	private String SimonTag = ChatColor.BLACK + "[" + ChatColor.GREEN + "SimonSays" + ChatColor.BLACK + "]" + " " + ChatColor.WHITE;
	
	MySQL sql;
	
	/*
	 * # MySQL Details
		host: NULL
		port: NULL
		database: NULL
		user: NULL
		password: NULL
	*/
	
	int SimonSGCTask;
	int SimonSGMTask;
	
	boolean usingLocalConfig;
	
	public void onEnable()
	{		
		saveDefaultConfig();
		reloadConfig();
		
		if(UsingMySQL() == true)
		{	
			SimonLog.logInfo("Detected MySQL usage, connecting..");
			
			usingLocalConfig = false;
			
			String sqlHost = this.getConfig().getString("host");
			String sqlPort = this.getConfig().getString("port");
			String sqlDb = this.getConfig().getString("database");
			String sqlUser = this.getConfig().getString("user");
			String sqlPw = this.getConfig().getString("password");
			
			sql = new MySQL(this, sqlHost, sqlPort, sqlDb, sqlUser, sqlPw);
			
			sql.openConnection();
			
			try 
			{
				Statement createtables = sql.getConnection().createStatement();
				createtables.executeUpdate("CREATE TABLE IF NOT EXISTS SimonSays_Arenas(ArenaName varchar(255) NOT NULL, ArenaLocation varchar(255) NOT NULL, ArenaType varchar(255) NOT NULL, RelatedArena varchar(255) NOT NULL, ArenaID int(255) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`ArenaID`))");
				SimonLog.logInfo("Successfully executed onEnable() queries!");
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
				SimonLog.logSevereError(ChatColor.RED + "WARNING: Arenas will not be saved!");
			}
			
			SimonCFGM.SQLLoadGameArenas();
		}
		else
		{
			SimonLog.logInfo("Using local-config, loading data...");
			usingLocalConfig = true;
			
			SimonCFGM.saveDefaultArenaConfig();
			SimonCFGM.saveArenaConfig();
			SimonCFGM.reloadArenaConfig();
			
			SimonCFGM.CFGLoadGameArenas();
		}
		
		CheckUpdate();
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		SimonSGCTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, SimonSGC, 0L, 75L);
		
		SimonLog.logInfo("Successfully loaded!");
	}
	
	public void onDisable()
	{
		//reloadConfig();
		//saveConfig();
		
		SimonLog.logInfo("Successfully unloaded!");
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
	            	SimonLog.logInfo("Success: SimonSays will be updated on next reload!");
	                break;
	            case FAIL_DOWNLOAD:
	            	SimonLog.logInfo("Download Failed: The updater found an update, but was unable to download SimonSays");
	                break;
	            case FAIL_DBO:
	            	SimonLog.logInfo("dev.bukkit.org Failed: for some reason, the updater was unable to contact DBO to download the file.");
	                break;
	            case FAIL_NOVERSION:
	            	SimonLog.logInfo("No version found: When running the version check, the file on DBO did not contain the a version in the format 'vVersion' such as 'v1.0'.");
	                break;
	            case FAIL_BADSLUG:
	            	SimonLog.logInfo("Bad slug: The slug provided by SimonSays is invalid and doesn't exist on DBO.");
	        }
		}
		else
		{
			SimonLog.logInfo("Disabled update-checking...");
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args) 
	{
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			
			if(cmd.getName().equalsIgnoreCase("simonhelp") || CommandLabel.equalsIgnoreCase("sh"))
			{
				player.sendMessage(SimonTag + "TODO: Finish /simonhelp");
				return true;
			}
			else if(cmd.getName().equalsIgnoreCase("creategamearena"))
			{
				if(!player.hasPermission("SimonSays.create"))
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
				
		  		SimonGameArenaManager.getGameManager().createArena(player.getLocation(), arenaname);
		  		
		  		if(UsingMySQL() == true)
		  		{
		  			SimonCFGM.AddArenaToSQL(SimonGameArenaManager.getGameManager().serializeLoc(player.getLocation()), arenaname, "0", relatedarena);
		  		}
		  		else
		  		{
		  			SimonCFGM.CFGAddArena(SimonGameArenaManager.getGameManager().serializeLoc(player.getLocation()), arenaname, "0");
		  			SimonCFGM.saveArenaConfig();
		  			SimonCFGM.reloadArenaConfig();
		  		}
		  		
	    		player.sendMessage(SimonTag + "Created " + arenaname + " at" + player.getLocation().toString());
	    		
	    		SimonLog.logInfo(player.getName() + " Created game arena " + arenaname + "at " + player.getLocation().toString());
				return true;
			}
			else if(cmd.getName().equalsIgnoreCase("createspecarena"))
			{	
				if(!player.hasPermission("SimonSays.create"))
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
		  			SimonCFGM.AddArenaToSQL(SimonGameArenaManager.getGameManager().serializeLoc(player.getLocation()), arenaname, "1", "none");
		  		}
		  		else
		  		{
		  			SimonCFGM.CFGAddArena(SimonGameArenaManager.getGameManager().serializeLoc(player.getLocation()), arenaname, "1");
		  		}
		  		
	    		player.sendMessage(SimonTag + "Created Spectator arena at " + player.getLocation().toString());
	    		SimonLog.logInfo(player.getName() + " Created Spectator arena at " + player.getLocation().toString());
				return true;
			}
			else if(cmd.getName().equalsIgnoreCase("deletearena"))
			{
				if(!player.hasPermission("SimonSays.delete"))
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
					player.sendMessage("Successfully removed arena: " + arenaname);
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
						
						player.sendMessage("Successfully removed all arenas!");
					}
					else
					{
						SimonCFGM.CFGRemoveArena(arenaname);
						player.sendMessage("Successfully removed arena: " + arenaname);
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
	    		
	    		if(!SimonGameArenaManager.getGameManager().gameInProgress(arenaname))
	    		{
		    		SimonGameArenaManager.getGameManager().addPlayer(player, arenaname);
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
				
				String GameArena = SimonGameArenaManager.getGameManager().getArenaIn(player);
				
				String RelatedArena = "none";
				
				if(UsingMySQL() == true)
				{
					RelatedArena = SimonCFGM.SQLGetRelatedGameArena(GameArena);
				}
				else
				{
					
				}
				
				
	    		SimonGameArenaManager.getGameManager().removePlayer(player);
	    		SimonSpectateArenaManager.getSpecManager().specPlayer(player, RelatedArena);
	    		player.sendMessage(SimonTag + "Successfully left the SimonArena!");
	    		SimonLog.logSevereError(RelatedArena + "    " + GameArena);
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
		
		if(SimonGameArenaManager.getGameManager().IsPlaying(p))
		{
			switch(SimonGameType)
			{
				case SGAME_DONTMOVE:
				{
					String GameArena = SimonGameArenaManager.getGameManager().getArenaIn(p);
					String RelatedArena = "";
					
					if(UsingMySQL() == true)
					{
						RelatedArena = SimonCFGM.SQLGetRelatedGameArena(GameArena);
					}
					else
					{
						
					}
					
					p.sendMessage(SimonTag + "[SGAME_DONTMOVE] Incorrect! Abandoned Game!");
					SimonGameArenaManager.getGameManager().removePlayer(p);
					SimonSpectateArenaManager.getSpecManager().specPlayer(p, RelatedArena);
					break;
				}
				
				case SGAME_WALK:
				{
					SimonSGM.SimonActionSetDone(p);
					
					if(!SimonSGM.SimonMsgSent(p))
					{
						p.sendMessage(SimonTag + "[SGAME_WALK] Correct! Lets Continue!");
						SimonSGM.SimonSetMsgSent(p);
					}
					
					break;
				}
				
				case SGAME_FAKEWALK:
				{
					String GameArena = SimonGameArenaManager.getGameManager().getArenaIn(p);
					String RelatedArena = "";
					
					if(UsingMySQL() == true)
					{
						RelatedArena = SimonCFGM.SQLGetRelatedGameArena(GameArena);
					}
					else
					{
						
					}
					
					p.sendMessage(SimonTag + "[SGAME_FAKEWALK] Incorrect! Abandoned Game!");
					SimonGameArenaManager.getGameManager().removePlayer(p);
					SimonSpectateArenaManager.getSpecManager().specPlayer(p, RelatedArena);
					break;
				}
				
				case SGAME_JUMP:
				{
					if(e.getTo().getY() > e.getFrom().getY())
					{
						Block block = e.getPlayer().getWorld().getBlockAt(new Location(e.getPlayer().getWorld(), e.getTo().getX(), e.getTo().getY()+2, e.getTo().getZ()));
						
						if(block.getType() == Material.AIR)
						{
							SimonSGM.SimonActionSetDone(p);
							
							if(!SimonSGM.SimonMsgSent(p))
							{
								p.sendMessage(SimonTag + "[SGAME_JUMP] Correct! Lets Continue!");
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
							String GameArena = SimonGameArenaManager.getGameManager().getArenaIn(p);
							String RelatedArena = "";
							
							if(UsingMySQL() == true)
							{
								RelatedArena = SimonCFGM.SQLGetRelatedGameArena(GameArena);
							}
							else
							{
								
							}
							
							p.sendMessage(SimonTag + "[SGAME_FAKEJUMP] Incorrect! Abandoned Game!");
							SimonGameArenaManager.getGameManager().removePlayer(p);
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
		
		if(SimonGameArenaManager.getGameManager().IsPlaying(p))
		{
			switch(SimonGameType)
			{
				case SGAME_SNEAK:
				{
					SimonSGM.SimonActionSetDone(p);
					
					if(!SimonSGM.SimonMsgSent(p))
					{
						p.sendMessage(SimonTag + "[SGAME_SNEAK] Correct! Lets Continue!");
						SimonSGM.SimonSetMsgSent(p);
					}
					
					
					break;
				}
				
				case SGAME_FAKESNEAK:
				{
					String GameArena = SimonGameArenaManager.getGameManager().getArenaIn(p);
					String RelatedArena = "";
					
					if(UsingMySQL() == true)
					{
						RelatedArena = SimonCFGM.SQLGetRelatedGameArena(GameArena);
					}
					else
					{
						
					}
					
					p.sendMessage(SimonTag + "[SGAME_FAKESNEAK] Incorrect! Abandoned Game!");
					SimonGameArenaManager.getGameManager().removePlayer(p);
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
		
		if(SimonGameArenaManager.getGameManager().IsPlaying(p))
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
							p.sendMessage(SimonTag + "[SGAME_SPRINT] Correct! Lets Continue!");
							SimonSGM.SimonSetMsgSent(p);
						}
					}
					break;
				}
				
				case SGAME_FAKESPRINT:
				{
					String GameArena = SimonGameArenaManager.getGameManager().getArenaIn(p);
					String RelatedArena = "";
					
					if(UsingMySQL() == true)
					{
						RelatedArena = SimonCFGM.SQLGetRelatedGameArena(GameArena);
					}
					else
					{
						
					}
					
					p.sendMessage(SimonTag + "[SGAME_FAKESPRINT] Incorrect! Abandoned Game!");
					SimonGameArenaManager.getGameManager().removePlayer(p);
					SimonSpectateArenaManager.getSpecManager().specPlayer(p, RelatedArena);
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
							if(!p.hasPermission("SimonSays.join"))
							{
								p.sendMessage(SimonTag + ChatColor.RED + "Access denied");
								return;
							}
							
				    		if(!SimonGameArenaManager.getGameManager().gameInProgress(SignLine[1]))
				    		{
					    		SimonGameArenaManager.getGameManager().addPlayer(p, SignLine[1]);
					    		p.setGameMode(GameMode.SURVIVAL);	
				    		}
				    		else
				    		{
				    			p.sendMessage(SimonTag + "Woops! Game is already in progress! (or arena is invalid)");
				    		}
						}
					}
				}
			}
			
			if(SimonGameArenaManager.getGameManager().IsPlaying(p))
			{
				switch(SimonGameType)
				{
					case SGAME_PUNCHBLOCK:
					{
							
						SimonSGM.SimonActionSetDone(p);
						if(!SimonSGM.SimonMsgSent(p))
						{
							p.sendMessage(SimonTag + "[SGAME_PUNCHBLOCK] Correct! Lets Continue!");
							SimonSGM.SimonSetMsgSent(p);
						}
							
						break;
					}
						
					case SGAME_FAKEPUNCHBLOCK:
					{
						String GameArena = SimonGameArenaManager.getGameManager().getArenaIn(p);
						String RelatedArena = "";
						
						if(UsingMySQL() == true)
						{
							RelatedArena = SimonCFGM.SQLGetRelatedGameArena(GameArena);
						}
						else
						{
							
						}
						
						p.sendMessage(SimonTag + "[SGAME_FAKEPUNCHBLOCK] Incorrect! Abandoned Game!");
						SimonGameArenaManager.getGameManager().removePlayer(p);
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
			
			if(SimonGameArenaManager.getGameManager().IsPlaying(p))
			{
				switch(SimonGameType)
				{
					case SGAME_ATTACKPLAYER:
					{
						SimonSGM.SimonActionSetDone(p);
						
						if(!SimonSGM.SimonMsgSent(p))
						{
							p.sendMessage(SimonTag + "[SGAME_ATTACKPLAYER] Correct! Lets Continue!");
							SimonSGM.SimonSetMsgSent(p);
						}
						
						break;
					}
					
					case SGAME_FAKEATTACKPLAYER:
					{
						String GameArena = SimonGameArenaManager.getGameManager().getArenaIn(p);
						String RelatedArena = "";
						
						if(UsingMySQL() == true)
						{
							RelatedArena = SimonCFGM.SQLGetRelatedGameArena(GameArena);
						}
						else
						{
							
						}
						
						p.sendMessage(SimonTag + "[SGAME_FAKEATTACKPLAYER] Incorrect! Abandoned Game!");
						SimonGameArenaManager.getGameManager().removePlayer(p);
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
			
			if(SimonGameArenaManager.getGameManager().IsPlaying(p))
			{
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onSignChanged(SignChangeEvent e)
	{
		String[] SignLine = e.getLines();
		
		if(SignLine[0].contains("[SimonSays]"))
		{
			if(!SignLine[1].isEmpty())
			{
				e.setLine(0, ChatColor.GREEN + "[SimonSays]");
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

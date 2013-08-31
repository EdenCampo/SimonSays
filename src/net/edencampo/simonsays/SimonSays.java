package net.edencampo.simonsays;

import java.sql.Connection;
import java.sql.ResultSet;
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
	
	private String SimonTag = ChatColor.BLACK + "[" + ChatColor.GREEN + "SimonSays" + ChatColor.BLACK + "]" + " " + ChatColor.WHITE;
	
	MySQL sql = new MySQL(this, "localhost", "3307", "mctest", ".", ".");
	
	/*
	 * # MySQL Details
		host: NULL
		port: NULL
		database: NULL
		user: NULL
		password: NULL
	
	String sqlHost = this.getConfig().getString("host");
	String sqlPort = this.getConfig().getString("port");
	String sqlDb = this.getConfig().getString("database");
	String sqlUser = this.getConfig().getString("user");
	String sqlPw = this.getConfig().getString("password");
	
	*/
	
	int SimonSGCTask;
	int SimonSGMTask;
	
	public void onEnable()
	{
		saveDefaultConfig();
		
		if(UsingMySQL() == true)
		{
			SimonLog.logInfo("Detected MySQL usage, prepering!");
			
			sql.openConnection();
			
			try 
			{
				Statement createtables = sql.getConnection().createStatement();
				createtables.executeUpdate("CREATE TABLE IF NOT EXISTS SimonSays_Arenas(ArenaName varchar(255) NOT NULL, ArenaLocation varchar(255) NOT NULL, ArenaType varchar(255) NOT NULL, ArenaID int(255) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`ArenaID`))");
				SimonLog.logInfo("Successfully executed onEnable() queries!");
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
			
			this.loadSQLGameArenas();
		}
		CheckUpdate();
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		SimonSGCTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, SimonSGC, 0L, 75L);
		
		SimonLog.logInfo("Successfully loaded!");
	}
	
	public void onDisable()
	{
		this.saveConfig();
		
		SimonLog.logInfo("Successfully unloaded!");
	}
	
	
	private void CheckUpdate()
	{
		Updater updater = new Updater(this, "simon-says", this.getFile(), Updater.UpdateType.NO_DOWNLOAD, true);
		
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
				String arenaname = args[0];
				
		  		SimonGameArenaManager.getGameManager().createArena(player.getLocation(), arenaname);
		  		
		  		AddArenaToSQL(SimonGameArenaManager.getGameManager().serializeLoc(player.getLocation()), arenaname, "0");
		  		
	    		player.sendMessage(SimonTag + "Created " + arenaname + " at" + player.getLocation().toString());
	    		
	    		SimonLog.logInfo(player.getName() + " Created game arena " + arenaname + "at " + player.getLocation().toString());
				return true;
			}
			else if(cmd.getName().equalsIgnoreCase("createspecarena"))
			{
				String arenaname = args[0];
				
		  		SimonSpectateArenaManager.getSpecManager().createArena(player.getLocation());
		  		AddArenaToSQL(SimonGameArenaManager.getGameManager().serializeLoc(player.getLocation()), arenaname, "1");
		  		
	    		player.sendMessage(SimonTag + "Created Spectate arena at " + player.getLocation().toString());
	    		SimonLog.logInfo(player.getName() + " Created Spectate arena at " + player.getLocation().toString());
				return true;
			}
			else if(cmd.getName().equalsIgnoreCase("simonjoin") || CommandLabel.equalsIgnoreCase("sj"))
			{
				if(args.length != 1)
				{
					player.sendMessage(SimonTag + "Usage: /simonjoin <#ArenaID>");
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
	    		SimonGameArenaManager.getGameManager().removePlayer(player);
	    		SimonSpectateArenaManager.getSpecManager().specPlayer(player, 1);
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
		
		if(SimonGameArenaManager.getGameManager().IsPlaying(p))
		{
			switch(SimonGameType)
			{
				case SGAME_DONTMOVE:
				{
					p.sendMessage(SimonTag + "[SGAME_DONTMOVE] Incorrect! Abandoned Game!");
					SimonGameArenaManager.getGameManager().removePlayer(p);
					SimonSpectateArenaManager.getSpecManager().specPlayer(p, 1);
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
					p.sendMessage(SimonTag + "[SGAME_FAKEWALK] Incorrect! Abandoned Game!");
					SimonGameArenaManager.getGameManager().removePlayer(p);
					SimonSpectateArenaManager.getSpecManager().specPlayer(p, 1);
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
							p.sendMessage(SimonTag + "[SGAME_FAKEJUMP] Incorrect! Abandoned Game!");
							SimonGameArenaManager.getGameManager().removePlayer(p);
							SimonSpectateArenaManager.getSpecManager().specPlayer(p, 1);
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
					p.sendMessage(SimonTag + "[SGAME_FAKESNEAK] Incorrect! Abandoned Game!");
					SimonGameArenaManager.getGameManager().removePlayer(p);
					SimonSpectateArenaManager.getSpecManager().specPlayer(p, 1);
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
					p.sendMessage(SimonTag + "[SGAME_FAKESPRINT] Incorrect! Abandoned Game!");
					SimonGameArenaManager.getGameManager().removePlayer(p);
					SimonSpectateArenaManager.getSpecManager().specPlayer(p, 1);
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
						p.sendMessage(SimonTag + "[SGAME_FAKEPUNCHBLOCK] Incorrect! Abandoned Game!");
						SimonGameArenaManager.getGameManager().removePlayer(p);
						SimonSpectateArenaManager.getSpecManager().specPlayer(p, 1);
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
						p.sendMessage(SimonTag + "[SGAME_FAKEATTACKPLAYER] Incorrect! Abandoned Game!");
						SimonGameArenaManager.getGameManager().removePlayer(p);
						SimonSpectateArenaManager.getSpecManager().specPlayer(p, 1);
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
		/*
		String useMySQL = this.getConfig().getString("useMySQL");
		
		if(useMySQL.equalsIgnoreCase("true") || useMySQL.equalsIgnoreCase("yes"))
		{
			return true;
		}*/
		
		return true;
	}
	
	public void AddArenaToSQL(String location, String arenaname, String type)
	{
		// types: 0-regular 1-spec
		
		if(UsingMySQL() == true)
		{
			try 
			{	
				Connection connection = sql.getConnection();
				
				Statement createtables = connection.createStatement();
				
				createtables.executeUpdate("INSERT INTO SimonSays_Arenas (`ArenaName`, `ArenaLocation`, `ArenaType`) VALUES ('" + arenaname + "', '" + location +"', '" + type + "');");
				SimonLog.logInfo("Successfully executed AddArenaToSQL query!");
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		 }
	}
	
	
	public String getSQLGameArenasCount()
	{
		if(this.UsingMySQL() == true)
		{
			try 
			{
				Connection connection = this.sql.getConnection();
				
				Statement loadarenas = connection.createStatement();
				
				ResultSet res = loadarenas.executeQuery("SELECT ArenaID FROM SimonSays_Arenas;");
				res.last();
				
				String arenacount = res.getString("ArenaID");
				
				return arenacount+1;
			}
			catch (SQLException e) 
			{
				
			}
		}
		
		return "0";
	}
	
	public void loadSQLGameArenas()
	{
		String arenacount = getSQLGameArenasCount();
		
		int arenas = Integer.parseInt(arenacount);
		
		if(arenas == 0)
		{
			return;
		}
		
		if(this.UsingMySQL() == true)
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
					
					Connection connection = this.sql.getConnection();
					
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
						//TODO: FIX - des.loc not working..
						GameArena a = new GameArena(SimonGameArenaManager.getGameManager().deserializeLoc(ArenaLocation), ArenaName);
						SimonGameArenaManager.getGameManager().arenas.add(a);
						SimonGameArenaManager.getGameManager().getArena(ArenaName).spawn = SimonGameArenaManager.getGameManager().deserializeLoc(ArenaLocation);
						SimonLog.logSevereError("" + SimonGameArenaManager.getGameManager().deserializeLoc(ArenaLocation));
						
						SimonLog.logWarning(ArenaType + "  " + ArenaName + "  " + ArenaLocation);
						
						this.SimonLog.logInfo("Successfully added arena:" + ArenaName + " at" + ArenaLocation);
					}
					
					this.SimonLog.logInfo("Successfully executed loadGameArenas query!");
					id++;
				}
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
	}
}

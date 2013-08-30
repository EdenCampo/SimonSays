package net.edencampo.simonsays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
	 * TODO: RENAME: SimonArenaManager - SimonGameArenaManger
	 * TODO: ADD: SimonSpecArenaManager
	 * TODO: FIX: Config Errors
	 * TODO: POST: BukkitDev.org post
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
	
	public void onEnable()
	{
		//this.saveDefaultConfig();
		
		CheckUpdate();
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		
		//20 L - Second
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, SimonSGC, 0L, 75L);
		
		SimonLog.logInfo("Successfully loaded!");
	}
	
	public void onDisable()
	{
		this.saveConfig();
		
		SimonLog.logInfo("Successfully unloaded!");
	}
	
	
	private void CheckUpdate()
	{
		Updater updater = new Updater(this, "slug", this.getFile(), Updater.UpdateType.NO_DOWNLOAD, true);
		
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
			else if(cmd.getName().equalsIgnoreCase("createarena"))
			{
		  		SimonArenaManager.getManager().createArena(player.getLocation());
	    		player.sendMessage(SimonTag + "Created arena at " + player.getLocation().toString());
	    		SimonLog.logInfo(player.getName() + " Created arena at " + player.getLocation().toString());
				return true;
			}
			else if(cmd.getName().equalsIgnoreCase("simonjoin") || CommandLabel.equalsIgnoreCase("sj"))
			{
				if(args.length != 1)
				{
					player.sendMessage(SimonTag + "Usage: /simonjoin <#ArenaID>");
					return true;
				}
				
	    		int num = 0;
	    		
	    		try
	    		{
	    		    num = Integer.parseInt(args[0]);
	    		}
	    		catch(NumberFormatException e)
	    		{
	    		    player.sendMessage(SimonTag + "Invalid SimonArena ID");
	    		    SimonLog.logWarning(player.getName() + " has triggered Invalid SimonArena ID");
	    		}
	    		
	    		SimonArenaManager.getManager().addPlayer(player, num);
	    		player.setGameMode(GameMode.SURVIVAL);
	    		
				return true;
			}
			
			else if(CommandLabel.equalsIgnoreCase("simonleave") || CommandLabel.equalsIgnoreCase("sl"))
			{
	    		SimonArenaManager.getManager().removePlayer(player);
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
		
		if(SimonArenaManager.getManager().IsPlaying(p))
		{
			switch(SimonGameType)
			{
				case SGAME_DONTMOVE:
				{
					p.sendMessage(SimonTag + "[SGAME_DONTMOVE] Incorrect! Abandoned Game!");
					SimonArenaManager.getManager().removePlayer(p);
					break;
				}
				
				case SGAME_WALK:
				{
					p.sendMessage(SimonTag + "[SGAME_WALK] Correct! Lets Continue!");
					SimonSGM.SimonActionSetDone(p);
					break;
				}
				
				case SGAME_FAKEWALK:
				{
					p.sendMessage(SimonTag + "[SGAME_FAKEWALK] Incorrect! Abandoned Game!");
					SimonArenaManager.getManager().removePlayer(p);
					break;
				}
				
				case SGAME_JUMP:
				{
					if(e.getTo().getY() > e.getFrom().getY())
					{
						Block block = e.getPlayer().getWorld().getBlockAt(new Location(e.getPlayer().getWorld(), e.getTo().getX(), e.getTo().getY()+2, e.getTo().getZ()));
						
						if(block.getType() == Material.AIR)
						{
							p.sendMessage(SimonTag + "[SGAME_JUMP] Correct! Lets Continue!");
							SimonSGM.SimonActionSetDone(p);
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
							SimonArenaManager.getManager().removePlayer(p);
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
		
		if(SimonArenaManager.getManager().IsPlaying(p))
		{
			switch(SimonGameType)
			{
				case SGAME_SNEAK:
				{
					p.sendMessage(SimonTag + "[SGAME_SNEAK] Correct! Lets Continue!");
					SimonSGM.SimonActionSetDone(p);
					break;
				}
				
				case SGAME_FAKESNEAK:
				{
					p.sendMessage(SimonTag + "[SGAME_FAKESNEAK] Incorrect! Abandoned Game!");
					SimonArenaManager.getManager().removePlayer(p);
				}
			
			}
		}
	}
	
	@EventHandler
	public void onPlayerSprint(PlayerToggleSprintEvent e)
	{
		SimonGame SimonGameType = this.SimonSGC.GetGame();
		
		Player p = e.getPlayer();
		
		if(SimonArenaManager.getManager().IsPlaying(p))
		{
			switch(SimonGameType)
			{
				case SGAME_SPRINT:
				{
					p.sendMessage(SimonTag + "[SGAME_SPRINT] Correct! Lets Continue!");
					SimonSGM.SimonActionSetDone(p);
					break;
				}
				
				case SGAME_FAKESPRINT:
				{
					p.sendMessage(SimonTag + "[SGAME_FAKESPRINT] Incorrect! Abandoned Game!");
					SimonArenaManager.getManager().removePlayer(p);
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
		
		if(SimonArenaManager.getManager().IsPlaying(p))
		{
			if(block != null)
			{
				switch(SimonGameType)
				{
					case SGAME_PUNCHBLOCK:
					{
						p.sendMessage(SimonTag + "[SGAME_PUNCHBLOCK] Correct! Lets Continue!");
						SimonSGM.SimonActionSetDone(p);
						break;
					}
					
					case SGAME_FAKEPUNCHBLOCK:
					{
						p.sendMessage(SimonTag + "[SGAME_FAKEPUNCHBLOCK] Incorrect! Abandoned Game!");
						SimonArenaManager.getManager().removePlayer(p);
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
			
			if(SimonArenaManager.getManager().IsPlaying(p))
			{
				switch(SimonGameType)
				{
					case SGAME_ATTACKPLAYER:
					{
						p.sendMessage(SimonTag + "[SGAME_ATTACKPLAYER] Correct! Lets Continue!");
						SimonSGM.SimonActionSetDone(p);
						break;
					}
					
					case SGAME_FAKEATTACKPLAYER:
					{
						p.sendMessage(SimonTag + "[SGAME_FAKEATTACKPLAYER] Incorrect! Abandoned Game!");
						SimonArenaManager.getManager().removePlayer(p);
					}
				}
			}
		}
	}
	
	/*
	 	SGAME_NONE(DONE),
		SGAME_DONTMOVE(DONE, GOTTA FIX),
		SGAME_SNEAK(DONE),
		SGAME_JUMP(DONE),
		SGAME_ATTACKPLAYER(DONE),
		SGAME_PUNCHBLOCK(DONE),
		SGAME_SPRINT(DONE),
		SGAME_WALK(DONE)
	 */
}

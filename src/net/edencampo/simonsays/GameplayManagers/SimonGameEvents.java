package net.edencampo.simonsays.GameplayManagers;

import net.edencampo.simonsays.SimonSays;
import net.edencampo.simonsays.SimonSays.SimonGame;
import net.edencampo.simonsays.ArenaManagers.SimonArenaManager;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
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

public class SimonGameEvents implements Listener
{
	SimonSays plugin;
	SimonArenaManager gManager;;
	
	public SimonGameEvents(SimonSays instance)
	{
		plugin = instance;
		gManager = new SimonArenaManager(plugin);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e)
	{
		SimonGame SimonGameType = this.plugin.SimonSGC.GetGame();
		
		Player p = e.getPlayer();
		
		String GameArena = plugin.SimonAM.getArenaIn(p);
		
		if(plugin.SimonAM.IsPlaying(p))
		{
			Location to = e.getTo();
			Location from = e.getFrom();
			
			switch(SimonGameType)
			{
				case SGAME_DONTMOVE:
				{
					if(to.getBlockX() == from.getBlockX())
					{
						return;
					}
					
					String RelatedArena = "";
					
					if(plugin.UsingMySQL() == true)
					{
						RelatedArena = plugin.SimonArenasM.SQLGetRelatedGameArena(GameArena);
					}
					else
					{
						RelatedArena = plugin.SimonArenasM.CFGGetRelatedArena(GameArena);
					}
					
					p.sendMessage(plugin.SimonTag + "Ohhh, you moved! Abandoned Game!");
					plugin.SimonAM.removePlayer(p);
					gManager.specPlayer(p, RelatedArena);
					break;
				}
				
				case SGAME_WALK:
				{
					if(to != from)
					{
						plugin.SimonSGM.SimonActionSetDone(p);
						
						if(!plugin.SimonSGM.SimonMsgSent(p))
						{
							p.sendMessage(plugin.SimonTag + "Great job! Lets Continue!");
							plugin.SimonSGM.SimonSetMsgSent(p);
						}
					}
					break;
				}
				
				case SGAME_FAKEWALK:
				{
					String RelatedArena = "";
					
					if(plugin.UsingMySQL() == true)
					{
						RelatedArena = plugin.SimonArenasM.SQLGetRelatedGameArena(GameArena);
					}
					else
					{
						RelatedArena = plugin.SimonArenasM.CFGGetRelatedArena(GameArena);
					}
					
					p.sendMessage(plugin.SimonTag + "Ohhh, you walked! Abandoned Game!");
					
					plugin.SimonAM.removePlayer(p);
					gManager.specPlayer(p, RelatedArena);
					break;
				}
				
				case SGAME_JUMP:
				{
					if(to.getY() > from.getY())
					{
						Block block = e.getPlayer().getWorld().getBlockAt(new Location(e.getPlayer().getWorld(), e.getTo().getX(), e.getTo().getY()+2, e.getTo().getZ()));
						
						if(block.getType() == Material.AIR)
						{
							plugin.SimonSGM.SimonActionSetDone(p);
							
							if(!plugin.SimonSGM.SimonMsgSent(p))
							{
								p.sendMessage(plugin.SimonTag + "Great job! Lets Continue!");
								plugin.SimonSGM.SimonSetMsgSent(p);
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
							
							if(plugin.UsingMySQL() == true)
							{
								RelatedArena = plugin.SimonArenasM.SQLGetRelatedGameArena(GameArena);
							}
							else
							{
								RelatedArena = plugin.SimonArenasM.CFGGetRelatedArena(GameArena);
							}
							
							p.sendMessage(plugin.SimonTag + "Nope.. I didn't want you to jump! Abandoned Game!");
							plugin.SimonAM.removePlayer(p);
							gManager.specPlayer(p, RelatedArena);
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerSneak(PlayerToggleSneakEvent e)
	{
		SimonGame SimonGameType = this.plugin.SimonSGC.GetGame();
		
		Player p = e.getPlayer();
		
		String GameArena = plugin.SimonAM.getArenaIn(p);
		
		if(plugin.SimonAM.IsPlaying(p))
		{
			switch(SimonGameType)
			{
				case SGAME_SNEAK:
				{
					plugin.SimonSGM.SimonActionSetDone(p);
					
					if(!plugin.SimonSGM.SimonMsgSent(p))
					{
						p.sendMessage(plugin.SimonTag + "Great job! Lets Continue!");
						plugin.SimonSGM.SimonSetMsgSent(p);
					}
					
					
					break;
				}
				
				case SGAME_FAKESNEAK:
				{
					String RelatedArena = "";
					
					if(plugin.UsingMySQL() == true)
					{
						RelatedArena = plugin.SimonArenasM.SQLGetRelatedGameArena(GameArena);
					}
					else
					{
						RelatedArena = plugin.SimonArenasM.CFGGetRelatedArena(GameArena);
					}
					
					p.sendMessage(plugin.SimonTag + "Woops! You've mistaken! Abandoned Game!");
					plugin.SimonAM.removePlayer(p);
					gManager.specPlayer(p, RelatedArena);
				}
			
			}
		}
	}
	
	@EventHandler
	public void onPlayerSprint(PlayerToggleSprintEvent e)
	{
		SimonGame SimonGameType = this.plugin.SimonSGC.GetGame();
		
		Player p = e.getPlayer();
		Boolean Sprinting = e.isSprinting();
		
		String GameArena = plugin.SimonAM.getArenaIn(p);
		
		if(plugin.SimonAM.IsPlaying(p))
		{
			switch(SimonGameType)
			{
				case SGAME_SPRINT:
				{
					if(Sprinting == true)
					{
						plugin.SimonSGM.SimonActionSetDone(p);
						
						if(!plugin.SimonSGM.SimonMsgSent(p))
						{
							p.sendMessage(plugin.SimonTag + "Great job! Lets Continue!");
							plugin.SimonSGM.SimonSetMsgSent(p);
						}
					}
					break;
				}
				
				case SGAME_FAKESPRINT:
				{
					if(Sprinting == true)
					{		
						String RelatedArena = "";
						
						if(plugin.UsingMySQL() == true)
						{
							RelatedArena = plugin.SimonArenasM.SQLGetRelatedGameArena(GameArena);
						}
						else
						{
							RelatedArena = plugin.SimonArenasM.CFGGetRelatedArena(GameArena);
						}
						
						p.sendMessage(plugin.SimonTag + "Nope. Abandoned Game!");
						plugin.SimonAM.removePlayer(p);
						gManager.specPlayer(p, RelatedArena);
					}
				}
			
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e)
	{
		SimonGame SimonGameType = this.plugin.SimonSGC.GetGame();
		
		Player p = e.getPlayer();
		
		Block block = e.getClickedBlock();
		
		if(block != null)
		{
			BlockState state = block.getState();
			
			if(state instanceof Sign)
			{
				if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && block.getType() == Material.SIGN || e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && block.getType() == Material.SIGN_POST || e.getAction().equals(Action.LEFT_CLICK_BLOCK) && block.getType() == Material.SIGN || e.getAction().equals(Action.LEFT_CLICK_BLOCK) && block.getType() == Material.SIGN_POST)
				{
					Sign sign = (Sign) state;
					
					String[] SignLine = sign.getLines();
					
					if(SignLine[0].contains("[SimonSays]"))
					{
						if(!SignLine[1].isEmpty())
						{	
							if(plugin.SimonAM.getArena(SignLine[1]) != null)
							{
								if(!p.hasPermission("SimonSays.sign.use"))
								{
									p.sendMessage(plugin.SimonTag + ChatColor.RED + "Access denied");
									return;
								}
								
					    		if(plugin.SimonAM.getArena(SignLine[1]).needsPlayers())
					    		{
						    		plugin.SimonAM.addPlayer(p, SignLine[1]);
						    		p.setGameMode(GameMode.SURVIVAL);	
					    		}
					    		else
					    		{
					    			p.sendMessage(plugin.SimonTag + "Woops! Game is already in progress! (or arena is invalid)");
					    		}
							}
							else
							{
								p.sendMessage(plugin.SimonTag + "Arena join attempt denied. Invalid Arena!");
							}
						}
					}
				}
			}
			
			String GameArena = plugin.SimonAM.getArenaIn(p);
			
			if(plugin.SimonAM.IsPlaying(p))
			{
				switch(SimonGameType)
				{
					case SGAME_PUNCHBLOCK:
					{
							
						plugin.SimonSGM.SimonActionSetDone(p);
						if(!plugin.SimonSGM.SimonMsgSent(p))
						{
							p.sendMessage(plugin.SimonTag + "Great job! Lets Continue!");
							plugin.SimonSGM.SimonSetMsgSent(p);
						}
							
						break;
					}
						
					case SGAME_FAKEPUNCHBLOCK:
					{
						String RelatedArena = "";
						
						if(plugin.UsingMySQL() == true)
						{
							RelatedArena = plugin.SimonArenasM.SQLGetRelatedGameArena(GameArena);
						}
						else
						{
							RelatedArena = plugin.SimonArenasM.CFGGetRelatedArena(GameArena);
						}
						
						p.sendMessage(plugin.SimonTag + "I confused you ah? Abandoned Game!");
						plugin.SimonAM.removePlayer(p);
						gManager.specPlayer(p, RelatedArena);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onHitPlayer(EntityDamageByEntityEvent e)
	{
		SimonGame SimonGameType = this.plugin.SimonSGC.GetGame();
		
		Entity entattacker = e.getDamager();
		
		if(entattacker instanceof Player)
		{
			Player p = (Player)entattacker;

			String GameArena = plugin.SimonAM.getArenaIn(p);
			
			if(plugin.SimonAM.IsPlaying(p))
			{
				switch(SimonGameType)
				{
					case SGAME_ATTACKPLAYER:
					{
						plugin.SimonSGM.SimonActionSetDone(p);
						
						if(!plugin.SimonSGM.SimonMsgSent(p))
						{
							p.sendMessage(plugin.SimonTag + "Ouch (: Lets Continue!");
							plugin.SimonSGM.SimonSetMsgSent(p);
						}
						
						break;
					}
					
					case SGAME_FAKEATTACKPLAYER:
					{
						String RelatedArena = "";
						
						if(plugin.UsingMySQL() == true)
						{
							RelatedArena = plugin.SimonArenasM.SQLGetRelatedGameArena(GameArena);
						}
						else
						{
							RelatedArena = plugin.SimonArenasM.CFGGetRelatedArena(GameArena);
						}
						
						p.sendMessage(plugin.SimonTag + "Wrong move, mate. Abandoned Game!");
						plugin.SimonAM.removePlayer(p);
						gManager.specPlayer(p, RelatedArena);
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
			
			if(plugin.SimonAM.IsPlaying(p))
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
					e.getPlayer().sendMessage(plugin.SimonTag + ChatColor.RED + "Access denied");
					e.getBlock().breakNaturally();
					return;
				}
				
				e.setLine(0, ChatColor.GREEN + "[SimonSays]");
				
				if(plugin.SimonAM.getArena(SignLine[1]) == null)
				{
					e.setLine(2, ChatColor.DARK_AQUA + "Invalid Arena");
					e.setLine(3, ChatColor.DARK_AQUA + "ERROR...");
					return;
				}
				
				if(plugin.UsingMySQL() == true)
				{
					plugin.SimonSignsM.SQLaddSignArena(SignLine[1], plugin.SimonAM.serializeLoc(e.getBlock().getLocation()));
				}
				else
				{
					plugin.SimonSignsM.CFGaddSignArena(SignLine[1], plugin.SimonAM.serializeLoc(e.getBlock().getLocation()));
				}
				
				plugin.SimonAM.getArena(SignLine[1]).arenaSign = arenasign;
			}
		}
	}
	
	@EventHandler
	public void onBlockPlaced(BlockPlaceEvent e)
	{
		SimonGame SimonGameType = this.plugin.SimonSGC.GetGame();
		
		Material block = e.getBlock().getType();
		
		Player p = e.getPlayer();
		
		String GameArena = plugin.SimonAM.getArenaIn(p);
		
		if(plugin.SimonAM.IsPlaying(p))
		{
			switch(SimonGameType)
			{
				case SGAME_PLACEBLOCK:
				{	
					Material correctblock = plugin.SimonSGM.getBlockToPlace(p);
					
					if(correctblock.equals(block))
					{
						plugin.SimonSGM.SimonActionSetDone(p);
						
						if(!plugin.SimonSGM.SimonMsgSent(p))
						{
							p.sendMessage(plugin.SimonTag + "Good job! Lets Continue!");
							plugin.SimonSGM.SimonSetMsgSent(p);
							e.setCancelled(true);
						}
					}
					
					break;
				}
				
				case SGAME_FAKEPLACEBLOCK:
				{
					String RelatedArena = "";
					
					if(plugin.UsingMySQL() == true)
					{
						RelatedArena = plugin.SimonArenasM.SQLGetRelatedGameArena(GameArena);
					}
					else
					{
						RelatedArena = plugin.SimonArenasM.CFGGetRelatedArena(GameArena);
					}
					
					p.sendMessage(plugin.SimonTag + "You've mistaken! Abandoned Game!");
					plugin.SimonAM.removePlayer(p);
					gManager.specPlayer(p, RelatedArena);
					
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
				
				if(plugin.UsingMySQL() == true)
				{
					if(plugin.SimonAM.getArena(arenaname) != null)
					{
						plugin.SimonSignsM.SQLremoveSignArena(arenaname);
						plugin.SimonSignsM.SQLlinkSignsToArenas();
						arenasign.update(false);
						
						plugin.SimonAM.getArena(arenaname).arenaSign = null;
					}
				}
				else
				{
					if(plugin.SimonAM.getArena(arenaname) != null)
					{
						plugin.SimonSignsM.CFGRemoveSign(arenaname, plugin.SimonAM.serializeLoc(e.getBlock().getLocation()));
						plugin.SimonSignsM.CFGlinkSignsToArenas();
						arenasign.update(false);
						
						plugin.SimonAM.getArena(arenaname).arenaSign = null;
					}
				}
			}
		}
	}
}

package net.edencampo.simonsays.ArenaManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.edencampo.simonsays.SimonSays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public class SimonGameArenaManager
{
	SimonSays plugin;
	
	public SimonGameArenaManager(SimonSays instance)
	{
		plugin = instance;
	}
	
	
	public Map<String, Location> locs = new HashMap<String, Location>();

	Map<String, ItemStack[]> inv = new HashMap<String, ItemStack[]>();
	Map<String, ItemStack[]> armor = new HashMap<String, ItemStack[]>();

	public List<GameArena> arenas = new ArrayList<GameArena>();
	
	int arenaSize = 0;
	
	private String SimonTag = ChatColor.BLACK + "[" + ChatColor.GREEN + "SimonSays" + ChatColor.BLACK + "]" + " " + ChatColor.WHITE;
	 
	public GameArena getArena(String arenaname)
	{
	    for(GameArena a : arenas)
	    {
	        if(a.getName().equalsIgnoreCase(arenaname))
	        {
	            return a;
	        }
	    }
	    return null;
	}
	 
	public void addPlayer(Player p, String arenaname)
	{
		GameArena a = getArena(arenaname);
	    if(a == null)
	    {
	        p.sendMessage(SimonTag + "Invalid arena!");
	        return;
	    }
	
	    a.getPlayers().add(p.getName());
	    inv.put(p.getName(), p.getInventory().getContents());
	    armor.put(p.getName(), p.getInventory().getArmorContents());
	    
	    p.getInventory().setArmorContents(null);
	    p.getInventory().clear();
	    
	    if(getArena(a.getName()).needsPlayers())
	    {
	    	plugin.SimonGSM.arenagamestage.put(a, "SGAMESTAGE_WAITINGPLAYERS");
	    	
	    	p.sendMessage(SimonTag + "Successfully joined " + a.getName() + ", waiting for more players...");
	    }
	    else
	    {
	    	plugin.SimonGSM.arenagamestage.put(a, "SGAMESTAGE_PREPERING");
	    	
	    	Bukkit.getServer().broadcastMessage(SimonTag + "About to start a game in arena '" + a.getName() + "' in 10 seconds!");
	    	plugin.SimonCD.CountDown(a.getName());
	    }
	    
	    p.teleport(a.spawn);
	}
	   

	public void removePlayer(Player p)
	{
		GameArena a = null;
	    for(GameArena arena : arenas)
	    {
	        if(arena.getPlayers().contains(p.getName()))
	        {
	            a = arena;
	        }
	    }
	    if(a == null || !a.getPlayers().contains(p.getName()))
	    {
	        p.sendMessage(SimonTag + "Invalid operation!");
	        return;
	    }
	       
	   a.getPlayers().remove(p.getName());
	   
	   p.getInventory().clear();
	   p.getInventory().setArmorContents(null);
	   
	   p.getInventory().setContents(inv.get(p.getName()));
	   p.getInventory().setArmorContents(armor.get(p.getName()));
	       
	   inv.remove(p.getName());
	   armor.remove(p.getName());
	   
	   if(a.getPlayers().size() == 0)
	   {
		   Bukkit.broadcastMessage(SimonTag + "" + p.getName() + " from arena '" + a.getName() + "' won his game!");
		   
		   plugin.SimonGSM.arenagamestage.put(plugin.SimonAM.getArena(a.getName()), "SGAMESTAGE_WAITINGPLAYERS");
	   }
	   
	    Objective arenaob = plugin.SimonAM.getArena(a.getName()).getSimonBoard().getObjective(a.getName());
	    Score players = arenaob.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_AQUA + "Players: "));
	    	
	    players.setScore(getArena(a.getName()).getPlayers().size());
	    	
	    plugin.SimonScore.removePlayerBoard(p);
	}

	public void createArena(Location loc, String arenaname)
	{
	   arenaSize++;
		
	   GameArena a = new GameArena(loc, arenaname, plugin);
	   arenas.add(a);
	}
	
	public void removeArena(String arenaname)
	{	
		arenas.remove(getArena(arenaname));
	}
	
	public boolean IsPlaying(Player p)
	{
		String PlayerArena = getArenaIn(p);
		
		if(plugin.SimonAM.getArena(PlayerArena) != null)
		{
			if(plugin.SimonGSM.arenagamestage.get(getArena(PlayerArena)).equalsIgnoreCase("SGAMESTAGE_INPROGRESS"))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean gameInProgress(String arenaname)
	{
		GameArena a = getArena(arenaname);
	    if(a == null)
	    {
	    	// Arena not found, return "game in progress" so it wont error.
	    	//plugin.SimonLog.logWarning("Attempted to join a null arena: " + arenaname);
	        return true;
	    }
		
	    String gamestage = plugin.SimonGSM.arenagamestage.get(getArena(arenaname));
	    
	    if(gamestage.equals("SGAMESTAGE_WAITINGPLAYERS"))
	    {
	    	return false;
	    }
	    else if(gamestage.equals("SGAMESTAGE_INPROGRESS"))
	    {
	    	return true;
	    }
	    
	    return true;
	}
	
	public String getArenaIn(Player p)
	{
		for(GameArena a : arenas)
		{
			if(a.getPlayers().contains(p.getName()))
				return a.getName();
		}
		
		return "none";
	}
	
	public String serializeLoc(Location l)
	{
		return l.getWorld().getName()+","+l.getBlockX()+","+l.getBlockY()+","+l.getBlockZ();
	}
	
	public Location deserializeLoc(String s)
	{
		String[] st = s.split(",");
		return new Location(Bukkit.getServer().getWorld(st[0]), Integer.parseInt(st[1]), Integer.parseInt(st[2]), Integer.parseInt(st[3]));
	}
	
	public String getLocWorld(String s)
	{	
		String[] st = s.split(",");
		
		return Bukkit.getServer().getWorld(st[0]).getName();
	}
}	
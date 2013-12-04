package net.edencampo.simonsays.ArenaManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.edencampo.simonsays.SimonSays;
import net.edencampo.simonsays.ArenaManagers.SimonArena.ARENA_STAGE;
import net.edencampo.simonsays.ArenaManagers.SimonArena.ARENA_TYPE;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public class SimonArenaManager
{
	SimonSays plugin;
	
	public SimonArenaManager(SimonSays instance)
	{
		plugin = instance;
	}
	
	
	public Map<String, Location> locs = new HashMap<String, Location>();

	Map<String, ItemStack[]> inv = new HashMap<String, ItemStack[]>();
	Map<String, ItemStack[]> armor = new HashMap<String, ItemStack[]>();

	public List<SimonArena> simonArenas = new ArrayList<SimonArena>();
	
	int arenaSize = 0;
	 
	public SimonArena getArena(String arenaName)
	{
	    for(SimonArena a : simonArenas)
	    {
	        if(a.getName().equalsIgnoreCase(arenaName))
	        {
	            return a;
	        }
	    }
	    return null;
	}
	
	public ARENA_TYPE getType(String arenaName)
	{
	    for(SimonArena a : simonArenas)
	    {
	        if(a.getName().equalsIgnoreCase(arenaName))
	        {
	            return a.arenaType;
	        }
	    }
	    
	    return null;
	}
	 
	public void addPlayer(Player p, String arenaname)
	{
		SimonArena a = getArena(arenaname);
	    if(a == null)
	    {
	        p.sendMessage(plugin.SimonTag + "Invalid arena!");
	        return;
	    }
	
	    a.getPlayers().add(p.getName());
	    inv.put(p.getName(), p.getInventory().getContents());
	    armor.put(p.getName(), p.getInventory().getArmorContents());
	    
	    p.getInventory().setArmorContents(null);
	    p.getInventory().clear();
	    
	    if(getArena(a.getName()).needsPlayers())
	    {
	    	a.arenaStage = ARENA_STAGE.SGAMESTAGE_WAITINGPLAYERS;
	    	
	    	p.sendMessage(plugin.SimonTag + "Successfully joined " + a.getName() + ", waiting for more players...");
	    }
	    else
	    {
	    	a.arenaStage = ARENA_STAGE.SGAMESTAGE_PREPERING;
	    	
	    	Bukkit.getServer().broadcastMessage(plugin.SimonTag + "About to start a game in arena '" + a.getName() + "' in 10 seconds!");
	    	plugin.SimonCD.countDown(a);
	    }
	    
	    p.teleport(a.getSpawn());
	}
	
	public void specPlayer(Player p, String arenaname)
	{
		SimonArena a = getArena(arenaname);
		
	    if(a == null || a.arenaType != ARENA_TYPE.TYPE_SPEC)
	    {
	        p.sendMessage(plugin.SimonTag + "Invalid Spectator arena " + arenaname + "!");
	        return;
	    }
	
	    a.getPlayers().add(p.getName());
	    
	    p.teleport(a.arenaSpawn);
	}
	   

	public void removePlayer(Player p)
	{
		SimonArena a = null;
	    for(SimonArena arena : simonArenas)
	    {
	        if(arena.getPlayers().contains(p.getName()))
	        {
	            a = arena;
	        }
	    }
	    if(a == null || !a.getPlayers().contains(p.getName()))
	    {
	        p.sendMessage(plugin.SimonTag + "Invalid operation!");
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
		   Bukkit.broadcastMessage(plugin.SimonTag + "" + p.getName() + " from arena '" + a.getName() + "' won his game!");
		   
		   a.arenaStage = ARENA_STAGE.SGAMESTAGE_WAITINGPLAYERS;
	   }
	   
	    Objective arenaob = getArena(a.getName()).getScoreboard().getObjective(a.getName());
	    Score players = arenaob.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_AQUA + "Players: "));
	    	
	    players.setScore(getArena(a.getName()).getPlayers().size());
	    	
	    plugin.SimonScore.removePlayerBoard(p);
	}

	public void createArena(Location loc, String arenaname)
	{
	   arenaSize++;
		
	   SimonArena a = new SimonArena(loc, arenaname, plugin);
	   simonArenas.add(a);
	}
	
	public void removeArena(String arenaname)
	{	
		simonArenas.remove(getArena(arenaname));
	}
	
	public boolean IsPlaying(Player p)
	{
		String PlayerArena = getArenaIn(p);
		
		if(getArena(PlayerArena) != null)
		{
			if(getArena(PlayerArena).arenaStage == ARENA_STAGE.SGAMESTAGE_INPROGRESS)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean gameInProgress(String arenaName)
	{
		SimonArena a = getArena(arenaName);
	    if(a == null)
	    {
	    	// Arena not found, return "game in progress" so it wont error.
	    	//plugin.SimonLog.logWarning("Attempted to join a null arena: " + arenaname);
	        return true;
	    }
		
	    ARENA_STAGE arenaStage = getArena(arenaName).arenaStage;
	    
	    if(arenaStage == ARENA_STAGE.SGAMESTAGE_WAITINGPLAYERS)
	    {
	    	return false;
	    }
	    else if(arenaStage == ARENA_STAGE.SGAMESTAGE_INPROGRESS)
	    {
	    	return true;
	    }
	    
	    return true;
	}
	
	public String getArenaIn(Player p)
	{
		for(SimonArena a : simonArenas)
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
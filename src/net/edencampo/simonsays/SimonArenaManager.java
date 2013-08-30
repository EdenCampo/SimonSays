package net.edencampo.simonsays;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SimonArenaManager
{
	
	public Map<String, Location> locs = new HashMap<String, Location>();

	public static SimonArenaManager a = new SimonArenaManager();

	Map<String, ItemStack[]> inv = new HashMap<String, ItemStack[]>();
	Map<String, ItemStack[]> armor = new HashMap<String, ItemStack[]>();

	List<Arena> arenas = new ArrayList<Arena>();
	int arenaSize = 0;
	
	private String SimonTag = ChatColor.BLACK + "[" + ChatColor.GREEN + "SimonSays" + ChatColor.BLACK + "]" + " " + ChatColor.WHITE;
	
	SimonSays plugin;
	public SimonArenaManager(SimonSays instance)
	{
		this.plugin = instance;
	}
	
	public SimonArenaManager()
	{
		
	}

	public static SimonArenaManager getManager()
	{
	    return a;
	}
	 
	public Arena getArena(int i){
	    for(Arena a : arenas){
	        if(a.getId() == i){
	            return a;
	        }
	    }
	    return null;
	}
	 
	public void addPlayer(Player p, int i)
	{
	    Arena a = getArena(i);
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
	    
	    p.teleport(a.spawn);
	    //GameListener.add(p);
	}
	   

	public void removePlayer(Player p)
	{
	    Arena a = null;
	    for(Arena arena : arenas)
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
	   //p.teleport(locs.get(p.getName()));
	   //locs.remove(p.getName());	
	}

	public void createArena(Location l)
	{
	   int num = arenaSize + 1;
	   arenaSize++;
	 
	   Arena a = new Arena(l, num);
	   arenas.add(a);
	   
	   /*
	   plugin.getConfig().set("Arenas" + num, serializeLoc(l));
	   List<Integer> list = plugin.getConfig().getIntegerList("Arenas.Arenas");
	   list.add(num);
	   plugin.getConfig().set("Arenas.Arenas", list);
	   plugin.saveConfig();
	   */
	}
	
	public void removeArena(int i)
	{
		/*
		for(String s : getArena(i).getPlayers())
		{
			Player p = Bukkit.getPlayerExact(s);
			if(p != null)
			{
				p.teleport(locs.get(p.getName()));
				locs.remove(p.getName());
			}
		}
		*/
		
		arenas.remove(getArena(i));
		
		/*
		List<Integer> list = plugin.getConfig().getIntegerList("Arenas.Arenas");
		list.remove(i);
		plugin.getConfig().set("Arenas.Arenas", list);
		plugin.saveConfig();
		*/
	}
	
	public boolean IsPlaying(Player p)
	{
		for(Arena a : arenas)
		{
			if(a.getPlayers().contains(p.getName()))
				return true;
		}
		return false;
	}
	
	public void loadGames()
	{
		if(plugin.getConfig().getIntegerList("Arenas.Arenas").isEmpty())
		{
			return;
		}
		
		for(int i : plugin.getConfig().getIntegerList("Arenas.Arenas"))
		{
			Arena a = new Arena(deserializeLoc(plugin.getConfig().getString("Arenas" + i)), i);
			arenas.add(a);
		}
	}
	public String serializeLoc(Location l)
	{
		return l.getWorld().getName()+","+l.getBlockX()+","+l.getBlockY()+","+l.getBlockZ();
	}
	
	public Location deserializeLoc(String s)
	{
		String[] st = s.split(",");
		return new Location(Bukkit.getWorld(st[1]), Integer.parseInt(st[1]), Integer.parseInt(st[2]), Integer.parseInt(st[3]));
	}
}	
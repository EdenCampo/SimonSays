package net.edencampo.simonsays;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SimonSpectateArenaManager
{
	public Map<String, Location> locs = new HashMap<String, Location>();

	public static SimonSpectateArenaManager a = new SimonSpectateArenaManager();

	List<SpectateArena> arenas = new ArrayList<SpectateArena>();
	int arenaSize = 0;
	
	private String SimonTag = ChatColor.BLACK + "[" + ChatColor.GREEN + "SimonSays" + ChatColor.BLACK + "]" + " " + ChatColor.WHITE;
	
	SimonSays plugin;
	public SimonSpectateArenaManager(SimonSays instance)
	{
		this.plugin = instance;
	}
	
	public SimonSpectateArenaManager()
	{
		
	}

	public static SimonSpectateArenaManager getSpecManager()
	{
	    return a;
	}
	 
	public SpectateArena getArena(int i){
	    for(SpectateArena a : arenas){
	        if(a.getId() == i){
	            return a;
	        }
	    }
	    return null;
	}
	 
	public void specPlayer(Player p, int i)
	{
		SpectateArena a = getArena(i);
	    if(a == null)
	    {
	        p.sendMessage(SimonTag + "Invalid arena!");
	        return;
	    }
	
	    a.getPlayers().add(p.getName());
	    
	    p.teleport(a.spawn);
	}

	public void createArena(Location l)
	{
	   int num = arenaSize + 1;
	   arenaSize++;
	 
	   SpectateArena a = new SpectateArena(l, num);
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
	
	/*
	public void loadSpecArenas()
	{
		if(plugin.getConfig().getIntegerList("Arenas.Arenas").isEmpty())
		{
			return;
		}
		
		for(int i : plugin.getConfig().getIntegerList("Arenas.Arenas"))
		{
			SpectateArena a = new SpectateArena(deserializeLoc(plugin.getConfig().getString("Arenas" + i)), i);
			arenas.add(a);
		}
	}
	*/
	
	public String serializeLoc(Location l)
	{
		return l.getWorld().getName()+","+l.getBlockX()+","+l.getBlockY()+","+l.getBlockZ();
	}
	
	public Location deserializeLoc(String s)
	{
		String[] st = s.split(",");
		return new Location(Bukkit.getWorld(st[0]), Integer.parseInt(st[1]), Integer.parseInt(st[2]), Integer.parseInt(st[3]));
	}
}

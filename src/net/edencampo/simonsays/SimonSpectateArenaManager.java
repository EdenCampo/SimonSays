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
	 
	public SpectateArena getArena(String arenaname){
	    for(SpectateArena a : arenas)
	    {
	        if(a.getName().equals(arenaname))
	        {
	            return a;
	        }
	    }
	    return null;
	}
	 
	public void specPlayer(Player p, String arenaname)
	{
		SpectateArena a = getArena(arenaname);
	    if(a == null)
	    {
	        p.sendMessage(SimonTag + "Invalid arena!");
	        return;
	    }
	
	    a.getPlayers().add(p.getName());
	    
	    p.teleport(a.spawn);
	}

	public void createArena(Location l, String arenaname)
	{
	   arenaSize++;
	 
	   SpectateArena a = new SpectateArena(l, arenaname);
	   arenas.add(a);
	   
	   /*
	   plugin.getConfig().set("Arenas" + num, serializeLoc(l));
	   List<Integer> list = plugin.getConfig().getIntegerList("Arenas.Arenas");
	   list.add(num);
	   plugin.getConfig().set("Arenas.Arenas", list);
	   plugin.saveConfig();
	   */
	}
	
	public void removeArena(String arenaname)
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
		
		arenas.remove(getArena(arenaname));
		
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

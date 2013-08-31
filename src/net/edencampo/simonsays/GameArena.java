package net.edencampo.simonsays;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

public class GameArena
{
	SimonSays plugin;
	
	public GameArena(SimonSays instance)
	{
		this.plugin = instance;
	}
	
	String arenaname = "";
	Location spawn = null;
	List<String> players = new ArrayList<String>();
	boolean inProgress = false;
	
	public GameArena(Location loc, String name)
	{
	  this.spawn = loc;
	  this.arenaname = name;
	}
	
	public String getName()
	{
	  return this.arenaname;
	}
	
	public List<String> getPlayers()
	{
	  return this.players;
	}
}

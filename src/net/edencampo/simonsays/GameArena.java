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
	
	int number = 0;
	Location spawn = null;
	List<String> players = new ArrayList<String>();
	boolean inProgress = false;
	
	public GameArena(Location loc, int id)
	{
	  this.spawn = loc;
	  this.number = id;
	}
	
	public int getId()
	{
	  return this.number;
	}
	
	public List<String> getPlayers()
	{
	  return this.players;
	}
}

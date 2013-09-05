package net.edencampo.simonsays;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Sign;

public class GameArena
{
	SimonSays plugin;
	
	public GameArena(SimonSays instance)
	{
		this.plugin = instance;
	}
	
	public String arenaname = "";
	public Sign arenasign = null;
	public Location spawn = null;
	public List<String> players = new ArrayList<String>();
	
	public GameArena(Location loc, String name)
	{
	  this.spawn = loc;
	  this.arenaname = name;
	}
	
	public String getName()
	{
	  return this.arenaname;
	}
	
	public Sign getSign()
	{
		return this.arenasign;
	}
	
	public void setSign(Sign sign)
	{
		arenasign = sign;
	}
	
	public List<String> getPlayers()
	{
	  return this.players;
	}

	public boolean needsPlayers() 
	{
		if(players.size() > 3)
		{
			return false;
		}
		
		return true;
	}
}

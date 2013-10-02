package net.edencampo.simonsays.ArenaManagers;

import java.util.ArrayList;
import java.util.List;

import net.edencampo.simonsays.SimonSays;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.scoreboard.Scoreboard;

public class GameArena
{
	SimonSays plugin;
	
	public Scoreboard scoreboard;
	public int countdown = 10;
	public String arenaname = "";
	public Sign arenasign = null;
	public Location spawn = null;
	public List<String> players = new ArrayList<String>();
	
	public GameArena(Location loc, String name, SimonSays instance)
	{
	  this.spawn = loc;
	  this.arenaname = name;
	  this.plugin = instance;
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
		String minimum = plugin.getConfig().getString("minimumPlayers");
		
		int min = Integer.parseInt(minimum);
		
		if(players.size() >= min)
		{
			return false;
		}
		
		return true;
	}
	
	public void setCountdown(int newcount)
	{
		countdown = newcount;
	}
	
	public int getCountdown()
	{
		return countdown;
	}

	public void setSimonBoard(Scoreboard simonboard) 
	{
		scoreboard = simonboard;
	}
	
	public Scoreboard getSimonBoard()
	{
		return scoreboard;
	}
}

package net.edencampo.simonsays;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

public class SpectateArena 
{
	  public String arenaname = "";
	  public Location spawn = null;
	  public List<String> players = new ArrayList<String>();
	
	  public SpectateArena(Location loc, String name)
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

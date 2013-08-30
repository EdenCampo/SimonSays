package net.edencampo.simonsays;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

public class SpectateArena 
{
	  int number = 0;
	  Location spawn = null;
	  List<String> players = new ArrayList<String>();
	
	  public SpectateArena(Location loc, int id)
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

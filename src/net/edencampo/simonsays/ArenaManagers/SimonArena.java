package net.edencampo.simonsays.ArenaManagers;

import java.util.ArrayList;
import java.util.List;

import net.edencampo.simonsays.SimonSays;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;

public class SimonArena implements Listener
{
	public static enum ARENA_TYPE
	{
		TYPE_NONE,
		TYPE_GAME,
		TYPE_SPEC
	}
	
	public static enum ARENA_STAGE
	{
		SGAMESTAGE_ERROR,
		SGAMESTAGE_WAITINGPLAYERS,
		SGAMESTAGE_PREPERING,
		SGAMESTAGE_INPROGRESS,
		SGAMESTAGE_ENDED
	}
	
	SimonSays plugin;
	
	public String arenaName;
	public int arenaID;
	public Scoreboard arenaScoreboard;
	public int arenaCountdown;
	public Sign arenaSign = null;
	public Location arenaSpawn;
	public ARENA_TYPE arenaType;
	public ARENA_STAGE arenaStage;
	public List<String> arenaPlayers = new ArrayList<String>();
	
	public SimonArena(String aName, int aID, Scoreboard aScore, int aCD, Sign aSign, Location aSpawn, ARENA_TYPE aType, ARENA_STAGE aStage, SimonSays instance)
	{
		arenaName = aName;
		arenaID = aID;
		arenaScoreboard = aScore;
		arenaCountdown = aCD;
		arenaSign = aSign;
		arenaSpawn = aSpawn;
		arenaType = aType;
		arenaStage = aStage;
	
		plugin = instance;
	}
	
	public String getName()
	{
		return arenaName;
	}
	
	public int getID()
	{
		return arenaID;
	}
	
	public Scoreboard getScoreboard()
	{
		return arenaScoreboard;
	}
	
	public int getCountdown()
	{
		return arenaCountdown;
	}
	
	public Sign getSign()
	{
		return arenaSign;
	}
	
	public Location getSpawn()
	{
		return arenaSpawn;
	}
	
	public ARENA_TYPE getType()
	{
		return arenaType;
	}
	
	public ARENA_STAGE getStage()
	{
		return arenaStage;
	}
	
	public List<String> getPlayers()
	{
		return arenaPlayers;
	}
	
	public boolean needsPlayers() 
	{
		String minimum = plugin.getConfig().getString("minimumPlayers");
		
		int min = Integer.parseInt(minimum);
		
		if(arenaPlayers.size() >= min)
		{
			return false;
		}
		
		return true;
	}
}

package net.edencampo.simonsays.GameplayManagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.edencampo.simonsays.SimonSays;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SimonGameManager implements Runnable
{
	SimonSays plugin;
	
	List<String> playerscompleted = new ArrayList<String>();
	List<String> playermesseged = new ArrayList<String>();
	
	HashMap<Player, Material> playerblockplace = new HashMap<Player, Material>();
	
	public SimonGameManager(SimonSays instance)
	{
		plugin = instance;
	}
	
	public List<String> getCompletedPlayers()
	{
		return this.playerscompleted;
	}
	
	public void SimonActionSetDone(Player p)
	{
		this.playerscompleted.add(p.getName());
	}
	
	public void SimonSetMsgSent(Player p)
	{
		this.playermesseged.add(p.getName());
	}
	
	public Boolean SimonActionCompleted(Player p)
	{
		if(this.playerscompleted.contains(p.getName()))
		{
			return true;
		}
		
		return false;
	}
	
	public Boolean SimonMsgSent(Player p)
	{
		if(this.playermesseged.contains(p.getName()))
		{
			return true;
		}
		
		return false;
	}
	
	public void setBlockToPlace(Player p, Material block)
	{
		this.playerblockplace.put(p, block);
	}

	public Material getBlockToPlace(Player p)
	{
		return this.playerblockplace.get(p);
	}
	
	@Override
	public void run() 
	{
		for(Player p : plugin.getServer().getOnlinePlayers())
		{
			String GameArena = plugin.SimonAM.getArenaIn(p);
			
			if(plugin.SimonAM.IsPlaying(p))
			{
				if(!SimonActionCompleted(p))
				{
					String RelatedArena = plugin.SimonArenasM.SQLGetRelatedGameArena(GameArena);
					
					plugin.SimonAM.removePlayer(p);
					plugin.SimonAM.specPlayer(p, RelatedArena);
					p.sendMessage(plugin.SimonTag + "Action not completed! Abandoned Game!");
					
					plugin.SimonLog.logDebug("Dropped player " + p.getName() + " from arena " + GameArena);
					plugin.SimonLog.logDebug("Found related arena " + RelatedArena + " to teleport the player to.");
				}
			}
		}
	}
}

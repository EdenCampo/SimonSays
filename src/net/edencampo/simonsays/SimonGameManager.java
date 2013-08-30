package net.edencampo.simonsays;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SimonGameManager implements Runnable
{
	SimonSays plugin;
	
	List<String> playerscompleted = new ArrayList<String>();
	
	private String SimonTag = ChatColor.BLACK + "[" + ChatColor.GREEN + "SimonSays" + ChatColor.BLACK + "]" + " " + ChatColor.WHITE;
	
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
	
	public Boolean SimonActionCompleted(Player p)
	{
		if(this.playerscompleted.contains(p.getName()))
		{
			return true;
		}
		
		return false;
	}

	@Override
	public void run() 
	{
		for(Player p : plugin.getServer().getOnlinePlayers())
		{
			if(SimonArenaManager.getManager().IsPlaying(p))
			{
				if(!SimonActionCompleted(p))
				{
					SimonArenaManager.getManager().removePlayer(p);
					p.sendMessage(SimonTag + "Action not completed! Abandoned Game!");
				}
			}
		}
	}
}

package net.edencampo.simonsays.GameplayManagers;

import net.edencampo.simonsays.SimonSays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SimonCountdown implements Runnable 
{
	SimonSays plugin;
	
	public SimonCountdown(SimonSays instance)
	{
		this.plugin = instance;
	}
	
	String GameArena = "";
	
	int second = 10;
	int task;
	
	public void CountDown(String ArenaName)
	{
		GameArena = ArenaName;
		
		task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 20L);
	}

	@Override
	public void run() 
	{
		if(second < 0)
		{
			second = 10;
			Bukkit.getServer().getScheduler().cancelTask(task);
			return;
		}
			
		for(Player p : plugin.getServer().getOnlinePlayers())
		{		
			String PlayerArena = plugin.SimonAM.getArenaIn(p);
				
			if(PlayerArena.equalsIgnoreCase(GameArena))
			{	
				
				if(second != 0)
				{
					p.sendMessage(plugin.SimonTag + ChatColor.BOLD + "" + second);
					
					plugin.SimonLog.logDebug("Waiting... Arena " + GameArena + " second:" + second);
				}
				
				switch(second)
				{
					case 0:
					{
						p.sendMessage(plugin.SimonTag + ChatColor.DARK_AQUA + "GO!");
						plugin.SimonGSM.arenagamestage.put(plugin.SimonAM.getArena(GameArena), "SGAMESTAGE_INPROGRESS");
						p.teleport(plugin.SimonAM.getArena(GameArena).spawn);
						plugin.SimonScore.createSimonBoard(GameArena);
						
						plugin.SimonLog.logDebug("Successfully started a game at arena " + GameArena);
					}
				}
			}
		}
			
		second--;
	}
}

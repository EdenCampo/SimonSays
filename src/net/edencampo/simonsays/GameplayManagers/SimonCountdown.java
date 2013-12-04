package net.edencampo.simonsays.GameplayManagers;

import net.edencampo.simonsays.SimonSays;
import net.edencampo.simonsays.ArenaManagers.SimonArena;
import net.edencampo.simonsays.ArenaManagers.SimonArena.ARENA_STAGE;

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
	
	SimonArena simonArena = null;
	
	int second = 10;
	int task;
	
	public void countDown(SimonArena sArena)
	{
		simonArena = sArena;
		
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
				
			if(PlayerArena.equalsIgnoreCase(simonArena.arenaName))
			{	
				if(second != 0)
				{
					p.sendMessage(plugin.SimonTag + ChatColor.BOLD + "" + second);
					
					plugin.SimonLog.logDebug("Waiting... Arena " + simonArena + " second:" + second);
				}
				
				switch(second)
				{
					case 0:
					{
						p.sendMessage(plugin.SimonTag + ChatColor.DARK_AQUA + "GO!");
						simonArena.arenaStage = ARENA_STAGE.SGAMESTAGE_INPROGRESS;
						p.teleport(simonArena.arenaSpawn);
						plugin.SimonScore.createSimonBoard(simonArena.arenaName);
						
						plugin.SimonLog.logDebug("Successfully started a game at arena " + simonArena);
					}
				}
			}
		}
			
		second--;
	}
}

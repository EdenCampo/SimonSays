package net.edencampo.simonsays.GameplayManagers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import net.edencampo.simonsays.SimonSays;

public class SimonScoreManager
{
	SimonSays plugin;
	
	public SimonScoreManager(SimonSays instance)
	{
		plugin = instance;
	}
	
	ScoreboardManager manager = Bukkit.getScoreboardManager();
	
	public void createSimonBoard(String Arena)
	{
		Scoreboard simonboard = manager.getNewScoreboard();
		
		Objective objective = simonboard.registerNewObjective(Arena, "dummy");
		
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		objective.setDisplayName("SimonArena: " + Arena);
		
		Score score = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_AQUA + "Players: "));
		score.setScore(plugin.SimonAM.getArena(Arena).getPlayers().size());
		
		for(Player p : Bukkit.getOnlinePlayers())
		{
			if(plugin.SimonAM.getArenaIn(p).equalsIgnoreCase(Arena))
			{
				p.setScoreboard(simonboard);
				
				Score score1 = objective.getScore(p);
				
				score1.setScore(0);
			}
		}
		
		plugin.SimonAM.getArena(Arena).setSimonBoard(simonboard);
		
		plugin.SimonLog.logDebug("Successfully created a 'SimonBoard' for arena " + Arena + "!");
	}
	
	public void removePlayerBoard(Player p)
	{
    	ScoreboardManager manager = Bukkit.getScoreboardManager();
    	
    	p.setScoreboard(manager.getNewScoreboard());
	}
	
}

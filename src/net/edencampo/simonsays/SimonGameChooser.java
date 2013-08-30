package net.edencampo.simonsays;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.edencampo.simonsays.SimonSays.SimonGame;

public class SimonGameChooser implements Runnable 
{	
	public SimonSays plugin;
	
	public SimonGameChooser(SimonSays instance)
	{
		this.plugin = instance;
	}
	
	private SimonGame result = SimonSays.SimonGame.SGAME_NONE;

	private String SimonTag = ChatColor.BLACK + "[" + ChatColor.GREEN + "SimonSays" + ChatColor.BLACK + "]" + " " + ChatColor.WHITE;
	
	@Override
	public void run() 
	{
		Random SimonRandomGame = new Random();
		
		int RandomNum = SimonRandomGame.nextInt(14);
		
		switch(RandomNum)
		{
			case 1:
			{
				result = SimonGame.SGAME_DONTMOVE;
				break;
			}
			case 2:
			{
				result = SimonGame.SGAME_SNEAK;
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, plugin.SimonSGM, 190L);
				break;
			}
			case 3:
			{
				result = SimonGame.SGAME_JUMP;
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, plugin.SimonSGM, 190L);
				break;
			}
			case 4:
			{
				result = SimonGame.SGAME_ATTACKPLAYER;
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, plugin.SimonSGM, 190L);
				break;
			}
			case 5:
			{
				result = SimonGame.SGAME_PUNCHBLOCK;
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, plugin.SimonSGM, 190L);
				break;
			}
			case 6:
			{
				result = SimonGame.SGAME_SPRINT;
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, plugin.SimonSGM, 190L);
				break;
			}
			case 7:
			{
				result = SimonGame.SGAME_WALK;
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, plugin.SimonSGM, 190L);
				break;
			}
			case 8:
			{
				result = SimonGame.SGAME_FAKEWALK;
				break;
			}
			case 9:
			{
				result = SimonGame.SGAME_FAKESPRINT;
				break;
			}
			case 10:
			{
				result = SimonGame.SGAME_FAKEPUNCHBLOCK;
				break;
			}
			case 11:
			{
				result = SimonGame.SGAME_FAKEATTACKPLAYER;
				break;
			}
			case 12:
			{
				result = SimonGame.SGAME_FAKEJUMP;
				break;
			}
			case 13:
			{
				result = SimonGame.SGAME_FAKESNEAK;
				break;
			}
			case 14:
			{
				result = SimonGame.SGAME_FAKEDONTMOVE;
			}
		}
		
		for(Player p : plugin.getServer().getOnlinePlayers())
		{
			if(SimonArenaManager.getManager().IsPlaying(p))
			{
				BroadCastGame(p);
				plugin.SimonSGM.playerscompleted.clear();
			}
		}
	}
	
	public void BroadCastGame(Player player)
	{
		switch(result)
		{
			case SGAME_NONE:
			{
				player.sendMessage(SimonTag + "Simon Says NONE!");
				break;
			}
			
			case SGAME_DONTMOVE:
			{
				player.sendMessage(SimonTag + "Simon Says DONT MOVE!");
				break;
			}
			
			case SGAME_FAKEDONTMOVE:
			{
				player.sendMessage(SimonTag + "DONT MOVE!");
				break;
			}
			
			case SGAME_SNEAK:
			{
				player.sendMessage(SimonTag + "Simon Says SNEAK!");
				break;
			}
			
			case SGAME_FAKESNEAK:
			{
				player.sendMessage(SimonTag + "SNEAK!");
				break;
			}
			
			case SGAME_JUMP:
			{
				player.sendMessage(SimonTag + "Simon Says JUMP!");
				break;
			}
			
			case SGAME_FAKEJUMP:
			{
				player.sendMessage(SimonTag + "JUMP!");
				break;
			}
			
			case SGAME_ATTACKPLAYER:
			{
				player.sendMessage(SimonTag + "Simon Says Attack a Player!");
				break;
			}
			
			case SGAME_FAKEATTACKPLAYER:
			{
				player.sendMessage(SimonTag + "Attack a Player!");
				break;
			}
			
			case SGAME_PUNCHBLOCK:
			{
				player.sendMessage(SimonTag + "Simon Says Punch a BLOCK!");
				break;
			}
			
			case SGAME_FAKEPUNCHBLOCK:
			{
				player.sendMessage(SimonTag + "Punch a BLOCK!");
				break;
			}
			
			case SGAME_SPRINT:
			{
				player.sendMessage(SimonTag + "Simon Says SPRINT!");
				break;
			}
			
			case SGAME_FAKESPRINT:
			{
				player.sendMessage(SimonTag + "SPRINT!");
				break;
			}
			
			case SGAME_WALK:
			{
				player.sendMessage(SimonTag + "Simon Says WALK!");
				break;
			}
			
			case SGAME_FAKEWALK:
			{
				player.sendMessage(SimonTag + "WALK!");
			}
		}
	}
	
	public SimonGame GetGame()
	{	
		return result;
	}
}

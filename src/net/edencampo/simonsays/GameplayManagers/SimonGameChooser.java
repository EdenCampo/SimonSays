package net.edencampo.simonsays.GameplayManagers;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.edencampo.simonsays.SimonSays;
import net.edencampo.simonsays.SimonSays.SimonGame;

public class SimonGameChooser implements Runnable 
{	
	public SimonSays plugin;
	
	public SimonGameChooser(SimonSays instance)
	{
		this.plugin = instance;
	}
	
	private SimonGame result = SimonSays.SimonGame.SGAME_NONE;
	
	@Override
	public void run() 
	{
		Random SimonRandomGame = new Random();
		
		int RandomNum = SimonRandomGame.nextInt(16);
		
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
				break;
			}
			case 3:
			{
				result = SimonGame.SGAME_JUMP;
				break;
			}
			case 4:
			{
				result = SimonGame.SGAME_ATTACKPLAYER;
				break;
			}
			case 5:
			{
				result = SimonGame.SGAME_PUNCHBLOCK;
				break;
			}
			case 6:
			{
				result = SimonGame.SGAME_SPRINT;
				break;
			}
			case 7:
			{
				result = SimonGame.SGAME_WALK;
				break;
			}
			case 8:
			{
				result = SimonGame.SGAME_PLACEBLOCK;
				break;
			}
			case 9:
			{
				result = SimonGame.SGAME_FAKEWALK;
				break;
			}
			case 10:
			{
				result = SimonGame.SGAME_FAKESPRINT;
				break;
			}
			case 11:
			{
				result = SimonGame.SGAME_FAKEPUNCHBLOCK;
				break;
			}
			case 12:
			{
				result = SimonGame.SGAME_FAKEATTACKPLAYER;
				break;
			}
			case 13:
			{
				result = SimonGame.SGAME_FAKEJUMP;
				break;
			}
			case 14:
			{
				result = SimonGame.SGAME_FAKESNEAK;
				break;
			}
			case 15:
			{
				result = SimonGame.SGAME_FAKEDONTMOVE;
				break;
			}
			case 16:
			{
				result = SimonGame.SGAME_FAKEPLACEBLOCK;
			}
		}
		
		for(Player p : plugin.getServer().getOnlinePlayers())
		{
			if(plugin.SimonAM.IsPlaying(p))
			{
				plugin.SimonSGM.playerscompleted.clear();
				plugin.SimonSGM.playermesseged.clear();
				BroadCastGame(p);
			}
		}
	}
	
	public void BroadCastGame(Player player)
	{
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, plugin.SimonSGM, 115L);
		
		switch(result)
		{
			case SGAME_NONE:
			{
				player.sendMessage(plugin.SimonTag + "Simon Says NONE! (Woops? Error.)");
				break;
			}
			
			case SGAME_DONTMOVE:
			{
				player.sendMessage(plugin.SimonTag + "Simon Says DONT MOVE!");
				plugin.SimonSGM.SimonActionSetDone(player);
				break;
			}
			
			case SGAME_FAKEDONTMOVE:
			{
				player.sendMessage(plugin.SimonTag + "DONT MOVE!");
				plugin.SimonSGM.SimonActionSetDone(player);
				break;
			}
			
			case SGAME_SNEAK:
			{
				player.sendMessage(plugin.SimonTag + "Simon Says SNEAK!");
				break;
			}
			
			case SGAME_FAKESNEAK:
			{
				player.sendMessage(plugin.SimonTag + "SNEAK!");
				plugin.SimonSGM.SimonActionSetDone(player);
				break;
			}
			
			case SGAME_JUMP:
			{
				player.sendMessage(plugin.SimonTag + "Simon Says JUMP!");
				break;
			}
			
			case SGAME_FAKEJUMP:
			{
				player.sendMessage(plugin.SimonTag + "JUMP!");
				plugin.SimonSGM.SimonActionSetDone(player);
				break;
			}
			
			case SGAME_ATTACKPLAYER:
			{
				player.sendMessage(plugin.SimonTag + "Simon Says Attack a Player!");
				break;
			}
			
			case SGAME_FAKEATTACKPLAYER:
			{
				player.sendMessage(plugin.SimonTag + "Attack a Player!");
				plugin.SimonSGM.SimonActionSetDone(player);
				break;
			}
			
			case SGAME_PUNCHBLOCK:
			{
				player.sendMessage(plugin.SimonTag + "Simon Says Punch a BLOCK!");
				break;
			}
			
			case SGAME_FAKEPUNCHBLOCK:
			{
				player.sendMessage(plugin.SimonTag + "Punch a BLOCK!");
				plugin.SimonSGM.SimonActionSetDone(player);
				break;
			}
			
			case SGAME_SPRINT:
			{
				player.sendMessage(plugin.SimonTag + "Simon Says SPRINT!");
				break;
			}
			
			case SGAME_FAKESPRINT:
			{
				player.sendMessage(plugin.SimonTag + "SPRINT!");
				plugin.SimonSGM.SimonActionSetDone(player);
				break;
			}
			
			case SGAME_WALK:
			{
				player.sendMessage(plugin.SimonTag + "Simon Says WALK!");
				break;
			}
			
			case SGAME_FAKEWALK:
			{
				player.sendMessage(plugin.SimonTag + "WALK!");
				plugin.SimonSGM.SimonActionSetDone(player);
				break;
			}
			
			case SGAME_PLACEBLOCK:
			{
				@SuppressWarnings("deprecation")
				Material randomblock = player.getTargetBlock(null, 5).getType();
				
				if(randomblock != null)
				{
					if(randomblock.equals(Material.AIR))
					{
						randomblock = Material.SPONGE;
					}
					
					player.sendMessage(plugin.SimonTag + "Simon Says PLACE A " + randomblock.toString() + "!");
					
					player.getInventory().clear();
					player.setItemInHand(new ItemStack(randomblock, 1, (short) 1));
					plugin.SimonSGM.setBlockToPlace(player, randomblock);
				}
				
				break;
			}
			
			case SGAME_FAKEPLACEBLOCK:
			{	
				@SuppressWarnings("deprecation")
				Material randomblock = player.getTargetBlock(null, 5).getType();
				
				if(randomblock != null)
				{
					if(randomblock.equals(Material.AIR))
					{
						randomblock = Material.SPONGE;
					}
					
					player.sendMessage(plugin.SimonTag + "PLACE A " + randomblock.toString() + "!");
					
					player.getInventory().clear();
					player.setItemInHand(new ItemStack(randomblock, 1, (short) 1));
					plugin.SimonSGM.setBlockToPlace(player, randomblock);
				}
				
				plugin.SimonSGM.SimonActionSetDone(player);
			}
		}
	}
	
	public SimonGame GetGame()
	{	
		return result;
	}
}

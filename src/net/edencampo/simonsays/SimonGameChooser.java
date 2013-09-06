package net.edencampo.simonsays;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
				player.sendMessage(SimonTag + "Simon Says NONE! [Report the ERROR]");
				break;
			}
			
			case SGAME_DONTMOVE:
			{
				player.sendMessage(SimonTag + "Simon Says DONT MOVE!");
				plugin.SimonSGM.SimonActionSetDone(player);
				break;
			}
			
			case SGAME_FAKEDONTMOVE:
			{
				player.sendMessage(SimonTag + "DONT MOVE!");
				plugin.SimonSGM.SimonActionSetDone(player);
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
				plugin.SimonSGM.SimonActionSetDone(player);
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
				plugin.SimonSGM.SimonActionSetDone(player);
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
				plugin.SimonSGM.SimonActionSetDone(player);
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
				plugin.SimonSGM.SimonActionSetDone(player);
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
				plugin.SimonSGM.SimonActionSetDone(player);
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
				plugin.SimonSGM.SimonActionSetDone(player);
				break;
			}
			
			case SGAME_PLACEBLOCK:
			{
				//Random RandomBlock = new Random(123456789);
				
				//int x = RandomBlock.nextInt(16);
				//int y = RandomBlock.nextInt(40);
				//int z = RandomBlock.nextInt(16);
				
				//Material randomblock = player.getWorld().getBlockAt(x, y, z).getType();
				
				//String blocklist = plugin.getConfig().getString("allowedBlockIDs");
				
				//String[] splitids = blocklist.split(" | ");
				
				//Random RandomBlock = new Random();
				//int BlockPos = RandomBlock.nextInt(splitids.length);
				
				//String RandomBlockName = splitids[BlockPos].toUpperCase();
				
				Material randomblock = player.getTargetBlock(null, 5).getType();
				
				if(randomblock != null)
				{
					if(randomblock.equals(Material.AIR))
					{
						randomblock = Material.SPONGE;
					}
					
					player.sendMessage(SimonTag + "Simon Says PLACE A " + randomblock.toString() + "!");
					
					player.setItemInHand(new ItemStack(randomblock, 1, (short) 1));
					plugin.SimonSGM.setBlockToPlace(player, randomblock);
				}
				
				break;
			}
			
			case SGAME_FAKEPLACEBLOCK:
			{	
				Material randomblock = player.getTargetBlock(null, 5).getType();
				
				if(randomblock != null)
				{
					if(randomblock.equals(Material.AIR))
					{
						randomblock = Material.SPONGE;
					}
					
					player.sendMessage(SimonTag + "PLACE A " + randomblock.toString() + "!");
					
					
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

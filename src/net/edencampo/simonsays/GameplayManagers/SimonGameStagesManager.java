package net.edencampo.simonsays.GameplayManagers;

import net.edencampo.simonsays.SimonSays;
import net.edencampo.simonsays.ArenaManagers.SimonArena;
import net.edencampo.simonsays.ArenaManagers.SimonArena.ARENA_STAGE;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.event.Listener;

public class SimonGameStagesManager implements Listener, Runnable 
{
    SimonSays plugin;
    
    public SimonGameStagesManager(SimonSays instance)
    {
        this.plugin = instance;
    }

    @Override
    public void run() 
    {
		for(SimonArena a : plugin.SimonAM.simonArenas)
		{
			String name = a.getName();
			
			UpdateSign(name, a.getSign());
		}
    }
    
    public void UpdateSign(String arenaname, Sign sign)
    {
    	if(sign == null)
    	{
    		return;
    	}
        
        sign.setLine(0, ChatColor.GREEN + "[SimonSays]");
        sign.setLine(1, arenaname);
        		
   		if(plugin.SimonAM.getArena(arenaname) == null)
   		{
   			sign.setLine(2, ChatColor.RED + "Invalid Arena");
   			sign.setLine(3, ChatColor.RED + "SIGN SHUTDOWN..");
   			return;
   		}
   				
   		sign.setLine(2, plugin.SimonAM.getArena(arenaname).getPlayers().size() + "/10 players");
   		
   		ARENA_STAGE arenaStage = plugin.SimonAM.getArena(arenaname).arenaStage;
   		
   		switch(arenaStage)
   		{
   			case SGAMESTAGE_ERROR:
   			{
   				sign.setLine(3, ChatColor.DARK_AQUA + "Sign Error..");
   				break;
   			}
   			
   			case SGAMESTAGE_WAITINGPLAYERS:
   			{
   				sign.setLine(3, ChatColor.DARK_AQUA + "Waiting..");
   				break;
   			}
   			
   			case SGAMESTAGE_PREPERING:
   			{
   				sign.setLine(3, ChatColor.DARK_AQUA + "Starting..");
   				break;
   			}
   			
   			case SGAMESTAGE_INPROGRESS:
   			{
   				sign.setLine(3, ChatColor.DARK_AQUA + "In Progress..");
   				break;
   			}
   			
   			case SGAMESTAGE_ENDED:
   			{
   				sign.setLine(3, ChatColor.DARK_AQUA + "Restarting...");
   				break;
   			}
   		}	
   		sign.update(true);
    }
}

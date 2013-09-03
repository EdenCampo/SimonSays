package net.edencampo.simonsays;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.event.Listener;
import java.util.HashMap;

public class SimonGameStagesManager implements Listener, Runnable 
{
	public enum SimonGameStage
	{
		SGAMESTAGE_ERROR,
		SGAMESTAGE_WAITINGPLAYERS,
		SGAMESTAGE_INPROGRESS,
		SGAMESTAGE_ENDED
	}
	
	HashMap<GameArena,String> arenagamestage = new HashMap<GameArena, String>();
    
    SimonSays plugin;
    
    public SimonGameStagesManager(SimonSays instance)
    {
        this.plugin = instance;
    }

    @Override
    public void run() 
    {
		for(GameArena a : SimonGameArenaManager.getGameManager().arenas)
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
        
        if(!SimonGameArenaManager.getGameManager().getArena(arenaname).needsPlayers())
        {
        	arenagamestage.put(SimonGameArenaManager.getGameManager().getArena(arenaname), SimonGameStage.SGAMESTAGE_INPROGRESS.toString());
        }
        else
        {
        	arenagamestage.put(SimonGameArenaManager.getGameManager().getArena(arenaname), SimonGameStage.SGAMESTAGE_WAITINGPLAYERS.toString());
        }
        
        sign.setLine(0, ChatColor.GREEN + "[SimonSays]");
        sign.setLine(1, arenaname);
        		
   		if(SimonGameArenaManager.getGameManager().getArena(arenaname) == null)
   		{
   			sign.setLine(2, ChatColor.RED + "Invalid Arena");
   			sign.setLine(3, ChatColor.RED + "SIGN SHUTDOWN..");
   			return;
   		}
   				
   		sign.setLine(2, SimonGameArenaManager.getGameManager().getArena(arenaname).getPlayers().size() + "/10 players");
   			
   		String gamestage = arenagamestage.get(SimonGameArenaManager.getGameManager().getArena(arenaname));

   		if(gamestage.equals("SGAMESTAGE_ERROR"))
   		{
   			sign.setLine(3, ChatColor.DARK_AQUA + "SIGN SHUTDOWN..");
   		}
   		else if(gamestage.equals("SGAMESTAGE_WAITINGPLAYERS"))
   		{
   			sign.setLine(3, ChatColor.DARK_AQUA + "Waiting..");
   		}
   		else if(gamestage.equals("SGAMESTAGE_INPROGRESS"))
   		{
   			sign.setLine(3, ChatColor.DARK_AQUA + "In Progress..");
   		}
   		else if(gamestage.equals("SGAMESTAGE_ENDED"))
   		{
   			sign.setLine(3, ChatColor.DARK_AQUA + "Restarting...");
   		}
   				
   		sign.update(true);
    }
}

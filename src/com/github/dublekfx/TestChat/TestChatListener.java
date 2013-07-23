package com.github.dublekfx.TestChat;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

import com.github.dublekfx.TestChat.Channel.Channel;
import com.github.dublekfx.TestChat.Channel.ChannelManager;

public class TestChatListener implements Listener	{
	@EventHandler
	public void onPlayerJoin (PlayerJoinEvent event)	{
		//Theoretically, each channel db knows all players listening, even those who are offline.
		//So as long as the channel has a list of players, the User doesn't need to know what channels it's listening to.
		
		//if (pg.SELECT*FROMPlayerDataWHEREplayerName=event.getPlayer().getName() == null)
		User.addPlayer(event.getPlayer());
		User.getUser(event.getPlayer().getName()).setCurrent(TestChat.getInstance().getChannelManager().getChannel("#"));
		//else
		//User.login(event.getPlayer());
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPlayerChat (AsyncPlayerChatEvent event)	{
		Logger.getLogger("Minecraft").info("onPlayerChat");
		if(User.getUser(event.getPlayer().getName()) != null)	{
			event.setCancelled(true);
			Logger.getLogger("Minecraft").info("event cancelled");
			if(event.getMessage().indexOf("/") == 0)	{
				event.getPlayer().performCommand(event.getMessage().substring(1));
			}
			else	{
			User.getUser(event.getPlayer().getName()).chat(event);
			}
		}
	}
	
	@EventHandler
	public void onPlayerQuit (PlayerQuitEvent event)	{
		User.logout(event.getPlayer());
	}
	
	/*@EventHandler
	public void onPlayerTagEvent(PlayerReceiveNameTagEvent event)	{
		Player p = event.getNamedPlayer();
		
		if (p.hasPermission("group.horrorterror"))	{
			event.setTag(ColorDef.RANK_ADMIN + p.getName());
		}
		else if (p.hasPermission("group.denizen"))	{
			event.setTag(ColorDef.RANK_MOD + p.getName());
		}
		else if (p.hasPermission("group.helper"))	{
			event.setTag(ColorDef.RANK_HELPER + p.getName());
		}
		else if (p.hasPermission("group.godtier"))	{
			event.setTag(ColorDef.RANK_GODTIER + p.getName());
		}
		else if (p.hasPermission("group.donator"))	{
			event.setTag(ColorDef.RANK_DONATOR + p.getName());
		}
		else if (p.hasPermission("group.hero"))	{
			event.setTag(ColorDef.RANK_HERO + p.getName());
		}
		
	}*/
}

package com.github.dublekfx.TestChat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.dublekfx.TestChat.Channel.ChannelManager;

public class TestChat extends JavaPlugin{

	private static TestChat instance;
	private ChannelManager cm = new ChannelManager();
	private UserManager um = new UserManager();
	private TestChatListener listener = new TestChatListener();
	
	@Override
	public void onEnable()	{
		instance = this;
		this.getServer().getPluginManager().registerEvents(listener, this);
		//cm.loadAllChannels();
		this.cm.createDefaultChannel();
	}
	@Override
	public void onDisable()	{
		//cm.saveAllChannels();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)	{
		return false;
		
	}
	public ChannelManager getChannelManager()	{
		return cm;
	}
	public UserManager getUserManager()	{
		return um;
	}
	public static TestChat getInstance()
	{
		return instance;
	}

}
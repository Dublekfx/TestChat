package com.github.dublekfx.TestChat.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.github.dublekfx.TestChat.TestChat;
import com.github.dublekfx.TestChat.User;
import com.github.dublekfx.TestChat.UserManager;

public class ChannelManager {
	//Writing this part from scratch
	//Here be dragons...
	//wait maybe this all needs to be static. ADAMMM
	
	//Should be the interface between plugin and database. Do all loads and saves as infrequently as possible
	
	private Map<String, Channel> channelList = new HashMap<String, Channel>();
	
	public ChannelManager()	{
		//establish connection to db coughAdamcough
	}
	public void disconnectFromDatabase()	{
		//idk if this is even necessary, but i'll leave it here just in case
	}
	
	public void loadAllChannels()	{
		//SELECT * FROM ChatChannels
		//foreach entry
		//new Channel(column info goes here)
	}
	public void loadChannel(String channelName)	{
		//SELECT * FROM ChatChannels WHERE name=channelName
		//new Channel(you get the idea already)
	}
	public void saveAllChannels()	{
		//foreach channel
		//UPDATE blahblahblah		
	}
	public void saveChannel(String channelName)	{
		//do the savey thing, john!
	}
	
	public void createNewChannel(String name, AccessLevel sendingAccess, AccessLevel listeningAccess, String creator)	{
		//INSERT INTO ChatChannels
		//VALUES (values go here, from parameters)
		//channelList.put(name, channel);
		Channel c = new NormalChannel(name, sendingAccess, listeningAccess, creator);
		this.channelList.put(name, c);
		Logger.getLogger("Minecraft").info("Channel" + c.getName() + "created: " + sendingAccess + " " + listeningAccess + " " + creator);
	}
	public void createDefaultChannel()	{
		Channel c = new NormalChannel("#", AccessLevel.PUBLIC, AccessLevel.PUBLIC, "Dublek");
		Logger.getLogger("Minecraft").info("Default chat channel created");
		this.channelList.put("#", c);
		for(User u : TestChat.getInstance().getUserManager().getUserList().values())	{
			u.addListening(c);
		}
	}
	public void dropChannel(String channelname)	{
		this.channelList.remove(channelname);
		//DROP row?
	}
	public Map<String, Channel> getChannelList()	{
		return channelList;
	}
	public Channel getChannel(String channelname)	{
		return channelList.get(channelname);
	}
	public boolean isValidChannel(String channelname)	{
		return channelList.containsValue(channelname);
	}
	
	//...fuck. did I just write channelmanager in the usermanager? GODDAMNIT! 1 JOB
	//fixed!
}

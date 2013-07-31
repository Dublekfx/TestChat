package com.github.dublekfx.TestChat;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.github.dublekfx.TestChat.Channel.Channel;

public class User {

	private String pname;
	private String classType;
	private String aspect;
	private String mPlanet;
	private String dPlanet;
	private short towerNum;
	private boolean sleepState;
	private String current;
	private List<String> listening = new ArrayList<String>();
	private boolean globalMute;
	private boolean isOnline;
	private String globalNick;
	//also data for lastlogin and timeplayed
	private String userIP;

	public User(String player)	{
		this.pname = player;
		Logger.getLogger("Minecraft").info("User created: " + this.getPlayerName());
	}
	public void newUser(User u)	{
		this.classType = "Heir";
		this.aspect = "Breath";
		this.mPlanet = "LOWAS";
		this.dPlanet = "Prospit";
		this.towerNum = (short) ((int)(8 * Math.random()));
		this.sleepState = this.getPlayer().isSleeping();
		this.current = "#";			//Eventually this will point to a region channel
		this.listening.add("#");
		this.globalMute = false;
		this.isOnline = this.getPlayer().isOnline();
		this.globalNick = this.pname;
		this.userIP = this.getUserIP();
	}
	
	public static User getUser (String name)	{
		return TestChat.getInstance().getUserManager().getUserList().get(name);
	}
	public void login()	{
		this.setOnline(true);
	}
	public void logout()	{
		this.setOnline(false);
	}
	public String getPlayerName()	{
		return this.pname;
	}
	public Player getPlayer()	{
		return Bukkit.getPlayerExact(this.pname);
	}
	public String getNick()	{
		return globalNick;
	}

	public void setNick(String newNick)	{
		this.globalNick = newNick;
	}

	public String getClassType()	{
		return classType;
	}
	public void setClassType(String type)	{
		classType = type;
	}
	public String getAspect()	{
		return aspect;
	}
	public void setAspect(String asp)	{
		aspect = asp;
	}
	public String getMPlanet()	{
		return mPlanet;
	}
	public void setMPlanet(String mediumP)	{
		mPlanet = mediumP;
	}
	public String getDPlanet()	{
		return dPlanet;
	}
	public void setDPlanet(String dreamP)	{
		dPlanet = dreamP;
	}
	public short getTowerNum()	{
		return towerNum;
	}
	public void setTowerNum(short num)	{
		towerNum = num;
	}
	public boolean isSleeping()	{
		return sleepState;
	}
	public void setSleeping(boolean b)	{
		sleepState = b;
	}
	public String getUserIP()	{
		return userIP;
	}
	public void setUserIP()	{
		userIP = this.getPlayer().getAddress().getAddress().getHostAddress();
	}
	public void setMute(boolean b)	{
		if (b)	{
			this.sendMessage(ChatColor.RED + "You have been muted in all channels.");
		}
		else	{
			this.sendMessage(ChatColor.GREEN + "You have been unmuted in all channels.");
		}
	}

	public boolean isMute()	{
		return globalMute;
	}

	public void setOnline(boolean b)	{
		this.isOnline = b;
	}

	public boolean isOnline()	{
		return isOnline;
	}
	
	public void setCurrent(Channel c)	{
		if(c.isBanned(this))	{
			this.getPlayer().sendMessage(ChatColor.RED + "You are banned in channel " + ChatColor.GOLD + c.getName() + ChatColor.RED + "!");
			return;
		}
		this.current = c.getName();
	}
	public Channel getCurrent()	{
		return TestChat.getInstance().getChannelManager().getChannel(current);
	}

	public boolean addListening(Channel c)
	{
		if (c == null)	{
			return false;
		}
		if(c.isBanned(this))	{
			this.getPlayer().sendMessage(ChatColor.RED + "You are banned in channel " + ChatColor.GOLD + c.getName() + ChatColor.RED + "!");
			return false;
		}
		if (!this.listening.contains(c))	{
			if (c.userJoin(this))	{
				this.listening.add(c.getName());
				this.sendMessage(ChatColor.GREEN + "Now listening to channel " + ChatColor.GOLD + c.getName() + ChatColor.GREEN + ".");
				return true;
			}
		}
		else	{
			this.sendMessage(ChatColor.RED + "Already listening to channel " + ChatColor.GOLD + c.getName() + ChatColor.RED + "!");
		}
		return false;
	}

	public void removeListening(Channel c)	{
		if (this.listening.contains(c))		{
			if (!this.current.equals(c))	{
				c.userLeave(this);
				this.listening.remove(c);
				this.sendMessage(ChatColor.GREEN + "No longer listening to channel " + ChatColor.GOLD + c.getName() + ChatColor.GREEN + ".");
			}
			else	{
				this.sendMessage(ChatColor.RED + "Cannot leave your current channel " + ChatColor.GOLD + c.getName() + ChatColor.RED + "!");
			}
		}
		else	{
			this.sendMessage(ChatColor.RED + "Not listening to channel " + ChatColor.GOLD + c.getName() + ChatColor.RED + "!");
		}
	}

	public List<String> getListening()	{
		return listening;
	}

	//-----------------------------------------------------------------------------------------------------------------------
	
	public void chat (AsyncPlayerChatEvent event)	{	//receives message from SblockChatListener
		//determine channel. if message doesn't begin with @$channelname, then this.current
		//confirm destination channel
		
		//confirm user has perm to send to channel (channel.cansend()) and also muteness
		//output of channel, string
		
		User sender = User.getUser(event.getPlayer().getName());
		String fullmsg = event.getMessage();
		String outputmessage = fullmsg;
		Channel sendto = TestChat.getInstance().getChannelManager().getChannel(sender.current);
		
		if(fullmsg.indexOf("@") == 0)	{	//Check for alternate channel destination
			int space = fullmsg.indexOf(" ");
			String newChannel = fullmsg.substring(1, space);
			if(TestChat.getInstance().getChannelManager().isValidChannel(newChannel))	{
				sendto = TestChat.getInstance().getChannelManager().getChannel(newChannel);
				outputmessage = fullmsg.substring(space + 1); 
			}
		}
		if (sender.globalMute)	{
			sender.getPlayer().sendMessage(ChatColor.RED + "You are muted in channel " + ChatColor.GOLD + sendto.getName() + ChatColor.RED + "!");
			return;
		}
		switch (sendto.getSAcess())		{
		case PUBLIC:
			break;
		case PRIVATE:
			if (sendto.getApprovedUsers().contains(sender.pname))	{
				break;
			}
			else	{
				return;
			}
		}
		//Logger.getLogger("Minecraft").info(sender.getName() + " " + sendto.getName() + " " + outputmessage);
		this.formatMessage(sender, sendto, outputmessage);
	}
	public void formatMessage (User sender, Channel c, String s)	{
		//remember, [$channel]<$player> $message
		
		//perhaps call getOutputChannelF and getOutputNameF?
		//though I should def include a ColorDefinitons class -- DONE
		
		//check for a global nick, prolly only occurs if admin is being tricksty
		
		//next add or strip colors in message. based on perm
			//this part may change as I start working on other channeltypes
		//check for thirdperson # modifier and reformat appropriately
		//finally, channel.sendtochannel
		
		String output = "";
		//colorformatting
		
		boolean isThirdPerson = false;
		isThirdPerson = (s.indexOf("#") == 0) ? true : false;
		
		if (!isThirdPerson)	{
			output = output + this.getOutputChannelF(sender, c);
		}
		if(isThirdPerson)	{
			s = s.substring(1);
		}
		output = output + this.getOutputNameF(sender, isThirdPerson);
		output = output + s;
		sender.getPlayer().sendMessage(output);			//This bypass will remain as long as the stupid thing can't tell what it's listening to
		
		c.sendToAll(sender, output);
		
	}
	public void sendMessageFromChannel (String s, Channel c)	{	//final output, sends message to user
		//alert for if its player's name is applied here i.e. {!}
		//then just send it and be done!
		this.getPlayer().sendMessage(s);
	}
	
	//Here begins output formatting. Abandon all hope ye who enter
	
	public String getOutputChannelF(User sender, Channel channel)	{	//colors for [$channel] applied here
		//SburbChat code. Handle with care
		String out = "";
		
		ChatColor color = ColorDef.CHATRANK_MEMBER;
		if (channel.getOwner().equalsIgnoreCase(sender.getPlayerName()))	{
			color = ColorDef.CHATRANK_OWNER;
		}
		else if (channel.getModList().contains(sender.getPlayerName()))	{
			color = ColorDef.CHATRANK_MOD;
		}
		out = ChatColor.WHITE + "[" + color + channel.getName() + ChatColor.WHITE + "] ";
		//sender.getPlayer().sendMessage(out);
		return out;
	}
	
	public String getOutputNameF(User sender, boolean isThirdPerson)	{	//colors for <$name> applied here
		//	SburbChat code. Handle with care
		String out = "";
		
		String outputName = sender.pname;
		if(!(sender.globalNick.equals(sender.pname)))	{
			outputName = sender.globalNick;
		}
		
		ChatColor colorP = ColorDef.RANK_HERO;
		ChatColor colorW = ColorDef.DEFAULT;

		if (sender.getPlayer().hasPermission("group.horrorterror"))
			colorP = ColorDef.RANK_ADMIN;
		else if (sender.getPlayer().hasPermission("group.denizen"))
			colorP = ColorDef.RANK_MOD;
		else if (sender.getPlayer().hasPermission("group.helper"))
			colorP = ColorDef.RANK_HELPER;
		else if (sender.getPlayer().hasPermission("group.godtier"))
			colorP = ColorDef.RANK_GODTIER;
		else if (sender.getPlayer().hasPermission("group.donator"))
			colorP = ColorDef.RANK_DONATOR;
		
		if (sender.getPlayer().getWorld().getName().equalsIgnoreCase("earth"))
			colorW = ColorDef.WORLD_EARTH;
		else if (sender.getPlayer().getWorld().getName().equalsIgnoreCase("innercircle"))
			colorW = ColorDef.WORLD_INNERCIRCLE;
		else if (sender.getPlayer().getWorld().getName().equalsIgnoreCase("outercircle"))
			colorW = ColorDef.WORLD_OUTERCIRCLE;
		else if (sender.getPlayer().getWorld().getName().equalsIgnoreCase("medium"))
			colorW = ColorDef.WORLD_MEDIUM;
		else if (sender.getPlayer().getWorld().getName().equalsIgnoreCase("furthestring"))
			colorW = ColorDef.WORLD_FURTHESTRING;
		
		out = (isThirdPerson ? "> " : colorW + "<") + colorP + outputName + ChatColor.WHITE + (isThirdPerson ? ": " : colorW + "> " + ChatColor.WHITE);		
		//sender.getPlayer().sendMessage(out);
		return out;
	}

	public void sendMessage(String string) {
		//Not sure if I even need this...
	}
	public String toString()	{		//For /whois usage mainly
		String s = "";
		return s;
	}
}


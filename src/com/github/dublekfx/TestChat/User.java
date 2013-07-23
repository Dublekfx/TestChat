package com.github.dublekfx.TestChat;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.github.dublekfx.TestChat.Channel.Channel;

public class User {
	
	private Player pthis;
	private String pname;
	private PlayerRank prank;
	//private Class, aspect, mplanet, dplanet, towernum, sleepstate, sleeploc
	//also data for lastlogin and timeplayed
	private Channel current;
	private boolean globalMute;
	private boolean isOnline;
	private String globalNick;
	
	private Set<Channel> listening = new HashSet<Channel>();	//figure out how the hell to save this between runs
		//also, do I even want/need this?
		//yes. yes I do
	
	//public static Map<String, User> userList = new HashMap<String, User>(); //same for this. halp. plz
	//either mysql will have lists, or I'm gonna have to split a largeass text string
	//actually I'm moving this to the UserManager
		
	public User(Player p)	{
		this.pthis = p;
		this.globalNick = pthis.getName();
		this.globalMute = false;
		TestChat.getInstance().getUserManager().getUserList().put(pthis.getName(), this);
		//Channel?.joinChannelFirstTime(Region);
	}
	
	public static void addPlayer (Player p)	{ //Used for first-time logins
		User u = new User(p);
		Logger.getLogger("Minecraft").info("User created: " + u.getName());
	}
	public static void login(Player p)	{
		TestChat.getInstance().getUserManager().getUserList().get(p.getName()).toggleOnline();
		TestChat.getInstance().getUserManager().getUserList().get(p.getName()).current = TestChat.getInstance().getChannelManager().getChannel("#");
	}
	public static void logout (Player p)	{
		TestChat.getInstance().getUserManager().getUserList().get(p.getName()).toggleOnline();
	}
	public static User getUser (String name)	{
		return TestChat.getInstance().getUserManager().getUserList().get(name);
	}
	public String getName()	{
		return this.pthis.getName();
	}
	public Player getPlayer()	{
		return pthis;
	}
	public PlayerRank getRank()	{
		return prank;
	}
	
	public String getNick()	{
		return globalNick;
	}

	public void setNick(String newNick)	{
		this.globalNick = newNick;
	}

	public void toggleMute()	{
		if (this.globalMute = !this.globalMute)	{
			this.sendMessage(ChatColor.RED + "You have been muted in all channels.");
		}
		else	{
			this.sendMessage(ChatColor.GREEN + "You have been unmuted in all channels.");
		}
	}

	public boolean isMute()	{
		return globalMute;
	}

	public void toggleOnline()	{
		this.isOnline = this.isOnline ? false : true;
	}

	public boolean isOnline()	{
		return isOnline;
	}
	
	public void setCurrent(Channel c)	{
		if(c.isBanned(this))	{
			pthis.sendMessage(ChatColor.RED + "You are banned in channel " + ChatColor.GOLD + c.getName() + ChatColor.RED + "!");
			return;
		}
		this.current = c;
	}

	public boolean addListening(Channel c)
	{
		if (c == null)	{
			return false;
		}
		if(c.isBanned(this))	{
			pthis.sendMessage(ChatColor.RED + "You are banned in channel " + ChatColor.GOLD + c.getName() + ChatColor.RED + "!");
			return false;
		}
		if (!this.listening.contains(c))	{
			if (c.userJoin(this))	{
				this.listening.add(c);
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

	public Set<Channel> getListening()	{
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
		Channel sendto = sender.current;
		
		if(fullmsg.indexOf("@") == 0)	{	//Check for alternate channel destination
			int space = fullmsg.indexOf(" ");
			String newChannel = fullmsg.substring(1, space);
			if(TestChat.getInstance().getChannelManager().isValidChannel(newChannel))	{
				sendto = TestChat.getInstance().getChannelManager().getChannel(newChannel);
				outputmessage = fullmsg.substring(space + 1); 
			}
		}
		if (sender.globalMute)	{
			pthis.sendMessage(ChatColor.RED + "You are muted in channel " + ChatColor.GOLD + sendto.getName() + ChatColor.RED + "!");
			return;
		}
		/*switch (sendto.getSAcess())
		{
		case PUBLIC:
			break;
		case PRIVATE:
			if (sendto.getApprovedUsers().contains(sender.pname))	{
				break;
			}
			else	{
				return;
			}
		case REQUEST:
			if (sendto.getApprovedUsers().contains(sender.pname))	{
				break;
			}
			else	{
				return;
			}
		}*/
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
		
		
		c.sendToAll(sender, s);
		
	}
	public void sendMessageFromChannel (String s, Channel c)	{	//final output, sends message to user
		//alert for if its player's name is applied here i.e. {!}
		//then just send it and be done!
		this.pthis.sendMessage(s);
	}
	
	//Here begins output formatting. Abandon all hope ye who enter
	
	public String getOutputChannelF(User sender, Channel channel)	{	//colors for [$channel] applied here
		//SburbChat code. Handle with care
		
		ChatColor color = ColorDef.CHATRANK_MEMBER;
		sender.pthis.sendMessage(channel.getOwner());
		if (channel.getOwner().equalsIgnoreCase(sender.getName()))	{
			color = ColorDef.CHATRANK_OWNER;
		}
		else if (channel.getModList().contains(sender.getName()))	{
			color = ColorDef.CHATRANK_MOD;
		}

		return ChatColor.WHITE + "[" + color + channel.getName() + ChatColor.WHITE + "] ";
	}
	
	public String getOutputNameF(User sender, boolean isThirdPerson)	{	//colors for <$name> applied here
		//	SburbChat code. Handle with care
		
		String outputName = sender.pname;
		if(!(sender.globalNick.equals(sender.pname)))	{
			outputName = sender.globalNick;
		}
		
		ChatColor colorP = ColorDef.RANK_HERO;
		ChatColor colorW = ColorDef.DEFAULT;

		if (sender.pthis.hasPermission("group.horrorterror"))
			colorP = ColorDef.RANK_ADMIN;
		else if (sender.pthis.hasPermission("group.denizen"))
			colorP = ColorDef.RANK_MOD;
		else if (sender.pthis.hasPermission("group.helper"))
			colorP = ColorDef.RANK_HELPER;
		else if (sender.pthis.hasPermission("group.godtier"))
			colorP = ColorDef.RANK_GODTIER;
		else if (sender.pthis.hasPermission("group.donator"))
			colorP = ColorDef.RANK_DONATOR;
		
		if (sender.pthis.getWorld().getName().equalsIgnoreCase("earth"))
			colorW = ColorDef.WORLD_EARTH;
		else if (sender.pthis.getWorld().getName().equalsIgnoreCase("innercircle"))
			colorW = ColorDef.WORLD_INNERCIRCLE;
		else if (sender.pthis.getWorld().getName().equalsIgnoreCase("outercircle"))
			colorW = ColorDef.WORLD_OUTERCIRCLE;
		else if (sender.pthis.getWorld().getName().equalsIgnoreCase("medium"))
			colorW = ColorDef.WORLD_MEDIUM;
		else if (sender.pthis.getWorld().getName().equalsIgnoreCase("furthestring"))
			colorW = ColorDef.WORLD_FURTHESTRING;
		
		return (isThirdPerson ? "> " : colorW + "<") + colorP + outputName + ChatColor.WHITE + (isThirdPerson ? ": " : colorW + "> " + ChatColor.WHITE);		
	}

	public void sendMessage(String string) {
		//Not sure if I even need this...
		
	}
}

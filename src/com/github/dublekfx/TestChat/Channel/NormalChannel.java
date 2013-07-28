package com.github.dublekfx.TestChat.Channel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;

import com.github.dublekfx.TestChat.User;

public class NormalChannel implements Channel	{

	protected String name;
	protected String alias;
	protected ChannelType type = ChannelType.NORMAL;
	protected AccessLevel listenAccess;
	protected AccessLevel sendAccess;
	protected String owner;
	
	protected List<String> approvedList = new ArrayList<String>();
	protected List<String> modList = new ArrayList<String>();
	protected List<String> muteList = new ArrayList<String>();
	protected List<String> banList = new ArrayList<String>();	

	protected List<User> listening = new ArrayList<User>();
	
	public NormalChannel(String name, AccessLevel sendingAccess, AccessLevel listeningAccess, String creator)	{
		this.name = name;
		this.alias = null;
		this.sendAccess = sendingAccess;
		this.listenAccess = listeningAccess;
		this.owner = creator;
		this.modList.add(creator);
		
		//also, INSERT INTO all this stuff into the main ChatChannels table in the db
		//also CREATE TABLE channelname and add owner as first record. This table for all listeners
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getJoinChatMessage(User sender) {
		String time24h = new SimpleDateFormat("HH:mm").format(new Date());
		return ChatColor.DARK_GREEN + sender.getPlayerName() + ChatColor.YELLOW + " began pestering " + ChatColor.GOLD 
				+ this.name + ChatColor.YELLOW + " at " + time24h;
	}

	@Override
	public String getLeaveChatMessage(User sender) {
		 return this.getJoinChatMessage(sender).replaceAll("began", "ceased");
	}

	@Override
	public AccessLevel getSAcess() {
		return this.sendAccess;
	}

	@Override
	public AccessLevel getLAcess() {
		return this.listenAccess;
	}

	@Override
	public List<User> getListening() {
		return this.listening;
	}

	@Override
	public ChannelType getType() {
		return ChannelType.NORMAL;
	}

	@Override
	public void setAlias(String name, User sender) {
		if(this.modList.contains(sender.getPlayerName()))	{
			this.alias = name;
		}
		else	{
			sender.sendMessage(ChatColor.RED + "You do not have permission to change the alias of channel " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		
	}

	@Override
	public void removeAlias(User sender) {
		if(this.modList.contains(sender.getPlayerName()))	{
			this.alias = null;
		}
		else	{
			sender.sendMessage(ChatColor.RED + "You do not have permission to remove the alias of channel " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		
	}

	@Override
	public boolean userJoin(User sender) {
			String joinMsg = this.getJoinChatMessage(sender);
			switch (listenAccess)
			{
			case PUBLIC:
			{
				if (!banList.contains(sender.getPlayerName()))	{
					this.listening.add(sender);
					this.sendToAll(sender, joinMsg);
					return true;
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "You are banned from " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
					return false;
				}
			}
			case PRIVATE:
			{
				if (approvedList.contains(sender.getPlayerName()))	{
					this.listening.add(sender);
					this.sendToAll(sender, joinMsg);
					return true;
				}
				else
				{
					sender.sendMessage(ChatColor.GOLD + this.name + ChatColor.RED + " is a " + ChatColor.BOLD + "private" + ChatColor.RESET + " channel!");
					return false;
				}
			}
			default:
			{
				return false;
			}
			}
	}

	@Override
	public void userLeave(User sender) {
		this.sendToAll(sender, this.getLeaveChatMessage(sender));
		this.listening.remove(sender);		
	}

	@Override
	public void setNick(String nick, User sender) {
		sender.sendMessage(ChatColor.RED + "This channel does not support nicknames!");		
	}

	@Override
	public void removeNick(User sender) {
		sender.sendMessage(ChatColor.RED + "This channel does not support nicknames!");		
	}

	@Override
	public void setOwner(String newO, User sender) {
		if(sender.equals(this.owner))	{
			this.owner = newO;
		}		
	}
	@Override
	public String getOwner()	{
		return this.owner;
	}
	@Override
	public boolean isOwner(User user)	{
		return user.getPlayerName().equalsIgnoreCase(owner);
	}

	@Override
	public void loadMod(String user) {
		this.modList.add(user);
	}

	@Override
	public void addMod(User user, User sender) {
		//SburbChat code. Handle with care
		
		if (modList.contains(sender.getPlayerName()) && !modList.contains(user.getPlayerName()))	{	
			this.modList.add(user.getPlayerName());
			this.sendToAll(sender, ChatColor.YELLOW + user.getPlayerName() + " is now a mod in " + ChatColor.GOLD + this.name + ChatColor.YELLOW + "!");
			user.sendMessage(ChatColor.GREEN + "You are now a mod in " + ChatColor.GOLD + this.name + ChatColor.GREEN + "!");
		}
		else if (!sender.getPlayerName().equals(owner))	{
			sender.sendMessage(ChatColor.RED + "You do not have permission to mod people in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else	{
			sender.sendMessage(ChatColor.YELLOW + user.getPlayerName() + ChatColor.RED + " is already a mod in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		
	}

	@Override
	public void removeMod(User user, User sender) {
		//SburbChat code. Handle with care
		
		 if (modList.contains(sender.getPlayerName()) && this.modList.contains(user.getPlayerName()))	{
			this.modList.remove(user.getPlayerName());
			this.sendToAll(sender, ChatColor.YELLOW + user.getPlayerName() + " is no longer a mod in " + ChatColor.GOLD + this.name + ChatColor.YELLOW + "!");
			user.sendMessage(ChatColor.RED + "You are no longer a mod in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else if (!sender.getPlayerName().equals(this.owner))	{
			sender.sendMessage(ChatColor.RED + "You do not have permission to demod people in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else	{
			sender.sendMessage(ChatColor.YELLOW + user.getPlayerName() + ChatColor.RED + " is not a mod in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}		 		 
	}

	@Override
	public List<String> getModList() {
		return this.modList;
	}

	@Override
	public boolean isMod(User user) {
		if(modList.contains(user.getPlayerName()) || user.getPlayer().hasPermission("group.denizen") || user.getPlayer().hasPermission("group.horrorterror"))	{
			return true;
		}
		return false;
	}

	@Override
	public void kickUser(User user, User sender) {
		//SburbChat code. Handle with care
		if (modList.contains(sender.getPlayerName()) && listening.contains(user))	{
			this.listening.remove(user);
			user.sendMessage(ChatColor.YELLOW + "You have been kicked from " + ChatColor.GOLD + this.getName() + ChatColor.YELLOW + "!");
			user.removeListening(this);
			this.sendToAll(sender, ChatColor.YELLOW + user.getPlayerName() + " has been kicked from " + ChatColor.GOLD + this.getName() + ChatColor.YELLOW + "!");
		}
		else if (!modList.contains(sender.getPlayerName()))	{
			sender.sendMessage(ChatColor.RED + "You do not have permission to kick people in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else	{
			sender.sendMessage(ChatColor.YELLOW + user.getPlayerName() + ChatColor.RED + " is not chatting in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		 
		
	}

	@Override
	public void loadBan(String user) {
		this.banList.add(user);
	}

	@Override
	public void banUser(User user, User sender) {
		if(this.isMod(sender) && !banList.contains(user.getPlayerName()))	{
			if(modList.contains(user))	{
				modList.remove(user);
			}
			if(listening.contains(user))	{
				this.listening.remove(user);
				user.removeListening(this);
			}
			this.banList.add(user.getPlayerName());
			user.sendMessage(ChatColor.RED + "You have been " + ChatColor.BOLD + "banned" + ChatColor.RESET + " from " + ChatColor.GOLD + this.getName() + ChatColor.RED + "!");
			this.sendToAll(sender, ChatColor.YELLOW + user.getPlayerName() + ChatColor.RED + " has been " + ChatColor.BOLD + "banned" + ChatColor.RESET + " from " + ChatColor.GOLD + this.getName() + ChatColor.RED + "!");
		}
		else if(!sender.getPlayerName().equalsIgnoreCase(owner))	{
			sender.sendMessage(ChatColor.RED + "You do not have permission to ban people in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else	{
			sender.sendMessage(ChatColor.YELLOW + user.getPlayerName() + ChatColor.RED + " is already banned in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
	}

	@Override
	public void unbanUser(User user, User sender) {
		if(sender.getPlayerName().equalsIgnoreCase(this.owner) && banList.contains(user.getPlayerName()))	{
			this.banList.remove(user.getPlayerName());
			user.sendMessage(ChatColor.RED + "You have been " + ChatColor.BOLD + "unbanned" + ChatColor.RESET + " from " + ChatColor.GOLD + this.getName() + ChatColor.RED + "!");
			this.sendToAll(sender, ChatColor.YELLOW + user.getPlayerName() + ChatColor.RED + " has been " + ChatColor.BOLD + "unbanned" + ChatColor.RESET + " from " + ChatColor.GOLD + this.getName() + ChatColor.RED + "!");
		}
		else if(!sender.getPlayerName().equalsIgnoreCase(owner))	{
			sender.sendMessage(ChatColor.RED + "You do not have permission to unban people in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else	{
			sender.sendMessage(ChatColor.YELLOW + user.getPlayerName() + ChatColor.RED + " is not banned in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}		
	}
	@Override
	public List<String>	getBanList()	{
		return banList;
	}
	@Override
	public boolean isBanned(User user)	{
		return banList.contains(user.getPlayerName());
	}

	@Override
	public void loadApproval(String user) {
		// Public channel; do nothing.
	}

	@Override
	public void approveUser(User user, User sender) {
		sender.sendMessage(ChatColor.GOLD + this.name + ChatColor.RED + " is a public channel!");
	}

	@Override
	public void deapproveUser(User user, User sender) {
		sender.sendMessage(ChatColor.GOLD + this.name + ChatColor.RED + " is a public channel!");
	}
	public List<String> getApprovedUsers()	{
		return approvedList;
	}

	@Override
	public void disband(User sender) {
		// TODO Auto-generated method stub
		//Prolly copy Ben's code here again		
	}
	@Override
	public void sendToAll(User sender, String s) {
		for (User u : this.listening)
		{
			u.sendMessageFromChannel(s, this);
		}
		Logger.getLogger("Minecraft").info(ChatColor.stripColor(s));
	}

}

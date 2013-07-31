package com.github.dublekfx.TestChat.Channel;

import java.util.List;

import com.github.dublekfx.TestChat.User;

public interface Channel {
	public String getName();
	public String getJoinChatMessage(User sender);
	public String getLeaveChatMessage(User sender);
	public AccessLevel getSAcess();
	public AccessLevel getLAcess();
	public List<User> getListening();
	public ChannelType getType();
	
	public void setAlias(String name, User sender);
	public void removeAlias(User sender);
	
	public boolean userJoin(User sender);
	public void userLeave(User sender);
	
	public void setNick(String nick, User sender);
	public void removeNick(User sender);
	
	public void setOwner(String name, User sender);
	public String getOwner();
	public void loadMod(String user);
	public void addMod(User user, User sender);
	public void removeMod(User user, User sender);
	public List<String>	getModList();
	public boolean isMod(User user);
	
	public void kickUser(User user, User sender);
	public void loadBan(String user);
	public void banUser(User user, User sender);
	public void unbanUser(User user, User sender);
	public boolean isBanned(User user);
	public void loadApproval(String user);
	public void approveUser(User user, User sender);
	public void deapproveUser(User user, User sender);
	public List<String> getApprovedUsers();
	
	public void disband(User sender);
	
	public void sendToAll(User sender, String message);
	List<String> getBanList();
	boolean isOwner(User user);
}

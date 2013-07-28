package com.github.dublekfx.TestChat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public class UserManager {
	//Writing this part from scratch
	//Here be dragons...
	//wait maybe this all needs to be static?
	
	private Map<String, User> userList = new HashMap<String, User>();
	
	public UserManager()	{
		//do stuff
	}
	public void loadUserFromDatabase(String name, User u)	{
		//SELECT * FROM PlayerData WHERE name=playerName
		//new User(datagoethhere)
		this.userList.put(name, u);
	}
	public void saveUserToDatabase(User u)	{
		DatabaseManager.getDatabaseManager().savePlayerData(u);
	}
	public Map<String, User> getUserList()	{
		return userList;
	}
	public User getUser(String name)	{
		return this.userList.get(name);
	}
	public void newUser(Player p)	{	//For first time logins
		User u = new User(p);
		this.userList.put(u.getName(), u);
		//DatabaseManager.getDatabaseManager().firstPlayerDataSave(u);	
	}
	public void dropUser(User u)	{
		//idek why I would want this. but here it is
		//actually, might as well remove users when they are banned
		this.userList.remove(u);
		
	}
	
}

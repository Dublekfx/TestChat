package com.github.dublekfx.TestChat;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.dublekfx.TestChat.Channel.AccessLevel;
import com.github.dublekfx.TestChat.Channel.Channel;
import com.github.dublekfx.TestChat.Channel.ChannelManager;

public class TestChat extends JavaPlugin {

	private static TestChat instance;
	private ChannelManager cm = new ChannelManager();
	private UserManager um = new UserManager();
	private TestChatListener listener = new TestChatListener();

	@Override
	public void onEnable() {
		instance = this;
		this.getServer().getPluginManager().registerEvents(listener, this);
		// cm.loadAllChannels();
		this.cm.createDefaultChannel();

		for (Player p : this.getServer().getOnlinePlayers()) {
			TestChat.getInstance().getUserManager().newUser(p);
		}
	}

	@Override
	public void onDisable() {
		// cm.saveAllChannels();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)	{
		boolean isConsole = !(sender instanceof Player);
		boolean isHelper = !isConsole && (sender.hasPermission("group.helper") || sender.hasPermission("group.denizen") || 
				sender.hasPermission("group.horrorterror"));
		boolean isMod = !isConsole && (sender.hasPermission("group.denizen") || sender.hasPermission("group.horrorterror"));
		boolean isAdmin = !isConsole && sender.hasPermission("group.horrorterror");
		if(cmd.getName().equalsIgnoreCase("o"))	{	//[o] Be Doc Scratch
			if(isConsole || isAdmin || sender.isOp())	{
				String output = "";
				for(String s : args)	{
					output = output + s + " ";
				}
				Logger.getLogger("Minecraft").info("[o] " + output);
				for(Player p: this.getServer().getOnlinePlayers())	{
					p.sendMessage(ChatColor.BOLD + "[o] " + output);
				}				
				return true;
			}
		}
		if(cmd.getName().equalsIgnoreCase("sban"))	{	//superban
			if(isConsole || sender.isOp())	{
				Player victim = this.getServer().getPlayer(args[0]);
				String ip = victim.getAddress().getAddress().getHostAddress();
				//normal ban goes here
				this.getServer().banIP(ip);
				//Also need banreason
				//also broadcast to all players
				//Also send to Logger
				return true;
			}
		}
		if(cmd.getName().equalsIgnoreCase("whois"))	{		//Master PlayerData output
			if(isConsole || isMod)	{
				Player subject = this.getServer().getPlayer(args[0]);
				User u = this.getUserManager().getUser(subject.getName());
				u.toString();
				return true;
			}
		}
		if(cmd.getName().equalsIgnoreCase("scmute"))	{
			if(isConsole || sender.isOp() || isHelper)	{
				
			}
		}
		if(cmd.getName().equalsIgnoreCase("sc"))	{
			if(isConsole)	{
				//figure out these later
			}
			else	{	//ingame commands
				User user = this.getUserManager().getUser(sender.getName());
				if(args[0].equalsIgnoreCase("c"))	{	//setChannel
					if(args.length == 1)	{
					sender.sendMessage(ChatColor.YELLOW + "Set current channel:\n\t/sc c <$channelname>");
					return true;
					}
					try	{
						user.setCurrent(this.getChannelManager().getChannel(args[1]));
					}
					catch(NullPointerException e)	{
						sender.sendMessage(ChatColor.RED + "Channel " + ChatColor.GOLD + args[1] + ChatColor.RED + " does not exist!");
					}
					return true;
				}
				if(args[0].equalsIgnoreCase("l"))	{	//addListening
					if(args.length == 1)	{
						sender.sendMessage(ChatColor.YELLOW + "Listen to a channel:\n\t/sc l <$channelname>");
						return true;
					}
					try	{
						user.addListening(this.getChannelManager().getChannel(args[1]));						
					}
					catch(NullPointerException e)	{
						sender.sendMessage(ChatColor.RED + "Channel " + ChatColor.GOLD + args[1] + ChatColor.RED + " does not exist!");
					}
					return true;
				}
				if(args[0].equalsIgnoreCase("leave"))	{	//removeListening
					if(args.length == 1)	{
						sender.sendMessage(ChatColor.YELLOW + "Stop listening to a channel:\n\t/sc leave <$channelname>");
						return true;
					}
					try	{
						user.removeListening(this.getChannelManager().getChannel(args[1]));
					}
					catch(NullPointerException e)	{
						sender.sendMessage(ChatColor.RED + "Channel " + ChatColor.GOLD + args[1] + ChatColor.RED + " does not exist!");
					}
					return true;
				}
				if(args[0].equalsIgnoreCase("list"))	{	//listListening
					String clist = ChatColor.YELLOW + "Currently pestering: ";
					for (Channel c : user.getListening())	{
						clist += c.getName() + " ";
					}
					sender.sendMessage(clist);
					return true;
				}
				if(args[0].equalsIgnoreCase("listall"))	{	//listAll
					String clist = ChatColor.YELLOW + "All channels: ";
					Map<String, Channel> channels = ChannelManager.getChannelList();
					for (Channel c : channels.values())	{
						String next;
						if(user.getListening().contains(c))	{
							next = ChatColor.YELLOW + c.getName() + " ";
							clist += next;
						}
						else if(c.getSAcess().equals(AccessLevel.PUBLIC))	{
							next = ChatColor.GREEN + c.getName() + " ";
							clist += next;
						}
						else if(c.getSAcess().equals(AccessLevel.PRIVATE))	{
							next = ChatColor.RED + c.getName() + " ";
							clist += next;
						}						
					}
					sender.sendMessage(clist);
					return true;
				}
				if(args[0].equalsIgnoreCase("new"))	{	//newChannel
					if(args.length != 4)	{
						sender.sendMessage(ChatColor.YELLOW + "Create a new channel:\n\t/sc new <$channelname> <$sAccess> <$lAccess>\n\t" +
								"sendAccess and listenAccess must be either PUBLIC or PRIVATE");
						return true;
					}
					this.getChannelManager().createNewChannel(args[1], AccessLevel.valueOf(args[2]), AccessLevel.valueOf(args[3]), user.getName());
					return true;
				}
				if(args[0].equalsIgnoreCase("whois"))	{
					sender.sendMessage(ChatColor.YELLOW + "Command coming soon!");
					return true;
				}
				if(args[0].equalsIgnoreCase("channel"))	{	//ChannelOwner/Mod commands
					Channel c = user.getCurrent();
					String helpMod = ChatColor.YELLOW + "Channel Mod commands:\n" +
							"\t/sc channel kick <$user>\tKick a user from the channel\n" +
							"\t/sc channel ban <$user>\tBan a user from the channel\n" +
							"\t/sc channel setalias <$alias>\tSet an alias for the channel\n" +
							"\t/sc channel rmalias\tRemove the channel alias\n" +
							"\t/sc channel getListeners\tList all users currently listening to this channel";
					if(user.getCurrent().getModList().contains(user.getName()) || isHelper)	{
						if(args[1].equalsIgnoreCase("kick"))	{
							c.kickUser(this.getUserManager().getUser(args[2]), user);
							return true;
						}
						if(args[1].equalsIgnoreCase("ban"))	{
							c.banUser(this.getUserManager().getUser(args[2]), user);
							return true;
						}
						if(args[1].equalsIgnoreCase("setalias"))	{
							c.setAlias(args[2], user);
						}
						if(args[1].equalsIgnoreCase("rmalias"))	{
							c.removeAlias(user);
						}
						if(args[1].equalsIgnoreCase("getlisteners"))	{
							
						}					
						String helpOwner = ChatColor.YELLOW + "Channel Owner commands:\n" +
								"\t/sc channel mod <add/remove> <$user>\tAdd or remove a channelMod\n" +
								"\t/sc channel <ban/unban> <$user>\t(Un)bans a user from the channel\n" +
								"\t/sc disband\tDisband coming soon!";
						if(c.getOwner().equalsIgnoreCase(user.getName()) || isMod)	{
							if(args.length == 1){
								sender.sendMessage(helpOwner);
								return true;
							}
							if(args[1].equalsIgnoreCase("mod"))	{
								if(args[2].equalsIgnoreCase("add"))	{
									c.addMod(this.getUserManager().getUser(args[3]), user);
									return true;
								}
								else if (args[2].equalsIgnoreCase("remove"))	{
									c.removeMod(this.getUserManager().getUser(args[3]), user);
									return true;
								}
								else {
									sender.sendMessage(helpOwner);
									return true;
								}
							}
							if(args[1].equalsIgnoreCase("unban"))	{
								this.getChannelManager().getChannel(c.getName()).unbanUser(this.getUserManager().getUser(args[2]), user);
								return true;
							}
							if(args[1].equalsIgnoreCase("disband"))	{
								sender.sendMessage(ChatColor.YELLOW + "Command coming soon!");
								return true;
							}
						}
						else	{
							sender.sendMessage(ChatColor.BLACK + "There are mysteries into which it behooves one not to delve too deeply...");
							return true;
						}
					}
				}
				
				//Global powers
				//setcurrent					//sc c <channelname>
				//addlistening					//sc l <channelname>
				//removelistening				//sc leave <channelname>
				//getlistening					//sc list
				//newchannel					//sc new <name, sAccess, lAccess>
				//listallchannels				//sc listall
				//whois(limited)				//sc whois
				
				//Channelowner powers			//prereq: Channel must be current
				//mod							//sc channel mod add <user>
				//demod							//sc channel mod remove <user>
				//unban							//sc channel unban <user>
				//disband						//sc channel disband
				
				//Channelmod powers
				//kick							//sc channel kick <player>
				//ban							//sc channel ban <player>
				//setalias						//sc channel alias set <alias>
				//rmalias						//sc channel alias remove
				//getListeners					//sc channel listeners
				
				
				//Mod powers
				//Mute							//sc global mute <player>
				//unmute						//sc global unmute <player>
				//setnick						//sc global nick set <player>
				//rmnick						//sc global nick remove <player>
			}
			
		}
		
		
		
		
		
		return false;
		
	}

	public ChannelManager getChannelManager() {
		return cm;
	}

	public UserManager getUserManager() {
		return um;
	}

	public static TestChat getInstance() {
		return instance;
	}
}
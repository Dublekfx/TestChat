package com.github.dublekfx.TestChat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Collection of all database-related functions
 * 
 * @author Jikoo
 * 
 */
public class DatabaseManager {

	private static DatabaseManager dbm;
	TestChat plugin;

	public static DatabaseManager getDatabaseManager() {
		if (dbm == null)
			dbm = new DatabaseManager();
		return dbm;
	}

	public DatabaseManager() {
		plugin = TestChat.getInstance();
	}

	private Connection connection;

	public boolean enable() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://"
					+ plugin.getConfig().getString("host") + ":"
					+ plugin.getConfig().getString("port") + "/"
					+ plugin.getConfig().getString("database"),
					plugin.getConfig().getString("username"),
					plugin.getConfig().getString("password"));
		} catch (ClassNotFoundException e) {
			// if we can't connect to the database, we're pretty much done here.
			plugin.getLogger().severe("The database driver was not found. " +
					"Plugin functionality will be limited.");
			return false;
		} catch (SQLException e) {
			plugin.getLogger().severe("An error occurred while connecting to the database. " +
					"Plugin functionality will be limited.");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void disable() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		dbm = null;
		connection = null;
	}

	public void saveUserData(User user) {
		try {
			PreparedStatement pst = connection.prepareStatement(
					"INSERT INTO PlayerData(playerName, class, aspect, mplanet, " +
					"dplanet, towernum, sleepstate, currentChannel, isMute, " +
					"nickname, channels, ip, LastLogin, timePlayed) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
					"ON DUPLICATE KEY UPDATE " +
					"class=VALUES(class), aspect=VALUES(aspect), " +
					"mplanet=VALUES(mplanet), dplanet=VALUES(dplanet), " +
					"towernum=VALUES(towernum), sleepstate=VALUES(sleepstate), " +
					"currentChannel=VALUES(currentChannel), isMute=VALUES(isMute), " +
					"nickname=VALUES(nickname), channels=VALUES(channels), " +
					"ip=VALUES(ip), LastLogin=VALUES(LastLogin), timePlayed=VALUES(timePlayed)");

			pst.setString(1, user.getName());
			pst.setString(2, user.getClassType());
			pst.setString(3, user.getAspect());
			pst.setString(4, user.getMPlanet());
			pst.setString(5, user.getDPlanet());
			pst.setShort(6, user.getTowerNum());
			pst.setBoolean(7, user.isSleeping());
			pst.setString(8, user.getCurrent().getName());
			pst.setBoolean(9, user.isMute());
			pst.setString(10, user.getNick());
//			pst.setArray(11, user.getListening()); // TODO Keiko, may need to be a List, not Set. Not sure.
//			pst.setString(12, user.getUserIP());
//			pst.setString(13, null); // TODO lastLogin     Keiko, to String, but need methods.
//			pst.setString(14, null); // TODO timePlayed

			pst.executeUpdate();
			pst.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void loadUserData(User user) {
		try {
			PreparedStatement pst = connection.prepareStatement(
					"SELECT * FROM PlayerData WHERE name=?");

			pst.setString(1, user.getName());

			ResultSet rs = pst.executeQuery();

			try {
				user.setAspect(rs.getString("aspect"));
				user.setClassType(rs.getString("class"));
				user.setMPlanet(rs.getString("mplanet"));
				user.setDPlanet(rs.getString("dplanet"));
				user.setTowerNum(rs.getShort("tower"));
				user.setSleeping(rs.getBoolean("sleepstate"));
				user.setCurrent(TestChat.getInstance().getChannelManager().getChannel(rs.getString("currentChannel")));
				user.setMute(rs.getBoolean("isMute"));
				user.setNick(rs.getString("nickname"));
				for (Entry e : rs.getArray("channels")) { // TODO Keiko, may need to be a List, not Set. Not sure.
					if (e instanceof String) {
						user.addListening(TestChat.getInstance().getChannelManager().getChannel((String) e));
					}
				}
//				// IP should not be set here. Update-only, for offline IPban.
//				// TODO lastLogin     Keiko, from String, but need methods.
//				// TODO timePlayed

				pst.close();

			} catch (Exception e) {
				// User may not be defined in the database
				plugin.getLogger().warning("Player "
						+ user.getPlayer().getName() +
						" does not have an entry in the database. Or something.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void deleteUser(User user) {
		try {
			PreparedStatement pst = connection.prepareStatement(
					"DELETE FROM PlayerData WHERE name = ?");
			pst.setString(1, user.getName());

			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveChannelData(/* Channel c */) {
		try {
			PreparedStatement pst = connection.prepareStatement(
					"INSERT INTO ChatChannels(name, alias, channelType, listenAccess, " +
					"sendAccess, owner, modList, banList, listening, approvedList) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
					"ON DUPLICATE KEY UPDATE alias=VALUES(alias), " +
					"channelType=VALUES(channelType), listenAccess=VALUES(listenAccess), " + 
					"sendAccess=VALUES(sendAccess), owner=VALUES(owner), " +
					"modList=VALUES(modList), banList=VALUES(banList), " +
					"listening=VALUES(listening), approvedList=VALUES(approvedList), ");

//			pst.setString(1, c.getName());
//			pst.setString(2, c.getAlias);
//			pst.setString(3, c.getType().name());
//			pst.setString(4, c.getLAccessLevel().name());
//			pst.setString(5, c.getSAccessLevel().name());
//			pst.setString(6, c.getOwner());
//			pst.setArray(7, c.getModList());
//			pst.setArray(8, null); // TODO banList
//			pst.setArray(9, null); // TODO listening
//			pst.setArray(10, c.getApprovedList());

			pst.executeUpdate();
			pst.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void loadChannelData(String cName) {
		PreparedStatement pst;
		try {
			pst = connection.prepareStatement(
					"SELECT * FROM ChatChannels WHERE name=?");

			pst.setString(1, cName);

			ResultSet rs = pst.executeQuery();

//			ChannelManager.getInstance().createNewChannel(cName,
//					AccessLevel.valueOf(rs.getString(sendAccess)),
//					AccessLevel.valueOf(rs.getString(listenAccess)),
//					rs.getString(owner)/*, ChannelType.valueOf(rs.getString(channelType))*/);
//			Channel c = ChannelManager.getInstance().getChannel(cName);
//			c.setAlias(rs.getString(alias));
//			for (Entry e : rs.getArray(modList) {
//				if (e instanceof String) {
//					c.addMod(UserManager.getInstance().getUser(e),
//							UserManager.getInstance().getUser(owner)); // TODO Keiko, an easier method? loadMod
//			}
//			for (Entry e : rs.getArray(banList) {
//				if (e instanceof String) {
//					c.banUser(UserManager.getInstance().getUser(e),
//							UserManager.getInstance().getUser(owner)); // TODO Keiko, an easier method? loadBan
//					//c.loadBan((String) e);
//			}
//			for (Entry e : rs.getArray(approvedList) {
//				if (e instanceof String) {
//					c.approveUser(UserManager.getInstance().getUser(e),
//							UserManager.getInstance().getUser(owner)); // TODO Keiko, an easier method?
//			}
//			for (Entry e : rs.getArray(listening) {
//				if (e instanceof String) {
//					User u = UserManager.getInstance().getUser(e);
//					if (u != null && u.getPlayer().isOnline()) {
//						c.userJoin(u);
//					}
//				}
//			}
//			
//			// That should be all. Required changes:
//			// - Merge UserManager and SblockPlayerManager in the future
//			// - UserManager.getInstance()
//			// - Channel.loadBan(String)(no reason, no banning player)
//			// - Channel.loadMod(String/User) (no modding player)
//			// - Channel.getBanList()
//			// - Channel.getListeningList()

			pst.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void deleteChannel(/* Channel c */) {
//		try {
//			PreparedStatement pst = connection.prepareStatement(
//					"DELETE FROM ChatChannels WHERE name = ?");
//			pst.setString(1, c.getName());
//			
//			pst.executeUpdate();
//			pst.close();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
	}

	public ResultSet makeCustomCall(String MySQLStatement, boolean resultExpected) {
		try {
			PreparedStatement pst = connection.prepareStatement(MySQLStatement);
			if (resultExpected) {
				ResultSet rs = pst.executeQuery();
				pst.close();
				return rs; // TODO find out if stream needs to be open for this
			} else {
				pst.executeUpdate();
				pst.close();
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

}
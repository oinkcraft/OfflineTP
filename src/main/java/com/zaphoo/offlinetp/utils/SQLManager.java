package com.zaphoo.offlinetp.utils;

import org.jetbrains.annotations.NotNull;
import com.zaphoo.offlinetp.Main;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class SQLManager {

    private final Main main = Main.getInstance();
    private static SQLManager instance;
    private FileConfiguration config = Main.getInstance().getConfig();
    private String prefix = Main.getInstance().getPrefix();
    private boolean exists = false;

    private SQLManager() {
    }


    public static SQLManager getInstance() {
        if (instance == null) {
            instance = new SQLManager();
        }
        return instance;
    }

    // Create the connection to the SQL server
    @NotNull
    private Connection getConnection() {
        String driver = "com.mysql.jdbc.Driver";
        String url = String.format("jdbc:mysql://%s:%d/%s", config.getString("sql.host"), config.getInt("sql.port"), config.getString("sql.database"));
        String username = config.getString("sql.user");
        String password = config.getString("sql.password");
        try {
            // Check if driver exists
            Class.forName(driver);
            return DriverManager.getConnection(url + "?useSSL=false", username, password);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.print("An error occurred while establishing connection to the SQL server. See stacktrace below for more information.");
            e.printStackTrace();
        }
        // Should never happen
        return null;
    }

    // Check if the given table exists
    public boolean tableExist(String tableName) {
        Connection connection = getConnection();
        boolean tExists = false;
        try (ResultSet rs = connection.getMetaData().getTables(null, null, tableName, null)) {
            while (rs.next()) {
                String tName = rs.getString("TABLE_NAME");
                if (tName != null && tName.equals(config.getString("sql.prefix") + tableName)) {
                    tExists = true;
                    break;
                }
            }
            connection.close();
        } catch (SQLException e) {
            System.err.print("An error occurred while if a table exists in your database. See stacktrace below for more information.");
            e.printStackTrace();
        }
        // Close connection to prevent too many open connections
        return tExists;
    }

    // We want to be able to check if the player exists
    public boolean checkIfExists(Player player) {
        ArrayList<Integer> list = new ArrayList<>();
        CompletableFuture<Boolean> f = CompletableFuture.supplyAsync(() -> {
            try {
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT count(*) as length FROM " + config.getString("sql.prefix") + "locations WHERE uuid = '" + player.getUniqueId().toString() + "'");
                ResultSet set = statement.executeQuery();
                while (set.next()) {
                    list.add(set.getInt("length"));
                }
                connection.close();
                return list.get(0) > 0;
            } catch (SQLException e) {
                System.err.print("An error occurred while while checking if the player exists in your database. See stacktrace below for more information.");
                e.printStackTrace();
            }
            return null;
        });
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    // We want to be able to check if the player exists based on UUID
    public boolean checkIfExists(UUID uuid) {

        ArrayList<Integer> list = new ArrayList<>();
        CompletableFuture<Boolean> f = CompletableFuture.supplyAsync(() -> {
            try {
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT count(*) as length FROM " + config.getString("sql.prefix") + "locations WHERE uuid = '" + uuid.toString() + "'");
                ResultSet set = statement.executeQuery();
                while (set.next()) {
                    list.add(set.getInt("length"));
                }
                connection.close();
                return list.get(0) > 0;
            } catch (SQLException e) {
                System.err.print("An error occurred while while checking if the player exists in your database. See stacktrace below for more information.");
                e.printStackTrace();
            }
            return null;
        });
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
        /*try {
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT count(*) as length FROM " + config.getString("sql.prefix") + "locations WHERE uuid = '" + uuid + "'");
            ResultSet set = statement.executeQuery();
            ArrayList<Integer> list = new ArrayList<>();
            while (set.next()) {
                list.add(set.getInt("length"));
            }
            connection.close();
            return list.get(0) > 0;
        } catch (SQLException e) {
            System.err.print("An error occurred while while checking if the player exists in your database. See stacktrace below for more information.");
            e.printStackTrace();
        }
        return false;*/
    }



    public void postLocationOnLogin(Player player) {
        // Create our table entries
        Location loc = player.getLocation();
        String location = String.format("%.2f %.2f %.2f", loc.getX(), loc.getY(), loc.getZ());
        String worldName = player.getWorld().getName();
        String UUID = player.getUniqueId().toString();
        try {
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + config.getString("sql.prefix") + "locations " +
                    "(uuid, world, location, moved) VALUES ('"
                    + UUID + "','" + worldName + "','" + location + "','" + 0 + "')");
            statement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            System.err.print("An error occurred while while posting the location of the player on login. See stacktrace below for more information.");
            e.printStackTrace();
        }
    }

    // On logout we want to post the logout location of the player to the SQL database
    // We want to store by UUID, World and Location
    public void postLocation(Player player) {
        // Create our table entries
        Location loc = player.getLocation();
        String location = String.format("%.2f %.2f %.2f", loc.getX(), loc.getY(), loc.getZ());
        String worldName = player.getWorld().getName();
        String UUID = player.getUniqueId().toString();
        try {
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE "
                    + config.getString("sql.prefix")
                    + "locations SET world = '" + worldName + "', location = '" + location + "', moved = false WHERE uuid = '" + UUID + "'");
            statement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            System.err.print("An error occurred while posting the location of the player. See stacktrace below for more information.");
            e.printStackTrace();
        }
    }

    // On logout we want to post the logout location of the player to the SQL database
    // We want to store by UUID, World and Location
    public void postLocation(UUID uuid, Player player) {
        // Create our table entries
        Location loc = player.getLocation();
        String moverName = player.getName();
        String location = String.format("%.2f %.2f %.2f", loc.getX(), loc.getY(), loc.getZ());
        String worldName = player.getWorld().getName();
        try {
            Connection connection = getConnection();
            Location currLoc = getLocation(uuid);
            String currLocation = String.format("%sx %sy %sz", currLoc.getX(), currLoc.getY(), currLoc.getZ());
            PreparedStatement statement = connection.prepareStatement("UPDATE "
                    + config.getString("sql.prefix")
                    + "locations SET world = '" + worldName + "', location = '" + location + "', moved = true, movedFrom = '" + currLocation + "', movedBy = '" + moverName + "' WHERE uuid = '" + uuid + "'");
            statement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            System.err.print("An error occurred while posting the location of the player. See stacktrace below for more information.");
            e.printStackTrace();
        }
    }

    // We want to be able to get the location of the player from the SQL database
    public Location getLocation(Player player) {
        try {
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlForUser(player));
            Map<String, String[]> locationMap = new TreeMap<>();

            ResultSet set = statement.executeQuery();

            while (set.next()) {
                locationMap.put(set.getString("world"), set.getString("location").split(" "));
            }
            // Get the world from the map key and the coordinates from the value
            String world = locationMap.entrySet().iterator().next().getKey();
            connection.close();

            return new Location(Main.getInstance().getServer().getWorld(world), Double.parseDouble(locationMap.get(world)[0]), Double.parseDouble(locationMap.get(world)[1]), Double.parseDouble(locationMap.get(world)[2]));
        } catch (SQLException e) {
            System.err.print("An error occurred while getting the location of the player. See stacktrace below for more information.");
            e.printStackTrace();
        }
        return null;
    }

    // We want to be able to check if the player was moved between sessions
    public boolean getMoved(Player player) {
        ArrayList<Integer> arr = new ArrayList<>();
        CompletableFuture<Boolean> f = CompletableFuture.supplyAsync(() -> {
            try {
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sqlForUser(player));

                ResultSet set = statement.executeQuery();
                while (set.next()) {
                    arr.add(set.getInt("moved"));
                }
                connection.close();
                return arr.get(0) != 0;
            } catch (SQLException e) {
                System.err.print("An error occurred while checking if the player was moved. See stacktrace below for more information.");
                e.printStackTrace();
            }
            return false;
        });
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    // We want to be able to get who moved the player
    public String getMover(Player player) {
        try {
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlForUser(player));

            ResultSet set = statement.executeQuery();
            ArrayList<String> arr = new ArrayList<>();
            while (set.next()) {
                arr.add(set.getString("movedBy"));
            }
            connection.close();

            return arr.get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // We want to be able to get who moved the player
    public String getPreviousLocation(Player player) {
        try {
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlForUser(player));

            ResultSet set = statement.executeQuery();
            ArrayList<String> arr = new ArrayList<>();
            while (set.next()) {
                arr.add(set.getString("movedFrom"));
            }
            connection.close();

            return arr.get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // After a player is teleported, we want to reset their moved state to 0, to prevent teleportation every time they join
    public void resetMoved(Player player) {
        try {
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE " + config.getString("sql.prefix")
                    + "locations SET moved = 0 WHERE uuid = '" + player.getUniqueId() + "'");
            statement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // We need to return the time the player was moved
    public String[] getTime(Player player) {
        try {
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + config.getString("sql.prefix")
                    + "locations WHERE uuid = '" + player.getUniqueId().toString() + "'");
            ArrayList<String> arrayList = new ArrayList<>();
            ResultSet set = statement.executeQuery();
            while (set.next()) {
                arrayList.add(set.getString("time"));
            }
            connection.close();

            return arrayList.get(0).split(" ");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // We want to be able to get the location of the player from the UUID in the SQL database
    public Location getLocation(UUID uuid) {
        try {
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + config.getString("sql.prefix") + "locations WHERE uuid = '" + uuid + "'");
            Map<String, String[]> locationMap = new TreeMap<>();
            ResultSet set = statement.executeQuery();
            while (set.next()) {
                locationMap.put(set.getString("world"), set.getString("location").split(" "));
            }
            // Get the world from the map key and the coordinates from the value
            String world = locationMap.entrySet().iterator().next().getKey();
            connection.close();
            return new Location(Main.getInstance().getServer().getWorld(world), Double.parseDouble(locationMap.get(world)[0]), Double.parseDouble(locationMap.get(world)[1]), Double.parseDouble(locationMap.get(world)[2]));
        } catch (SQLException e) {
            System.err.print("An error occurred while getting the location of a player. See stacktrace below for more information.");
            e.printStackTrace();
        }
        return null;
    }

    // We want to be able to create the table if it does not already exist in the database
    public void createTableIfNotExists() {
        Connection connection = getConnection();
        if (!tableExist(config.getString("sql.prefix") + "locations")) {
            try {
                getConnection().createStatement().execute("CREATE TABLE IF NOT EXISTS " + config.get("sql.prefix") + "locations (\n" +
                        "uuid VARCHAR(255) NOT NULL PRIMARY KEY,\n" +
                        "world VARCHAR(255) NOT NULL,\n" +
                        "location VARCHAR(255) NOT NULL,\n" +
                        "moved BOOLEAN NOT NULL,\n" +
                        "movedBy VARCHAR(255),\n" +
                        "movedFrom VARCHAR(255),\n" +
                        "time DATETIME NOT NULL DEFAULT NOW()\n" +
                        ")");
                connection.close();
            } catch (SQLException e) {
                System.err.print("An error occurred while establishing connection to the SQL server. See stacktrace below for more information.");
                e.printStackTrace();
            }
        }
    }

    private String sqlForUser(Player player) {
        return "SELECT * FROM " + config.getString("sql.prefix") + "locations WHERE uuid = '" + player.getUniqueId().toString() + "'";
    }
}

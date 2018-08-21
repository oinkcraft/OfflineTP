package com.zaphoo.offlinetp;

import com.zaphoo.offlinetp.Commands.OfflineTPCommand;
import com.zaphoo.offlinetp.Commands.OfflineTPHereCommand;
import com.zaphoo.offlinetp.Listeners.OnJoinListener;
import com.zaphoo.offlinetp.Listeners.OnLeaveListener;
import com.zaphoo.offlinetp.utils.SQLManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private String prefix = "§8[§7OfflineTP§8]§7 ";
    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        SQLManager sql = SQLManager.getInstance();
        sql.createTableIfNotExists();

        getServer().getPluginManager().registerEvents(new OnJoinListener(), this);
        getServer().getPluginManager().registerEvents(new OnLeaveListener(), this);
        getCommand("offlinetphere").setExecutor(new OfflineTPHereCommand());
        getCommand("offlinetp").setExecutor(new OfflineTPCommand());
    }


    @Override
    public void onDisable() {



    }

    public static Main getInstance() {
        return instance;
    }

    public String getPrefix() {
        return prefix;
    }
}

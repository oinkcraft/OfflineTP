package com.zaphoo.offlinetp.Listeners;

import com.zaphoo.offlinetp.Main;
import com.zaphoo.offlinetp.utils.SQLManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnJoinListener implements Listener {

    private SQLManager sql = SQLManager.getInstance();
    private String prefix = Main.getInstance().getPrefix();
    private final String MOVED_MESSAGE = ChatColor.GRAY + "------ %s------\nYou were moved between sessions!\nThis happened %s at %s server time.\nThe action was performed by: %s\nYour previous location was: %s";

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new Thread(() -> {

            if (!sql.checkIfExists(player)) {
                sql.postLocationOnLogin(player);
            } else {
                if (sql.getMoved(player)) {
                    Location location = sql.getLocation(player);


                    sql.resetMoved(player);
                    String date = sql.getTime(player)[0];
                    String time = sql.getTime(player)[1];
                    String mover = sql.getMover(player);
                    String movedFrom = sql.getPreviousLocation(player);
                    player.sendMessage(String.format(MOVED_MESSAGE, prefix, date, time, mover, movedFrom));

                    player.teleport(location);

                }
            }
        }).start();
    }
}

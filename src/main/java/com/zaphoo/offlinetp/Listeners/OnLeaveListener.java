package com.zaphoo.offlinetp.Listeners;

import com.zaphoo.offlinetp.Main;
import com.zaphoo.offlinetp.utils.SQLManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnLeaveListener implements Listener {

    private SQLManager sql = SQLManager.getInstance();
    private String prefix = Main.getInstance().getPrefix();

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        new Thread(() -> {
            sql.postLocation(player);
        }).start();
    }

}

package com.zaphoo.offlinetp.utils;

import com.zaphoo.offlinetp.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class OfflineTask extends BukkitRunnable {

    private Main main = Main.getInstance();
    private String prefix = main.getPrefix();
    private SQLManager sql;
    private Player sender;
    private String[] args;
    private String error = prefix + ChatColor.RED;
    private UUID uuid;
    private Location location;

    public OfflineTask(SQLManager sql, Player sender, String[] args) {

        this.sql = sql;
        this.sender = sender;
        this.args = args;


    }


    @Override
    public void run() {
        Future<Void> f = CompletableFuture.supplyAsync(() -> {
            uuid = UUIDFetcher.getUUID(args[0]);
            if (!sql.checkIfExists(uuid)) {
                sender.sendMessage(error + "That player is not in the database!");
                return null;
            }
            sender.sendMessage(prefix + ChatColor.GREEN + "Successfully teleported to " + args[0] + " logout location!");
            sender.sendMessage(ChatColor.DARK_GRAY + "(UUID of player: " + uuid + ")");
            this.location = sql.getLocation(uuid);
            return null;
        });
        try {
            f.get();
            main.getServer().getScheduler().runTask(main, () -> sender.teleport(this.location));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}

package com.zaphoo.offlinetp.Commands;

import com.zaphoo.offlinetp.Main;
import com.zaphoo.offlinetp.utils.OfflineTask;
import com.zaphoo.offlinetp.utils.SQLManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OfflineTPCommand implements CommandExecutor {

    private String prefix = Main.getInstance().getPrefix();
    private SQLManager sql = SQLManager.getInstance();
    private String error = prefix + ChatColor.RED;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(error + "You must be a player to perform that command!");
            return true;
        }
        Player sender = (Player) commandSender;
        if (!sender.hasPermission("otp.tp")) {
            sender.sendMessage(error + "You do not have permission to perform that command!" + ChatColor.DARK_RED + "otp.tp");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(error + "You did not provide enough arguments!");
            return true;
        }
        if (args.length > 1) {
            sender.sendMessage(error + "You provided too many arguments!");
            return true;
        }
        if (Main.getInstance().getServer().getPlayer(args[0]) != null) {
            sender.sendMessage(error + "The player must be offline for you to tp to their offline location!");
            return true;
        }
        new OfflineTask(sql, sender, args).run();
        return true;
    }
}

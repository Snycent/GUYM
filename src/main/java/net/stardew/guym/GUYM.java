package net.stardew.guym;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.md_5.bungee.api.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUYM extends JavaPlugin implements CommandExecutor {

    private final Map<UUID, BukkitTask> playerTasks = new HashMap<>();

    @Override
    public void onEnable() {
        this.getCommand("guym").setExecutor(this);
        getLogger().info("GiveUpYourMind has been enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("GiveUpYourMind plugin has been disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /guym <player> OR /guym me");
            return true;
        }

        if (!player.hasPermission("stardewcore.guym")) {
            player.sendMessage(ChatColor.RED + "You are not part of the Stardew Federation.");
            return true;
        }

        Player target;

        if (args[0].equalsIgnoreCase("me")) {
            target = player;
        } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(ChatColor.RED + "Player not found or is not online.");
                return true;
            }
        }

        applyGiveUpYourMindEffect(target);

        player.sendMessage(ChatColor.GREEN + "The 'Give Up Your Mind' effect has been applied to " + (target.equals(player) ? "yourself" : target.getName()) + ".");

        return true;
    }

    private void applyGiveUpYourMindEffect(Player player) {
        UUID playerId = player.getUniqueId();

        if (playerTasks.containsKey(playerId)) {
            playerTasks.get(playerId).cancel();
            playerTasks.remove(playerId);
        }

        BukkitTask task = new BukkitRunnable() {
            int duration = 60;

            @Override
            public void run() {
                if (duration > 0) {
                    player.sendTitle(ChatColor.RED + "GIVE UP YOUR MIND", "", 10, 40, 20);
                    sendActionBarMessage(player, ChatColor.RED + "give it up");

                    if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                        player.removePotionEffect(PotionEffectType.BLINDNESS);
                    } else {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));
                    }

                    duration--;
                } else {
                    player.sendMessage(ChatColor.GREEN + "You have regained composure.");
                    this.cancel();
                    playerTasks.remove(playerId);
                }
            }
        }.runTaskTimer(this, 0, 20);

        playerTasks.put(playerId, task);

        playSoundsAndApplyEffects(player);
    }

    private void playSoundsAndApplyEffects(Player player) {
        playSoundAtLocation(player, "smusic:guym");
        playSoundAtLocation(player, "smusic:guym_heartbeat");
        playSoundAtLocation(player, "smusic:guym_ambience");

        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 60 * 20, 1));

        Bukkit.getScheduler().runTaskLater(this, () -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 22 * 20, 1));
            playStoneSounds(player);
        }, 60 * 20);
    }

    private void playSoundAtLocation(Player player, String sound) {
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }

    private void playStoneSounds(Player player) {
        player.playSound(player.getLocation(), "block.stone.hit", 1.0f, 1.0f);
        player.playSound(player.getLocation(), "block.stone.break", 1.0f, 1.0f);
    }

    private void sendActionBarMessage(Player player, String message) {
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
    }
}

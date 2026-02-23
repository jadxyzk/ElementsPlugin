package com.elements.listeners;

import com.elements.ElementsPlugin;
import com.elements.utils.Element;
import com.elements.utils.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ElementCommand implements CommandExecutor, TabCompleter {

    private final ElementsPlugin plugin;
    private final boolean isAdmin;

    public ElementCommand(ElementsPlugin plugin, boolean isAdmin) {
        this.plugin = plugin;
        this.isAdmin = isAdmin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!isAdmin) {
            // /element — show own element info
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use /element");
                return true;
            }
            PlayerData data = plugin.getElementManager().getPlayerData(player);
            if (data == null) { player.sendMessage("§cElement data not found."); return true; }

            player.sendMessage("§6════ Your Element ════");
            player.sendMessage("§7Element: " + data.getElement().getColoredName());
            player.sendMessage("§7Level: §e" + data.getLevel() + "§7/§e3");
            player.sendMessage("§7Kills toward next level: §e" + data.getKills() + "§7/§e" + data.getKillsRequired());
            player.sendMessage("§7Deaths toward level loss: §e" + data.getDeaths() + "§7/§e" +
                    plugin.getConfig().getInt("deaths-to-derank", 3));
            player.sendMessage("§6═════════════════════");
            return true;
        }

        // /elementadmin <set|reset> <player> [element]
        if (!sender.hasPermission("elements.admin")) {
            sender.sendMessage("§cNo permission.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /elementadmin <set|reset|setlevel> <player> [element/level]");
            return true;
        }

        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { sender.sendMessage("§cPlayer not found."); return true; }

        PlayerData data = plugin.getElementManager().getPlayerData(target);
        if (data == null) { sender.sendMessage("§cNo data for that player."); return true; }

        switch (action) {
            case "set":
                if (args.length < 3) { sender.sendMessage("§cProvide an element: fire, water, earth, air"); return true; }
                try {
                    Element el = Element.valueOf(args[2].toUpperCase());
                    data.setElement(el);
                    plugin.getElementManager().savePlayer(data);
                    sender.sendMessage("§aSet " + target.getName() + "'s element to " + el.getColoredName());
                    target.sendMessage("§6An admin set your element to " + el.getColoredName());
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("§cInvalid element. Use: fire, water, earth, air");
                }
                break;

            case "reset":
                data.setLevel(1);
                data.setKills(0);
                data.resetDeaths();
                plugin.getElementManager().savePlayer(data);
                sender.sendMessage("§aReset " + target.getName() + "'s progress.");
                target.sendMessage("§cYour elemental progress has been reset by an admin.");
                break;

            case "setlevel":
                if (args.length < 3) { sender.sendMessage("§cProvide a level: 1, 2, or 3"); return true; }
                try {
                    int lvl = Integer.parseInt(args[2]);
                    if (lvl < 1 || lvl > 3) { sender.sendMessage("§cLevel must be 1-3"); return true; }
                    data.setLevel(lvl);
                    data.setKills(0);
                    plugin.getElementManager().savePlayer(data);
                    sender.sendMessage("§aSet " + target.getName() + "'s level to " + lvl);
                    target.sendMessage("§6An admin set your element level to §e" + lvl);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid level number.");
                }
                break;

            default:
                sender.sendMessage("§cUnknown action. Use: set, reset, setlevel");
        }

        plugin.getHudManager().updateHUD(target);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (!isAdmin) return List.of();
        if (args.length == 1) return Arrays.asList("set", "reset", "setlevel");
        if (args.length == 2) return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) return Arrays.asList("fire", "water", "earth", "air");
            if (args[0].equalsIgnoreCase("setlevel")) return Arrays.asList("1", "2", "3");
        }
        return List.of();
    }
}

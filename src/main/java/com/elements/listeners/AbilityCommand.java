package com.elements.listeners;

import com.elements.ElementsPlugin;
import com.elements.utils.Element;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class AbilityCommand implements CommandExecutor, TabCompleter {

    private final ElementsPlugin plugin;

    public AbilityCommand(ElementsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cUsage: /ability <element><number> — e.g. §e/ability fire1");
            return true;
        }

        String input = args[0].toLowerCase();

        // Parse input like "fire1", "water2", "earth3", "air1"
        String elementStr = null;
        int abilityNum = -1;

        for (Element el : Element.values()) {
            String elName = el.name().toLowerCase();
            if (input.startsWith(elName)) {
                String numPart = input.substring(elName.length());
                try {
                    abilityNum = Integer.parseInt(numPart);
                    elementStr = el.name();
                    break;
                } catch (NumberFormatException ignored) {}
            }
        }

        if (elementStr == null || abilityNum < 1 || abilityNum > 3) {
            player.sendMessage("§cInvalid ability. Use: §e/ability <fire|water|earth|air><1-3>");
            player.sendMessage("§7Example: §e/ability fire1§7, §e/ability earth3");
            return true;
        }

        Element element = Element.valueOf(elementStr);
        plugin.getAbilityManager().useAbility(player, element, abilityNum);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList(
                    "fire1", "fire2", "fire3",
                    "water1", "water2", "water3",
                    "earth1", "earth2", "earth3",
                    "air1", "air2", "air3"
            );
        }
        return List.of();
    }
}

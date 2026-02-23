package com.elements.managers;

import com.elements.ElementsPlugin;
import com.elements.utils.Element;
import com.elements.utils.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class HUDManager {

    private final ElementsPlugin plugin;

    public HUDManager(ElementsPlugin plugin) {
        this.plugin = plugin;
        startHUDTask();
    }

    private void startHUDTask() {
        new BukkitRunnable() {
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    updateHUD(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Update every second
    }

    public void updateHUD(Player player) {
        PlayerData data = plugin.getElementManager().getPlayerData(player);
        if (data == null) return;

        Element el = data.getElement();
        int level = data.getLevel();
        String color = el.getColor();
        String elName = el.getDisplayName();

        // Build cooldown display
        StringBuilder sb = new StringBuilder();
        sb.append(color).append("【").append(elName).append(" Lv").append(level).append("】  ");

        Map<String, Long> cooldowns = plugin.getAbilityManager().getCooldowns(player.getUniqueId());

        for (int i = 1; i <= 3; i++) {
            String abilityLabel = getAbilityLabel(el, i);
            String key = el.name() + i;
            long remaining = plugin.getAbilityManager().getRemainingCooldown(player.getUniqueId(), key);

            if (i > level) {
                // Locked
                sb.append("§8[").append(abilityLabel).append(": §8Locked]  ");
            } else if (remaining > 0) {
                // On cooldown
                sb.append("§c[").append(abilityLabel).append(": §e").append((int) Math.ceil(remaining / 1000.0)).append("s§c]  ");
            } else {
                // Ready
                sb.append("§a[").append(abilityLabel).append(": §aReady]  ");
            }
        }

        // Send as action bar
        player.sendActionBar(Component.text(sb.toString()));
    }

    private String getAbilityLabel(Element el, int num) {
        switch (el) {
            case FIRE:
                switch (num) {
                    case 1: return "Ignite";
                    case 2: return "Fireball";
                    case 3: return "Fire Ring";
                }
            case WATER:
                switch (num) {
                    case 1: return "Slow";
                    case 2: return "Burst";
                    case 3: return "Heal";
                }
            case EARTH:
                switch (num) {
                    case 1: return "Launch";
                    case 2: return "Wall";
                    case 3: return "Quake";
                }
            case AIR:
                switch (num) {
                    case 1: return "Dash";
                    case 2: return "Cyclone";
                    case 3: return "Tornado";
                }
        }
        return "Ability " + num;
    }
}

package com.elements.listeners;

import com.elements.ElementsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final ElementsPlugin plugin;

    public PlayerListener(ElementsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getElementManager().initPlayer(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getElementManager().removePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();
        Player killer = dead.getKiller();

        // Handle death (derank system)
        plugin.getElementManager().handleDeath(dead);

        // Handle kill reward
        if (killer != null && killer != dead) {
            plugin.getElementManager().handleKill(killer);
        }
    }
}

package com.elements.listeners;

import com.elements.ElementsPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.block.Action;

import java.util.Arrays;

public class GemListener implements Listener {

    private final ElementsPlugin plugin;
    private final NamespacedKey GEM_KEY;
    private final NamespacedKey ENERGY_KEY;

    public GemListener(ElementsPlugin plugin) {
        this.plugin = plugin;
        GEM_KEY = new NamespacedKey(plugin, "element_change_gem");
        ENERGY_KEY = new NamespacedKey(plugin, "energy_bottle");
        registerRecipes();
    }

    private void registerRecipes() {
        // ── Element Change Gem Recipe ──────────────────────────────────────────
        // Pattern:
        //   D D D
        //   D N D
        //   D D D
        // D = Diamond, N = Nether Star

        ItemStack gem = createGem();
        ShapedRecipe gemRecipe = new ShapedRecipe(GEM_KEY, gem);
        gemRecipe.shape("DDD", "DND", "DDD");
        gemRecipe.setIngredient('D', Material.DIAMOND);
        gemRecipe.setIngredient('N', Material.NETHER_STAR);

        try {
            plugin.getServer().addRecipe(gemRecipe);
        } catch (Exception e) {
            // Recipe already registered
        }

        // ── Energy Bottle Recipe ───────────────────────────────────────────────
        // Pattern:
        //   G G G
        //   G B G
        //   G G G
        // G = Glowstone Dust, B = Glass Bottle

        ItemStack energyBottle = createEnergyBottle();
        ShapedRecipe bottleRecipe = new ShapedRecipe(ENERGY_KEY, energyBottle);
        bottleRecipe.shape("GGG", "GBG", "GGG");
        bottleRecipe.setIngredient('G', Material.GLOWSTONE_DUST);
        bottleRecipe.setIngredient('B', Material.GLASS_BOTTLE);

        try {
            plugin.getServer().addRecipe(bottleRecipe);
        } catch (Exception e) {
            // Recipe already registered
        }
    }

    public ItemStack createGem() {
        ItemStack gem = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = gem.getItemMeta();
        meta.setDisplayName("§5✦ Element Change Gem");
        meta.setLore(Arrays.asList(
                "§7Right-click to be reborn",
                "§7as a new element.",
                "§8Cannot assign your current element."
        ));
        meta.getPersistentDataContainer().set(GEM_KEY, PersistentDataType.BYTE, (byte) 1);
        gem.setItemMeta(meta);
        return gem;
    }

    public ItemStack createEnergyBottle() {
        ItemStack bottle = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = bottle.getItemMeta();
        meta.setDisplayName("§eEnergy Bottle");
        meta.setLore(Arrays.asList(
                "§7Grants elemental experience.",
                "§7Use to progress your element level."
        ));
        meta.getPersistentDataContainer().set(ENERGY_KEY, PersistentDataType.BYTE, (byte) 1);
        bottle.setItemMeta(meta);
        return bottle;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();

        // ── Element Change Gem ─────────────────────────────────────────────────
        if (meta.getPersistentDataContainer().has(GEM_KEY, PersistentDataType.BYTE)) {
            event.setCancelled(true);
            boolean changed = plugin.getElementManager().changeElementWithGem(player);
            if (changed) {
                item.setAmount(item.getAmount() - 1);
            }
            return;
        }

        // ── Energy Bottle ──────────────────────────────────────────────────────
        if (meta.getPersistentDataContainer().has(ENERGY_KEY, PersistentDataType.BYTE)) {
            event.setCancelled(true);
            handleEnergyBottle(player, item);
        }
    }

    private void handleEnergyBottle(Player player, ItemStack item) {
        var data = plugin.getElementManager().getPlayerData(player);
        if (data == null) return;

        if (data.isMaxLevel()) {
            player.sendMessage("§eYou are already at max level!");
            return;
        }

        // Energy bottle counts as 1 kill progress
        data.addKill();
        int required = data.getKillsRequired();

        player.sendMessage("§eEnergy absorbed! Kill progress: §6" + data.getKills() + "§e/§6" + required);

        if (data.getKills() >= required) {
            data.setLevel(data.getLevel() + 1);
            data.setKills(0);
            data.resetDeaths();
            plugin.getElementManager().savePlayer(data);
            player.sendMessage("§6✨ Energy overflowed! " +
                    plugin.getConfig().getString("messages.level-up", "&aLevel up! Now level %level%")
                            .replace("&", "§").replace("%level%", String.valueOf(data.getLevel())));
        } else {
            plugin.getElementManager().savePlayer(data);
        }

        plugin.getHudManager().updateHUD(player);
        item.setAmount(item.getAmount() - 1);
    }
}

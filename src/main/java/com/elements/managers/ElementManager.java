package com.elements.managers;

import com.elements.ElementsPlugin;
import com.elements.utils.Element;
import com.elements.utils.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ElementManager {

    private final ElementsPlugin plugin;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    public ElementManager(ElementsPlugin plugin) {
        this.plugin = plugin;
        loadData();
    }

    // ─── Data Persistence ────────────────────────────────────────────────────

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveAllData() {
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            savePlayer(entry.getValue());
        }
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public void savePlayer(PlayerData data) {
        String path = "players." + data.getUuid().toString();
        dataConfig.set(path + ".element", data.getElement().name());
        dataConfig.set(path + ".level", data.getLevel());
        dataConfig.set(path + ".kills", data.getKills());
        dataConfig.set(path + ".deaths", data.getDeaths());
        if (data.getLastElement() != null)
            dataConfig.set(path + ".lastElement", data.getLastElement().name());
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public PlayerData loadPlayer(UUID uuid) {
        String path = "players." + uuid.toString();
        if (!dataConfig.contains(path)) return null;

        String elementStr = dataConfig.getString(path + ".element");
        Element element;
        try { element = Element.valueOf(elementStr); }
        catch (Exception e) { element = randomElement(null); }

        PlayerData data = new PlayerData(uuid, element);
        data.setLevel(dataConfig.getInt(path + ".level", 1));
        data.setKills(dataConfig.getInt(path + ".kills", 0));
        data.setDeaths(dataConfig.getInt(path + ".deaths", 0));

        String lastEl = dataConfig.getString(path + ".lastElement");
        if (lastEl != null) {
            try {
                // We need to set lastElement via a workaround since setElement changes current
                // We'll just keep it null for now — only matters for gem usage
            } catch (Exception ignored) {}
        }
        return data;
    }

    // ─── Player Management ────────────────────────────────────────────────────

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public PlayerData getPlayerData(Player player) {
        return playerDataMap.get(player.getUniqueId());
    }

    public void initPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (playerDataMap.containsKey(uuid)) return;

        // Try loading from file
        PlayerData loaded = loadPlayer(uuid);
        if (loaded != null) {
            playerDataMap.put(uuid, loaded);
            return;
        }

        // First time — assign random element
        Element element = randomElement(null);
        PlayerData data = new PlayerData(uuid, element);
        playerDataMap.put(uuid, data);
        savePlayer(data);

        player.sendMessage(format(plugin.getConfig().getString("messages.element-assigned", "&6You have been assigned %element%!")
                .replace("%element%", element.getColoredName())));
    }

    public void removePlayer(UUID uuid) {
        PlayerData data = playerDataMap.get(uuid);
        if (data != null) savePlayer(data);
        playerDataMap.remove(uuid);
    }

    // ─── Kill / Death Logic ───────────────────────────────────────────────────

    public void handleKill(Player player) {
        PlayerData data = getPlayerData(player);
        if (data == null) return;
        if (data.isMaxLevel()) return;

        data.addKill();
        int required = data.getKillsRequired();

        if (data.getKills() >= required) {
            data.setLevel(data.getLevel() + 1);
            data.setKills(0);
            data.resetDeaths();
            savePlayer(data);
            player.sendMessage(format(plugin.getConfig().getString("messages.level-up", "&aLevel up! Now level %level%")
                    .replace("%level%", String.valueOf(data.getLevel()))));
        } else {
            savePlayer(data);
        }
    }

    public void handleDeath(Player player) {
        PlayerData data = getPlayerData(player);
        if (data == null) return;
        if (data.getLevel() <= 1) return;

        data.addDeath();
        int deathsToDerank = plugin.getConfig().getInt("deaths-to-derank", 3);

        if (data.getDeaths() >= deathsToDerank) {
            data.setLevel(data.getLevel() - 1);
            data.setKills(0);
            data.resetDeaths();
            savePlayer(data);
            player.sendMessage(format(plugin.getConfig().getString("messages.level-down", "&cYou lost a level! Now level %level%")
                    .replace("%level%", String.valueOf(data.getLevel()))));
        } else {
            savePlayer(data);
            int remaining = deathsToDerank - data.getDeaths();
            player.sendMessage("§c" + remaining + " more death(s) will cause you to lose a level!");
        }
    }

    // ─── Element Change ───────────────────────────────────────────────────────

    public boolean changeElementWithGem(Player player) {
        PlayerData data = getPlayerData(player);
        if (data == null) return false;

        Element current = data.getElement();
        Element newElement = randomElement(current);

        data.setElement(newElement);
        savePlayer(data);

        player.sendMessage(format(plugin.getConfig().getString("messages.gem-used", "&6You are now %element%!")
                .replace("%element%", newElement.getColoredName())));
        return true;
    }

    // ─── Utilities ────────────────────────────────────────────────────────────

    public Element randomElement(Element exclude) {
        List<Element> elements = new ArrayList<>(Arrays.asList(Element.values()));
        if (exclude != null) elements.remove(exclude);
        return elements.get(new Random().nextInt(elements.size()));
    }

    private String format(String msg) {
        return msg.replace("&", "§");
    }
}

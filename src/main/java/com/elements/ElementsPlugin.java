package com.elements;

import com.elements.listeners.AbilityCommand;
import com.elements.listeners.ElementCommand;
import com.elements.listeners.GemListener;
import com.elements.listeners.PlayerListener;
import com.elements.managers.AbilityManager;
import com.elements.managers.ElementManager;
import com.elements.managers.HUDManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ElementsPlugin extends JavaPlugin {

    private ElementManager elementManager;
    private AbilityManager abilityManager;
    private HUDManager hudManager;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Init managers
        elementManager = new ElementManager(this);
        abilityManager = new AbilityManager(this);
        hudManager = new HUDManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        GemListener gemListener = new GemListener(this);
        getServer().getPluginManager().registerEvents(gemListener, this);

        // Register commands
        getCommand("ability").setExecutor(new AbilityCommand(this));
        getCommand("ability").setTabCompleter(new AbilityCommand(this));

        ElementCommand elementCmd = new ElementCommand(this, false);
        getCommand("element").setExecutor(elementCmd);
        getCommand("element").setTabCompleter(elementCmd);

        ElementCommand adminCmd = new ElementCommand(this, true);
        getCommand("elementadmin").setExecutor(adminCmd);
        getCommand("elementadmin").setTabCompleter(adminCmd);

        getLogger().info("ElementsPlugin enabled! Four elements awakened.");
    }

    @Override
    public void onDisable() {
        elementManager.saveAllData();
        getLogger().info("ElementsPlugin disabled. All player data saved.");
    }

    public ElementManager getElementManager() { return elementManager; }
    public AbilityManager getAbilityManager() { return abilityManager; }
    public HUDManager getHudManager() { return hudManager; }
}

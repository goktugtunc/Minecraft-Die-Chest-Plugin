package com.gotunc.playerChestDiePlugin;

import com.gotunc.playerChestDiePlugin.Listeners.InventoryControl;
import com.gotunc.playerChestDiePlugin.Listeners.PlayerDeath;
import com.gotunc.playerChestDiePlugin.Listeners.blockBreak;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerChestDiePlugin extends JavaPlugin {

    public static JavaPlugin instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("PlayerChestDiePlugin Enabled");
        instance = this;
        Bukkit.getPluginManager().registerEvents(new PlayerDeath(), this);
        Bukkit.getPluginManager().registerEvents(new blockBreak(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryControl(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("PlayerChestDiePlugin Disabled");
    }
}

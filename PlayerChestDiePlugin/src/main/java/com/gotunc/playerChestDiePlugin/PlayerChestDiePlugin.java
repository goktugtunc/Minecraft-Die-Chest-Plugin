package com.gotunc.playerChestDiePlugin;

import com.gotunc.playerChestDiePlugin.Listeners.InventoryControl;
import com.gotunc.playerChestDiePlugin.Listeners.PlayerDeath;
import com.gotunc.playerChestDiePlugin.Listeners.blockBreak;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;

public final class PlayerChestDiePlugin extends JavaPlugin {

    public static JavaPlugin instance;
    public static Connection connection;
    public static float randomChestCost = 20.0f;
    public static String randomChestCostCurrency = "MineCoin";
    public static float nearestChestCost = 100.0f;
    public static String nearestChestCostCurrency = "MineCoin";

    private void connectDatabase()
    {
        try
        {
            String url = "jdbc:mysql://xxx.xxx:xxx/xxx?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            String user = "xxx";
            String password = "xxx";
            connection = DriverManager.getConnection(url, user,password);
            getLogger().info("Connected to database!");
        }
        catch (Exception e)
        {
            getLogger().info("Failed to connect to database!");
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("PlayerChestDiePlugin Enabled");
        instance = this;
        connectDatabase();
        Bukkit.getPluginManager().registerEvents(new PlayerDeath(), this);
        Bukkit.getPluginManager().registerEvents(new blockBreak(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryControl(), this);
        getCommand("findDieChest").setExecutor(new PlayerChestDieCommand());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("PlayerChestDiePlugin Disabled");
    }
}

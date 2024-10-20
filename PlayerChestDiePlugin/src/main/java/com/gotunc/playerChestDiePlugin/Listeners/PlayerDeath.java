package com.gotunc.playerChestDiePlugin.Listeners;

import com.gotunc.playerChestDiePlugin.PlayerChestDiePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;

public class PlayerDeath implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getDrops().isEmpty())
            return;
        boolean isDoubleChest;
        if (event.getDrops().size() > 27)
            isDoubleChest = true;
        else
            isDoubleChest = false;
        Location location = event.getEntity().getLocation(); // Oyuncunun öldüğü konumu al
        Block block = location.getBlock();
        if (isDoubleChest)
        {
            while (!block.getType().isAir() || !block.getRelative(1, 0, 0).getType().isAir()) {
                location.add(0, 1, 0);
                block = location.getBlock();
            }
        }
        else
        {
            while (!block.getType().isAir()) {
                location.add(0, 1, 0);
                block = location.getBlock();
            }
        }

        try{
            String query = "INSERT INTO DieChests (World, X, Y, Z) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = PlayerChestDiePlugin.connection.prepareStatement(query);
            statement.setString(1, location.getWorld().getName());
            statement.setInt(2, location.getBlockX());
            statement.setInt(3, location.getBlockY());
            statement.setInt(4, location.getBlockZ());
            statement.executeUpdate();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }

        // Sandık yerleştir
        block.setType(Material.CHEST);

        // İlk sandığı al ve envanterini oluştur
        Chest chest = (Chest) block.getState();
        Inventory chestInventory = chest.getInventory();
        chest.setCustomName(ChatColor.AQUA + "[RAHMETLI] " + ChatColor.RED + ChatColor.STRIKETHROUGH + event.getEntity().getName());
        chest.update();

        // İkinci sandığı oluştur
        Chest secondChest = null;
        if (isDoubleChest)
        {
            Location secondChestLocation = location.clone().add(1, 0, 0); // Yanına eklemek için
            Block secondBlock = secondChestLocation.getBlock();
            secondBlock.setType(Material.CHEST); // İkinci sandığı oluştur
            secondChest = (Chest) secondBlock.getState();
            secondChest.setCustomName(ChatColor.AQUA + "[RAHMETLI] " + ChatColor.RED + ChatColor.STRIKETHROUGH + event.getEntity().getName());
            secondChest.update();
        }
        // İkinci sandığın envanteri
        Inventory secondChestInventory = null;
        if (secondChest != null)
            secondChestInventory = secondChest.getInventory();
        // Sandığın önüne bir tabela yerleştir
        Location signLocation = location.clone().add(0, 0, -1); // Sandığın önündeki bloğa tabela
        Block signBlock = signLocation.getBlock();
        signBlock.setType(Material.OAK_WALL_SIGN); // Duvar tabelası oluştur

        WallSign wallSign = (WallSign) signBlock.getBlockData();
        Directional directional = (Directional) chest.getBlockData();
        wallSign.setFacing(directional.getFacing()); // Tabela sandığa bakacak şekilde yönlendir
        signBlock.setBlockData(wallSign);

        Sign sign = (Sign) signBlock.getState();
        sign.setLine(0, ChatColor.AQUA + "[RAHMETLI]"); // İlk satıra yazı ekle
        sign.setLine(1, ChatColor.RED + "" + ChatColor.STRIKETHROUGH + event.getEntity().getName()); // İkinci satıra oyuncu ismi ekle
        sign.update();

        // Eşyaları sandıklara ekle
        int dropCount = 0;
        for (ItemStack item : event.getDrops()) {
            if (item != null) {
                // Eşyalar sığmazsa ikinci sandığa ekle
                if (dropCount < 27) {
                    chestInventory.addItem(item);
                } else {
                    secondChestInventory.addItem(item);
                }
                dropCount++;
            }
        }

        // Eşyaların yere düşmesini engelle
        event.getDrops().clear();
    }
}
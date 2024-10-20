package com.gotunc.playerChestDiePlugin.Listeners;

import com.gotunc.playerChestDiePlugin.PlayerChestDiePlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.PreparedStatement;

public class InventoryControl implements Listener {

    private void removeSign(Block block)
    {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        for (BlockFace face : faces) {
            Block adjacentBlock = block.getRelative(face); // Yanındaki bloğu al

            // Eğer bu blok bir duvar tabelasıysa
            if (adjacentBlock.getType() == Material.OAK_WALL_SIGN) {
                // Eğer tabela bu sandığa yapışık ise
                if (((Sign) adjacentBlock.getState()).getLine(0).contains(ChatColor.AQUA + "[RAHMETLI]")) {
                    Sign sign = (Sign) adjacentBlock.getState(); // Tabelayı al
                    World world = block.getWorld();
                    Location location = block.getLocation();
                    sign.getBlock().setType(Material.AIR); // Tabelayı kaldır
                    world.playSound(location, Sound.BLOCK_WOOD_BREAK, 1, 1);
                    world.playEffect(location, Effect.STEP_SOUND, Material.OAK_WALL_SIGN);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof Chest)
        {
            Chest chest = (Chest) event.getInventory().getHolder();
            if (chest.getCustomName() != null && chest.getCustomName().contains(ChatColor.AQUA + "[RAHMETLI]"))
            {
                Inventory chestInventory = chest.getInventory();
                Block secondBlock = null;
                Inventory secondChestInventory = null;
                if (chestInventory.getSize() > 27)
                {
                    Location location = chest.getLocation();
                    Location secondChestLocation = location.clone().add(1, 0, 0);
                    secondBlock = secondChestLocation.getBlock();
                    Chest secondChest = (Chest) secondBlock.getState();
                    secondChestInventory = secondChest.getInventory();
                }
                int itemCount = 0;
                for (ItemStack item : chestInventory.getContents())
                {
                    if (item != null)
                        itemCount++;
                }
                if (itemCount == 0)
                {
                    removeSign(chest.getBlock());
                    chest.getBlock().setType(Material.AIR);
                    chest.getBlock().getWorld().playSound(chest.getBlock().getLocation(), Sound.BLOCK_WOOD_BREAK, 1, 1);
                    chest.getBlock().getWorld().playEffect(chest.getBlock().getLocation(), Effect.STEP_SOUND, Material.CHEST);
                    Location location = chest.getBlock().getLocation();
                    ItemStack compass = new ItemStack(Material.COMPASS);
                    CompassMeta meta = (CompassMeta) compass.getItemMeta();
                    meta.setLodestone(location);
                    meta.setLodestoneTracked(false);
                    meta.setDisplayName(ChatColor.AQUA + "X: " + location.getBlockX() + " Y: " + location.getBlockY() + " Z: " + location.getBlockZ());
                    compass.setItemMeta(meta);
                    if (event.getPlayer().getInventory().contains(compass))
                        event.getPlayer().getInventory().removeItem(compass);
                    try{
                        String query = "DELETE FROM DieChests WHERE World = ? AND X = ? AND Y = ? AND Z = ?";
                        PreparedStatement statement = PlayerChestDiePlugin.connection.prepareStatement(query);
                        statement.setString(1, chest.getBlock().getWorld().getName());
                        statement.setInt(2, chest.getBlock().getX());
                        statement.setInt(3, chest.getBlock().getY());
                        statement.setInt(4, chest.getBlock().getZ());
                        statement.executeUpdate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (secondBlock != null)
                {
                    itemCount = 0;
                    for (ItemStack item : secondChestInventory.getContents())
                    {
                        if (item != null)
                            itemCount++;
                    }
                    if (itemCount == 0)
                    {
                        removeSign(secondBlock);
                        secondBlock.setType(Material.AIR);
                    }
                }
            }
        }
    }
}
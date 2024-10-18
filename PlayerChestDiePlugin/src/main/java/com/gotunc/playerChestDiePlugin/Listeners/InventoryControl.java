package com.gotunc.playerChestDiePlugin.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryControl implements Listener {

    private void removeSign(Block block)
    {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        for (BlockFace face : faces) {
            Block adjacentBlock = block.getRelative(face); // Yanındaki bloğu al

            // Eğer bu blok bir duvar tabelasıysa
            if (adjacentBlock.getType() == Material.OAK_WALL_SIGN) {

                WallSign wallSign = (WallSign) adjacentBlock.getBlockData(); // Tabela verisini al
                BlockFace attachedFace = wallSign.getFacing().getOppositeFace(); // Tabelanın yapışık olduğu bloğun yönünü bul

                // Eğer tabela bu sandığa yapışık ise
                if (((Sign) adjacentBlock.getState()).getLine(0).contains(ChatColor.AQUA + "[RAHMETLI]")) {
                    Sign sign = (Sign) adjacentBlock.getState(); // Tabelayı al
                    sign.getBlock().setType(Material.AIR); // Tabelayı kaldır

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
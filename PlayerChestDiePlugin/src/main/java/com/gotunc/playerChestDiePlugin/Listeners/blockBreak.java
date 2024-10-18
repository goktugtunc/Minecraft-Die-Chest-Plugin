package com.gotunc.playerChestDiePlugin.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class blockBreak implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().isOp() || event.getPlayer().hasPermission("playerchestdieplugin.breakBlock"))
            return;
        if (event.getBlock().getType() == Material.CHEST)
        {
            Block block = event.getBlock();
            Chest chest = (Chest) block.getState();
            if (chest.getCustomName() != null && chest.getCustomName().contains(ChatColor.AQUA + "[RAHMETLI]"))
            {
                event.getPlayer().sendMessage(ChatColor.RED + "Bir uyanık sen değilsin. Bu sandığı kıramazsın!");
                event.setCancelled(true);
            }
        }
        else if (event.getBlock().getType() == Material.OAK_WALL_SIGN && event.getBlock().getBlockData() instanceof WallSign)
        {
            Block block = event.getBlock();
            Directional sign = (Directional) block.getBlockData();
            Sign signBlock = (Sign) block.getState();
            BlockFace face = sign.getFacing();
            Block attachedBlock = block.getRelative(face.getOppositeFace());
            if (attachedBlock.getType() == Material.CHEST && ((Chest) attachedBlock.getState()).getCustomName().contains(ChatColor.AQUA + "[RAHMETLI]") && signBlock.getLine(0).contains(ChatColor.AQUA + "[RAHMETLI]"))
            {
                event.getPlayer().sendMessage(ChatColor.RED + "Bir uyanık sen değilsin. Bu tabelayı kıramazsın!");
                event.setCancelled(true);
            }
        }
    }
}
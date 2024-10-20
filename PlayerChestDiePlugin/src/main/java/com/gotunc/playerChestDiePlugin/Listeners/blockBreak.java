package com.gotunc.playerChestDiePlugin.Listeners;

import com.gotunc.playerChestDiePlugin.PlayerChestDiePlugin;
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

import java.sql.PreparedStatement;

public class blockBreak implements Listener {

    private void deleteChestFromDatabase(Block block)
    {
        try{
            String query = "DELETE FROM DieChests WHERE World = ? AND X = ? AND Y = ? AND Z = ?";
            PreparedStatement statement = PlayerChestDiePlugin.connection.prepareStatement(query);
            statement.setString(1, block.getWorld().getName());
            statement.setInt(2, block.getX());
            statement.setInt(3, block.getY());
            statement.setInt(4, block.getZ());
            statement.executeUpdate();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().isOp() || event.getPlayer().hasPermission("playerchestdieplugin.breakBlock"))
        {
            deleteChestFromDatabase(event.getBlock());
            return;
        }
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
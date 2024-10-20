package com.gotunc.playerChestDiePlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PlayerChestDieCommand implements CommandExecutor {

    private String getPlayerLanguage(String playerName)
    {
        try{
            String query = "SELECT Language FROM PlayerInformations WHERE GameName = ?";
            PreparedStatement preparedStatement = PlayerChestDiePlugin.connection.prepareStatement(query);
            preparedStatement.setString(1, playerName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return resultSet.getString("Language");
            else
                return "EN";
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "EN";
    }

    private float getPlayerMoney(String playerName, String currency)
    {
        try{
            String query = "SELECT " + currency + " FROM PlayerInformations WHERE GameName = ?";
            PreparedStatement preparedStatement = PlayerChestDiePlugin.connection.prepareStatement(query);
            preparedStatement.setString(1, playerName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return resultSet.getFloat(currency);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    private void setNewMoney(String playerName, String currency, float newMoney)
    {
        try{
            String query = "UPDATE PlayerInformations SET " + currency + " = ? WHERE GameName = ?";
            PreparedStatement preparedStatement = PlayerChestDiePlugin.connection.prepareStatement(query);
            preparedStatement.setFloat(1, newMoney);
            preparedStatement.setString(2, playerName);
            preparedStatement.executeUpdate();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void findRandomChest(Player player)
    {
        if (getPlayerMoney(player.getName(), PlayerChestDiePlugin.randomChestCostCurrency) < PlayerChestDiePlugin.randomChestCost)
        {
            player.sendMessage(getPlayerLanguage(player.getName()).equalsIgnoreCase("TR") ? "Bu işlemi yapmak için yeterli paranız yok" : "You don't have enough money to do this");
            return;
        }
        try{
            String query = "SELECT * FROM DieChests WHERE World = ? ORDER BY RAND() LIMIT 1";
            PreparedStatement statement = PlayerChestDiePlugin.connection.prepareStatement(query);
            statement.setString(1, player.getWorld().getName());
            ResultSet rs = statement.executeQuery();
            if (rs.next())
            {
                setNewMoney(player.getName(), PlayerChestDiePlugin.randomChestCostCurrency, getPlayerMoney(player.getName(), PlayerChestDiePlugin.randomChestCostCurrency) - PlayerChestDiePlugin.randomChestCost);
                player.sendMessage(getPlayerLanguage(player.getName()).equalsIgnoreCase("TR") ? "Rastgele bir sandık bulundu" : "A random chest has been found");
                Location location = new Location(Bukkit.getWorld(rs.getString("World")), rs.getInt("X"), rs.getInt("Y"), rs.getInt("Z"));
                ItemStack compass = new ItemStack(Material.COMPASS);
                CompassMeta meta = (CompassMeta) compass.getItemMeta();
                meta.setLodestone(location);
                meta.setLodestoneTracked(false);
                meta.setDisplayName(ChatColor.AQUA + "X: " + location.getBlockX() + " Y: " + location.getBlockY() + " Z: " + location.getBlockZ());
                compass.setItemMeta(meta);
                boolean isAdded = false;
                Inventory playerInventory = player.getInventory();
                for (int i = 0; i < 36; i++)
                {
                    if (playerInventory.getItem(i) == null || playerInventory.getItem(i).getType() == Material.AIR)
                    {
                        playerInventory.setItem(i, compass);
                        isAdded = true;
                        break;
                    }
                }
                if (isAdded) // envanterinde boş yer olup olmama durumuna göre envanteri güncelle veya itemi düşür
                    player.updateInventory();
                else
                    player.getWorld().dropItem(player.getLocation(), compass);
                String deleteQuery = "DELETE FROM DieChests WHERE World = ? AND X = ? AND Y = ? AND Z = ?";
                PreparedStatement deleteStatement = PlayerChestDiePlugin.connection.prepareStatement(deleteQuery);
                deleteStatement.setString(1, location.getWorld().getName());
                deleteStatement.setInt(2, location.getBlockX());
                deleteStatement.setInt(3, location.getBlockY());
                deleteStatement.setInt(4, location.getBlockZ());
                deleteStatement.executeUpdate();
            }
            else
                player.sendMessage(ChatColor.RED + (getPlayerLanguage(player.getName()).equalsIgnoreCase("TR") ? "Hiç sandık bulunamadı. Bir dahaki sefere dostum." : "No chest found. Next time buddy."));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void findNearestChest(CommandSender commandSender)
    {
        if (getPlayerMoney(commandSender.getName(), PlayerChestDiePlugin.nearestChestCostCurrency) < PlayerChestDiePlugin.nearestChestCost)
        {
            commandSender.sendMessage(getPlayerLanguage(commandSender.getName()).equalsIgnoreCase("TR") ? "Bu işlemi yapmak için yeterli paranız yok" : "You don't have enough money to do this");
            return;
        }
        try{
            if (!(commandSender instanceof Player))
            {
                commandSender.sendMessage(getPlayerLanguage(commandSender.getName()).equalsIgnoreCase("TR") ? "Bu komut yalnızca oyuncular tarafından kullanılabilir" : "This command can only be executed by players");
                return;
            }
            Player player = (Player) commandSender;
            String query = "SELECT * FROM DieChests WHERE World = ? ORDER BY ABS(X - ?) + ABS(Y - ?) + ABS(Z - ?) LIMIT 1";
            PreparedStatement statement = PlayerChestDiePlugin.connection.prepareStatement(query);
            statement.setString(1, player.getWorld().getName());
            statement.setInt(2, player.getLocation().getBlockX());
            statement.setInt(3, player.getLocation().getBlockY());
            statement.setInt(4, player.getLocation().getBlockZ());
            ResultSet rs = statement.executeQuery();
            if (rs.next())
            {
                setNewMoney(commandSender.getName(), PlayerChestDiePlugin.nearestChestCostCurrency, getPlayerMoney(commandSender.getName(), PlayerChestDiePlugin.nearestChestCostCurrency) - PlayerChestDiePlugin.nearestChestCost);
                player.sendMessage(getPlayerLanguage(player.getName()).equalsIgnoreCase("TR") ? "En yakın sandık bulundu" : "The nearest chest has been found");
                Location location = new Location(Bukkit.getWorld(rs.getString("World")), rs.getInt("X"), rs.getInt("Y"), rs.getInt("Z"));
                ItemStack compass = new ItemStack(Material.COMPASS);
                CompassMeta meta = (CompassMeta) compass.getItemMeta();
                meta.setLodestone(location);
                meta.setLodestoneTracked(false);
                meta.setDisplayName(ChatColor.AQUA + "X: " + location.getBlockX() + " Y: " + location.getBlockY() + " Z: " + location.getBlockZ());
                compass.setItemMeta(meta);
                boolean isAdded = false;
                Inventory playerInventory = player.getInventory();
                for (int i = 0; i < 36; i++)
                {
                    if (playerInventory.getItem(i) == null || playerInventory.getItem(i).getType() == Material.AIR)
                    {
                        playerInventory.setItem(i, compass);
                        isAdded = true;
                        break;
                    }
                }
                if (isAdded) // envanterinde boş yer olup olmama durumuna göre envanteri güncelle veya itemi düşür
                    player.updateInventory();
                else
                    player.getWorld().dropItem(player.getLocation(), compass);
                String deleteQuery = "DELETE FROM DieChests WHERE World = ? AND X = ? AND Y = ? AND Z = ?";
                PreparedStatement deleteStatement = PlayerChestDiePlugin.connection.prepareStatement(deleteQuery);
                deleteStatement.setString(1, location.getWorld().getName());
                deleteStatement.setInt(2, location.getBlockX());
                deleteStatement.setInt(3, location.getBlockY());
                deleteStatement.setInt(4, location.getBlockZ());
                deleteStatement.executeUpdate();
            }
            else
                player.sendMessage(ChatColor.RED + (getPlayerLanguage(player.getName()).equalsIgnoreCase("TR") ? "Hiç sandık bulunamadı. Bir dahaki sefere dostum." : "No chest found. Next time buddy."));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        try {
            if (strings.length != 1) {
                commandSender.sendMessage(getPlayerLanguage(commandSender.getName()).equalsIgnoreCase("TR") ? "Bu komut yalnızca tek argümanlı çalışabilir" : "This command can only be executed with a single argument");
                return true;
            }
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage("Bu komut yalnızca oyuncular tarafından kullanılabilir");
                return true;
            }
            if (strings[0].equalsIgnoreCase("findRandom") || strings[0].equalsIgnoreCase("findNearest")) {
                if (strings[0].equalsIgnoreCase("findRandom"))
                    findRandomChest((Player) commandSender);
                else
                    findNearestChest(commandSender);
                return true;
            }
            else
                commandSender.sendMessage(getPlayerLanguage(commandSender.getName()).equalsIgnoreCase("TR") ? "Geçersiz argüman" : "Invalid argument");
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
package org.example.categoryhopper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class CategoryHopperPlugin extends JavaPlugin {

    public static CategoryHopperPlugin instance;
    public static NamespacedKey CATEGORY_KEY;
    public static NamespacedKey IS_FILTER_HOPPER_KEY;
    private CategoryManager categoryManager;

    @Override
    public void onEnable() {
        instance = this;
        // Keys for storing data on items and blocks
        CATEGORY_KEY = new NamespacedKey(this, "filter_category");
        IS_FILTER_HOPPER_KEY = new NamespacedKey(this, "is_filter_hopper");

        // Initialize Categories
        categoryManager = new CategoryManager();

        // Register Events
        getServer().getPluginManager().registerEvents(new HopperListener(this), this);

        getLogger().info("CategoryHoppers enabled for 1.21!");
    }

    public CategoryManager getCategoryManager() {
        return categoryManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("givehopper")) {
            // Permission check
            if (!sender.hasPermission("categoryhopper.admin")) {
                sender.sendMessage(ChatColor.RED + "No permission.");
                return true;
            }

            Player target = null;
            int amount = 1; // Default to 1 hopper

            // Case 1: /givehopper (Gives 1 to self)
            if (args.length == 0) {
                if (sender instanceof Player) {
                    target = (Player) sender;
                } else {
                    sender.sendMessage(ChatColor.RED + "Console must specify a player: /givehopper <player> [amount]");
                    return true;
                }
            }
            // Case 2: /givehopper <player> (Gives 1 to target)
            else if (args.length >= 1) {
                target = Bukkit.getPlayer(args[0]);
            }

            // Check if player exists
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + args[0] + "' not found.");
                return true;
            }

            // Case 3: /givehopper <player> <amount>
            if (args.length >= 2) {
                try {
                    amount = Integer.parseInt(args[1]);
                    if (amount < 1) amount = 1; // Prevent negative/zero numbers
                    if (amount > 64) amount = 64; // Cap at stack limit (optional)
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid number: " + args[1]);
                    return true;
                }
            }

            // Create the item and set the amount
            ItemStack hopperItem = getFilterHopperItem();
            hopperItem.setAmount(amount);

            // Give to player
            target.getInventory().addItem(hopperItem);

            // Success messages
            String hopperName = "Category Filter Hopper" + (amount > 1 ? "s" : "");
            target.sendMessage(ChatColor.GREEN + "Received " + amount + " " + hopperName + "!");
            if (!sender.equals(target)) {
                sender.sendMessage(ChatColor.GREEN + "Given " + amount + " hopper(s) to " + target.getName());
            }

            return true;
        }
        return false;
    }

    // Creates the special item
    public ItemStack getFilterHopperItem() {
        ItemStack hopper = new ItemStack(Material.HOPPER);
        ItemMeta meta = hopper.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Category Filter Hopper");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Place this down and");
        lore.add(ChatColor.GRAY + "Right-Click to select a category.");
        meta.setLore(lore);

        // Mark this item as special
        meta.getPersistentDataContainer().set(IS_FILTER_HOPPER_KEY, PersistentDataType.BYTE, (byte) 1);

        hopper.setItemMeta(meta);
        return hopper;
    }


}
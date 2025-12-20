package org.example.categoryhopper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;

public class HopperListener implements Listener {

    private final CategoryHopperPlugin plugin;

    public HopperListener(CategoryHopperPlugin plugin) {
        this.plugin = plugin;
    }

    // 1. Handle Placing the Special Hopper
    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();

        if (item.getType() == Material.HOPPER && item.getItemMeta() != null) {
            PersistentDataContainer itemPDC = item.getItemMeta().getPersistentDataContainer();
            
            // Check if it is our special hopper
            if (itemPDC.has(CategoryHopperPlugin.IS_FILTER_HOPPER_KEY, PersistentDataType.BYTE)) {
                Container hopper = (Container) event.getBlockPlaced().getState();
                
                // Mark the block as special
                hopper.getPersistentDataContainer().set(
                    CategoryHopperPlugin.IS_FILTER_HOPPER_KEY, 
                    PersistentDataType.BYTE, 
                    (byte) 1
                );
                
                // Set default category (NONE)
                hopper.getPersistentDataContainer().set(
                    CategoryHopperPlugin.CATEGORY_KEY, 
                    PersistentDataType.STRING, 
                    "NONE"
                );
                
                hopper.update();
                event.getPlayer().sendMessage(ChatColor.GREEN + "Filter Hopper placed!");
            }
        }
    }

    // 2. Handle Opening the GUI
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().name().contains("RIGHT_CLICK_BLOCK")) {
            Block block = event.getClickedBlock();
            if (block != null && block.getState() instanceof Hopper) {
                Hopper hopper = (Hopper) block.getState();
                PersistentDataContainer pdc = hopper.getPersistentDataContainer();

                // Only open GUI if it's a Filter Hopper AND player is sneaking
                if (pdc.has(CategoryHopperPlugin.IS_FILTER_HOPPER_KEY, PersistentDataType.BYTE)) {
                    if (event.getPlayer().isSneaking()) {
                        event.setCancelled(true); // Don't open normal hopper inventory
                        openCategoryGUI(event.getPlayer(), hopper);
                    }
                }
            }
        }
    }

    private void openCategoryGUI(Player player, Hopper hopper) {
        // 4 rows (36 slots) to fit 28 categories
        Inventory gui = Bukkit.createInventory(hopper, 36, ChatColor.DARK_BLUE + "Select Filter Category");

        String currentCat = hopper.getPersistentDataContainer().get(
            CategoryHopperPlugin.CATEGORY_KEY, 
            PersistentDataType.STRING
        );

        for (CategoryManager.Category cat : CategoryManager.Category.values()) {
            ItemStack icon = new ItemStack(cat.icon);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + cat.displayName);
            
            if (cat.name().equals(currentCat)) {
                meta.setLore(Collections.singletonList(ChatColor.GREEN + "Currently Selected"));
                meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true); // Visual glow
            } else {
                meta.setLore(Collections.singletonList(ChatColor.GRAY + "Click to select"));
            }
            
            icon.setItemMeta(meta);
            gui.addItem(icon);
        }

        // Add a "Clear/Disable" button at the end
        ItemStack clear = new ItemStack(Material.BARRIER);
        ItemMeta cm = clear.getItemMeta();
        cm.setDisplayName(ChatColor.RED + "Disable Filter");
        clear.setItemMeta(cm);
        gui.setItem(35, clear);

        player.openInventory(gui);
    }

    // 3. Handle GUI Clicks
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.DARK_BLUE + "Select Filter Category")) {
            event.setCancelled(true); // Prevent taking items
            
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            if (!(event.getInventory().getHolder() instanceof Hopper)) return;

            Hopper hopper = (Hopper) event.getInventory().getHolder();
            Player player = (Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();

            String selectedCategory = "NONE";

            if (clicked.getType() == Material.BARRIER) {
                player.sendMessage(ChatColor.RED + "Filter disabled.");
            } else {
                // Find which category matches the clicked icon name
                for (CategoryManager.Category cat : CategoryManager.Category.values()) {
                    if (clicked.getItemMeta().getDisplayName().contains(cat.displayName)) {
                        selectedCategory = cat.name();
                        player.sendMessage(ChatColor.GREEN + "Category set to: " + cat.displayName);
                        break;
                    }
                }
            }

            // Save to Block
            hopper.getPersistentDataContainer().set(
                CategoryHopperPlugin.CATEGORY_KEY, 
                PersistentDataType.STRING, 
                selectedCategory
            );
            hopper.update();
            player.closeInventory();
        }
    }

    // 4. The Filter Logic (Item Moving into Hopper)
    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (event.getDestination().getHolder() instanceof Hopper) {
            Hopper hopper = (Hopper) event.getDestination().getHolder();
            checkAndFilter(hopper, event.getItem(), event);
        }
    }

    // 5. The Filter Logic (Hopper Picking up Item)
    @EventHandler
    public void onPickup(InventoryPickupItemEvent event) {
        if (event.getInventory().getHolder() instanceof Hopper) {
            Hopper hopper = (Hopper) event.getInventory().getHolder();
            checkAndFilter(hopper, event.getItem().getItemStack(), event);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        // 1. Check if it's a Hopper
        if (block.getType() != Material.HOPPER) return;

        // 2. Check for our special key in the block's data
        if (block.getState() instanceof Hopper) {
            Hopper hopper = (Hopper) block.getState();
            PersistentDataContainer pdc = hopper.getPersistentDataContainer();

            if (pdc.has(CategoryHopperPlugin.IS_FILTER_HOPPER_KEY, PersistentDataType.BYTE)) {

                // 3. Stop the normal vanilla hopper from dropping
                event.setDropItems(false);

                // 4. Get the current category so we can save it (Optional, but nice feature)
                String savedCategory = pdc.get(CategoryHopperPlugin.CATEGORY_KEY, PersistentDataType.STRING);

                // 5. Create the special item drop
                ItemStack drop = plugin.getFilterHopperItem(); // Get the base special item

                // (Optional) If you want the item to remember the category when placed again:
                // You would need to add logic to write 'savedCategory' into the item's PDC here.

                // 6. Drop the special item at the block location
                block.getWorld().dropItemNaturally(block.getLocation(), drop);

                // 7. Handle inventory contents (Standard hoppers spill items, we need to mimic that)
                // Since we cancelled dropItems, the stuff INSIDE the hopper won't drop automatically.
                for (ItemStack content : hopper.getInventory().getContents()) {
                    if (content != null && content.getType() != Material.AIR) {
                        block.getWorld().dropItemNaturally(block.getLocation(), content);
                    }
                }
            }
        }
    }

    private void checkAndFilter(Hopper hopper, ItemStack item, org.bukkit.event.Cancellable event) {
        PersistentDataContainer pdc = hopper.getPersistentDataContainer();

        // 1. Is this a special hopper?
        if (!pdc.has(CategoryHopperPlugin.IS_FILTER_HOPPER_KEY, PersistentDataType.BYTE)) return;

        // 2. What is the category?
        String catName = pdc.get(CategoryHopperPlugin.CATEGORY_KEY, PersistentDataType.STRING);
        if (catName == null || catName.equals("NONE")) return; // No filter active

        // 3. Check if item matches category
        CategoryManager.Category category = CategoryManager.Category.valueOf(catName);
        if (!plugin.getCategoryManager().isItemInCategory(category, item.getType())) {
            event.setCancelled(true);
        }
    }
}
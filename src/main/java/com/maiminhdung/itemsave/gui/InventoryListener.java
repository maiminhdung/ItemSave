package com.maiminhdung.itemsave.gui;

import com.maiminhdung.itemsave.ItemSave;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class InventoryListener implements Listener {

    private final ItemSave plugin;
    public static final NamespacedKey GROUP_NAME_KEY = new NamespacedKey("itemsave", "group_name");
    public static final NamespacedKey BUTTON_KEY = new NamespacedKey("itemsave", "button_action");

    public InventoryListener(ItemSave plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;

        // Listen for clicks in the main GUI
        if (clickedInventory.getHolder() == null && event.getView().title().equals(plugin.getLangManager().getComponent("gui.main_title"))) {
            handleMainGuiClick(event);
            return;
        }

        // Edit GUI
        if (clickedInventory.getHolder() instanceof GuiHolder) {
            handleEditGuiClick(event);
        }
    }

    private void handleMainGuiClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().isAir()) return;
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(GROUP_NAME_KEY, PersistentDataType.STRING)) return;

        String groupName = meta.getPersistentDataContainer().get(GROUP_NAME_KEY, PersistentDataType.STRING);
        if (groupName == null) return;

        if (event.isLeftClick()) {
            plugin.getGuiManager().openEditGui(player, groupName);
        } else if (event.isRightClick()) {
            plugin.getDataManager().deleteGroup(groupName);
            player.closeInventory();
            player.sendMessage(plugin.getLangManager().getPrefixedComponent("success.group_deleted", "group", groupName));
        }
    }

    private void handleEditGuiClick(InventoryClickEvent event) {
        // Only handle clicks in the bottom row (slots 45-53)
        if (event.getRawSlot() >= 45) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType().isAir()) return;
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null || !meta.getPersistentDataContainer().has(BUTTON_KEY, PersistentDataType.STRING)) return;

            String action = meta.getPersistentDataContainer().get(BUTTON_KEY, PersistentDataType.STRING);
            if (action == null) return;

            GuiHolder holder = (GuiHolder) event.getClickedInventory().getHolder();
            String groupName = holder.getGroupName();

            if ("save".equals(action)) {
                saveItemsFromGui(event.getClickedInventory(), groupName);
                player.closeInventory();
                player.sendMessage(plugin.getLangManager().getPrefixedComponent("success.group_items_updated", "group", groupName));
            } else if ("close".equals(action)) {
                player.closeInventory();
            }
        }
    }

    private void saveItemsFromGui(Inventory gui, String groupName) {
        List<ItemStack> itemsToSave = new ArrayList<>();
        // Chỉ lấy item từ các slot có thể chỉnh sửa (0-44)
        for (int i = 0; i < 45; i++) {
            itemsToSave.add(gui.getItem(i));
        }
        plugin.getDataManager().saveItems(groupName, itemsToSave);
    }
}

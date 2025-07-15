package com.maiminhdung.itemsave.gui;

import com.maiminhdung.itemsave.ItemSave;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GuiManager {

    private final ItemSave plugin;

    public GuiManager(ItemSave plugin) {
        this.plugin = plugin;
    }

    public void openMainGui(Player player) {
        Set<String> groupNames = plugin.getDataManager().getGroupNames();
        int size = (int) (Math.ceil(groupNames.size() / 9.0) * 9);
        if (size == 0) size = 9;

        Component guiTitle = plugin.getLangManager().getComponent("gui.main_title");
        Inventory gui = Bukkit.createInventory(null, size, guiTitle);

        for (String groupName : groupNames) {
            ItemStack groupItem = new ItemStack(Material.CHEST);
            ItemMeta meta = groupItem.getItemMeta();
            meta.displayName(Component.text(groupName)); // Tên item đơn giản

            // Count the number of items in the group
            long itemCount = plugin.getDataManager().getItems(groupName).stream()
                    .filter(Objects::nonNull)
                    .filter(item -> !item.getType().isAir())
                    .count();

            List<Component> lore = new ArrayList<>();
            lore.add(plugin.getLangManager().getComponent("gui.group_item_lore_amount", "amount", String.valueOf(itemCount)));
            lore.add(Component.empty()); // Dòng trống
            lore.add(plugin.getLangManager().getComponent("gui.group_item_lore_view"));
            lore.add(plugin.getLangManager().getComponent("gui.group_item_lore_delete"));
            meta.lore(lore);

            meta.getPersistentDataContainer().set(InventoryListener.GROUP_NAME_KEY, PersistentDataType.STRING, groupName);
            groupItem.setItemMeta(meta);
            gui.addItem(groupItem);
        }
        player.openInventory(gui);
    }

    public void openEditGui(Player player, String groupName) {
        List<ItemStack> items = plugin.getDataManager().getItems(groupName);
        Component guiTitle = plugin.getLangManager().getComponent("gui.edit_title", "group", groupName);
        Inventory editGui = Bukkit.createInventory(new GuiHolder(groupName), 54, guiTitle);

        // Add items to the edit GUI
        for (int i = 0; i < items.size() && i < 45; i++) {
            if (items.get(i) != null) {
                editGui.setItem(i, items.get(i));
            }
        }

        // Create filler items for empty slots
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.displayName(Component.text(" "));
        filler.setItemMeta(fillerMeta);

        for(int i = 45; i < 54; i++) {
            editGui.setItem(i, filler);
        }

        // Save Button
        ItemStack saveButton = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta saveMeta = saveButton.getItemMeta();
        saveMeta.displayName(plugin.getLangManager().getComponent("gui.save_button_name"));
        saveMeta.lore(List.of(plugin.getLangManager().getComponent("gui.save_button_lore")));
        saveMeta.getPersistentDataContainer().set(InventoryListener.BUTTON_KEY, PersistentDataType.STRING, "save");
        saveButton.setItemMeta(saveMeta);

        // Close Button
        ItemStack closeButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.displayName(plugin.getLangManager().getComponent("gui.close_button_name"));
        closeMeta.lore(List.of(plugin.getLangManager().getComponent("gui.close_button_lore")));
        closeMeta.getPersistentDataContainer().set(InventoryListener.BUTTON_KEY, PersistentDataType.STRING, "close");
        closeButton.setItemMeta(closeMeta);

        editGui.setItem(48, saveButton);
        editGui.setItem(50, closeButton);

        player.openInventory(editGui);
    }
}

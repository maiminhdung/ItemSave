package com.maiminhdung.itemsave.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class GuiHolder implements InventoryHolder {
    private final String groupName;

    public GuiHolder(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
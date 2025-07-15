package com.maiminhdung.itemsave.data;

import com.maiminhdung.itemsave.ItemSave;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DataManager {

    private final ItemSave plugin;
    private FileConfiguration dataConfig = null;
    private File configFile = null;

    public DataManager(ItemSave plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
    }

    public void reloadConfig() {
        if (this.configFile == null) {
            this.configFile = new File(this.plugin.getDataFolder(), "data.yml");
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(this.configFile);
    }

    public FileConfiguration getConfig() {
        if (this.dataConfig == null) {
            reloadConfig();
        }
        return this.dataConfig;
    }

    public void saveConfig() {
        if (this.dataConfig == null || this.configFile == null) {
            return;
        }
        try {
            this.getConfig().save(this.configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Không thể lưu file data.yml!");
        }
    }

    public void saveDefaultConfig() {
        if (this.configFile == null) {
            this.configFile = new File(this.plugin.getDataFolder(), "data.yml");
        }
        if (!this.configFile.exists()) {
            this.plugin.saveResource("data.yml", false);
        }
    }

    // Save list item into a group
    public void saveItems(String groupName, List<ItemStack> items) {
        getConfig().set("groups." + groupName, items);
        saveConfig();
    }

    // Get items from a group
    public List<ItemStack> getItems(String groupName) {
        return (List<ItemStack>) getConfig().getList("groups." + groupName, new ArrayList<>());
    }

    // Delete a group
    public void deleteGroup(String groupName) {
        getConfig().set("groups." + groupName, null);
        saveConfig();
    }

    // Check if a group exists
    public boolean groupExists(String groupName) {
        return getConfig().isSet("groups." + groupName);
    }

    // Take all group names
    public Set<String> getGroupNames() {
        ConfigurationSection section = getConfig().getConfigurationSection("groups");
        if (section == null) {
            return Set.of();
        }
        return section.getKeys(false);
    }
}

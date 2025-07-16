package com.maiminhdung.itemsave;

import com.maiminhdung.itemsave.bstats.Metrics;
import com.maiminhdung.itemsave.commands.ItemSaveCommand;
import com.maiminhdung.itemsave.data.DataManager;
import com.maiminhdung.itemsave.gui.GuiManager;
import com.maiminhdung.itemsave.gui.InventoryListener;
import com.maiminhdung.itemsave.lang.LangManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ItemSave extends JavaPlugin {

    private static ItemSave instance;
    private DataManager dataManager;
    private GuiManager guiManager;
    private LangManager langManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();

        this.dataManager = new DataManager(this);
        this.langManager = new LangManager(this);
        this.guiManager = new GuiManager(this);

        // Register commands
        getCommand("itemsave").setExecutor(new ItemSaveCommand(this));
        getCommand("itemsave").setTabCompleter(new ItemSaveCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);

        // Bstats Metrics
        setupBtatsMetrics();

        getLogger().info("ItemSave has been enabled!");
    }

    private void setupBtatsMetrics() {
        Metrics metrics = new Metrics(this, 26520);
    }

    @Override
    public void onDisable() {
        getLogger().info("ItemSave has been disbaled!");
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }
    public LangManager getLangManager() {
        return langManager;
    }

    public void reload() {
        // Reload config.yml
        super.reloadConfig();

        // Reload language files
        this.langManager.reload();
    }
}
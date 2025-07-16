package com.maiminhdung.itemsave.lang;

import com.maiminhdung.itemsave.ItemSave;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LangManager {

    private final ItemSave plugin;
    private FileConfiguration langConfig;
    private final MiniMessage miniMessage;

    public LangManager(ItemSave plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        reload();
    }

    public void reload() {
        String locale = plugin.getConfig().getString("default-locale", "vi");
        File langFile = new File(plugin.getDataFolder(), "lang/" + locale + ".yml");

        if (!langFile.exists()) {
            plugin.saveResource("lang/" + locale + ".yml", false);
        }

        this.langConfig = YamlConfiguration.loadConfiguration(langFile);

        try (InputStream defaultStream = plugin.getResource("lang/" + locale + ".yml")) {
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                this.langConfig.setDefaults(defaultConfig);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Component getListItemComponent(String groupName) {
        // 1. Take raw strings from the language file
        Component prefix = miniMessage.deserialize(getRawString("command.list_item_prefix", " » "));
        Component text = miniMessage.deserialize(getRawString("command.list_item_text", "<group>").replace("<group>", groupName));
        Component hover = miniMessage.deserialize(getRawString("command.list_item_hover", "Click for <group>").replace("<group>", groupName));

        // 2. Use hover and click events
        return prefix.append(
                text.hoverEvent(HoverEvent.showText(hover))
                        .clickEvent(ClickEvent.runCommand("/isave gui " + groupName))
        );
    }

    public Component getComponent(String path, String... args) {
        String message = langConfig.getString(path, "<red>Missing translation: " + path + "</red>");
        List<TagResolver> placeholders = new ArrayList<>();
        if (args.length > 0 && args.length % 2 == 0) {
            for (int i = 0; i < args.length; i += 2) {
                placeholders.add(Placeholder.unparsed(args[i], args[i + 1]));
            }
        }
        return miniMessage.deserialize(message, TagResolver.resolver(placeholders));
    }

    public Component getPrefixedComponent(String path, String... args) {
        Component prefix = getComponent("prefix");
        Component message = getComponent(path, args);
        return prefix.append(message);
    }

    public Component getComponent(String path) {
        return getComponent(path, new String[]{});
    }

    public String getRawString(String path, String def) {
        return langConfig.getString(path, def);
    }
}

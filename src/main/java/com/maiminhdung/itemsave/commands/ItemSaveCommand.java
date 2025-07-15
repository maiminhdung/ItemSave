package com.maiminhdung.itemsave.commands;

import com.maiminhdung.itemsave.ItemSave;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ItemSaveCommand implements CommandExecutor, TabCompleter {

    private final ItemSave plugin;
    private static final int ITEMS_PER_PAGE = 8;

    public ItemSaveCommand(ItemSave plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        String mainCommand = args[0].toLowerCase();

        switch (mainCommand) {
            case "save" -> handleSave(sender, args);
            case "delete" -> handleDelete(sender, args);
            case "list" -> handleList(sender, args);
            case "gui" -> handleGui(sender, args);
            case "give" -> handleGive(sender, args);
            default -> sendHelpMessage(sender);
        }
        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(plugin.getLangManager().getComponent("command.help_header"));
        sender.sendMessage(plugin.getLangManager().getComponent("command.help_save"));
        sender.sendMessage(plugin.getLangManager().getComponent("command.help_delete"));
        sender.sendMessage(plugin.getLangManager().getComponent("command.help_list"));
        sender.sendMessage(plugin.getLangManager().getComponent("command.help_gui"));
        sender.sendMessage(plugin.getLangManager().getComponent("command.help_give"));
        sender.sendMessage(plugin.getLangManager().getComponent("command.help_footer"));
    }

    private void handleList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("itemsave.list")) {
            sender.sendMessage(plugin.getLangManager().getPrefixedComponent("command.no_permission"));
            return;
        }

        List<String> sortedGroups = new ArrayList<>(plugin.getDataManager().getGroupNames());
        Collections.sort(sortedGroups, String.CASE_INSENSITIVE_ORDER);

        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getLangManager().getPrefixedComponent("command.invalid_page", "page", args[1]));
                return;
            }
        }

        int totalItems = sortedGroups.size();
        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;

        if (page < 1 || page > totalPages) {
            sender.sendMessage(plugin.getLangManager().getPrefixedComponent("command.invalid_page", "page", String.valueOf(page)));
            return;
        }

        sender.sendMessage(plugin.getLangManager().getComponent("command.list_header"));

        if (sortedGroups.isEmpty()) {
            sender.sendMessage(plugin.getLangManager().getComponent("command.list_empty"));
        } else {
            int startIndex = (page - 1) * ITEMS_PER_PAGE;
            int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);
            List<String> pageItems = sortedGroups.subList(startIndex, endIndex);

            for (String groupName : pageItems) {
                sender.sendMessage(plugin.getLangManager().getListItemComponent(groupName));
            }

            Component prevButton;
            if (page > 1) {
                prevButton = plugin.getLangManager().getComponent("command.list_footer_prev")
                        .clickEvent(ClickEvent.runCommand("/isave list " + (page - 1)));
            } else {
                prevButton = plugin.getLangManager().getComponent("command.list_footer_prev_disabled");
            }

            Component nextButton;
            if (page < totalPages) {
                nextButton = plugin.getLangManager().getComponent("command.list_footer_next")
                        .clickEvent(ClickEvent.runCommand("/isave list " + (page + 1)));
            } else {
                nextButton = plugin.getLangManager().getComponent("command.list_footer_next_disabled");
            }

            Component currentPageInfo = plugin.getLangManager().getComponent("command.list_footer_current",
                    "current_page", String.valueOf(page), "total_pages", String.valueOf(totalPages));

            Component footer = Component.text()
                    .append(prevButton)
                    .append(Component.text("    "))
                    .append(currentPageInfo)
                    .append(Component.text("    "))
                    .append(nextButton)
                    .build();
            sender.sendMessage(Component.text(""));
            sender.sendMessage(footer);
        }
    }

    private void handleSave(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLangManager().getComponent("command.no_player"));
            return;
        }
        if (args.length < 3) {
            player.sendMessage(plugin.getLangManager().getPrefixedComponent("command.usage_save"));
            return;
        }

        String saveType = args[1].toLowerCase();
        String groupName = args[2];

        if ("inventory".equals(saveType)) {
            if (!player.hasPermission("itemsave.save.inventory")) {
                player.sendMessage(plugin.getLangManager().getPrefixedComponent("command.no_permission"));
                return;
            }
            if (plugin.getDataManager().groupExists(groupName)) {
                player.sendMessage(plugin.getLangManager().getPrefixedComponent("command.group_exists", "group", groupName));
                return;
            }
            List<ItemStack> items = Arrays.asList(player.getInventory().getContents());
            plugin.getDataManager().saveItems(groupName, items);
            player.sendMessage(plugin.getLangManager().getPrefixedComponent("success.inventory_saved", "group", groupName));

        } else if ("hand".equals(saveType)) {
            if (!player.hasPermission("itemsave.save.hand")) {
                player.sendMessage(plugin.getLangManager().getPrefixedComponent("command.no_permission"));
                return;
            }
            if (plugin.getDataManager().groupExists(groupName)) {
                player.sendMessage(plugin.getLangManager().getPrefixedComponent("command.group_exists", "group", groupName));
                return;
            }
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand.getType().isAir()) {
                player.sendMessage(plugin.getLangManager().getPrefixedComponent("command.no_item_in_hand"));
                return;
            }
            plugin.getDataManager().saveItems(groupName, List.of(itemInHand));
            player.sendMessage(plugin.getLangManager().getPrefixedComponent("success.item_saved", "group", groupName));
        } else {
            player.sendMessage(plugin.getLangManager().getPrefixedComponent("command.usage_save"));
        }
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("itemsave.delete")) {
            sender.sendMessage(plugin.getLangManager().getPrefixedComponent("command.no_permission"));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLangManager().getComponent("command.no_player"));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(plugin.getLangManager().getPrefixedComponent("command.usage_delete"));
            return;
        }
        String groupName = args[1];
        if (!plugin.getDataManager().groupExists(groupName)) {
            player.sendMessage(plugin.getLangManager().getPrefixedComponent("command.group_not_exists", "group", groupName));
            return;
        }
        plugin.getDataManager().deleteGroup(groupName);
        player.sendMessage(plugin.getLangManager().getPrefixedComponent("success.group_deleted", "group", groupName));
    }

    private void handleGui(CommandSender sender, String[] args) {
        if (!sender.hasPermission("itemsave.gui")) {
            sender.sendMessage(plugin.getLangManager().getPrefixedComponent("command.no_permission"));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLangManager().getComponent("command.no_player"));
            return;
        }
        if (args.length > 1) {
            String groupName = args[1];
            if (!plugin.getDataManager().groupExists(groupName)) {
                player.sendMessage(plugin.getLangManager().getPrefixedComponent("command.group_not_exists", "group", groupName));
                return;
            }
            plugin.getGuiManager().openEditGui(player, groupName);
        } else {
            plugin.getGuiManager().openMainGui(player);
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("itemsave.give")) {
            sender.sendMessage(plugin.getLangManager().getPrefixedComponent("command.no_permission"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(plugin.getLangManager().getPrefixedComponent("command.usage_give"));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        String groupName = args[2];
        if (target == null) {
            sender.sendMessage(plugin.getLangManager().getPrefixedComponent("command.player_not_found", "player", args[1]));
            return;
        }
        if (!plugin.getDataManager().groupExists(groupName)) {
            sender.sendMessage(plugin.getLangManager().getPrefixedComponent("command.group_not_exists", "group", groupName));
            return;
        }
        List<ItemStack> items = plugin.getDataManager().getItems(groupName);
        ItemStack[] itemsArray = items.stream().filter(Objects::nonNull).toArray(ItemStack[]::new);
        HashMap<Integer, ItemStack> didNotFit = target.getInventory().addItem(itemsArray);
        for (ItemStack item : didNotFit.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), item);
        }
        sender.sendMessage(plugin.getLangManager().getPrefixedComponent("success.group_given", "group", groupName, "player", target.getName()));
        target.sendMessage(plugin.getLangManager().getPrefixedComponent("success.group_received", "group", groupName, "sender", sender.getName()));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], List.of("save", "delete", "list", "gui", "give"), new ArrayList<>());
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("save")) {
                return StringUtil.copyPartialMatches(args[1], List.of("hand", "inventory"), new ArrayList<>());
            }
            if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("gui")) {
                return StringUtil.copyPartialMatches(args[1], plugin.getDataManager().getGroupNames(), new ArrayList<>());
            }
            if (args[0].equalsIgnoreCase("give")) {
                return null;
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return StringUtil.copyPartialMatches(args[2], plugin.getDataManager().getGroupNames(), new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
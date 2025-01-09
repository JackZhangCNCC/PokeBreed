package io.github.johnson.pokebreed.command;

import io.github.johnson.pokebreed.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 繁殖命令处理器
 */
public class BreedCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    
    public BreedCommand(Main plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 处理子命令
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "help":
                    sendHelpMessage(sender);
                    return true;
                case "reload":
                    if (!sender.hasPermission("pokebreed.admin")) {
                        sender.sendMessage(plugin.getMessageManager().getMessage("commands.no_permission"));
                        return true;
                    }
                    plugin.reload();
                    sender.sendMessage(plugin.getMessageManager().getMessage("commands.reload_success"));
                    return true;
                case "test":
                    if (!sender.hasPermission("pokebreed.admin")) {
                        sender.sendMessage(plugin.getMessageManager().getMessage("commands.no_permission"));
                        return true;
                    }
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(plugin.getMessageManager().getMessage("commands.player_only"));
                        return true;
                    }
                    runTests((Player) sender);
                    return true;
                default:
                    sender.sendMessage("§c未知的子命令！使用 /breed help 查看帮助。");
                    return true;
            }
        }

        // 打开主界面（仅玩家可用）
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage("commands.player_only"));
            return true;
        }
        plugin.getBreedingGUI().openMainMenu((Player) sender);
        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§6========= §b宝可梦繁殖系统 §6=========");
        sender.sendMessage("§e/breed §7- 打开繁殖界面");
        sender.sendMessage("§e/breed help §7- 显示此帮助信息");
        if (sender.hasPermission("pokebreed.admin")) {
            sender.sendMessage("§e/breed reload §7- 重载配置文件");
            sender.sendMessage("§e/breed test §7- 运行功能测试");
        }
        sender.sendMessage("§6================================");
    }

    private void runTests(Player player) {
        player.sendMessage("§6========= 开始功能测试 =========");
        
        // 测试配置加载
        player.sendMessage("§7测试配置加载...");
        if (plugin.getConfigManager().isVersionMatched()) {
            player.sendMessage("§a配置版本检查通过");
        } else {
            player.sendMessage("§c配置版本不匹配");
        }

        // 测试经济系统
        player.sendMessage("§7测试经济系统...");
        if (plugin.getEconomy() != null) {
            player.sendMessage("§a经济系统连接成功");
        } else {
            player.sendMessage("§c经济系统连接失败");
        }

        // 测试GUI系统
        player.sendMessage("§7测试GUI系统...");
        try {
            plugin.getBreedingGUI().openMainMenu(player);
            player.sendMessage("§aGUI系统测试通过");
        } catch (Exception e) {
            player.sendMessage("§cGUI系统测试失败: " + e.getMessage());
        }

        // 测试物品系统
        player.sendMessage("§7测试物品系统...");
        try {
            // 测试基本物品功能
            plugin.getItemManager().getItemAmount(player, "time_reduction");
            plugin.getItemManager().createItemStack("time_reduction");
            player.sendMessage("§a物品系统测试通过");
        } catch (Exception e) {
            player.sendMessage("§c物品系统测试失败: " + e.getMessage());
        }

        // 测试存储系统
        player.sendMessage("§7测试存储系统...");
        try {
            plugin.getStorageManager().saveData();
            player.sendMessage("§a存储系统测试通过");
        } catch (Exception e) {
            player.sendMessage("§c存储系统测试失败: " + e.getMessage());
        }

        player.sendMessage("§6========= 测试完成 =========");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("help");
            if (sender.hasPermission("pokebreed.admin")) {
                completions.add("reload");
                completions.add("test");
            }
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return completions;
    }
} 
package io.github.johnson.pokebreed.manager;

import io.github.johnson.pokebreed.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息管理器
 */
public class MessageManager {
    private final Main plugin;
    private FileConfiguration messageConfig;
    private final Map<String, String> messages;
    private String prefix;
    
    public MessageManager(Main plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        loadMessages();
    }
    
    /**
     * 加载消息配置
     */
    public void loadMessages() {
        File messageFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messageFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messageConfig = YamlConfiguration.loadConfiguration(messageFile);
        messages.clear();
        
        // 加载前缀
        prefix = messageConfig.getString("prefix", "§8[§9宝可梦繁殖§8] ");
        
        // 递归加载所有消息
        loadSection("", messageConfig);
    }
    
    /**
     * 递归加载配置节点
     */
    private void loadSection(String path, ConfigurationSection config) {
        for (String key : config.getKeys(false)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;
            if (config.isConfigurationSection(key)) {
                loadSection(fullPath, config.getConfigurationSection(key));
            } else {
                String message = config.getString(key);
                if (message != null) {
                    messages.put(fullPath, message);
                }
            }
        }
    }
    
    /**
     * 获取消息
     */
    public String getMessage(String key) {
        return messages.getOrDefault(key, "§c未找到消息: " + key);
    }
    
    /**
     * 获取带前缀的消息
     */
    public String getPrefixedMessage(String key) {
        return prefix + getMessage(key);
    }
    
    /**
     * 获取带变量替换的消息
     */
    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return message;
    }
    
    /**
     * 获取带前缀和变量替换的消息
     */
    public String getPrefixedMessage(String key, Map<String, String> placeholders) {
        return prefix + getMessage(key, placeholders);
    }
    
    /**
     * 发送消息给玩家
     */
    public void sendMessage(Player player, String key) {
        player.sendMessage(getPrefixedMessage(key));
    }
    
    /**
     * 发送带变量的消息给玩家
     */
    public void sendMessage(Player player, String key, Object... args) {
        Map<String, String> placeholders = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            placeholders.put("arg" + i, String.valueOf(args[i]));
        }
        player.sendMessage(getPrefixedMessage(key, placeholders));
    }
    
    /**
     * 重载消息
     */
    public void reload() {
        loadMessages();
    }
} 
package io.github.johnson.pokebreed.manager;

import io.github.johnson.pokebreed.Main;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置管理器
 */
@Getter
public class ConfigManager {
    private static final String CURRENT_VERSION = "1.0.0";
    private static final int MIN_BREEDING_DURATION = 1;  // 最短1秒
    private static final int MAX_BREEDING_DURATION = 259200;  // 最长72小时
    
    private final Main plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration items;
    
    // 缓存的配置值
    private String version;
    private int minLevel;
    private int baseDuration;
    private boolean useTitle;
    private boolean itemsEnabled;
    private boolean shopEnabled;
    private boolean economyEnabled;
    private double breedingCost;
    private Map<String, Map<String, Object>> itemsData;
    
    // IV提升相关配置
    private double baseIvBoost;      // 基础IV提升系数
    private double maxIvBoost;       // 最大IV提升系数
    private boolean ivBoostStack;    // IV提升是否可叠加
    
    // 闪光概率提升相关配置
    private double baseShinyBoost;   // 基础闪光概率提升系数
    private double maxShinyBoost;    // 最大闪光概率提升系数
    private boolean shinyBoostStack; // 闪光概率提升是否可叠加
    
    // 个体值随机提升相关配置
    private int minRandomBoost;      // 最小随机提升值
    private int maxRandomBoost;      // 最大随机提升值
    private Map<Integer, Double> ivSuccessRates;  // 不同个体值的成功率
    
    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        this.itemsData = new HashMap<>();
        this.ivSuccessRates = new HashMap<>();
        loadConfigs();
    }
    
    /**
     * 加载配置文件
     */
    public void loadConfigs() {
        // 保存默认配置
        saveDefaultConfig("config.yml");
        saveDefaultConfig("messages.yml");
        saveDefaultConfig("items.yml");
        
        // 加载配置
        config = plugin.getConfig();
        messages = loadConfig("messages.yml");
        items = loadConfig("items.yml");
        
        // 加载配置值
        loadValues();
        
        // 检查版本
        checkVersion();
    }
    
    /**
     * 加载配置值
     */
    private void loadValues() {
        // 基本配置
        version = config.getString("version", "1.0");
        minLevel = config.getInt("breeding.min-level", 5);
        baseDuration = validateBreedingTime(config.getInt("breeding.base-duration", 300));
        useTitle = config.getBoolean("breeding.use-title", true);
        
        // 道具系统配置
        itemsEnabled = items.getBoolean("settings.enabled", true);
        shopEnabled = items.getBoolean("settings.shop-enabled", true);
        
        // 经济系统配置
        economyEnabled = config.getBoolean("breeding.economy.enabled", true);
        breedingCost = config.getDouble("breeding.economy.breeding-cost", 1000.0);
        
        // 加载道具数据
        loadItemsData();
        
        // IV提升配置
        baseIvBoost = validateBoostValue(config.getDouble("breeding.iv-boost.base", 0.1));
        maxIvBoost = validateBoostValue(config.getDouble("breeding.iv-boost.max", 0.5));
        ivBoostStack = config.getBoolean("breeding.iv-boost.stackable", false);
        
        // 闪光概率提升配置
        baseShinyBoost = validateBoostValue(config.getDouble("breeding.shiny-boost.base", 0.1));
        maxShinyBoost = validateBoostValue(config.getDouble("breeding.shiny-boost.max", 0.5));
        shinyBoostStack = config.getBoolean("breeding.shiny-boost.stackable", false);
        
        // 个体值随机提升配置
        minRandomBoost = config.getInt("breeding.random-boost.min", 1);
        maxRandomBoost = config.getInt("breeding.random-boost.max", 3);
        
        // 加载个体值成功率
        ConfigurationSection ratesSection = config.getConfigurationSection("breeding.success-rates");
        if (ratesSection != null) {
            for (String key : ratesSection.getKeys(false)) {
                try {
                    int iv = Integer.parseInt(key);
                    double rate = ratesSection.getDouble(key);
                    ivSuccessRates.put(iv, rate);
                } catch (NumberFormatException ignored) {}
            }
        }
    }
    
    /**
     * 加载道具数据
     */
    private void loadItemsData() {
        itemsData.clear();
        ConfigurationSection shopSection = items.getConfigurationSection("shop");
        if (shopSection != null) {
            for (String key : shopSection.getKeys(false)) {
                ConfigurationSection itemSection = shopSection.getConfigurationSection(key);
                if (itemSection != null) {
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("name", itemSection.getString("name", "未命名道具"));
                    itemData.put("price", itemSection.getDouble("price", 0.0));
                    itemData.put("material", itemSection.getString("material", "BARRIER"));
                    itemData.put("lore", itemSection.getStringList("lore"));
                    itemsData.put(key, itemData);
                }
            }
        }
    }
    
    /**
     * 检查配置文件版本
     */
    private void checkVersion() {
        if (!version.equals(CURRENT_VERSION)) {
            plugin.getLogger().warning("配置文件版本不匹配！");
            plugin.getLogger().warning("当前版本: " + version);
            plugin.getLogger().warning("最新版本: " + CURRENT_VERSION);
            plugin.getLogger().warning("请备份并删除旧的配置文件，让插件生成新的配置文件。");
        }
    }
    
    /**
     * 检查版本是否匹配
     */
    public boolean isVersionMatched() {
        return version.equals(CURRENT_VERSION);
    }
    
    /**
     * 验证繁殖时间
     */
    private int validateBreedingTime(int time) {
        return Math.max(MIN_BREEDING_DURATION, Math.min(MAX_BREEDING_DURATION, time));
    }
    
    /**
     * 验证提升值
     */
    private double validateBoostValue(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
    
    /**
     * 加载配置文件
     */
    private FileConfiguration loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        return YamlConfiguration.loadConfiguration(file);
    }
    
    /**
     * 保存默认配置文件
     */
    private void saveDefaultConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
    }
    
    /**
     * 重载配置
     */
    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        messages = loadConfig("messages.yml");
        items = loadConfig("items.yml");
        loadValues();
    }
    
    /**
     * 获取消息
     */
    public String getMessage(String path) {
        return messages.getString(path, "§c消息未找到: " + path);
    }
    
    /**
     * 获取消息并替换变量
     */
    public String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return message;
    }
    
    /**
     * 设置经济系统是否启用
     */
    public void setEconomyEnabled(boolean enabled) {
        this.economyEnabled = enabled;
        config.set("breeding.economy.enabled", enabled);
        plugin.saveConfig();
    }
    
    /**
     * 获取指定道具的配置数据
     */
    public Map<String, Object> getItemData(String itemId) {
        return itemsData.get(itemId);
    }
    
    /**
     * 获取是否显示标题
     */
    public boolean isShowTitle() {
        return useTitle;
    }
} 
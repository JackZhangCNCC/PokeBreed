package io.github.johnson.pokebreed;

import io.github.johnson.pokebreed.command.BreedCommand;
import io.github.johnson.pokebreed.gui.*;
import io.github.johnson.pokebreed.listener.BreedListener;
import io.github.johnson.pokebreed.listener.GUIListener;
import io.github.johnson.pokebreed.listener.DayCareListener;
import io.github.johnson.pokebreed.manager.*;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

/**
 * 插件主类
 */
@Getter
public class Main extends JavaPlugin {
    private ConfigManager configManager;
    private MessageManager messageManager;
    private BreedingManager breedingManager;
    private ItemManager itemManager;
    private StorageManager storageManager;
    private Economy economy;
    
    // GUI界面
    private BreedingGUI breedingGUI;
    private SelectPokemonGUI selectPokemonGUI;
    private ItemGUI itemGUI;
    private ShopGUI shopGUI;
    
    @Override
    public void onEnable() {
        // 初始化配置管理器
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        
        // 初始化管理器
        breedingManager = new BreedingManager(this);
        itemManager = new ItemManager(this);
        storageManager = new StorageManager(this);
        
        // 初始化GUI
        breedingGUI = new BreedingGUI(this);
        selectPokemonGUI = new SelectPokemonGUI(this);
        itemGUI = new ItemGUI(this);
        shopGUI = new ShopGUI(this);
        
        // 初始化经济系统
        if (configManager.isEconomyEnabled()) {
            if (!setupEconomy()) {
                getLogger().warning("未找到 Vault 插件，经济系统将被禁用！");
                configManager.setEconomyEnabled(false);
            }
        }
        
        // 注册命令
        getCommand("breed").setExecutor(new BreedCommand(this));
        
        // 注册监听器
        getServer().getPluginManager().registerEvents(new BreedListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new DayCareListener(this), this);
        
        getLogger().info("§6宝可梦繁殖系统已启动！§a作者QQ：1151335385");
    }
    
    @Override
    public void onDisable() {
        // 保存所有数据
        if (storageManager != null) {
            storageManager.saveAll();
        }
        
        // 取消所有正在进行的繁殖
        if (breedingManager != null) {
            for (Player player : getServer().getOnlinePlayers()) {
                if (breedingManager.isBreeding(player)) {
                    breedingManager.cancelBreeding(player);
                }
            }
        }
        
        getLogger().info("§6宝可梦繁殖系统已关闭！");
    }
    
    /**
     * 设置经济系统
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        
        economy = rsp.getProvider();
        return economy != null;
    }
    
    /**
     * 重载插件
     */
    public void reload() {
        // 保存所有数据
        storageManager.saveAll();
        
        // 取消所有正在进行的繁殖
        for (Player player : getServer().getOnlinePlayers()) {
            if (breedingManager.isBreeding(player)) {
                breedingManager.cancelBreeding(player);
            }
        }
        
        // 重载配置
        configManager.reloadConfigs();
        messageManager.reload();
        
        // 检查配置文件版本
        if (!configManager.isVersionMatched()) {
            getLogger().warning("配置文件版本不匹配，某些功能可能无法正常工作！");
            getLogger().warning("建议备份当前配置文件后删除，让插件生成新的配置文件。");
        }
        
        // 重新设置经济系统
        if (configManager.isEconomyEnabled()) {
            if (!setupEconomy()) {
                getLogger().warning("未找到 Vault 插件，经济系统将被禁用！");
                configManager.setEconomyEnabled(false);
            }
        }
        
        // 重新初始化GUI
        breedingGUI = new BreedingGUI(this);
        selectPokemonGUI = new SelectPokemonGUI(this);
        itemGUI = new ItemGUI(this);
        shopGUI = new ShopGUI(this);
    }
} 
package io.github.johnson.pokebreed.manager;

import io.github.johnson.pokebreed.Main;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 道具管理器
 */
public class ItemManager {
    private final Main plugin;
    @Setter
    private Economy economy;
    private final Map<UUID, Map<String, Integer>> playerItems;
    
    public ItemManager(Main plugin) {
        this.plugin = plugin;
        this.playerItems = new HashMap<>();
    }
    
    /**
     * 获取玩家的道具数量
     */
    public int getItemAmount(Player player, String itemId) {
        return playerItems
            .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
            .getOrDefault(itemId, 0);
    }
    
    /**
     * 给予玩家道具
     */
    public void giveItem(Player player, String itemId, int amount) {
        Map<String, Integer> items = playerItems.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        items.put(itemId, items.getOrDefault(itemId, 0) + amount);
    }
    
    /**
     * 移除玩家的道具
     */
    public boolean removeItem(Player player, String itemId, int amount) {
        Map<String, Integer> items = playerItems.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        int current = items.getOrDefault(itemId, 0);
        if (current >= amount) {
            items.put(itemId, current - amount);
            return true;
        }
        return false;
    }
    
    /**
     * 创建道具物品堆
     */
    public ItemStack createItemStack(String itemId) {
        Map<String, Object> itemData = plugin.getConfigManager().getItemData(itemId);
        if (itemData == null) return null;

        Material material = Material.valueOf((String) itemData.get("material"));
        String name = (String) itemData.get("name");
        List<String> lore = (List<String>) itemData.get("lore");
        boolean glow = (boolean) itemData.getOrDefault("glow", false);

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * 检查道具是否可叠加
     */
    public boolean isStackable(String itemId) {
        Map<String, Object> itemData = plugin.getConfigManager().getItemData(itemId);
        if (itemData == null) return false;
        return (boolean) itemData.getOrDefault("stackable", false);
    }
    
    /**
     * 使用道具
     */
    public boolean useItem(Player player, String itemId) {
        if (getItemAmount(player, itemId) <= 0) {
            plugin.getMessageManager().sendMessage(player, "items.not_enough");
            return false;
        }

        // 检查是否可以使用该道具
        if (!canUseItem(player, itemId)) {
            plugin.getMessageManager().sendMessage(player, "items.cannot_use");
            return false;
        }

        // 移除道具
        removeItem(player, itemId, 1);

        // 标记道具为已使用
        plugin.getBreedingManager().getBreedingData(player).markItemAsUsed(itemId);

        // 应用道具效果
        applyItemEffect(player, itemId);
        return true;
    }
    
    /**
     * 购买道具
     */
    public boolean buyItem(Player player, String itemId, int amount) {
        if (!plugin.getConfigManager().isShopEnabled()) {
            plugin.getMessageManager().sendMessage(player, "shop.disabled");
            return false;
        }
        
        Map<String, Object> itemData = plugin.getConfigManager().getItemData(itemId);
        if (itemData == null) {
            plugin.getMessageManager().sendMessage(player, "shop.item_not_found");
            return false;
        }
        
        double price = (double) itemData.get("price") * amount;
        if (!economy.has(player, price)) {
            plugin.getMessageManager().sendMessage(player, "shop.not_enough_money");
            return false;
        }
        
        economy.withdrawPlayer(player, price);
        giveItem(player, itemId, amount);
        plugin.getMessageManager().sendMessage(player, "shop.purchase_success");
        return true;
    }
    
    /**
     * 检查是否可以使用道具
     */
    private boolean canUseItem(Player player, String itemId) {
        // 如果玩家没有在繁殖中，不能使用道具
        if (!plugin.getBreedingManager().isBreeding(player)) {
            return false;
        }

        // 如果道具不可叠加且已经使用过，不能使用
        if (!isStackable(itemId) && plugin.getBreedingManager().getBreedingData(player).hasUsedItem(itemId)) {
            return false;
        }

        return true;
    }
    
    /**
     * 应用道具效果
     */
    private void applyItemEffect(Player player, String itemId) {
        switch (itemId) {
            case "time_reduction":
                plugin.getBreedingManager().useTimeReductionItem(player);
                break;
            case "instant_breed":
                plugin.getBreedingManager().useInstantBreedItem(player);
                break;
            case "iv_boost":
                // IV提升效果在繁殖完成时应用
                break;
            case "shiny_boost":
                // 闪光提升效果在繁殖完成时应用
                break;
            case "random_boost":
                // 随机提升效果在繁殖完成时应用
                break;
        }
    }
    
    /**
     * 保存数据
     */
    public void saveData() {
        // TODO: 实现数据保存
    }
    
    /**
     * 加载数据
     */
    public void loadData() {
        // TODO: 实现数据加载
    }
} 
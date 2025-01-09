package io.github.johnson.pokebreed.gui;

import io.github.johnson.pokebreed.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 道具界面
 */
public class ItemGUI {
    private final Main plugin;
    private final Map<Player, Inventory> itemMenus;
    
    public ItemGUI(Main plugin) {
        this.plugin = plugin;
        this.itemMenus = new HashMap<>();
    }
    
    /**
     * 打开道具界面
     */
    public void openItemMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 36, "§9繁殖道具");
        
        // 添加道具按钮
        addItemButton(inventory, player, 10, "time_reduction", "时间缩减道具", Material.CLOCK,
            "§7使用后可以缩短繁殖时间", "§7可叠加使用", "§e点击使用");
        
        addItemButton(inventory, player, 11, "instant_breed", "立即完成道具", Material.NETHER_STAR,
            "§7使用后立即完成繁殖", "§7不可叠加使用", "§e点击使用");
        
        addItemButton(inventory, player, 12, "iv_inheritance", "个体继承道具", Material.DIAMOND,
            "§7使用后增加继承父母个体值的数量", "§7可叠加使用", "§e点击使用");
        
        addItemButton(inventory, player, 13, "shiny_boost", "闪光概率提升道具", Material.GOLD_INGOT,
            "§7使用后提高蛋的闪光概率", "§7不可叠加使用", "§e点击使用");
        
        addItemButton(inventory, player, 14, "random_boost", "个体随机提升道具", Material.EMERALD,
            "§7使用后随机提升蛋的个体值", "§7不可叠加使用", "§e点击使用");
        
        // 商店按钮
        if (plugin.getConfigManager().isShopEnabled()) {
            ItemStack shopButton = createShopButton();
            inventory.setItem(31, shopButton);
        }
        
        // 返回按钮
        ItemStack backButton = createBackButton();
        inventory.setItem(35, backButton);
        
        itemMenus.put(player, inventory);
        player.openInventory(inventory);
    }
    
    /**
     * 添加道具按钮
     */
    private void addItemButton(Inventory inventory, Player player, int slot, String itemId, String name, Material material, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6" + name);
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(line);
            }
            int amount = plugin.getItemManager().getItemAmount(player, itemId);
            loreList.add("");
            loreList.add("§7拥有数量: §f" + amount);
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        inventory.setItem(slot, item);
    }
    
    /**
     * 创建商店按钮
     */
    private ItemStack createShopButton() {
        ItemStack item = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6道具商店");
            List<String> lore = new ArrayList<>();
            lore.add("§7点击打开商店");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * 创建返回按钮
     */
    private ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c返回");
            List<String> lore = new ArrayList<>();
            lore.add("§7点击返回上一页");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * 获取道具菜单
     */
    public Inventory getItemMenu(Player player) {
        return itemMenus.get(player);
    }
    
    /**
     * 清除玩家数据
     */
    public void clearPlayerData(Player player) {
        itemMenus.remove(player);
    }
} 
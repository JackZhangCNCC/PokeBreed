package io.github.johnson.pokebreed.gui;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import io.github.johnson.pokebreed.Main;
import io.github.johnson.pokebreed.data.BreedingData;
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
 * 繁殖主界面
 */
public class BreedingGUI {
    private final Main plugin;
    private final Map<Player, Pokemon> selectedParent1;
    private final Map<Player, Pokemon> selectedParent2;
    
    public BreedingGUI(Main plugin) {
        this.plugin = plugin;
        this.selectedParent1 = new HashMap<>();
        this.selectedParent2 = new HashMap<>();
    }
    
    /**
     * 打开主界面
     */
    public void openMainMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, "§9宝可梦繁殖");
        
        // 获取繁殖数据
        BreedingData breedingData = plugin.getBreedingManager().getBreedingData(player);
        boolean isBreeding = breedingData != null;
        
        // 父系宝可梦按钮
        ItemStack parent1Button = createParentButton(player, true, isBreeding);
        inventory.setItem(11, parent1Button);
        
        // 母系宝可梦按钮
        ItemStack parent2Button = createParentButton(player, false, isBreeding);
        inventory.setItem(15, parent2Button);
        
        // 如果正在繁殖，显示进度
        if (isBreeding) {
            if (breedingData.isEggReady()) {
                // 显示可领取的蛋
                ItemStack eggButton = createEggButton();
                inventory.setItem(13, eggButton);
            } else {
                // 显示进度
                ItemStack progressButton = createProgressButton(breedingData);
                inventory.setItem(13, progressButton);
            }
        } else if (selectedParent1.containsKey(player) && selectedParent2.containsKey(player)) {
            // 如果已选择父母，显示开始按钮
            ItemStack startButton = createStartButton();
            inventory.setItem(13, startButton);
        }
        
        // 道具按钮
        if (plugin.getConfigManager().isItemsEnabled() && isBreeding && !breedingData.isEggReady()) {
            ItemStack itemButton = createItemButton();
            inventory.setItem(22, itemButton);
        }
        
        // 关闭按钮
        ItemStack closeButton = createCloseButton();
        inventory.setItem(26, closeButton);
        
        player.openInventory(inventory);
    }
    
    /**
     * 更新界面
     */
    public void updateGUI(Player player) {
        if (player.getOpenInventory() != null && 
            player.getOpenInventory().getTitle().equals("§9宝可梦繁殖")) {
            openMainMenu(player);
        }
    }
    
    /**
     * 创建父母按钮
     */
    private ItemStack createParentButton(Player player, boolean isParent1, boolean isBreeding) {
        Pokemon selected = isParent1 ? selectedParent1.get(player) : selectedParent2.get(player);
        BreedingData breedingData = plugin.getBreedingManager().getBreedingData(player);
        
        if (breedingData != null) {
            selected = isParent1 ? breedingData.getParent1() : breedingData.getParent2();
        }
        
        if (selected != null) {
            // 显示已选择的宝可梦
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§6" + (isParent1 ? "父系" : "母系") + "宝可梦");
                List<String> lore = new ArrayList<>();
                lore.add("§7名称: §f" + selected.getLocalizedName());
                lore.add("§7等级: §f" + selected.getPokemonLevel());
                lore.add("§7性别: §f" + selected.getGender().name());
                lore.add("§7个体值:");
                for (BattleStatsType stat : BattleStatsType.values()) {
                    if (stat == BattleStatsType.NONE) continue;
                    lore.add("§7- " + stat.name() + ": §f" + selected.getIVs().getStat(stat));
                }
                if (isBreeding) {
                    lore.add("");
                    lore.add("§7正在繁殖中...");
                } else {
                    lore.add("");
                    lore.add("§e点击更换");
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            return item;
        } else {
            // 显示选择按钮
            ItemStack item = new ItemStack(Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§6选择" + (isParent1 ? "父系" : "母系") + "宝可梦");
                List<String> lore = new ArrayList<>();
                lore.add("§7点击选择一只" + (isParent1 ? "雄性" : "雌性") + "宝可梦");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            return item;
        }
    }
    
    /**
     * 创建开始按钮
     */
    private ItemStack createStartButton() {
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a开始繁殖");
            List<String> lore = new ArrayList<>();
            lore.add("§7点击开始繁殖");
            if (plugin.getConfigManager().isEconomyEnabled()) {
                lore.add("§7费用: §f" + plugin.getConfigManager().getBreedingCost() + " 金币");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * 创建进度按钮
     */
    private ItemStack createProgressButton(BreedingData data) {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6繁殖进度");
            List<String> lore = new ArrayList<>();
            lore.add("§7进度: §f" + String.format("%.1f%%", data.getProgress() * 100));
            lore.add("§7剩余时间: §f" + formatTime(data.getRemainingTime()));
            lore.add("");
            lore.add("§c点击取消繁殖");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * 创建蛋按钮
     */
    private ItemStack createEggButton() {
        ItemStack item = new ItemStack(Material.TURTLE_EGG);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6宝可梦蛋");
            List<String> lore = new ArrayList<>();
            lore.add("§7繁殖完成！");
            lore.add("§a点击领取");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * 创建道具按钮
     */
    private ItemStack createItemButton() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6繁殖道具");
            List<String> lore = new ArrayList<>();
            lore.add("§7点击打开道具界面");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * 创建关闭按钮
     */
    private ItemStack createCloseButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c关闭");
            List<String> lore = new ArrayList<>();
            lore.add("§7点击关闭界面");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * 格式化时间
     */
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "秒";
        }
        if (seconds < 3600) {
            return String.format("%d分%d秒", seconds / 60, seconds % 60);
        }
        return String.format("%d时%d分%d秒", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }
    
    /**
     * 获取已选择的父系宝可梦
     */
    public Pokemon getSelectedParent1(Player player) {
        return selectedParent1.get(player);
    }
    
    /**
     * 获取已选择的母系宝可梦
     */
    public Pokemon getSelectedParent2(Player player) {
        return selectedParent2.get(player);
    }
    
    /**
     * 设置已选择的父系宝可梦
     */
    public void setSelectedParent1(Player player, Pokemon pokemon) {
        selectedParent1.put(player, pokemon);
        plugin.getMessageManager().sendMessage(player, "breeding.parent1_selected", new Object[]{pokemon.getLocalizedName()});
        updateGUI(player);
    }
    
    /**
     * 设置已选择的母系宝可梦
     */
    public void setSelectedParent2(Player player, Pokemon pokemon) {
        selectedParent2.put(player, pokemon);
        plugin.getMessageManager().sendMessage(player, "breeding.parent2_selected", new Object[]{pokemon.getLocalizedName()});
        updateGUI(player);
    }
    
    /**
     * 清除已选择的宝可梦
     */
    public void clearSelectedParents(Player player) {
        selectedParent1.remove(player);
        selectedParent2.remove(player);
        updateGUI(player);
    }
} 
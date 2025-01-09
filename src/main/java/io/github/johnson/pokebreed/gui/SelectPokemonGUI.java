package io.github.johnson.pokebreed.gui;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import io.github.johnson.pokebreed.Main;
import lombok.Getter;
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
 * 选择宝可梦界面
 */
public class SelectPokemonGUI {
    private final Main plugin;
    private final Map<Player, Inventory> selectMenus;
    @Getter
    private final Map<Player, SelectionType> selectionTypes;
    
    public SelectPokemonGUI(Main plugin) {
        this.plugin = plugin;
        this.selectMenus = new HashMap<>();
        this.selectionTypes = new HashMap<>();
    }
    
    /**
     * 打开选择界面
     */
    public void openSelectMenu(Player player, SelectionType type) {
        Inventory inventory = Bukkit.createInventory(null, 36, "§9选择" + (type == SelectionType.PARENT1 ? "父系" : "母系") + "宝可梦");
        
        // 获取玩家的宝可梦队伍
        PlayerPartyStorage storage = plugin.getBreedingManager().getPartyStorage(player);
        
        // 显示可选择的宝可梦
        for (int i = 0; i < 6; i++) {
            Pokemon pokemon = storage.get(i);
            if (pokemon != null) {
                // 检查性别是否符合要求
                boolean isMale = pokemon.getGender().toString().equals("MALE");
                if ((type == SelectionType.PARENT1 && isMale) || (type == SelectionType.PARENT2 && !isMale)) {
                    // 检查是否可以作为父母
                    if (plugin.getBreedingManager().canBeParent(pokemon)) {
                        inventory.setItem(10 + i, createPokemonButton(pokemon));
                    } else {
                        inventory.setItem(10 + i, createInvalidPokemonButton(pokemon, "该宝可梦不能作为父母"));
                    }
                } else {
                    inventory.setItem(10 + i, createInvalidPokemonButton(pokemon, "性别不符合要求"));
                }
            } else {
                inventory.setItem(10 + i, createEmptySlotButton());
            }
        }
        
        // 返回按钮
        ItemStack backButton = createBackButton();
        inventory.setItem(31, backButton);
        
        selectMenus.put(player, inventory);
        selectionTypes.put(player, type);
        player.openInventory(inventory);
    }
    
    /**
     * 创建宝可梦按钮
     */
    private ItemStack createPokemonButton(Pokemon pokemon) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6" + pokemon.getLocalizedName());
            List<String> lore = new ArrayList<>();
            lore.add("§7等级: §f" + pokemon.getPokemonLevel());
            lore.add("§7性别: §f" + pokemon.getGender().name());
            lore.add("§7特性: §f" + pokemon.getAbility().getLocalizedName());
            lore.add("§7性格: §f" + pokemon.getNature().getLocalizedName());
            lore.add("§7个体值:");
            for (BattleStatsType stat : BattleStatsType.values()) {
                if (stat == BattleStatsType.NONE) continue;
                lore.add("§7- " + stat.name() + ": §f" + pokemon.getIVs().getStat(stat));
            }
            lore.add("");
            lore.add("§e点击选择");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * 创建无效宝可梦按钮
     */
    private ItemStack createInvalidPokemonButton(Pokemon pokemon, String reason) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c" + pokemon.getLocalizedName());
            List<String> lore = new ArrayList<>();
            lore.add("§7等级: §f" + pokemon.getPokemonLevel());
            lore.add("§7性别: §f" + pokemon.getGender().name());
            lore.add("");
            lore.add("§c" + reason);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * 创建空槽位按钮
     */
    private ItemStack createEmptySlotButton() {
        ItemStack item = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7空槽位");
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
     * 获取选择菜单
     */
    public Inventory getSelectMenu(Player player) {
        return selectMenus.get(player);
    }
    
    /**
     * 清除玩家数据
     */
    public void clearPlayerData(Player player) {
        selectMenus.remove(player);
        selectionTypes.remove(player);
    }
    
    /**
     * 选择类型枚举
     */
    public enum SelectionType {
        PARENT1,  // 父系
        PARENT2   // 母系
    }
} 
package io.github.johnson.pokebreed.listener;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import io.github.johnson.pokebreed.Main;
import io.github.johnson.pokebreed.data.BreedingData;
import io.github.johnson.pokebreed.gui.SelectPokemonGUI.SelectionType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * GUI监听器
 */
public class GUIListener implements Listener {
    private final Main plugin;
    
    public GUIListener(Main plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getItemMeta() == null) return;
        
        // 主界面
        if (event.getView().getTitle().equals("§9宝可梦繁殖")) {
            event.setCancelled(true);
            handleMainMenuClick(player, clickedItem, event.getSlot());
            return;
        }
        
        // 选择宝可梦界面
        if (event.getView().getTitle().startsWith("§9选择") && event.getView().getTitle().endsWith("宝可梦")) {
            event.setCancelled(true);
            handleSelectMenuClick(player, clickedItem, event.getSlot());
            return;
        }
        
        // 道具界面
        if (event.getView().getTitle().equals("§9繁殖道具")) {
            event.setCancelled(true);
            handleItemMenuClick(player, clickedItem, event.getSlot());
            return;
        }
        
        // 商店界面
        if (event.getView().getTitle().equals("§9道具商店")) {
            event.setCancelled(true);
            handleShopMenuClick(player, clickedItem, event.getSlot());
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        
        // 清理数据
        if (inventory.equals(plugin.getSelectPokemonGUI().getSelectMenu(player))) {
            plugin.getSelectPokemonGUI().clearPlayerData(player);
        } else if (inventory.equals(plugin.getItemGUI().getItemMenu(player))) {
            plugin.getItemGUI().clearPlayerData(player);
        } else if (inventory.equals(plugin.getShopGUI().getShopMenu(player))) {
            plugin.getShopGUI().clearPlayerData(player);
        }
    }
    
    /**
     * 处理主界面点击
     */
    private void handleMainMenuClick(Player player, ItemStack clickedItem, int slot) {
        String title = clickedItem.getItemMeta().getDisplayName();
        
        // 父系宝可梦按钮
        if (slot == 11) {
            if (!plugin.getBreedingManager().isBreeding(player)) {
                plugin.getSelectPokemonGUI().openSelectMenu(player, SelectionType.PARENT1);
            }
            return;
        }
        
        // 母系宝可梦按钮
        if (slot == 15) {
            if (!plugin.getBreedingManager().isBreeding(player)) {
                plugin.getSelectPokemonGUI().openSelectMenu(player, SelectionType.PARENT2);
            }
            return;
        }
        
        // 开始繁殖按钮
        if (slot == 13 && title.equals("§a开始繁殖")) {
            Pokemon parent1 = plugin.getBreedingGUI().getSelectedParent1(player);
            Pokemon parent2 = plugin.getBreedingGUI().getSelectedParent2(player);
            if (parent1 != null && parent2 != null) {
                if (plugin.getBreedingManager().startBreeding(player, parent1, parent2)) {
                    plugin.getBreedingGUI().clearSelectedParents(player);
                }
                plugin.getBreedingGUI().openMainMenu(player);
            }
            return;
        }
        
        // 取消繁殖按钮
        if (slot == 13 && title.equals("§6繁殖进度")) {
            plugin.getBreedingManager().cancelBreeding(player);
            plugin.getBreedingGUI().openMainMenu(player);
            return;
        }
        
        // 领取蛋按钮
        if (slot == 13 && title.equals("§6宝可梦蛋")) {
            plugin.getBreedingManager().collectEgg(player);
            plugin.getBreedingGUI().openMainMenu(player);
            return;
        }
        
        // 道具按钮
        if (slot == 22 && title.equals("§6繁殖道具")) {
            plugin.getItemGUI().openItemMenu(player);
            return;
        }
        
        // 关闭按钮
        if (slot == 26 && title.equals("§c关闭")) {
            player.closeInventory();
        }
    }
    
    /**
     * 处理选择界面点击
     */
    private void handleSelectMenuClick(Player player, ItemStack clickedItem, int slot) {
        String title = clickedItem.getItemMeta().getDisplayName();
        
        // 返回按钮
        if (slot == 31 && title.equals("§c返回")) {
            plugin.getBreedingGUI().openMainMenu(player);
            return;
        }
        
        // 选择宝可梦
        if (slot >= 10 && slot <= 15) {
            SelectionType type = plugin.getSelectPokemonGUI().getSelectionTypes().get(player);
            if (type == null) return;
            
            PlayerPartyStorage storage = plugin.getBreedingManager().getPartyStorage(player);
            Pokemon pokemon = storage.get(slot - 10);
            if (pokemon == null) return;
            
            // 检查性别
            boolean isMale = pokemon.getGender().toString().equals("MALE");
            if ((type == SelectionType.PARENT1 && !isMale) || (type == SelectionType.PARENT2 && isMale)) {
                player.sendMessage(plugin.getMessageManager().getMessage("breeding.wrong_gender"));
                return;
            }
            
            // 检查是否可以作为父母
            if (!plugin.getBreedingManager().canBeParent(pokemon)) {
                player.sendMessage(plugin.getMessageManager().getMessage("breeding.cannot_be_parent"));
                return;
            }
            
            // 设置选择的宝可梦
            if (type == SelectionType.PARENT1) {
                plugin.getBreedingGUI().setSelectedParent1(player, pokemon);
            } else {
                plugin.getBreedingGUI().setSelectedParent2(player, pokemon);
            }
            
            plugin.getBreedingGUI().openMainMenu(player);
        }
    }
    
    /**
     * 处理道具界面点击
     */
    private void handleItemMenuClick(Player player, ItemStack clickedItem, int slot) {
        String title = clickedItem.getItemMeta().getDisplayName();
        
        // 返回按钮
        if (slot == 35 && title.equals("§c返回")) {
            plugin.getBreedingGUI().openMainMenu(player);
            return;
        }
        
        // 商店按钮
        if (slot == 31 && title.equals("§6道具商店")) {
            plugin.getShopGUI().openShopMenu(player);
            return;
        }
        
        // 使用道具
        if (slot >= 10 && slot <= 14) {
            BreedingData breedingData = plugin.getBreedingManager().getBreedingData(player);
            if (breedingData == null) {
                player.sendMessage(plugin.getMessageManager().getMessage("items.no_breeding"));
                return;
            }
            
            String itemId = null;
            switch (slot) {
                case 10:
                    itemId = "time_reduction";
                    break;
                case 11:
                    itemId = "instant_breed";
                    break;
                case 12:
                    itemId = "iv_inheritance";
                    break;
                case 13:
                    itemId = "shiny_boost";
                    break;
                case 14:
                    itemId = "random_boost";
                    break;
            }
            
            if (itemId != null) {
                // 检查是否可叠加
                if (!plugin.getItemManager().isStackable(itemId)) {
                    // 检查是否已经使用过
                    switch (itemId) {
                        case "instant_breed":
                            // 立即完成道具不需要检查
                            break;
                        case "shiny_boost":
                            if (breedingData.getShinyBoost() > 0) {
                                player.sendMessage(plugin.getMessageManager().getMessage("items.already_used"));
                                return;
                            }
                            break;
                        case "random_boost":
                            if (breedingData.isUseRandomBoost()) {
                                player.sendMessage(plugin.getMessageManager().getMessage("items.already_used"));
                                return;
                            }
                            break;
                    }
                }
                
                // 使用道具
                if (plugin.getItemManager().useItem(player, itemId)) {
                    // 应用效果
                    switch (itemId) {
                        case "time_reduction":
                            plugin.getBreedingManager().useTimeReductionItem(player);
                            break;
                        case "instant_breed":
                            plugin.getBreedingManager().useInstantBreedItem(player);
                            break;
                        case "iv_inheritance":
                            breedingData.setIvBoost(breedingData.getIvBoost() + 0.1);
                            break;
                        case "shiny_boost":
                            breedingData.setShinyBoost(0.5);
                            break;
                        case "random_boost":
                            breedingData.setUseRandomBoost(true);
                            break;
                    }
                    
                    player.sendMessage(plugin.getMessageManager().getMessage("items.use_success"));
                    plugin.getItemGUI().openItemMenu(player);
                }
            }
        }
    }
    
    /**
     * 处理商店界面点击
     */
    private void handleShopMenuClick(Player player, ItemStack clickedItem, int slot) {
        String title = clickedItem.getItemMeta().getDisplayName();
        
        // 返回按钮
        if (slot == 31 && title.equals("§c返回")) {
            plugin.getItemGUI().openItemMenu(player);
            return;
        }
        
        // 购买道具
        if (slot >= 10 && slot <= 14) {
            String itemId = null;
            switch (slot) {
                case 10:
                    itemId = "time_reduction";
                    break;
                case 11:
                    itemId = "instant_breed";
                    break;
                case 12:
                    itemId = "iv_inheritance";
                    break;
                case 13:
                    itemId = "shiny_boost";
                    break;
                case 14:
                    itemId = "random_boost";
                    break;
            }
            
            if (itemId != null) {
                if (plugin.getItemManager().buyItem(player, itemId, 1)) {
                    plugin.getShopGUI().openShopMenu(player);
                }
            }
        }
    }
} 
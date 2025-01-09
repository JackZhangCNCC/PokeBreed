package io.github.johnson.pokebreed.listener;

import io.github.johnson.pokebreed.Main;
import io.github.johnson.pokebreed.data.BreedingData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 繁殖监听器
 */
public class BreedListener implements Listener {
    private final Main plugin;
    
    public BreedListener(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 玩家退出时取消繁殖
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        BreedingData data = plugin.getBreedingManager().getBreedingData(player);
        
        if (data != null) {
            // 返还父母宝可梦
            plugin.getBreedingManager().cancelBreeding(player);
        }
        
        // 清理GUI数据
        plugin.getBreedingGUI().clearSelectedParents(player);
        plugin.getSelectPokemonGUI().clearPlayerData(player);
        plugin.getItemGUI().clearPlayerData(player);
        plugin.getShopGUI().clearPlayerData(player);
    }
} 
package io.github.johnson.pokebreed.listener;

import io.github.johnson.pokebreed.Main;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;

/**
 * 培育屋监听器
 */
public class DayCareListener implements Listener {
    private final Main plugin;
    
    public DayCareListener(Main plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;
        
        String blockType = block.getType().name().toLowerCase();
        // 检查是否是培育屋方块
        if (blockType.contains("pixelmon") && blockType.contains("day_care")) {
            Player player = event.getPlayer();
            
            // 取消原版GUI打开
            event.setCancelled(true);
            
            // 打开我们的繁殖界面
            plugin.getBreedingGUI().openMainMenu(player);
        }
    }
} 
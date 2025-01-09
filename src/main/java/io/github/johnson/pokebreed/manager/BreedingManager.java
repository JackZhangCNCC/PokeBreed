package io.github.johnson.pokebreed.manager;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.egg.BreedingLogicProxy;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.pokemon.stats.IVStore;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import io.github.johnson.pokebreed.Main;
import io.github.johnson.pokebreed.data.BreedingData;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * 繁殖管理器
 */
@Getter
public class BreedingManager {
    private final Main plugin;
    private final Map<UUID, BreedingData> breedingMap;
    private final Map<UUID, BukkitRunnable> breedingTasks;
    
    public BreedingManager(Main plugin) {
        this.plugin = plugin;
        this.breedingMap = new HashMap<>();
        this.breedingTasks = new HashMap<>();
    }
    
    /**
     * 获取玩家的队伍存储
     */
    public PlayerPartyStorage getPartyStorage(Player player) {
        return StorageProxy.getParty(player.getUniqueId());
    }
    
    /**
     * 获取玩家的PC存储
     */
    public PCStorage getPCStorage(Player player) {
        return StorageProxy.getPCForPlayer(player.getUniqueId());
    }
    
    /**
     * 检查玩家是否正在繁殖
     */
    public boolean isBreeding(Player player) {
        return breedingMap.containsKey(player.getUniqueId());
    }
    
    /**
     * 获取玩家的繁殖数据
     */
    public BreedingData getBreedingData(Player player) {
        return breedingMap.get(player.getUniqueId());
    }
    
    /**
     * 开始繁殖
     */
    public boolean startBreeding(Player player, Pokemon parent1, Pokemon parent2) {
        if (!canBreed(player, parent1, parent2)) {
            return false;
        }
        
        // 检查玩家是否有足够的金钱
        if (plugin.getConfigManager().isEconomyEnabled()) {
            double cost = plugin.getConfigManager().getBreedingCost();
            if (plugin.getEconomy() == null || !plugin.getEconomy().has(player, cost)) {
                plugin.getMessageManager().sendMessage(player, "breeding.not_enough_money", new Object[]{cost});
                return false;
            }
            // 扣除金钱
            plugin.getEconomy().withdrawPlayer(player, cost);
        }
        
        // 从玩家队伍中移除父母宝可梦
        PlayerPartyStorage party = getPartyStorage(player);
        if (!removeParentsFromParty(party, parent1, parent2)) {
            plugin.getMessageManager().sendMessage(player, "breeding.pokemon_not_found");
            return false;
        }
        
        // 创建繁殖数据
        int duration = plugin.getConfigManager().getBaseDuration();
        BreedingData breedData = new BreedingData(parent1, parent2, duration);
        breedData.setStartTime(System.currentTimeMillis());
        
        // 应用提升效果
        if (plugin.getItemManager().getItemAmount(player, "random_boost") > 0) {
            breedData.setUseRandomBoost(true);
            plugin.getItemManager().removeItem(player, "random_boost", 1);
            plugin.getMessageManager().sendMessage(player, "breeding.random_boost_applied");
        }
        if (plugin.getItemManager().getItemAmount(player, "iv_boost") > 0) {
            breedData.setIvBoost(plugin.getConfigManager().getBaseIvBoost());
            plugin.getItemManager().removeItem(player, "iv_boost", 1);
            plugin.getMessageManager().sendMessage(player, "breeding.iv_boost_applied");
        }
        if (plugin.getItemManager().getItemAmount(player, "shiny_boost") > 0) {
            breedData.setShinyBoost(plugin.getConfigManager().getBaseShinyBoost());
            plugin.getItemManager().removeItem(player, "shiny_boost", 1);
            plugin.getMessageManager().sendMessage(player, "breeding.shiny_boost_applied");
        }
        
        // 保存繁殖数据
        breedingMap.put(player.getUniqueId(), breedData);
        
        // 启动繁殖任务
        startBreedingTask(player);
        
        // 发送成功消息
        plugin.getMessageManager().sendMessage(player, "breeding.started");
        
        // 更新GUI
        plugin.getBreedingGUI().updateGUI(player);
        
        return true;
    }
    
    /**
     * 从玩家队伍中移除父母宝可梦
     */
    private boolean removeParentsFromParty(PlayerPartyStorage party, Pokemon parent1, Pokemon parent2) {
        boolean found1 = false;
        boolean found2 = false;
        
        // 遍历队伍中的宝可梦
        for (int i = 0; i < 6; i++) {
            Pokemon pokemon = party.get(i);
            if (pokemon == null) continue;
            
            if (!found1 && pokemon == parent1) {
                party.set(i, null);
                found1 = true;
            } else if (!found2 && pokemon == parent2) {
                party.set(i, null);
                found2 = true;
            }
            
            if (found1 && found2) break;
        }
        
        return found1 && found2;
    }
    
    /**
     * 检查是否可以繁殖
     */
    public boolean canBreed(Player player, Pokemon parent1, Pokemon parent2) {
        // 检查玩家是否已经在繁殖
        if (isBreeding(player)) {
            plugin.getMessageManager().sendMessage(player, "breeding.already_breeding");
            return false;
        }
        
        // 检查父母是否可以繁殖
        if (!BreedingLogicProxy.canBreed(parent1, parent2)) {
            plugin.getMessageManager().sendMessage(player, "breeding.cannot_breed");
            return false;
        }
        
        // 检查父母等级是否达到要求
        int minLevel = plugin.getConfigManager().getMinLevel();
        if (parent1.getPokemonLevel() < minLevel || parent2.getPokemonLevel() < minLevel) {
            plugin.getMessageManager().sendMessage(player, "breeding.level_too_low", new Object[]{minLevel});
            return false;
        }
        
        return true;
    }
    
    /**
     * 取消繁殖
     */
    public void cancelBreeding(Player player) {
        UUID uuid = player.getUniqueId();
        if (!isBreeding(player)) {
            return;
        }
        
        // 取消任务
        BukkitRunnable task = breedingTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
        
        // 返回父母
        BreedingData data = breedingMap.remove(uuid);
        if (data != null) {
            returnParents(player, data);
            plugin.getMessageManager().sendMessage(player, "breeding.cancelled");
            
            // 更新GUI
            plugin.getBreedingGUI().updateGUI(player);
        }
    }
    
    /**
     * 返回父母
     */
    private void returnParents(Player player, BreedingData data) {
        PlayerPartyStorage party = getPartyStorage(player);
        PCStorage pc = getPCStorage(player);
        
        // 尝试返回到队伍
        Pokemon parent1 = data.getParent1();
        Pokemon parent2 = data.getParent2();
        
        if (party.hasSpace()) {
            party.add(parent1);
        } else {
            pc.add(parent1);
        }
        
        if (party.hasSpace()) {
            party.add(parent2);
        } else {
            pc.add(parent2);
        }
    }
    
    /**
     * 检查繁殖进度
     */
    public void checkBreedingProgress(Player player) {
        UUID uuid = player.getUniqueId();
        BreedingData breedData = breedingMap.get(uuid);
        
        if (breedData != null) {
            long currentTime = System.currentTimeMillis();
            long startTime = breedData.getStartTime();
            int duration = breedData.getDuration();
            
            // 检查是否完成
            if (currentTime - startTime >= duration * 1000L) {
                completeBreeding(player, breedData);
            }
        }
    }
    
    /**
     * 完成繁殖
     */
    private void completeBreeding(Player player, BreedingData breedData) {
        // 生成蛋
        Pokemon parent1 = breedData.getParent1();
        Pokemon parent2 = breedData.getParent2();
        
        Optional<Pokemon> eggOptional = BreedingLogicProxy.makeEgg(parent1, parent2);
        if (!eggOptional.isPresent()) {
            plugin.getMessageManager().sendMessage(player, "breeding.egg_failed");
            return;
        }
        
        Pokemon egg = eggOptional.get();
        
        // 应用提升效果
        if (breedData.isUseRandomBoost()) {
            applyRandomBoost(egg);
        }
        if (breedData.getIvBoost() > 0) {
            applyIVBoost(egg, breedData.getIvBoost());
        }
        if (breedData.getShinyBoost() > 0) {
            applyShinyBoost(egg, breedData.getShinyBoost());
        }
        
        // 保存蛋
        breedData.setEgg(egg);
        breedData.setEggReady(true);
        
        // 通知玩家
        plugin.getMessageManager().sendMessage(player, "breeding.egg_ready");
        if (plugin.getConfigManager().isShowTitle()) {
            player.sendTitle(
                plugin.getMessageManager().getMessage("breeding.egg_ready_title"),
                plugin.getMessageManager().getMessage("breeding.egg_ready_subtitle"),
                10, 70, 20
            );
        }
        
        // 更新GUI
        plugin.getBreedingGUI().updateGUI(player);
    }
    
    /**
     * 启动繁殖任务
     */
    private void startBreedingTask(Player player) {
        UUID uuid = player.getUniqueId();
        BreedingData data = breedingMap.get(uuid);
        if (data == null) return;
        
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                // 更新GUI显示
                plugin.getBreedingGUI().updateGUI(player);
                
                // 检查是否完成
                if (System.currentTimeMillis() - data.getStartTime() >= data.getDuration() * 1000L) {
                    // 完成繁殖
                    completeBreeding(player, data);
                    
                    // 清除任务
                    breedingTasks.remove(uuid);
                    this.cancel();
                }
            }
        };
        
        // 每秒检查一次
        task.runTaskTimer(plugin, 20L, 20L);
        breedingTasks.put(uuid, task);
    }
    
    /**
     * 应用随机提升
     */
    private void applyRandomBoost(Pokemon pokemon) {
        Random random = new Random();
        IVStore ivs = pokemon.getIVs();
        
        // 随机选择一个个体值进行提升
        BattleStatsType[] stats = BattleStatsType.values();
        BattleStatsType stat = stats[random.nextInt(stats.length)];
        
        // 获取当前个体值
        int currentIV = ivs.getStat(stat);
        
        // 计算提升值
        int minBoost = plugin.getConfigManager().getMinRandomBoost();
        int maxBoost = plugin.getConfigManager().getMaxRandomBoost();
        int boost = random.nextInt(maxBoost - minBoost + 1) + minBoost;
        
        // 应用提升，确保不超过31
        ivs.setStat(stat, Math.min(31, currentIV + boost));
    }
    
    /**
     * 应用IV提升
     */
    private void applyIVBoost(Pokemon pokemon, double boost) {
        IVStore ivs = pokemon.getIVs();
        Random random = new Random();
        
        // 遍历所有个体值
        for (BattleStatsType stat : BattleStatsType.values()) {
            int currentIV = ivs.getStat(stat);
            
            // 根据当前个体值获取成功率
            double successRate = plugin.getConfigManager().getIvSuccessRates().getOrDefault(currentIV, 0.0);
            
            // 检查是否成功
            if (random.nextDouble() < successRate * boost) {
                ivs.setStat(stat, Math.min(31, currentIV + 1));
            }
        }
    }
    
    /**
     * 应用闪光提升
     */
    private void applyShinyBoost(Pokemon pokemon, double boost) {
        Random random = new Random();
        if (random.nextDouble() < boost) {
            pokemon.setShiny(true);
        }
    }
    
    /**
     * 给予蛋
     */
    private void giveEgg(Player player, Pokemon egg) {
        PlayerPartyStorage party = getPartyStorage(player);
        PCStorage pc = getPCStorage(player);
        
        // 尝试添加到队伍
        if (party.hasSpace()) {
            party.add(egg);
            plugin.getMessageManager().sendMessage(player, "breeding.egg_claimed");
        } else {
            pc.add(egg);
            plugin.getMessageManager().sendMessage(player, "breeding.pokemon_to_pc");
        }
    }
    
    /**
     * 使用时间减少道具
     */
    public void useTimeReductionItem(Player player) {
        UUID uuid = player.getUniqueId();
        BreedingData breedData = breedingMap.get(uuid);
        
        if (breedData != null && !breedData.isEggReady()) {
            // 减少20%的剩余时间
            long currentTime = System.currentTimeMillis();
            long startTime = breedData.getStartTime();
            int duration = breedData.getDuration();
            
            long elapsed = currentTime - startTime;
            long remaining = duration * 1000L - elapsed;
            long reduction = (long)(remaining * 0.2);
            
            breedData.setStartTime(startTime - reduction);
            plugin.getMessageManager().sendMessage(player, "breeding.time_reduced");
            
            // 检查是否完成
            checkBreedingProgress(player);
        }
    }
    
    /**
     * 使用立即完成道具
     */
    public void useInstantBreedItem(Player player) {
        UUID uuid = player.getUniqueId();
        BreedingData breedData = breedingMap.get(uuid);
        
        if (breedData != null && !breedData.isEggReady()) {
            // 立即完成繁殖
            completeBreeding(player, breedData);
            plugin.getMessageManager().sendMessage(player, "breeding.instantly_completed");
        }
    }
    
    /**
     * 领取蛋
     */
    public void collectEgg(Player player) {
        UUID uuid = player.getUniqueId();
        BreedingData breedData = breedingMap.get(uuid);
        
        if (breedData != null && breedData.isEggReady()) {
            // 返还父母宝可梦和蛋
            returnParents(player, breedData);
            giveEgg(player, breedData.getEgg());
            
            // 清除繁殖数据
            breedingMap.remove(uuid);
        }
    }
    
    /**
     * 检查宝可梦是否可以作为父母
     */
    public boolean canBeParent(Pokemon pokemon) {
        if (pokemon == null) return false;
        // 检查宝可梦是否可以繁殖
        return !pokemon.isUnbreedable();
    }
} 
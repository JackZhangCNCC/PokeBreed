package io.github.johnson.pokebreed.data;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

/**
 * 繁殖数据类
 */
@Getter
@Setter
public class BreedingData {
    private Pokemon parent1;          // 父系宝可梦
    private Pokemon parent2;          // 母系宝可梦
    private Pokemon egg;              // 生成的蛋
    private long startTime;           // 开始时间
    private int duration;             // 持续时间（秒）
    private boolean eggReady;         // 蛋是否已经准备好
    private double ivBoost;           // IV提升系数
    private double shinyBoost;        // 闪光概率提升系数
    private boolean useRandomBoost;   // 是否使用随机提升
    private Set<String> usedItems;    // 已使用的道具
    
    public BreedingData(Pokemon parent1, Pokemon parent2, int duration) {
        this.parent1 = parent1;
        this.parent2 = parent2;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
        this.eggReady = false;
        this.ivBoost = 0.0;
        this.shinyBoost = 0.0;
        this.useRandomBoost = false;
        this.usedItems = new HashSet<>();
    }
    
    /**
     * 检查繁殖是否完成
     */
    public boolean isComplete() {
        return System.currentTimeMillis() - startTime >= duration * 1000L;
    }
    
    /**
     * 获取剩余时间（秒）
     */
    public long getRemainingTime() {
        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = duration * 1000L - elapsed;
        return Math.max(0, remaining / 1000);
    }
    
    /**
     * 获取进度（0-1）
     */
    public double getProgress() {
        long elapsed = System.currentTimeMillis() - startTime;
        return Math.min(1.0, (double) elapsed / (duration * 1000L));
    }
    
    /**
     * 重置繁殖数据
     */
    public void reset() {
        this.parent1 = null;
        this.parent2 = null;
        this.egg = null;
        this.startTime = 0;
        this.duration = 0;
        this.eggReady = false;
        this.ivBoost = 0.0;
        this.shinyBoost = 0.0;
        this.useRandomBoost = false;
    }
    
    /**
     * 检查是否使用过指定道具
     */
    public boolean hasUsedItem(String itemId) {
        return usedItems.contains(itemId);
    }
    
    /**
     * 标记道具为已使用
     */
    public void markItemAsUsed(String itemId) {
        usedItems.add(itemId);
    }
} 
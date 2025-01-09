package io.github.johnson.pokebreed.manager;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.util.NBTTools;
import io.github.johnson.pokebreed.Main;
import io.github.johnson.pokebreed.data.BreedingData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 存储管理器
 */
public class StorageManager {
    private final Main plugin;
    private final Map<UUID, BreedingData> breedingData;
    private final File dataFile;
    private FileConfiguration data;
    
    public StorageManager(Main plugin) {
        this.plugin = plugin;
        this.breedingData = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        loadData();
    }
    
    /**
     * 加载数据
     */
    private void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("无法创建数据文件！");
                e.printStackTrace();
                return;
            }
        }
        
        data = YamlConfiguration.loadConfiguration(dataFile);
        loadBreedingData();
    }
    
    /**
     * 加载繁殖数据
     */
    private void loadBreedingData() {
        breedingData.clear(); // 清除现有数据
        
        ConfigurationSection breedingSection = data.getConfigurationSection("breeding");
        if (breedingSection == null) {
            plugin.getLogger().info("没有找到繁殖数据，将创建新的数据部分");
            data.createSection("breeding");
            return;
        }
        
        int loadedCount = 0;
        int errorCount = 0;
        
        for (String uuidStr : breedingSection.getKeys(false)) {
            try {
                // 验证UUID格式
                if (!uuidStr.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
                    plugin.getLogger().warning("无效的UUID格式：" + uuidStr);
                    errorCount++;
                    continue;
                }
                
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection playerSection = breedingSection.getConfigurationSection(uuidStr);
                
                if (playerSection == null) {
                    plugin.getLogger().warning("玩家 " + uuidStr + " 的数据部分为空");
                    errorCount++;
                    continue;
                }
                
                // 加载并验证必要的数据
                if (!playerSection.contains("duration") || !playerSection.contains("startTime")) {
                    plugin.getLogger().warning("玩家 " + uuidStr + " 的数据不完整");
                    errorCount++;
                    continue;
                }
                
                // 加载父母宝可梦和蛋
                Pokemon parent1 = deserializePokemon(playerSection.getString("parent1"));
                Pokemon parent2 = deserializePokemon(playerSection.getString("parent2"));
                Pokemon egg = deserializePokemon(playerSection.getString("egg"));
                
                // 验证必要的宝可梦数据
                if (parent1 == null && parent2 == null && egg == null) {
                    plugin.getLogger().warning("玩家 " + uuidStr + " 没有有效的宝可梦数据");
                    errorCount++;
                    continue;
                }
                
                // 创建繁殖数据
                int duration = playerSection.getInt("duration");
                if (duration <= 0) {
                    plugin.getLogger().warning("玩家 " + uuidStr + " 的繁殖持续时间无效：" + duration);
                    errorCount++;
                    continue;
                }
                
                BreedingData breedData = new BreedingData(parent1, parent2, duration);
                breedData.setStartTime(playerSection.getLong("startTime"));
                breedData.setEgg(egg);
                breedData.setEggReady(playerSection.getBoolean("eggReady", false));
                breedData.setIvBoost(validateBoostValue(playerSection.getDouble("ivBoost", 0.0)));
                breedData.setShinyBoost(validateBoostValue(playerSection.getDouble("shinyBoost", 0.0)));
                breedData.setUseRandomBoost(playerSection.getBoolean("useRandomBoost", false));
                
                breedingData.put(uuid, breedData);
                loadedCount++;
                
            } catch (Exception e) {
                plugin.getLogger().warning("加载玩家 " + uuidStr + " 的繁殖数据时出错：" + e.getMessage());
                e.printStackTrace();
                errorCount++;
            }
        }
        
        plugin.getLogger().info(String.format("已加载 %d 个繁殖数据，%d 个错误", loadedCount, errorCount));
    }
    
    /**
     * 验证提升值
     */
    private double validateBoostValue(double value) {
        if (value < 0.0) return 0.0;
        if (value > 1.0) return 1.0;
        return value;
    }
    
    /**
     * 保存数据
     */
    public void saveData() {
        saveBreedingData();
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存数据文件！");
            e.printStackTrace();
        }
    }
    
    /**
     * 保存繁殖数据
     */
    private void saveBreedingData() {
        try {
            // 清除现有数据
            data.set("breeding", null);
            ConfigurationSection breedingSection = data.createSection("breeding");
            
            int savedCount = 0;
            int errorCount = 0;
            
            for (Map.Entry<UUID, BreedingData> entry : breedingData.entrySet()) {
                try {
                    UUID uuid = entry.getKey();
                    BreedingData breedData = entry.getValue();
                    
                    // 验证数据完整性
                    if (breedData == null) {
                        plugin.getLogger().warning("玩家 " + uuid + " 的繁殖数据为空");
                        errorCount++;
                        continue;
                    }
                    
                    ConfigurationSection playerSection = breedingSection.createSection(uuid.toString());
                    
                    // 保存父母宝可梦和蛋
                    if (breedData.getParent1() != null) {
                        playerSection.set("parent1", serializePokemon(breedData.getParent1()));
                    }
                    if (breedData.getParent2() != null) {
                        playerSection.set("parent2", serializePokemon(breedData.getParent2()));
                    }
                    if (breedData.getEgg() != null) {
                        playerSection.set("egg", serializePokemon(breedData.getEgg()));
                    }
                    
                    // 保存基本数据
                    playerSection.set("duration", breedData.getDuration());
                    playerSection.set("startTime", breedData.getStartTime());
                    playerSection.set("eggReady", breedData.isEggReady());
                    playerSection.set("ivBoost", validateBoostValue(breedData.getIvBoost()));
                    playerSection.set("shinyBoost", validateBoostValue(breedData.getShinyBoost()));
                    playerSection.set("useRandomBoost", breedData.isUseRandomBoost());
                    
                    savedCount++;
                    
                } catch (Exception e) {
                    plugin.getLogger().warning("保存玩家繁殖数据时出错：" + e.getMessage());
                    e.printStackTrace();
                    errorCount++;
                }
            }
            
            // 保存到文件
            try {
                data.save(dataFile);
                plugin.getLogger().info(String.format("已保存 %d 个繁殖数据，%d 个错误", savedCount, errorCount));
            } catch (IOException e) {
                plugin.getLogger().severe("保存数据文件时出错：" + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("保存繁殖数据时发生严重错误：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 序列化宝可梦数据为字符串
     */
    private String serializePokemon(Pokemon pokemon) {
        if (pokemon == null) return null;
        
        try {
            CompoundNBT nbt = new CompoundNBT();
            pokemon.writeToNBT(nbt);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CompressedStreamTools.func_74799_a(nbt, outputStream);
            
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
            
        } catch (Exception e) {
            plugin.getLogger().warning("序列化宝可梦数据时出错：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 从字符串反序列化宝可梦数据
     */
    private Pokemon deserializePokemon(String data) {
        if (data == null || data.isEmpty()) return null;
        
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
            DataInputStream dataStream = new DataInputStream(byteStream);
            
            CompoundNBT nbt = CompressedStreamTools.func_74794_a(dataStream);
            Pokemon pokemon = PokemonFactory.create(nbt);
            
            return pokemon;
            
        } catch (Exception e) {
            plugin.getLogger().warning("反序列化宝可梦数据时出错：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 获取玩家的繁殖数据
     */
    public BreedingData getBreedingData(Player player) {
        return breedingData.get(player.getUniqueId());
    }
    
    /**
     * 设置玩家的繁殖数据
     */
    public void setBreedingData(Player player, BreedingData data) {
        breedingData.put(player.getUniqueId(), data);
    }
    
    /**
     * 移除玩家的繁殖数据
     */
    public void removeBreedingData(Player player) {
        breedingData.remove(player.getUniqueId());
    }
    
    /**
     * 保存所有数据
     */
    public void saveAll() {
        saveData();
    }
    
    /**
     * 重载数据
     */
    public void reload() {
        loadData();
    }
} 
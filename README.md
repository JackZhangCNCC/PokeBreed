# PokeBreed (宝可梦繁殖系统)

这是一个基于 Pixelmon Mod 的宝可梦繁殖插件，提供了一个完整的宝可梦繁殖系统。

## 功能特点

- 自定义繁殖界面，操作简单直观
- 支持宝可梦个体值显示
- 支持繁殖加速道具系统
- 支持经济系统（可选）
- 支持繁殖进度实时显示
- 支持多语言配置
- 支持自定义消息
- 支持命令控制台使用
- 支持权限管理

## 命令

- `/breed` - 打开繁殖主界面
- `/breed help` - 显示帮助信息
- `/breed reload` - 重载插件配置（需要权限）

## 权限

- `pokebreed.use` - 使用繁殖系统的权限
- `pokebreed.admin` - 管理员权限，可以使用reload命令

## 配置文件

### config.yml
```yaml
# 经济系统设置
economy:
  enabled: true
  breeding-cost: 1000

# 繁殖设置
breeding:
  base-time: 300  # 基础繁殖时间（秒）
  use-title: true  # 是否使用标题显示进度

# 道具系统设置
items:
  enabled: true
```

### messages.yml
```yaml
# 包含所有可自定义的消息
messages:
  prefix: "§6[宝可梦繁殖] "
  breeding:
    start: "§a开始繁殖..."
    complete: "§a繁殖完成！"
    egg_received: "§a已领取宝可梦蛋！"
```

## 依赖

- Pixelmon Mod 1.16.5-9.1.13
- Bukkit/Spigot 1.16.5
- Vault

## 安装

1. 确保服务器已安装所需的依赖
2. 将插件放入 plugins 文件夹
3. 启动服务器，插件会自动生成配置文件
4. 根据需要修改配置文件
5. 使用 `/breed reload` 重载配置

## 注意事项

- 请确保使用正确版本的 Pixelmon Mod
- 如果启用经济系统，请确保已安装 Vault 插件
- 建议在修改配置文件前先备份

## 问题反馈

如果遇到任何问题或有建议，请通过以下方式联系：

- 提交 Issue
- QQ：1151335385

## 许可证

本项目采用 MIT 许可证 
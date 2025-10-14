# 字体文件目录

此目录用于存放书法字体文件。

## 需要的字体文件

请将以下字体文件放置在此目录中：

1. **kaishu.ttf** - 楷书字体
   - 推荐: 思源宋体、文泉驿正黑、或其他开源楷书字体

2. **xingshu.ttf** - 行书字体
   - 推荐: 清松手写体、霞鹜文楷

3. **lishu.ttf** - 隶书字体
   - 需要自行寻找开源隶书字体

4. **caoshu.ttf** - 草书字体
   - 需要自行寻找开源草书字体

5. **zhuanshu.ttf** - 篆书字体
   - 需要自行寻找开源篆书字体

## 推荐开源字体来源

### 思源字体系列 (免费商用)
- **思源宋体**: https://github.com/adobe-fonts/source-han-serif
- **思源黑体**: https://github.com/adobe-fonts/source-han-sans

### 文泉驿字体 (开源)
- **文泉驿正黑**: https://github.com/anthonyfok/fonts-wqy-zenhei
- **文泉驿微米黑**: http://wenq.org/wqy2/index.cgi?MicroHei

### 霞鹜字体 (开源)
- **霞鹜文楷**: https://github.com/lxgw/LxgwWenKai
- **霞鹜新晰黑**: https://github.com/lxgw/LxgwNeoXiHei

### 清松手写体 (开源)
- **清松手写体**: https://github.com/lxgw/kose-font

## 如何添加字体

1. 下载字体文件 (.ttf 或 .otf 格式)
2. 将字体文件重命名为对应的名称 (如 kaishu.ttf)
3. 复制到此目录
4. 重新构建应用

## 注意事项

- 确保字体文件是TrueType (.ttf) 或 OpenType (.otf) 格式
- 字体文件名必须与代码中指定的名称完全一致
- 注意字体的许可证，确保可以用于您的用途
- 字体文件较大会增加APK大小

## 临时解决方案

如果暂时没有书法字体，应用会回退使用系统默认字体。为了获得最佳体验，建议至少添加一个楷书字体文件。

## 最小配置

如果只是测试应用，可以：
1. 只添加一个楷书字体 (kaishu.ttf)
2. 其他字体可以复制同一个文件并重命名
3. 或者将系统字体复制到此目录并重命名

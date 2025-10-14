# 汉字书法练习应用 (Chinese Calligraphy Practice App)

一个专为华为MatePad设计的汉字书法练习原生Android应用，支持M-Pencil手写笔。

## 开发环境要求

### 必需软件
1. **Android Studio** (推荐最新稳定版 Hedgehog 或更高)
   - 下载地址: https://developer.android.com/studio
   - 安装时确保包含 Android SDK、Android SDK Platform 和 Android Virtual Device

2. **JDK** (Java Development Kit)
   - JDK 17 或更高版本
   - Android Studio 通常会自带，也可以单独安装

3. **Kotlin插件**
   - Android Studio 已内置

### Android SDK要求
- **最低SDK版本**: API 26 (Android 8.0)
- **目标SDK版本**: API 34 (Android 14)
- **编译SDK版本**: API 34

### 华为MatePad特定要求
- 为了更好地支持HarmonyOS设备，建议安装:
  - HMS Core SDK (可选，用于华为生态系统集成)
  - 华为开发者工具 (可选)

## 项目技术栈

- **开发语言**: Kotlin
- **UI框架**: Jetpack Compose + 传统View (Canvas绘制)
- **架构模式**: MVVM (Model-View-ViewModel)
- **依赖注入**: Hilt
- **数据库**: Room (本地数据持久化)
- **异步处理**: Kotlin Coroutines + Flow

## 需要准备的资源

### 1. 书法字体文件 (必需)
需要以下开源中文书法字体(TTF/OTF格式):

**推荐开源字体来源:**

- **楷书**:
  - 方正楷体 (需商业授权) 或
  - 思源宋体 (开源): https://github.com/adobe-fonts/source-han-serif
  - 文泉驿正黑: https://github.com/anthonyfok/fonts-wqy-zenhei

- **行书**:
  - 清松手写体: https://github.com/lxgw/kose-font
  - 霞鹜文楷: https://github.com/lxgw/LxgwWenKai

- **隶书/篆书**:
  - 需要自行寻找开源字体或购买授权

**字体放置位置**:
```
app/src/main/assets/fonts/
├── kaishu.ttf          # 楷书
├── xingshu.ttf         # 行书
├── lishu.ttf           # 隶书
├── caoshu.ttf          # 草书
└── zhuanshu.ttf        # 篆书
```

### 2. 笔画顺序数据 (可选但推荐)
- **开源笔画顺序数据库**:
  - Make Me a Hanzi: https://github.com/skishore/makemeahanzi
  - 包含8000+常用汉字的笔画、笔顺、字形数据
  - JSON格式，可直接使用

**数据放置位置**:
```
app/src/main/assets/stroke_data/
└── dictionary.txt      # 从makemeahanzi下载的图形数据
```

### 3. 基础字库列表
已在项目中内置常用3500字列表，按笔画数和难度分类。

## 项目结构

```
ChineseCalligraphy/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/calligraphy/
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/      # 自定义UI组件
│   │   │   │   │   │   ├── CalligraphyCanvas.kt      # 核心手写Canvas
│   │   │   │   │   │   ├── CharacterDisplay.kt       # 汉字显示组件
│   │   │   │   │   │   ├── GridOverlay.kt            # 辅助线网格
│   │   │   │   │   │   └── StrokeAnimator.kt         # 笔画动画
│   │   │   │   │   ├── screens/         # 各个界面
│   │   │   │   │   │   ├── MainScreen.kt             # 主界面
│   │   │   │   │   │   ├── PracticeScreen.kt         # 练习界面
│   │   │   │   │   │   ├── CharacterLibrary.kt       # 字库浏览
│   │   │   │   │   │   └── HistoryScreen.kt          # 历史记录
│   │   │   │   │   └── theme/           # 主题样式
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/           # 数据模型
│   │   │   │   │   ├── repository/      # 数据仓库
│   │   │   │   │   └── database/        # Room数据库
│   │   │   │   ├── domain/              # 业务逻辑
│   │   │   │   └── utils/               # 工具类
│   │   │   ├── res/
│   │   │   │   ├── drawable/            # 图标资源
│   │   │   │   ├── values/              # 字符串、颜色等
│   │   │   │   └── xml/                 # 配置文件
│   │   │   └── assets/
│   │   │       ├── fonts/               # 字体文件 (需要添加)
│   │   │       ├── stroke_data/         # 笔画数据 (需要添加)
│   │   │       └── characters/          # 字库列表
│   │   └── build.gradle.kts
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 关键技术决策

### 1. 手写绘制实现方案
**选择**: 自定义View + Canvas API
- **优点**:
  - 性能最优,延迟<10ms
  - 完全控制绘制细节
  - 支持压感和笔触效果
- **替代方案**: 使用第三方库(如Sketch Library)，但不够灵活

### 2. 压感支持
**实现**: 使用 MotionEvent.getPressure() 和 MotionEvent.getToolType()
- 检测 MotionEvent.TOOL_TYPE_STYLUS 识别M-Pencil
- 根据压力值(0.0-1.0)动态调整笔画粗细
- 使用贝塞尔曲线平滑笔画

### 3. 笔触效果模拟
**方案**: 自定义Paint设置
```kotlin
paint.strokeWidth = basWidth * pressure  // 根据压感调整
paint.strokeCap = Paint.Cap.ROUND        // 圆形笔触
paint.style = Paint.Style.STROKE
paint.isAntiAlias = true                 // 抗锯齿
```

### 4. 字体渲染
**方案**: 使用Typeface加载自定义字体
- 从assets加载字体文件
- 多层Canvas渲染(范本层 + 书写层)
- 半透明叠加实现临摹效果

### 5. 笔画顺序数据
**方案**: 使用SVG路径数据
- 从makemeahanzi获取每个汉字的SVG路径
- 解析成Path对象
- 使用PathMeasure实现逐笔动画

### 6. 数据持久化
**方案**: Room数据库 + File存储
- 字库、练习记录存储在Room
- 书写图片保存为PNG文件
- 支持导出和分享

## 开发阶段规划

### 第一阶段: 基础手写和单字临摹 (MVP)
- [x] 创建项目结构
- [ ] 实现CalligraphyCanvas手写组件
- [ ] 实现压感支持
- [ ] 实现撤销/重做/清除
- [ ] 显示单个汉字范本
- [ ] 实现米字格辅助线

**预计时间**: 2-3天

### 第二阶段: 字库管理和字体切换
- [ ] 创建字库数据结构
- [ ] 实现字体加载和切换
- [ ] 字库分类和筛选
- [ ] 收藏功能

**预计时间**: 2天

### 第三阶段: 笔画顺序和高级功能
- [ ] 集成笔画顺序数据
- [ ] 实现笔画动画播放
- [ ] 添加田字格、九宫格
- [ ] 优化性能

**预计时间**: 2-3天

### 第四阶段: 作品保存和完善
- [ ] 实现作品保存
- [ ] 历史记录浏览
- [ ] 导出分享功能
- [ ] UI/UX优化
- [ ] 横竖屏适配

**预计时间**: 1-2天

## 如何开始

### 1. 环境搭建
```bash
# 安装Android Studio后，创建新项目或导入此项目
# 确保SDK已安装API 26-34
```

### 2. 添加字体资源
- 从上述推荐来源下载字体文件
- 放置到 `app/src/main/assets/fonts/` 目录
- 至少需要一个楷书字体才能运行MVP版本

### 3. 添加笔画数据(可选)
```bash
# 克隆makemeahanzi仓库
git clone https://github.com/skishore/makemeahanzi.git
# 复制dictionary.txt到项目assets目录
```

### 4. 构建运行
```bash
# 使用Android Studio打开项目
# 连接华为MatePad或使用模拟器
# 点击Run按钮
```

## 性能目标

- **手写延迟**: < 20ms (目标 < 10ms)
- **帧率**: 60 FPS
- **内存占用**: < 200MB
- **APK大小**: < 50MB (不含字体), < 100MB (含字体)

## 许可证

待定 (建议使用 MIT 或 Apache 2.0)

## 联系方式

项目开发中，欢迎反馈建议。

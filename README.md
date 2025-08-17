# 盛思掌控板编程助手

一款专为盛思掌控板设计的Android应用，支持通过USB转接头将Python程序刷入掌控板。

## 功能特性

### 核心功能
- **USB串口通信**: 支持通过USB转接头与盛思掌控板进行通信
- **Python代码编辑**: 内置语法高亮的Python代码编辑器
- **项目管理**: 创建、保存和管理多个Python项目
- **代码上传**: 一键将Python代码上传到掌控板
- **设备管理**: 自动检测和连接USB设备

### 技术特性
- 支持ESP32和ESP32-S3芯片的掌控板
- 兼容多种USB转串口芯片（CH340、CP2102、FTDI等）
- MicroPython REPL协议支持
- 代码语法检查和格式化
- 项目文件管理和备份

## 系统要求

- Android 5.0 (API Level 21) 或更高版本
- 支持USB Host模式的Android设备
- USB OTG转接头
- 盛思掌控板（2.0或3.0版本）

## 安装说明

### 从源码编译
1. 克隆项目到本地
2. 使用Android Studio打开项目
3. 同步Gradle依赖
4. 连接Android设备或启动模拟器
5. 点击运行按钮编译安装

### 直接安装APK
1. 下载最新版本的APK文件
2. 在Android设备上启用"未知来源"安装
3. 安装APK文件

## 使用指南

### 首次使用
1. 打开应用，授予必要的权限
2. 使用USB OTG转接头连接掌控板
3. 在设备管理界面扫描并连接设备
4. 创建第一个Python项目
5. 编写代码并上传到掌控板

### 日常使用
1. 在主界面查看和管理项目
2. 点击项目进入代码编辑界面
3. 编写或修改Python代码
4. 保存项目并上传到掌控板
5. 查看掌控板运行结果

## 开发说明

### 项目结构
```
ShenSiControlBoardApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/shensi/controlboard/
│   │   │   ├── MainActivity.kt              # 主界面
│   │   │   ├── CodeEditorActivity.kt        # 代码编辑器
│   │   │   ├── DeviceManagerActivity.kt     # 设备管理
│   │   │   ├── manager/
│   │   │   │   ├── USBManager.kt           # USB通信管理
│   │   │   │   └── ProjectManager.kt       # 项目管理
│   │   │   ├── data/
│   │   │   │   └── Project.kt              # 数据模型
│   │   │   └── adapter/
│   │   │       ├── ProjectAdapter.kt       # 项目列表适配器
│   │   │       └── DeviceAdapter.kt        # 设备列表适配器
│   │   ├── res/                            # 资源文件
│   │   └── AndroidManifest.xml             # 应用清单
│   └── build.gradle                        # 应用级构建配置
├── build.gradle                            # 项目级构建配置
└── settings.gradle                         # Gradle设置
```

### 主要依赖
- `usb-serial-for-android`: USB串口通信库
- `sora-editor`: 代码编辑器组件
- `Material Design Components`: UI组件库
- `AndroidX`: Android支持库

### 技术实现

#### USB通信
使用`usb-serial-for-android`库实现USB串口通信，支持多种USB转串口芯片。通信流程：
1. 扫描USB设备
2. 请求设备权限
3. 建立串口连接
4. 发送MicroPython命令

#### 代码编辑
集成`sora-editor`提供Python语法高亮和代码编辑功能，支持：
- 语法高亮
- 代码自动补全
- 撤销/重做
- 查找替换

#### 项目管理
使用Android文件系统API管理项目文件，支持：
- 项目创建和删除
- 文件保存和读取
- 项目列表显示
- 最近修改排序

## 故障排除

### 常见问题

**设备无法连接**
- 检查USB OTG转接头是否正常工作
- 确认掌控板已正确连接
- 在设备管理界面重新扫描设备
- 检查USB权限是否已授予

**代码上传失败**
- 确认设备已正确连接
- 检查代码语法是否正确
- 尝试重新连接设备
- 查看错误信息并相应处理

**应用崩溃**
- 检查Android版本是否满足要求
- 清除应用数据并重新启动
- 查看系统日志获取详细错误信息

### 调试模式
在开发者选项中启用USB调试，可以通过ADB查看详细日志：
```bash
adb logcat | grep ShenSiControlBoard
```

## 贡献指南

欢迎提交问题报告和功能请求。如需贡献代码：

1. Fork项目仓库
2. 创建功能分支
3. 提交更改
4. 创建Pull Request

## 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 联系方式

如有问题或建议，请通过以下方式联系：
- 项目Issues页面
- 邮箱：support@shensi.com

## 更新日志

### v1.0.0 (2024-01-15)
- 初始版本发布
- 支持基本的USB串口通信
- Python代码编辑和项目管理
- 设备自动检测和连接
- MicroPython代码上传功能


# 构建说明

本文档详细说明如何从源码构建盛思掌控板编程助手Android应用。

## 环境要求

### 开发环境
- **操作系统**: Windows 10/11, macOS 10.14+, 或 Ubuntu 18.04+
- **Android Studio**: Arctic Fox (2020.3.1) 或更高版本
- **JDK**: OpenJDK 11 或 Oracle JDK 11
- **Android SDK**: API Level 21-34
- **Gradle**: 7.0+ (通常由Android Studio管理)

### Android SDK组件
确保安装以下SDK组件：
- Android SDK Platform 34
- Android SDK Build-Tools 34.0.0
- Android SDK Platform-Tools
- Android SDK Tools
- Google USB Driver (Windows)

## 项目设置

### 1. 克隆项目
```bash
git clone https://github.com/your-repo/ShenSiControlBoardApp.git
cd ShenSiControlBoardApp
```

### 2. 导入Android Studio
1. 打开Android Studio
2. 选择 "Open an existing Android Studio project"
3. 选择项目根目录
4. 等待Gradle同步完成

### 3. 配置签名（可选）
如需发布版本，在 `app/build.gradle` 中配置签名：
```gradle
android {
    signingConfigs {
        release {
            storeFile file('path/to/your/keystore.jks')
            storePassword 'your_store_password'
            keyAlias 'your_key_alias'
            keyPassword 'your_key_password'
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            // ... 其他配置
        }
    }
}
```

## 依赖管理

### 主要依赖库
项目使用以下主要依赖：

```gradle
dependencies {
    // Android核心库
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    
    // USB串口通信
    implementation 'com.github.mik3y:usb-serial-for-android:3.7.3'
    
    // 代码编辑器
    implementation 'io.github.rosemoe.sora-editor:editor:0.23.4'
    implementation 'io.github.rosemoe.sora-editor:language-python:0.23.4'
    
    // 数据库
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    kapt 'androidx.room:room-compiler:2.6.1'
}
```

### 仓库配置
确保在 `build.gradle` 中包含必要的仓库：
```gradle
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

## 构建过程

### Debug版本构建
```bash
# 命令行构建
./gradlew assembleDebug

# 或在Android Studio中
Build -> Build Bundle(s) / APK(s) -> Build APK(s)
```

### Release版本构建
```bash
# 命令行构建
./gradlew assembleRelease

# 或在Android Studio中
Build -> Generate Signed Bundle / APK
```

### 构建输出
构建完成后，APK文件位于：
- Debug版本: `app/build/outputs/apk/debug/app-debug.apk`
- Release版本: `app/build/outputs/apk/release/app-release.apk`

## 代码检查

### Lint检查
```bash
./gradlew lint
```
检查报告位于: `app/build/reports/lint-results.html`

### 单元测试
```bash
./gradlew test
```

### 仪器测试
```bash
./gradlew connectedAndroidTest
```

## 常见构建问题

### 1. Gradle同步失败
**问题**: 无法下载依赖或同步失败
**解决方案**:
- 检查网络连接
- 配置代理（如需要）
- 清理Gradle缓存: `./gradlew clean`
- 重新同步项目

### 2. 编译错误
**问题**: Kotlin编译错误
**解决方案**:
- 检查Kotlin版本兼容性
- 更新Android Studio到最新版本
- 清理并重新构建: `./gradlew clean build`

### 3. 依赖冲突
**问题**: 库版本冲突
**解决方案**:
- 检查依赖版本兼容性
- 使用 `./gradlew dependencies` 查看依赖树
- 排除冲突的传递依赖

### 4. 内存不足
**问题**: 构建过程中内存不足
**解决方案**:
在 `gradle.properties` 中增加内存配置:
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=512m
```

## 代码混淆

### ProGuard配置
Release版本默认启用代码混淆，配置文件位于 `app/proguard-rules.pro`：

```proguard
# 保留USB串口库
-keep class com.hoho.android.usbserial.** { *; }

# 保留代码编辑器
-keep class io.github.rosemoe.sora.** { *; }

# 保留数据模型
-keep class com.shensi.controlboard.data.** { *; }

# 保留Kotlin元数据
-keep class kotlin.Metadata { *; }
```

## 版本管理

### 版本号配置
在 `app/build.gradle` 中配置版本信息：
```gradle
android {
    defaultConfig {
        versionCode 1
        versionName "1.0.0"
    }
}
```

### 自动版本号
可以使用Git提交数作为版本号：
```gradle
def getVersionCode() {
    return 'git rev-list --count HEAD'.execute().text.trim().toInteger()
}

def getVersionName() {
    return 'git describe --tags --dirty'.execute().text.trim()
}

android {
    defaultConfig {
        versionCode getVersionCode()
        versionName getVersionName()
    }
}
```

## 持续集成

### GitHub Actions配置
创建 `.github/workflows/build.yml`：
```yaml
name: Build APK

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
        
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
          
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
      
    - name: Build with Gradle
      run: ./gradlew assembleDebug
      
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

## 发布准备

### 1. 代码审查
- 确保所有功能正常工作
- 运行完整的测试套件
- 检查代码质量和安全性

### 2. 版本更新
- 更新版本号
- 更新CHANGELOG.md
- 创建Git标签

### 3. 签名和对齐
```bash
# 签名APK
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore your-keystore.jks app-release-unsigned.apk your-key-alias

# 对齐APK
zipalign -v 4 app-release-unsigned.apk app-release.apk
```

### 4. 测试发布版本
- 在多个设备上测试
- 验证所有功能正常
- 检查性能和稳定性

## 故障排除

### 构建日志
查看详细构建日志：
```bash
./gradlew assembleDebug --info --stacktrace
```

### 清理项目
完全清理项目：
```bash
./gradlew clean
rm -rf .gradle
rm -rf app/build
```

### 重置Gradle
重置Gradle Wrapper：
```bash
./gradlew wrapper --gradle-version 7.6
```

## 开发工具

### 推荐插件
Android Studio推荐插件：
- Kotlin
- Android APK Analyzer
- Database Inspector
- Layout Inspector

### 调试工具
- ADB (Android Debug Bridge)
- Logcat
- Memory Profiler
- CPU Profiler

## 性能优化

### 构建性能
优化构建速度：
```properties
# gradle.properties
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
```

### APK大小优化
- 启用代码混淆
- 移除未使用的资源
- 使用WebP格式图片
- 启用APK分包（如需要）

```gradle
android {
    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

## 技术支持

如在构建过程中遇到问题，请：
1. 查看本文档的故障排除部分
2. 搜索相关错误信息
3. 提交Issue到项目仓库
4. 联系开发团队

---

**注意**: 本文档会随着项目更新而更新，请定期查看最新版本。


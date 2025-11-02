# @shenzhi/ad-aggregator

基于穿山甲的聚合模式开发的RN版本的广告sdk

## 特性

- ✅ 封装了穿山甲7.2.0.9融合SDK
- ✅ 内置所有必要的依赖，无需额外配置
- ✅ 支持多个ADN（广告联盟）：
  - 穿山甲（字节跳动）
  - GDT优量汇（腾讯）
  - 百度广告
  - 快手广告
  - Sigmob
  - AdMob（谷歌）

## 安装

```sh
npm install @shenzhi/ad-aggregator
```

或使用yarn：

```sh
yarn add @shenzhi/ad-aggregator
```

## 配置

### Android配置

安装后**无需额外配置**，所有穿山甲SDK依赖已内置在库中。

- ✅ 穿山甲融合SDK（7.2.0.9版本，本地aar方式）
- ✅ 已添加okhttp依赖（3.12.1版本）
- ✅ 所有ADN SDK已内置在`android/libs/adn`目录
- ✅ 所有ADN Adapter已内置在`android/libs/adapter`目录，版本已匹配
- ✅ 已配置FileProvider和TTMultiProvider
- ✅ 已添加所有必要权限
- ✅ 已配置ProGuard混淆规则

#### 必要配置检查

请确保你的React Native项目Android配置满足以下要求：

1. **最低SDK版本**：
   - minSdkVersion >= 21（API Level 21，Android 5.0）
   - 注意：穿山甲SDK 68版本起要求minSdkVersion >= 24，本库使用7.2.0.9版本，最低支持21

2. **权限配置**：本库已自动包含所有必要权限，应用需要在运行时动态请求相关权限

3. **Provider配置**：
   - 本库已自动配置FileProvider和TTMultiProvider
   - **重要**：`${applicationId}`占位符会在构建时**自动替换为应用的applicationId（包名）**，无需手动配置
   - 如果应用已配置FileProvider，Android构建系统会自动合并配置
   - 如果出现Provider冲突，请检查应用的AndroidManifest.xml，确保authorities不重复

4. **混淆配置**：本库已包含ProGuard规则，会自动应用到你的项目中



#### 项目build.gradle配置

确保你的项目根目录`android/build.gradle`包含以下仓库：

```gradle
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
```
#### 项目工程gradle.properties配置
 - Automatically convert third-party libraries to use AndroidX

```
android.useAndroidX=true
# Automatically convert third-party libraries to use AndroidX
android.enableJetifier=true
```

#### 依赖说明

本库已包含以下依赖：

**本地aar方式（方式二）：**
- ✅ 穿山甲融合SDK：open_ad_sdk_7.2.0.9.aar
- ✅ okhttp3:okhttp:3.12.1（穿山甲SDK必需，Maven方式）
- ✅ 所有ADN SDK（GDT、百度、快手、Sigmob）
- ✅ 所有ADN Adapter（已与SDK版本匹配）
- ✅ 测试工具（tools-release.aar，仅在Debug构建中包含，Release构建自动排除）

**配置说明：**
- 所有SDK和Adapter均采用本地aar方式引入，参考官方demo项目配置
- 使用`fileTree`方式自动引入libs目录下的所有aar和jar文件
- 测试工具（tools-release.aar）仅在Debug构建中包含，Release构建自动排除

#### AdMob配置

本库已内置AdMob SDK（Google Mobile Ads SDK 17.2.0），并使用占位符方式配置App ID。

**配置方式：通过build.gradle配置（推荐）**

库中已提供默认测试App ID，您可以在应用的`android/app/build.gradle`中配置真实App ID：

```gradle
android {
    defaultConfig {
        // ... 其他配置 ...

        // AdMob App ID配置
        manifestPlaceholders = [
            ADMOB_APP_ID: "ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX" // 您的真实App ID
        ]
    }
}
```

**配置说明**：
- 库中已默认配置测试App ID：`ca-app-pub-3940256099942544~3347511713`（仅用于开发测试）
- 生产环境请在应用的`build.gradle`中配置您的真实App ID
- 应用中的`manifestPlaceholders`会自动覆盖库中的默认值
- 获取真实App ID：登录[AdMob控制台](https://apps.admob.com)创建应用后获取

**示例配置**（`android/app/build.gradle`）：

```gradle
android {
    defaultConfig {
        applicationId "com.your.app"
        // ... 其他配置 ...

        manifestPlaceholders = [
            ADMOB_APP_ID: "ca-app-pub-3940256099942544~3347511713" // 测试App ID
            // 生产环境请替换为：
            // ADMOB_APP_ID: "ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX"
        ]
    }
}
```

### iOS配置

iOS配置将在后续版本中提供。

## 使用方法

```js
import { multiply } from '@shenzhi/ad-aggregator';

// 示例方法
const result = multiply(3, 7);

// TODO: 更多广告相关API将在后续版本中添加
```

## SDK版本信息

- 穿山甲融合SDK：7.2.0.9
- GDT SDK：4.660.1530
- 百度 SDK：9.423
- 快手 SDK：4.9.20.1
- Sigmob SDK：4.24.7
- AdMob SDK：17.2.0

## 注意事项

1. 本SDK已包含穿山甲测试工具（tools-release.aar），在生产环境构建时会自动优化
2. 请确保在使用前已在穿山甲平台注册应用并获取AppID
3. 建议在用户同意隐私协议后再初始化SDK


## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)

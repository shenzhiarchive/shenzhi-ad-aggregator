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

本库采用**官方推荐的方式二（本地aar方式）**（参考[穿山甲官方文档](https://www.csjplatform.com/supportcenter/5397)），参考官方demo项目配置，已包含以下配置：

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

#### applicationId说明

**重要说明**：本库的AndroidManifest.xml中使用了`${applicationId}`占位符：

- ✅ **自动替换**：Android构建系统会在编译时自动将`${applicationId}`替换为**你的应用的applicationId（包名）**
- ✅ **无需配置**：你不需要在库中或应用中做任何额外配置
- ✅ **自动合并**：如果应用已配置FileProvider，构建系统会自动合并配置

**Provider配置示例**（库中已配置，应用无需修改）：
```xml
<!-- 库中的配置会自动使用应用的applicationId -->
<provider
    android:authorities="${applicationId}.fileprovider"
    ... />
    
<provider
    android:authorities="${applicationId}.TTMultiProvider"
    ... />
```

**如果出现Manifest合并冲突**：

常见的冲突包括：
- `android:allowBackup`：穿山甲SDK默认值为`true`，如果应用设置为`false`会有冲突
- Provider的`android:authorities`：如果应用已配置相同authorities的Provider

**解决方法**：在应用的`android/app/src/main/AndroidManifest.xml`的`<application>`标签中添加：
```xml
<application
    ...
    tools:replace="android:allowBackup">
```

如果需要替换多个属性，可以用逗号分隔：
```xml
tools:replace="android:allowBackup,android:authorities"
```

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

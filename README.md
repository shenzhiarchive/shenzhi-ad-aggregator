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

本库采用**官方推荐的Maven方式（方式一）**（参考[穿山甲官方文档](https://www.csjplatform.com/supportcenter/5397)），并结合**自动拉取Adapter AAR功能**（参考[自动拉取Adapter文档](https://bytedance.larkoffice.com/docx/VnXJdoYIroFxEkx2s5ActC9hnSb)），已包含以下配置：

- ✅ 穿山甲融合SDK通过Maven自动拉取（7.2.0.9版本）
- ✅ 已添加okhttp依赖（3.12.1版本，Maven方式）
- ✅ 所有ADN SDK已内置在`android/libs/adn`目录
- ✅ ADN Adapter通过mediation-auto-adapter插件自动拉取，无需手动配置
- ✅ 已配置FileProvider和TTMultiProvider
- ✅ 已添加所有必要权限
- ✅ 已配置ProGuard混淆规则

#### 必要配置检查

请确保你的React Native项目Android配置满足以下要求：

1. **最低SDK版本**：
   - minSdkVersion >= 21（API Level 21，Android 5.0）
   - 注意：穿山甲SDK 68版本起要求minSdkVersion >= 24，本库使用7.2.0.9版本，最低支持21

2. **权限配置**：本库已自动包含所有必要权限，应用需要在运行时动态请求相关权限

3. **Provider配置**：本库已自动配置FileProvider和TTMultiProvider，无需额外配置

4. **混淆配置**：本库已包含ProGuard规则，会自动应用到你的项目中

#### 项目build.gradle配置

确保你的项目根目录`android/build.gradle`包含以下仓库：

```gradle
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url "https://artifact.bytedance.com/repository/pangle" }
    }
}
```

#### 依赖说明

本库已包含以下依赖：

**Maven方式（自动拉取）：**
- ✅ okhttp3:okhttp:3.12.1（穿山甲SDK必需）
- ✅ com.pangle.cn:mediation-sdk:7.2.0.9（穿山甲融合SDK）

**本地aar方式：**
- ✅ 所有ADN SDK（GDT、百度、快手、Sigmob）
- ✅ 测试工具（tools-release.aar，仅在Debug构建中包含，Release构建自动排除）

**自动拉取（无需手动配置）：**
- ✅ 所有ADN Adapter通过mediation-auto-adapter插件自动拉取匹配版本

#### 自动拉取Adapter说明

本库已配置mediation-auto-adapter插件，会自动：
- 检测已引入的ADN SDK版本
- 自动下载匹配的Adapter版本
- 无需手动管理Adapter依赖

如需自定义配置，可参考[官方文档](https://bytedance.larkoffice.com/docx/VnXJdoYIroFxEkx2s5ActC9hnSb)。

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

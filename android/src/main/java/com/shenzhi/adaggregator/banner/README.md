# Banner广告组件使用文档

## 概述

BannerAdView是基于穿山甲融合SDK实现的Banner广告原生组件，用于在React Native应用中展示Banner广告。

## 组件位置

原生Android组件位于：
```
android/src/main/java/com/shenzhi/adaggregator/banner/
├── BannerAdView.kt          # Banner广告View组件
└── BannerAdViewManager.kt   # React Native ViewManager
```

布局文件：
```
android/src/main/res/layout/mediation_activity_banner.xml
```

## 功能特性

1. **自动加载和展示**：设置codeId和adSize后自动加载并展示广告
2. **事件回调**：支持广告点击、展示、渲染成功/失败、错误等事件回调
3. **Dislike功能**：支持用户点击不喜欢按钮关闭广告
4. **资源管理**：组件销毁时自动清理广告资源，避免内存泄漏
5. **SDK集成**：通过ADCore获取TTAdManager实例，确保SDK正确初始化

## 使用前准备

### 1. 初始化SDK

在使用Banner广告组件之前，必须先初始化穿山甲融合SDK：

```typescript
import { initMediationAdSdk } from '@shenzhi/ad-aggregator';

// 初始化SDK
await initMediationAdSdk({
  appId: 'your-app-id',
  debug: false,
  // ... 其他配置
});
```

### 2. 确保SDK已就绪

```typescript
import { isSdkReady } from '@shenzhi/ad-aggregator';

const ready = await isSdkReady();
if (!ready) {
  console.warn('SDK未就绪，请先初始化');
}
```

## 组件使用

### 基础用法

```tsx
import BannerAdView from '@shenzhi/ad-aggregator';

function App() {
  return (
    <BannerAdView
      codeId="your-banner-code-id"
      adSize={{ width: 320, height: 50 }}
      onAdClicked={() => console.log('广告被点击')}
      onAdShow={() => console.log('广告展示')}
      onError={(error) => console.error('广告错误:', error)}
    />
  );
}
```

### 完整示例

```tsx
import React, { useRef } from 'react';
import { View, StyleSheet } from 'react-native';
import BannerAdView, { type BannerAdViewRef } from '@shenzhi/ad-aggregator';

function BannerExample() {
  const bannerRef = useRef<BannerAdViewRef>(null);

  return (
    <View style={styles.container}>
      <BannerAdView
        ref={bannerRef}
        codeId="your-banner-code-id"
        adSize={{ width: 320, height: 50 }}
        onAdClicked={() => {
          console.log('广告被点击');
        }}
        onAdShow={() => {
          console.log('广告展示成功');
        }}
        onRenderSuccess={(data) => {
          console.log('广告渲染成功:', data.width, data.height);
        }}
        onRenderFail={(error) => {
          console.error('广告渲染失败:', error.code, error.message);
        }}
        onDislike={(data) => {
          console.log('用户点击不喜欢:', data.position, data.value);
        }}
        onError={(error) => {
          console.error('广告加载错误:', error.code, error.message);
        }}
        style={styles.banner}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  banner: {
    width: 320,
    height: 50,
  },
});
```

## 属性说明

### BannerAdViewProps

| 属性名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| codeId | string | 是 | 聚合广告位ID |
| adSize | BannerSize | 是 | 广告尺寸，单位dp。格式：`{width: number, height: number}` |
| style | ViewStyle | 否 | 容器样式 |
| onAdClicked | () => void | 否 | 广告点击回调 |
| onAdShow | () => void | 否 | 广告展示回调 |
| onRenderSuccess | (data: {width: number, height: number}) => void | 否 | 广告渲染成功回调 |
| onRenderFail | (error: {code: number, message: string}) => void | 否 | 广告渲染失败回调 |
| onDislike | (data: {position: number, value: string}) => void | 否 | 用户点击不喜欢回调 |
| onError | (error: {code: number, message: string}) => void | 否 | 广告加载错误回调 |

### BannerSize

```typescript
interface BannerSize {
  width: number;  // 单位：dp
  height: number; // 单位：dp
}
```

## 方法说明

### BannerAdViewRef

通过ref可以调用以下方法：

```typescript
interface BannerAdViewRef {
  /**
   * 手动加载广告
   */
  loadAd: () => void;

  /**
   * 销毁广告
   */
  destroy: () => void;

  /**
   * 检查广告是否已加载
   */
  isAdLoaded: () => boolean;
}
```

### 使用ref方法

```tsx
const bannerRef = useRef<BannerAdViewRef>(null);

// 手动加载广告
bannerRef.current?.loadAd();

// 销毁广告
bannerRef.current?.destroy();

// 检查广告是否已加载
const loaded = bannerRef.current?.isAdLoaded();
```

## 事件说明

### onAdClicked
广告被点击时触发，无参数。

### onAdShow
广告展示时触发，无参数。

### onRenderSuccess
广告渲染成功时触发，返回广告的实际宽高。

```typescript
onRenderSuccess={(data) => {
  console.log('宽度:', data.width);
  console.log('高度:', data.height);
}}
```

### onRenderFail
广告渲染失败时触发，返回错误码和错误信息。

```typescript
onRenderFail={(error) => {
  console.error('错误码:', error.code);
  console.error('错误信息:', error.message);
}}
```

### onDislike
用户点击不喜欢/关闭广告时触发，返回位置和值。

```typescript
onDislike={(data) => {
  console.log('位置:', data.position);
  console.log('值:', data.value);
}}
```

### onError
广告加载错误时触发，返回错误码和错误信息。

```typescript
onError={(error) => {
  console.error('错误码:', error.code);
  console.error('错误信息:', error.message);
}}
```

## 实现原理

### 1. 组件初始化

- BannerAdView继承自FrameLayout
- 加载`mediation_activity_banner.xml`布局文件
- 获取`banner_container`作为广告容器

### 2. 广告加载流程

1. **创建TTAdNative对象**
   ```kotlin
   val adManager = ADCore.getTTAdManager()
   val adNativeLoader = adManager.createAdNative(activity)
   ```

2. **创建AdSlot**
   ```kotlin
   val adSlot = AdSlot.Builder()
       .setCodeId(codeId)
       .setImageAcceptedSize(widthPx, heightPx)  // 自渲染尺寸，单位px
       .setExpressViewAcceptedSize(screenWidthDp, 0f)  // 模板广告尺寸，单位dp
       .build()
   ```

3. **加载广告**
   ```kotlin
   adNativeLoader.loadBannerExpressAd(adSlot, listener)
   ```

4. **展示广告**
   - 在`onNativeExpressAdLoad`回调中获取广告对象
   - 设置交互监听器和dislike回调
   - 获取广告View并添加到容器

### 3. 事件处理

所有事件通过`RCTEventEmitter`发送到React Native层：

```kotlin
reactContext.getJSModule(RCTEventEmitter::class.java)
    .receiveEvent(id, eventName, eventData)
```

### 4. 资源清理

- 组件销毁时（`onDetachedFromWindow`）自动调用`destroyAd()`
- 销毁广告对象，清理容器，避免内存泄漏

## 注意事项

1. **SDK初始化**：使用前必须确保SDK已初始化并就绪
2. **广告尺寸**：建议设置的宽高比例与广告位配置的比例接近，避免非等比例拉伸
3. **广告位ID**：使用聚合广告位ID，不是ADN的代码位
4. **生命周期**：组件会自动处理广告的加载和销毁，无需手动管理
5. **线程安全**：所有UI操作都在主线程执行
6. **错误处理**：建议实现`onError`回调来处理加载失败的情况

## 常见问题

### Q: 广告不显示怎么办？

A: 检查以下几点：
1. SDK是否已初始化
2. codeId是否正确
3. adSize是否已设置
4. 查看日志中的错误信息
5. 检查网络连接

### Q: 如何获取广告的ECPM信息？

A: 可以在`onRenderSuccess`或`onAdShow`回调后，通过`TTNativeExpressAd.getMediationManager().getShowEcpm()`获取。

### Q: 支持Banner混出信息流广告吗？

A: 当前实现支持模板广告和自渲染广告，如需支持Banner混出信息流，需要在AdSlot中设置`setMediationNativeToBannerListener`。

## 参考文档

- [穿山甲Banner广告接入文档](https://www.csjplatform.com/union/media/union/download/detail?id=195&docId=27627&locale=zh-CN&osType=android)
- ADCore核心类：`android/src/main/java/com/shenzhi/adaggregator/core/ADCore.kt`

## 更新日志

### v1.0.0
- 初始版本
- 支持Banner广告加载和展示
- 支持所有事件回调
- 支持Dislike功能
- 自动资源清理


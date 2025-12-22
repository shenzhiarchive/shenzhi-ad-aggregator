# @shenzhi/ad-aggregator

基于穿山甲的聚合模式开发的 React Native 版本的广告 SDK

## 特性

- ✅ 支持穿山甲聚合模式
- ✅ TypeScript 类型支持
- ✅ 支持 React Native 新架构（TurboModules）
- ✅ 单例模式管理，确保 SDK 只初始化一次
- ✅ 完整的错误处理和日志记录
- ✅ 线程安全，自动在主线程执行初始化

## Installation

```sh
npm install @shenzhi/ad-aggregator
```

或使用 yarn:

```sh
yarn add @shenzhi/ad-aggregator
```

## 快速开始

### 1. 初始化 SDK

在使用任何广告功能之前，必须先初始化 SDK：

```typescript
import { initialize } from '@shenzhi/ad-aggregator';

// 基本初始化
await initialize('your-app-id');

// 或使用自定义配置
await initialize('your-app-id', {
  appName: 'My Application',
  debug: false, // 生产环境必须设置为 false
  useMediation: true, // 聚合模式必须为 true
});
```

### 2. 在应用启动时初始化

推荐在应用启动时（如 `App.tsx`）进行初始化：

```typescript
import React, { useEffect } from 'react';
import { initialize } from '@shenzhi/ad-aggregator';

function App() {
  useEffect(() => {
    const initSDK = async () => {
      try {
        await initialize('your-app-id', {
          appName: 'My App',
          debug: __DEV__,
        });
        console.log('广告 SDK 初始化成功');
      } catch (error) {
        console.error('广告 SDK 初始化失败:', error);
      }
    };

    initSDK();
  }, []);

  return (
    // 你的应用组件
  );
}
```

## API 文档

详细的 API 文档请参考：[API 文档](./docs/API.md)

### 主要 API

#### `initialize(appId: string, config?: SDKConfig): Promise<void>`

初始化广告 SDK。

**参数：**
- `appId` (必填): 广告平台应用 ID
- `config` (可选): SDK 配置参数

**配置参数：**
- `appName?: string` - 应用名称
- `debug?: boolean` - 调试模式（默认: `false`）
- `useMediation?: boolean` - 使用聚合功能（默认: `true`）
- `allowShowNotify?: boolean` - 允许显示通知（默认: `true`）
- `supportMultiProcess?: boolean` - 支持多进程（默认: `false`）
- `titleBarTheme?: number` - 标题栏主题（默认: `1` DARK）

## 注意事项

1. **单次初始化**：SDK 只能初始化一次，重复调用会直接返回成功
2. **初始化时机**：必须在加载任何广告之前完成初始化
3. **生产环境**：上线前必须将 `debug` 设置为 `false`
4. **聚合模式**：`useMediation` 必须设置为 `true`

## 错误处理

```typescript
try {
  await initialize('your-app-id');
} catch (error: any) {
  switch (error.code) {
    case 'INVALID_APP_ID':
      console.error('应用 ID 无效');
      break;
    case 'NO_CONTEXT':
      console.error('应用上下文不可用');
      break;
    case 'INIT_ERROR':
      console.error('初始化失败:', error.message);
      break;
  }
}
```


## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)

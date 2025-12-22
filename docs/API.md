# SDK 初始化 API 文档

## 概述

本文档介绍如何使用 `@shenzhi/ad-aggregator` SDK 的初始化功能。SDK 初始化是使用所有广告功能的前提，必须在应用启动时完成初始化。

## 架构设计

### 设计原则

- **单例模式**：SDK 初始化管理器采用单例模式，确保全局只有一个初始化管理器
- **原生层实现**：所有初始化逻辑在原生层（Android/iOS）实现，JS 层仅通过接口调用
- **类型安全**：完整的 TypeScript 类型定义，提供类型检查和自动补全
- **线程安全**：初始化操作在主线程执行，使用同步机制保护初始化状态
- **单次初始化**：SDK 只能初始化一次，重复调用会返回成功状态

## API 接口

### `initialize(appId: string, config?: SDKConfig): Promise<void>`

初始化广告 SDK。

#### 参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `appId` | `string` | 是 | 广告平台应用 ID，从穿山甲媒体平台获取 |
| `config` | `SDKConfig` | 否 | SDK 配置参数对象，详见下方配置说明 |

#### 返回值

返回 `Promise<void>`，初始化成功时 resolve，失败时 reject。

#### 配置参数说明 (`SDKConfig`)

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `appName` | `string` | - | 应用名称，用于日志和调试 |
| `debug` | `boolean` | `false` | 是否开启调试模式，上线前必须关闭 |
| `useMediation` | `boolean` | `true` | 是否使用聚合功能，聚合模式必须设置为 `true` |
| `allowShowNotify` | `boolean` | `true` | 是否允许显示通知 |
| `supportMultiProcess` | `boolean` | `false` | 是否支持多进程 |
| `titleBarTheme` | `number` | `1` (DARK) | 标题栏主题，可选值：`1` (DARK) 或 `2` (LIGHT) |

## 使用示例

### 基本初始化

最简单的初始化方式，使用默认配置：

```typescript
import { initialize } from '@shenzhi/ad-aggregator';

try {
  await initialize('your-app-id');
  console.log('SDK 初始化成功');
} catch (error) {
  console.error('SDK 初始化失败:', error);
}
```

### 完整配置初始化

使用自定义配置参数：

```typescript
import { initialize } from '@shenzhi/ad-aggregator';

try {
  await initialize('your-app-id', {
    appName: 'My Application',
    debug: false, // 生产环境必须设置为 false
    useMediation: true, // 聚合模式必须为 true
    allowShowNotify: true,
    supportMultiProcess: false,
    titleBarTheme: 1 // TTAdConstant.TITLE_BAR_THEME_DARK
  });
  console.log('SDK 初始化成功');
} catch (error) {
  console.error('SDK 初始化失败:', error);
}
```

### 在 React Native 应用中使用

推荐在应用启动时（如 `App.tsx` 或 `index.js`）进行初始化：

```typescript
import React, { useEffect } from 'react';
import { initialize } from '@shenzhi/ad-aggregator';

function App() {
  useEffect(() => {
    const initSDK = async () => {
      try {
        await initialize('your-app-id', {
          appName: 'My App',
          debug: __DEV__, // 开发环境开启调试
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

### 使用 Promise 链式调用

```typescript
import { initialize } from '@shenzhi/ad-aggregator';

initialize('your-app-id')
  .then(() => {
    console.log('初始化成功');
    // 可以开始加载广告
  })
  .catch((error) => {
    console.error('初始化失败:', error);
    // 处理错误
  });
```

## 错误处理

### 错误类型

初始化可能返回以下错误：

| 错误代码 | 说明 | 解决方案 |
|----------|------|----------|
| `INVALID_APP_ID` | appId 为空或无效 | 检查 appId 是否正确传递 |
| `NO_CONTEXT` | 应用上下文不可用 | 确保在应用启动后调用初始化 |
| `INIT_ERROR` | 初始化过程出错 | 查看详细错误信息，检查网络和权限配置 |

### 错误处理示例

```typescript
import { initialize } from '@shenzhi/ad-aggregator';

try {
  await initialize('your-app-id');
} catch (error: any) {
  switch (error.code) {
    case 'INVALID_APP_ID':
      console.error('应用 ID 无效，请检查配置');
      break;
    case 'NO_CONTEXT':
      console.error('应用上下文不可用，请稍后重试');
      break;
    case 'INIT_ERROR':
      console.error('初始化失败:', error.message);
      // 可以重试初始化
      break;
    default:
      console.error('未知错误:', error);
  }
}
```

## 重要注意事项

### 1. 单次初始化原则

- SDK 只能初始化一次
- 重复调用 `initialize` 方法会直接返回成功，不会重复初始化
- 初始化状态在原生层持久化，应用重启后需要重新初始化

### 2. 初始化时机

- **推荐**：在应用启动时（如 `App.tsx` 或 `index.js`）进行初始化
- **避免**：在广告组件内部初始化，可能导致重复初始化或时序问题
- **必须**：在加载任何广告之前完成初始化

### 3. 配置参数建议

- **生产环境**：必须将 `debug` 设置为 `false`，否则会影响性能
- **聚合模式**：`useMediation` 必须设置为 `true`
- **应用名称**：建议设置 `appName`，便于日志追踪和问题排查

### 4. 线程安全

- 初始化操作会自动在主线程执行
- 无需手动处理线程切换
- 初始化过程是异步的，不会阻塞 JS 线程

### 5. 错误恢复

- 初始化失败后可以重试
- 建议实现重试机制，最多重试 3 次
- 重试间隔建议为 1-2 秒

## 最佳实践

### 1. 初始化封装

创建一个初始化工具函数：

```typescript
// utils/adSDK.ts
import { initialize, type SDKConfig } from '@shenzhi/ad-aggregator';

const APP_ID = 'your-app-id';

const defaultConfig: SDKConfig = {
  appName: 'My Application',
  debug: __DEV__,
  useMediation: true,
};

let isInitialized = false;
let initPromise: Promise<void> | null = null;

export async function initAdSDK(config?: SDKConfig): Promise<void> {
  if (isInitialized) {
    return Promise.resolve();
  }

  if (initPromise) {
    return initPromise;
  }

  initPromise = initialize(APP_ID, { ...defaultConfig, ...config })
    .then(() => {
      isInitialized = true;
    })
    .catch((error) => {
      initPromise = null;
      throw error;
    });

  return initPromise;
}
```

### 2. 带重试的初始化

```typescript
async function initWithRetry(
  appId: string,
  config?: SDKConfig,
  maxRetries: number = 3
): Promise<void> {
  let lastError: Error | null = null;

  for (let i = 0; i < maxRetries; i++) {
    try {
      await initialize(appId, config);
      return;
    } catch (error: any) {
      lastError = error;
      if (i < maxRetries - 1) {
        await new Promise((resolve) => setTimeout(resolve, 1000 * (i + 1)));
      }
    }
  }

  throw lastError || new Error('初始化失败');
}
```

### 3. 初始化状态检查

虽然 SDK 内部已经处理了重复初始化，但你可以在应用层面添加状态检查：

```typescript
let initStatus: 'idle' | 'initializing' | 'initialized' | 'failed' = 'idle';

export async function safeInitialize(
  appId: string,
  config?: SDKConfig
): Promise<void> {
  if (initStatus === 'initialized') {
    return Promise.resolve();
  }

  if (initStatus === 'initializing') {
    // 等待正在进行的初始化
    return new Promise((resolve, reject) => {
      const checkInterval = setInterval(() => {
        if (initStatus === 'initialized') {
          clearInterval(checkInterval);
          resolve();
        } else if (initStatus === 'failed') {
          clearInterval(checkInterval);
          reject(new Error('初始化失败'));
        }
      }, 100);
    });
  }

  initStatus = 'initializing';
  try {
    await initialize(appId, config);
    initStatus = 'initialized';
  } catch (error) {
    initStatus = 'failed';
    throw error;
  }
}
```

## 类型定义

完整的 TypeScript 类型定义：

```typescript
export interface SDKConfig {
  /** 应用名称，用于日志和调试 */
  appName?: string;
  
  /** 是否开启调试模式，上线前必须关闭 */
  debug?: boolean;
  
  /** 是否使用聚合功能，聚合模式必须设置为 true */
  useMediation?: boolean;
  
  /** 是否允许显示通知 */
  allowShowNotify?: boolean;
  
  /** 是否支持多进程 */
  supportMultiProcess?: boolean;
  
  /** 标题栏主题，1=DARK, 2=LIGHT */
  titleBarTheme?: number;
}
```

## 相关资源

- [穿山甲 SDK 官方文档](https://www.csjplatform.com/union/media/union/download/detail?id=195&docId=27616&locale=zh-CN&osType=android)
- [React Native 官方文档](https://reactnative.dev/)
- [项目 GitHub 仓库](https://github.com/shenzhiarchive/shenzhi-ad-aggregator)

## 常见问题

### Q: 初始化失败怎么办？

A: 检查以下几点：
1. appId 是否正确
2. 网络连接是否正常
3. 应用权限是否配置正确
4. 查看原生层日志获取详细错误信息

### Q: 可以多次调用 initialize 吗？

A: 可以，但只有第一次调用会真正执行初始化，后续调用会直接返回成功。

### Q: 初始化是同步还是异步的？

A: 初始化是异步的，返回 Promise，不会阻塞 JS 线程。

### Q: 必须在主线程调用吗？

A: 不需要，SDK 内部会自动切换到主线程执行初始化。

### Q: 如何判断初始化是否成功？

A: 使用 try-catch 捕获 Promise，成功时不会抛出异常，失败时会 reject Promise。


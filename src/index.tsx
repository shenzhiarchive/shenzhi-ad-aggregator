import AdAggregator, { type SDKConfig } from './NativeAdAggregator';

/**
 * 初始化广告SDK
 * @param appId 广告平台应用ID（必填）
 * @param config SDK配置参数（可选）
 * @returns Promise<void>
 */
export function initialize(appId: string, config?: SDKConfig): Promise<void> {
  return AdAggregator.initialize(appId, config);
}

export type { SDKConfig };

import PangleAdManager, { type InitConfig } from './NativePangleAdManager';

/**
 * 初始化穿山甲融合SDK
 *
 * @param config 配置参数
 * @returns Promise<boolean> 初始化成功返回true
 */
export function initMediationAdSdk(config: InitConfig): Promise<boolean> {
  return PangleAdManager.initMediationAdSdk(config);
}

/**
 * 检查SDK是否已初始化
 *
 * @returns Promise<boolean> SDK就绪返回true
 */
export function isSdkReady(): Promise<boolean> {
  return PangleAdManager.isSdkReady();
}

// 导出类型
export type { InitConfig };

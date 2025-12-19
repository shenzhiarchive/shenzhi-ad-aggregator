import type { TurboModule } from 'react-native';
import { NativeModules, TurboModuleRegistry } from 'react-native';

/**
 * 初始化配置参数接口
 */
export interface InitConfig {
  // 基础配置
  appId: string;
  debug?: boolean;
  supportMultiProcess?: boolean;

  // 隐私设置（默认true）
  allowLocation?: boolean;
  allowPhoneState?: boolean;
  allowWifiState?: boolean;
  allowAndroidId?: boolean;
  allowWriteExternal?: boolean;

  // 进阶设置
  themeStatus?: number; // 0:正常模式, 1:夜间模式
  limitPersonalAds?: boolean;
  limitProgrammaticAds?: boolean;
}

/**
 * PangleAdManager TurboModule接口规格
 */
export interface Spec extends TurboModule {
  /**
   * 初始化穿山甲融合SDK
   *
   * @param config 配置参数
   * @returns Promise<boolean> 初始化成功返回true
   */
  initMediationAdSdk(config: InitConfig): Promise<boolean>;

  /**
   * 检查SDK是否已初始化
   *
   * @returns Promise<boolean> SDK就绪返回true
   */
  isSdkReady(): Promise<boolean>;
}

/**
 * PangleAdManager TurboModule实例
 */
const TurboModule = TurboModuleRegistry.get<Spec>('PangleAdManager');
const LegacyModule = NativeModules?.PangleAdManager as Spec | undefined;

const PangleAdManager: Spec | undefined = TurboModule ?? LegacyModule;

if (!PangleAdManager) {
  // 这里不要静默失败，否则调用方会出现 `Cannot read property 'xxx' of undefined`
  throw new Error(
    "Native module 'PangleAdManager' not found. Did you rebuild the app after installing the library?"
  );
}

export default PangleAdManager;

import { TurboModuleRegistry, type TurboModule } from 'react-native';

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
 * PangleAdManager TurboModule接口
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
export default TurboModuleRegistry.getEnforcing<Spec>('PangleAdManager');

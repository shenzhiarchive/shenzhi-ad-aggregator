import { TurboModuleRegistry, type TurboModule } from 'react-native';

export interface SDKConfig {
  appName?: string;
  debug?: boolean;
  useMediation?: boolean;
  allowShowNotify?: boolean;
  supportMultiProcess?: boolean;
  titleBarTheme?: number;
}

export interface Spec extends TurboModule {
  initialize(appId: string, config?: SDKConfig): Promise<void>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('AdAggregator');

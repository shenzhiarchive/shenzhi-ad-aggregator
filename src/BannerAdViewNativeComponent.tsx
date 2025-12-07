import type { ViewProps } from 'react-native';
import type { HostComponent } from 'react-native';
import { codegenNativeComponent } from 'react-native';
import type { Int32 } from 'react-native/Libraries/Types/CodegenTypes';

export interface NativeProps extends ViewProps {
  codeId?: string;
  adSize?: {
    width: Int32;
    height: Int32;
  };
}

export default codegenNativeComponent<NativeProps>(
  'ShenzhiBannerAdView'
) as HostComponent<NativeProps>;

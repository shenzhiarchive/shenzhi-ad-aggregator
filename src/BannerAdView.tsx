import { useRef, useImperativeHandle, forwardRef } from 'react';
import {
  View,
  requireNativeComponent,
  type ViewStyle,
  type StyleProp,
  StyleSheet,
  UIManager,
  findNodeHandle,
} from 'react-native';
import BannerAdViewNativeComponent from './BannerAdViewNativeComponent';

/**
 * Banner广告尺寸
 */
export interface BannerSize {
  width: number; // 单位：dp
  height: number; // 单位：dp
}

/**
 * ECPM信息
 * 展示广告后获取的详细信息
 */
export interface EcpmInfo {
  /**
   * SDK名称
   */
  sdkName?: string;

  /**
   * 自定义SDK名称
   */
  customSdkName?: string;

  /**
   * 广告位ID
   */
  slotId?: string;

  /**
   * ECPM价格（单位：分）
   * 注意：一般情况下兜底代码位的ecpm是0
   */
  ecpm?: number;

  /**
   * 请求竞价类型
   */
  reqBiddingType?: number;

  /**
   * 错误信息
   */
  errorMsg?: string;

  /**
   * 请求ID
   */
  requestId?: string;

  /**
   * 广告位类型
   */
  ritType?: number;

  /**
   * AB测试ID
   */
  abTestId?: string;

  /**
   * 场景ID
   */
  scenarioId?: string;

  /**
   * 流量分组ID
   */
  segmentId?: string;

  /**
   * 渠道名称
   */
  channel?: string;

  /**
   * 子渠道名称
   */
  subChannel?: string;

  /**
   * 自定义数据
   */
  customData?: string;
}

// 原生组件接口
interface NativeBannerAdViewProps {
  codeId: string;
  adSize: BannerSize;
  onAdClicked?: () => void;
  onAdShow?: () => void;
  onRenderFail?: (event: {
    nativeEvent: { code: number; message: string };
  }) => void;
  onRenderSuccess?: (event: {
    nativeEvent: { width: number; height: number };
  }) => void;
  onDislike?: (event: {
    nativeEvent: { position: number; value: string };
  }) => void;
  onError?: (event: { nativeEvent: { code: number; message: string } }) => void;
  onEcpmInfo?: (event: { nativeEvent: EcpmInfo }) => void;
  style?: StyleProp<ViewStyle>;
}

// 原生组件（用于旧架构兼容）
const NativeBannerAdView = requireNativeComponent<NativeBannerAdViewProps>(
  'ShenzhiBannerAdView'
);

// 获取命令ID（用于Fabric和旧架构）
const getCommands = () => {
  try {
    const config = UIManager.getViewManagerConfig('ShenzhiBannerAdView');
    if (config?.Commands) {
      return {
        loadAd: config.Commands.loadAd ?? 1,
        destroy: config.Commands.destroy ?? 2,
        isAdLoaded: config.Commands.isAdLoaded ?? 3,
      };
    }
  } catch (e) {
    // 如果获取配置失败，使用默认值
  }
  // 默认命令ID（与原生端保持一致）
  return {
    loadAd: 1,
    destroy: 2,
    isAdLoaded: 3,
  };
};

/**
 * Banner广告事件
 */
export interface BannerAdEvent {
  /**
   * 广告点击事件
   */
  onAdClicked?: () => void;

  /**
   * 广告展示事件
   */
  onAdShow?: () => void;

  /**
   * 渲染失败事件
   */
  onRenderFail?: (error: { code: number; message: string }) => void;

  /**
   * 渲染成功事件
   */
  onRenderSuccess?: (data: { width: number; height: number }) => void;

  /**
   * 用户点击不喜欢/关闭广告事件
   */
  onDislike?: (data: { position: number; value: string }) => void;

  /**
   * 广告加载错误事件
   */
  onError?: (error: { code: number; message: string }) => void;

  /**
   * ECPM信息事件
   * 在广告展示后触发，包含广告的详细信息
   */
  onEcpmInfo?: (info: EcpmInfo) => void;
}

/**
 * BannerAdView属性
 */
export interface BannerAdViewProps extends BannerAdEvent {
  /**
   * 广告位ID（必填）
   */
  codeId: string;

  /**
   * 广告尺寸（必填）
   * 可以是对象 {width: number, height: number}，单位dp
   * 或者直接设置width和height属性
   */
  adSize?: BannerSize;

  /**
   * 广告宽度（单位：dp）
   * 如果设置了adSize，则忽略此属性
   */
  width?: number;

  /**
   * 广告高度（单位：dp）
   * 如果设置了adSize，则忽略此属性
   */
  height?: number;

  /**
   * 容器样式
   */
  style?: StyleProp<ViewStyle>;
}

/**
 * BannerAdView引用方法
 */
export interface BannerAdViewRef {
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

/**
 * Banner广告组件
 *
 * @example
 * ```tsx
 * <BannerAdView
 *   codeId="your-code-id"
 *   adSize={{ width: 320, height: 50 }}
 *   onAdClicked={() => console.log('Ad clicked')}
 *   onAdShow={() => console.log('Ad shown')}
 *   onEcpmInfo={(info) => console.log('ECPM Info:', info)}
 *   onError={(error) => console.log('Error:', error)}
 * />
 * ```
 */
const BannerAdView = forwardRef<BannerAdViewRef, BannerAdViewProps>(
  (props, ref) => {
    const {
      codeId,
      adSize,
      width,
      height,
      style,
      onAdClicked,
      onAdShow,
      onRenderFail,
      onRenderSuccess,
      onDislike,
      onError,
      onEcpmInfo,
    } = props;

    const viewRef = useRef<any>(null);

    // 暴露给父组件的方法
    useImperativeHandle(ref, () => ({
      loadAd: () => {
        if (viewRef.current) {
          const nodeHandle = findNodeHandle(viewRef.current);
          if (nodeHandle != null) {
            try {
              const commands = getCommands();
              // 使用命令
              UIManager.dispatchViewManagerCommand(
                nodeHandle,
                commands.loadAd,
                []
              );
            } catch (e) {
              // 回退到旧架构
              viewRef.current?.setNativeProps?.({ codeId });
            }
          } else {
            // 回退到旧架构
            viewRef.current?.setNativeProps?.({ codeId });
          }
        }
      },
      destroy: () => {
        if (viewRef.current) {
          const nodeHandle = findNodeHandle(viewRef.current);
          if (nodeHandle != null) {
            try {
              const commands = getCommands();
              // 使用命令
              UIManager.dispatchViewManagerCommand(
                nodeHandle,
                commands.destroy,
                []
              );
            } catch (e) {
              // 回退到旧架构
              viewRef.current?.setNativeProps?.({ codeId: '' });
            }
          } else {
            // 回退到旧架构
            viewRef.current?.setNativeProps?.({ codeId: '' });
          }
        }
      },
      isAdLoaded: () => {
        // 注意：isAdLoaded在旧架构中无法直接获取，需要通过其他方式实现
        // 这里暂时返回false，实际实现需要原生支持
        return false;
      },
    }));

    // 计算广告尺寸
    const finalAdSize =
      adSize || (width && height ? { width, height } : undefined);

    if (!codeId) {
      console.warn('BannerAdView: codeId is required');
      return null;
    }

    if (!finalAdSize) {
      console.warn('BannerAdView: adSize or width/height is required');
      return null;
    }

    // 事件处理函数
    const handleRenderFail = onRenderFail
      ? (event: { nativeEvent: { code: number; message: string } }) => {
          onRenderFail({
            code: event.nativeEvent.code,
            message: event.nativeEvent.message,
          });
        }
      : undefined;

    const handleRenderSuccess = onRenderSuccess
      ? (event: { nativeEvent: { width: number; height: number } }) => {
          onRenderSuccess({
            width: event.nativeEvent.width,
            height: event.nativeEvent.height,
          });
        }
      : undefined;

    const handleDislike = onDislike
      ? (event: { nativeEvent: { position: number; value: string } }) => {
          onDislike({
            position: event.nativeEvent.position,
            value: event.nativeEvent.value,
          });
        }
      : undefined;

    const handleError = onError
      ? (event: { nativeEvent: { code: number; message: string } }) => {
          onError({
            code: event.nativeEvent.code,
            message: event.nativeEvent.message,
          });
        }
      : undefined;

    const handleEcpmInfo = onEcpmInfo
      ? (event: { nativeEvent: EcpmInfo }) => {
          onEcpmInfo(event.nativeEvent);
        }
      : undefined;

    const dynamicContainerStyle: ViewStyle | undefined = finalAdSize
      ? {
          ...(finalAdSize.width ? { width: finalAdSize.width } : {}),
          ...(finalAdSize.height ? { minHeight: finalAdSize.height } : {}),
        }
      : undefined;

    const dynamicNativeStyle: ViewStyle | undefined = finalAdSize
      ? {
          ...(finalAdSize.width ? { width: finalAdSize.width } : {}),
          ...(finalAdSize.height ? { height: finalAdSize.height } : {}),
        }
      : undefined;

    // 优先使用Fabric组件，如果不可用则回退到旧架构
    const useFabric = BannerAdViewNativeComponent != null;
    const Component = useFabric
      ? BannerAdViewNativeComponent
      : NativeBannerAdView;

    return (
      <View style={[styles.container, dynamicContainerStyle, style]}>
        <Component
          ref={viewRef}
          codeId={codeId}
          adSize={finalAdSize}
          onAdClicked={onAdClicked}
          onAdShow={onAdShow}
          onRenderFail={handleRenderFail}
          onRenderSuccess={handleRenderSuccess}
          onDislike={handleDislike}
          onError={handleError}
          onEcpmInfo={handleEcpmInfo}
          style={[styles.nativeView, dynamicNativeStyle]}
        />
      </View>
    );
  }
);

BannerAdView.displayName = 'BannerAdView';

const styles = StyleSheet.create({
  container: {
    overflow: 'hidden',
  },
  nativeView: {
    width: '100%',
    minHeight: 1,
  },
});

export default BannerAdView;

import { useRef, useImperativeHandle, forwardRef, useCallback } from 'react';
import {
  View,
  requireNativeComponent,
  type ViewStyle,
  StyleSheet,
} from 'react-native';

/**
 * Banner广告尺寸
 */
export interface BannerSize {
  /** 广告宽度，单位：dp */
  width: number;
  /** 广告高度，单位：dp */
  height: number;
}

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
}

/**
 * BannerAdView属性
 */
export interface BannerAdViewProps extends BannerAdEvent {
  /**
   * 广告位ID（必填）
   * 聚合广告位ID，不是ADN的代码位
   */
  codeId: string;

  /**
   * 广告尺寸（必填）
   * 格式：{width: number, height: number}，单位dp
   * 建议设置的宽高比例与广告位配置的比例接近，避免非等比例拉伸
   */
  adSize: BannerSize;

  /**
   * 容器样式
   */
  style?: ViewStyle;
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
  style?: ViewStyle;
}

// 原生组件
const NativeBannerAdView =
  requireNativeComponent<NativeBannerAdViewProps>('BannerAdView');

/**
 * Banner广告组件
 *
 * @example
 * ```tsx
 * import BannerAdView from '@shenzhi/ad-aggregator';
 *
 * function App() {
 *   return (
 *     <BannerAdView
 *       codeId="your-code-id"
 *       adSize={{ width: 320, height: 50 }}
 *       onAdClicked={() => console.log('Ad clicked')}
 *       onAdShow={() => console.log('Ad shown')}
 *       onError={(error) => console.log('Error:', error)}
 *     />
 *   );
 * }
 * ```
 */
const BannerAdView = forwardRef<BannerAdViewRef, BannerAdViewProps>(
  (props, ref) => {
    const {
      codeId,
      adSize,
      style,
      onAdClicked,
      onAdShow,
      onRenderFail,
      onRenderSuccess,
      onDislike,
      onError,
    } = props;

    // 参数验证 - 必须在所有 hooks 调用之前进行
    const isValid = codeId && adSize && adSize.width && adSize.height;

    const viewRef = useRef<any>(null);

    // 事件处理函数 - 必须在所有早期返回之前调用
    const handleRenderFail = useCallback(
      (event: { nativeEvent: { code: number; message: string } }) => {
        onRenderFail?.({
          code: event.nativeEvent.code,
          message: event.nativeEvent.message,
        });
      },
      [onRenderFail]
    );

    const handleRenderSuccess = useCallback(
      (event: { nativeEvent: { width: number; height: number } }) => {
        onRenderSuccess?.({
          width: event.nativeEvent.width,
          height: event.nativeEvent.height,
        });
      },
      [onRenderSuccess]
    );

    const handleDislike = useCallback(
      (event: { nativeEvent: { position: number; value: string } }) => {
        onDislike?.({
          position: event.nativeEvent.position,
          value: event.nativeEvent.value,
        });
      },
      [onDislike]
    );

    const handleError = useCallback(
      (event: { nativeEvent: { code: number; message: string } }) => {
        onError?.({
          code: event.nativeEvent.code,
          message: event.nativeEvent.message,
        });
      },
      [onError]
    );

    // 暴露给父组件的方法
    useImperativeHandle(
      ref,
      () => ({
        loadAd: () => {
          // 通过更新codeId触发重新加载
          if (viewRef.current && isValid) {
            viewRef.current.setNativeProps({ codeId });
          }
        },
        destroy: () => {
          // 通过设置codeId为空字符串触发销毁
          if (viewRef.current) {
            viewRef.current.setNativeProps({ codeId: '' });
          }
        },
        isAdLoaded: () => {
          // 检查广告是否已加载
          // 注意：需要原生端实现对应的方法才能返回真实状态
          // 当前返回false，后续可以通过原生方法或事件状态来实现
          return false;
        },
      }),
      [codeId, isValid]
    );

    // 参数验证失败时返回空视图
    if (!isValid) {
      if (!codeId) {
        console.warn('BannerAdView: codeId is required');
      }
      if (!adSize || !adSize.width || !adSize.height) {
        console.warn('BannerAdView: adSize with width and height is required');
      }
      return null;
    }

    return (
      <View style={[styles.container, style]}>
        <NativeBannerAdView
          ref={viewRef}
          codeId={codeId}
          adSize={adSize}
          onAdClicked={onAdClicked}
          onAdShow={onAdShow}
          onRenderFail={handleRenderFail}
          onRenderSuccess={handleRenderSuccess}
          onDislike={handleDislike}
          onError={handleError}
          style={styles.nativeView}
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
    height: '100%',
  },
});

export default BannerAdView;

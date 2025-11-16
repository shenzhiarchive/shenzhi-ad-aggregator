import { useRef, useImperativeHandle, forwardRef } from 'react';
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
  width: number; // 单位：dp
  height: number; // 单位：dp
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
    } = props;

    const viewRef = useRef<any>(null);

    // 暴露给父组件的方法
    useImperativeHandle(ref, () => ({
      loadAd: () => {
        // 触发重新加载广告（通过更新props）
        if (viewRef.current) {
          viewRef.current.setNativeProps({ codeId });
        }
      },
      destroy: () => {
        // 销毁广告（通过设置codeId为空）
        if (viewRef.current) {
          viewRef.current.setNativeProps({ codeId: '' });
        }
      },
      isAdLoaded: () => {
        // 检查广告是否已加载（需要原生支持）
        return false; // 暂时返回false，后续可以通过原生方法实现
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

    return (
      <View style={[styles.container, style]}>
        <NativeBannerAdView
          ref={viewRef}
          codeId={codeId}
          adSize={finalAdSize}
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

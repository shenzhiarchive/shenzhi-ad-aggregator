import { Text, View, StyleSheet } from 'react-native';
import { useEffect, useState } from 'react';
import {
  type InitConfig,
  initMediationAdSdk,
  isSdkReady,
  BannerAdView,
} from '@shenzhi/ad-aggregator';

export default function App() {
  const [isReady, setIsReady] = useState<boolean>(false);

  useEffect(() => {
    // 等待 React Native 完全初始化后再调用原生模块
    isSdkReady()
      .then((res) => {
        if (res) {
          setIsReady(res);
        } else {
          const params: InitConfig = {
            appId: '5666682',
          };
          initMediationAdSdk(params)
            .then((result) => {
              setIsReady(result);
            })
            .catch((err) => {
              console.log('initMediationAdSdk', err);
            });
        }
      })
      .catch((e) => {
        console.error('isSdkReady', e);
      });
  }, []);
  return (
    <View style={styles.container}>
      <Text>Result: {isReady ? '初始化' : '没有初始化'}</Text>
      <View style={styles.banner}>
        {isReady && (
          <BannerAdView
            codeId="103503633"
            adSize={{ width: 320, height: 50 }}
            onAdClicked={() => console.log('Ad clicked')}
            onAdShow={() => console.log('Ad shown')}
            onError={(error) => console.log('Error:', error)}
            onEcpmInfo={(info) => console.log('ecpm:', info)}
          />
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  banner: {
    backgroundColor: '#7c3232',
  },
});

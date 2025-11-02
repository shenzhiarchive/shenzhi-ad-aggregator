import { Text, View, StyleSheet } from 'react-native';
import { useEffect, useState } from 'react';
import {
  type InitConfig,
  initMediationAdSdk,
  isSdkReady,
} from '@shenzhi/ad-aggregator';

export default function App() {
  const [isReady, setIsReady] = useState<boolean>(false);

  useEffect(() => {
    // 等待 React Native 完全初始化后再调用原生模块
    isSdkReady()
      .then((res) => {
        console.log(res);
        if (res) {
          setIsReady(res);
        } else {
          const params: InitConfig = {
            appId: '5666682',
          };
          initMediationAdSdk(params)
            .then((result) => {
              console.log(result);
              setIsReady(result);
            })
            .catch((err) => {
              console.log(err);
            });
        }
      })
      .catch((e) => {
        console.error(e);
      });
  }, []);
  return (
    <View style={styles.container}>
      <Text>Result: {isReady ? '初始化' : '没有初始化'}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});

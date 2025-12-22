import { useEffect } from 'react';

import { Text, View, StyleSheet } from 'react-native';
import { initialize, type SDKConfig } from '@shenzhi/ad-aggregator';

const APP_ID = '5666682';

const defaultConfig: SDKConfig = {
  appName: '开心乐',
  debug: __DEV__,
  useMediation: true,
};

export default function App() {
  useEffect(() => {
    const initSDK = async () => {
      try {
        await initialize(APP_ID, defaultConfig);
        console.log('广告 SDK 初始化成功');
      } catch (error) {
        console.error('广告 SDK 初始化失败:', error);
      }
    };

    initSDK();
  }, []);

  return (
    <View style={styles.container}>
      <Text>Ad Aggregator Example</Text>
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

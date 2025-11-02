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
    isSdkReady().then((res) => {
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
            console.log(err);
          });
      }
    });
  }, []);
  return (
    <View style={styles.container}>
      <Text>Result: {isReady}</Text>
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

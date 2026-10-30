/**
 * Fruki — Motor de recomendação (React Native)
 */
import React from 'react';
import { StatusBar, View } from 'react-native';
import { SafeAreaProvider, useSafeAreaInsets } from 'react-native-safe-area-context';

import MotorScreen from './src/screens/MotorScreen';
import { cores } from './src/theme';

function Conteudo() {
  const insets = useSafeAreaInsets();
  return (
    <View
      style={{
        flex: 1,
        backgroundColor: cores.fundo,
        paddingTop: insets.top,
        paddingBottom: insets.bottom,
        paddingLeft: insets.left,
        paddingRight: insets.right,
      }}>
      <MotorScreen />
    </View>
  );
}

export default function App() {
  return (
    <SafeAreaProvider>
      <StatusBar barStyle="dark-content" />
      <Conteudo />
    </SafeAreaProvider>
  );
}

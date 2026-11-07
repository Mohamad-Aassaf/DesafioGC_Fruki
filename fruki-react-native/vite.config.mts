// Prévia web do app (npm run web): roda o mesmo código React Native no navegador
// via react-native-web, sem precisar de Android SDK/NDK/emulador.
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'node:path';

const raiz = import.meta.dirname;

// Arquivos .web.* têm prioridade, para pegar as versões web das bibliotecas (ex.: react-native-svg).
const extensions = ['.web.tsx', '.web.ts', '.web.jsx', '.web.js', '.tsx', '.ts', '.jsx', '.js', '.json'];
const alias = {
  'react-native': path.resolve(raiz, 'web/react-native-shim.js'),
  '@react-native/assets-registry/registry': path.resolve(raiz, 'web/assets-registry-shim.js'),
};

export default defineConfig({
  root: path.resolve(raiz, 'web'),
  plugins: [react()],
  define: {
    __DEV__: JSON.stringify(true),
    global: 'globalThis',
    'process.env.NODE_ENV': JSON.stringify('development'),
  },
  resolve: { alias, extensions },
  optimizeDeps: {
    rolldownOptions: {
      resolve: { alias, extensions },
      moduleTypes: { '.js': 'jsx' },
    },
  },
  server: { port: 5173, open: true, fs: { allow: [raiz] } },
});

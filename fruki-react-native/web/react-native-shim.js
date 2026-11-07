// Na web, 'react-native' aponta para cá: tudo vem do react-native-web,
// mais stubs de APIs só-nativas que bibliotecas referenciam mas não usam na web.
export * from 'react-native-web';

export const TurboModuleRegistry = {
  get: () => null,
  getEnforcing: () => null,
};

/// <reference lib="dom" />
import { AppRegistry } from 'react-native';
import App from '../App';
import { name as appName } from '../app.json';

AppRegistry.registerComponent(appName, () => App);
AppRegistry.runApplication(appName, {
  initialProps: {},
  // @ts-expect-error na web, o react-native-web aceita o elemento HTML como rootTag
  rootTag: document.getElementById('root'),
});

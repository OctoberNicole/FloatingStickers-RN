import {useEffect, useState} from 'react';
import {Button, NativeModules, Text, View} from 'react-native';

const App = () => {
  const [data, setData] = useState<any>('');

  // Call the native module named Loader
  const LaunchARSession = () => {
    NativeModules.Loader.launchStickerActivity();
  };

  // get data from native module
  const getData = () => {
    NativeModules.Loader.fetchData((data: any) => {
      console.log(data);
      setData(data);
    });
  };

  useEffect(() => {
    getData();
  }, []);

  return (
    <View
      style={{
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
      }}>
      <Button title="Launch AR Session" onPress={LaunchARSession} />
      <Button title="Get Data" onPress={getData} />

      <Text>{data}</Text>
    </View>
  );
};

export default App;

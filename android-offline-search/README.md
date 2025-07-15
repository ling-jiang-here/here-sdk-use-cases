The Search example app shows how to search for places including autosuggestions, for the address that belongs to certain geographic coordinates (_reverse geocoding_) and for the geographic coordinates that belong to an address (_geocoding_). You can find how this is done in [SearchExample.java](app/src/main/java/com/here/search/SearchExample.java).

Build instructions:
-------------------

1) Copy the AAR file of the HERE SDK for Android to your app's `app/libs` folder.

Note: If your AAR version is different than the version shown in the _Developer Guide_, you may need to adapt the source code of the example app.

2) Open Android Studio and sync the project.

Please do not forget: To run the app, you need to add your HERE SDK credentials to the `MainActivity.java` file. More information can be found in the _Get Started_ section of the _Developer Guide_.

Play instructions:
-------------------

1. Modify the following boolean value to tru in SearchExample.java:
```
// Change the following value to ture for online search
private final boolean isDeviceConnected = false;
```

2. Run the app and perform all search, long press on map, and geocode searches in online mode.
This step downloads the map data that will be used for offline search in next steps.

3. Change the device or emulator network status to airplane mode or turn its network feature off.

4. Modify the above boolean value in the first step to false to switch to offline search mode.
Run the app and perform all searches again, all search features should be fine.

5. Keep the device or emulator network status to be in airplane mode or network feature to be off.

6. Modify the above boolean value in the first step to true to switch back to online search mode.
Run the app and perform all searches again, all search features should be disabled.

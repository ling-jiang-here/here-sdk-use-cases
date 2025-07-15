The Search example app shows how to search for places including autosuggestions, for the address that belongs to certain geographic coordinates (_reverse geocoding_) and for the geographic coordinates that belong to an address (_geocoding_). You can find how this is done in [SearchExample.java](app/src/main/java/com/here/search/SearchExample.java).

Build instructions:
-------------------

1) Copy the AAR file of the HERE SDK for Android to your app's `app/libs` folder.

Note: If your AAR version is different than the version shown in the _Developer Guide_, you may need to adapt the source code of the example app.

2) Open Android Studio and sync the project.

Please do not forget: To run the app, you need to add your HERE SDK credentials to the `MainActivity.java` file. More information can be found in the _Get Started_ section of the _Developer Guide_.

Play instructions:
-------------------

SDK 4.23.0 introduced a new field "accessRestrictionReasons" in https://www.here.com/docs/bundle/sdk-for-android-navigate-api-reference/page/com/here/sdk/search/EVChargingPool.html

This demo app demonstrates how to get this field values for charge stations.

Note that:

1) For offline EV rich attributes, also enable LayerConfiguration.Feature.EV in SDKOptions.layerConfiguration.

2) Use offline search engine such as offlineSearchEngine.searchByCategory() because it is populated only for offline search.

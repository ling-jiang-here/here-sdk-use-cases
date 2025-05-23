/*
 * Copyright (C) 2019-2025 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package com.here.hellomap;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.here.hellomap.PermissionsRequestor.ResultListener;
import com.here.sdk.core.Color;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolygon;
import com.here.sdk.core.Point2D;
import com.here.sdk.core.Rectangle2D;
import com.here.sdk.core.Size2D;
import com.here.sdk.core.engine.AuthenticationMode;
import com.here.sdk.core.engine.SDKNativeEngine;
import com.here.sdk.core.engine.SDKOptions;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.gestures.TapListener;
import com.here.sdk.mapview.MapError;
import com.here.sdk.mapview.MapMeasure;
import com.here.sdk.mapview.MapPolygon;
import com.here.sdk.mapview.MapScene;
import com.here.sdk.mapview.MapScheme;
import com.here.sdk.mapview.MapView;
import com.here.sdk.mapview.PickMapContentResult;
import com.here.sdk.mapview.PickMapItemsResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private PermissionsRequestor permissionsRequestor;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Usually, you need to initialize the HERE SDK only once during the lifetime of an application.
        // Before creating a MapView instance please make sure that the HERE SDK is initialized.
        initializeHERESDK();

        setContentView(R.layout.activity_main);

        // Get a MapView instance from layout.
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        mapView.setOnReadyListener(new MapView.OnReadyListener() {
            @Override
            public void onMapViewReady() {
                // This will be called each time after this activity is resumed.
                // It will not be called before the first map scene was loaded.
                // Any code that requires map data may not work as expected until this event is received.
                Log.d(TAG, "HERE Rendering Engine attached.");
            }
        });

        // Note that for this app handling of permissions is optional as no sensitive permissions
        // are required.
        handleAndroidPermissions();

        MapScene mapScene = mapView.getMapScene();
        showMapPolygon(mapScene);
        setTapGestureHandler();
    }

    private void initializeHERESDK() {
        // Set your credentials for the HERE SDK.
        String accessKeyID = "";
        String accessKeySecret = "";
        AuthenticationMode authenticationMode = AuthenticationMode.withKeySecret(accessKeyID, accessKeySecret);
        SDKOptions options = new SDKOptions(authenticationMode);

        try {
            Context context = this;
            SDKNativeEngine.makeSharedInstance(context, options);
        } catch (InstantiationErrorException e) {
            throw new RuntimeException("Initialization of HERE SDK failed: " + e.error.name());
        }
    }

    private void handleAndroidPermissions() {
        permissionsRequestor = new PermissionsRequestor(this);
        permissionsRequestor.request(new ResultListener(){

            @Override
            public void permissionsGranted() {
                loadMapScene();
            }

            @Override
            public void permissionsDenied() {
                Log.e(TAG, "Permissions denied by user.");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsRequestor.onRequestPermissionsResult(requestCode, grantResults);
    }

    private void loadMapScene() {
        // The camera can be configured before or after a scene is loaded.
        double distanceInMeters = 1000 * 10;
        MapMeasure mapMeasureZoom = new MapMeasure(MapMeasure.Kind.DISTANCE_IN_METERS, distanceInMeters);
        mapView.getCamera().lookAt(new GeoCoordinates(52.52, 13.38), mapMeasureZoom);

        // Load a scene from the HERE SDK to render the map with a map scheme.
        mapView.getMapScene().loadScene(MapScheme.NORMAL_DAY, new MapScene.LoadSceneCallback() {
            @Override
            public void onLoadScene(@Nullable MapError mapError) {
                if (mapError != null) {
                    Log.d(TAG, "Loading map failed: mapError: " + mapError.name());
                }
            }
        });
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        disposeHERESDK();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        mapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    private void disposeHERESDK() {
        // Free HERE SDK resources before the application shuts down.
        // Usually, this should be called only on application termination.
        // Afterwards, the HERE SDK is no longer usable unless it is initialized again.
        SDKNativeEngine sdkNativeEngine = SDKNativeEngine.getSharedInstance();
        if (sdkNativeEngine != null) {
            sdkNativeEngine.dispose();
            // For safety reasons, we explicitly set the shared instance to null to avoid situations,
            // where a disposed instance is accidentally reused.
            SDKNativeEngine.setSharedInstance(null);
        }
    }

    public void showMapPolygon(MapScene mapScene) {
        MapPolygon mapPolygon = createPolygon();
        mapScene.addMapPolygon(mapPolygon);
    }

    private MapPolygon createPolygon() {
        List<GeoCoordinates> coordinates = new ArrayList<>();
        // Note that a polygon requires a clockwise or counter-clockwise order of the coordinates.
        coordinates.add(new GeoCoordinates(52.54014, 13.37958));
        coordinates.add(new GeoCoordinates(52.53894, 13.39194));
        coordinates.add(new GeoCoordinates(52.5309, 13.3946));
        coordinates.add(new GeoCoordinates(52.53032, 13.37409));

        GeoPolygon geoPolygon;
        try {
            geoPolygon = new GeoPolygon(coordinates);
        } catch (InstantiationErrorException e) {
            // Less than three vertices.
            return null;
        }

        Color fillColor = Color.valueOf(0f, 0.56f, 0.54f, 0.63f); // RGBA
        MapPolygon mapPolygon = new MapPolygon(geoPolygon, fillColor);

        return mapPolygon;
    }


    private void setTapGestureHandler() {
        mapView.getGestures().setTapListener(new TapListener() {
            @Override
            public void onTap(@NonNull Point2D touchPoint) {
                pickMapContent(touchPoint);
            }
        });
    }

    private void pickMapContent(final Point2D touchPoint) {
        // Reset the searching area to be centered on the touch point
        double range = 500.0;
        touchPoint.x = touchPoint.x - range/2;
        touchPoint.y = touchPoint.y - range/2;
        // You can also use a larger area to include multiple map icons.
        Rectangle2D rectangle2D = new Rectangle2D(touchPoint, new Size2D(range, range));
        // Creates a list of map content type from which the results will be picked.
        // The content type values can be MAP_CONTENT, MAP_ITEMS and CUSTOM_LAYER_DATA.
        ArrayList<MapScene.MapPickFilter.ContentType> contentTypesToPickFrom = new ArrayList<>();

        // MAP_CONTENT is used when picking embedded Carto POIs, traffic incidents, vehicle restriction etc.
        // MAP_ITEMS is used when picking map items such as MapMarker, MapPolyline, MapPolygon etc.
        // Currently we need Carto POIs so adding the MAP_CONTENT filter.
        contentTypesToPickFrom.add(MapScene.MapPickFilter.ContentType.MAP_ITEMS);
        MapScene.MapPickFilter filter = new MapScene.MapPickFilter(contentTypesToPickFrom);

        // If you do not want to specify any filter you can pass filter as NULL and all of the pickable contents will be picked.
        mapView.pick(filter, rectangle2D, mapPickResult -> {
            if (mapPickResult == null) {
                Log.e("onPickMapContent", "An error occurred while performing the pick operation.");
                return;
            }
            PickMapItemsResult pickedItems = mapPickResult.getMapItems();
            assert pickedItems != null;
            handlePickedCartoPOIs(pickedItems.getPolygons());
        });
    }

    private void handlePickedCartoPOIs(List<MapPolygon> polygonList) {
        int listSize = polygonList.size();
        if (listSize == 0) {
            return;
        }

        MapPolygon topmostPolygon = polygonList.get(0);
        Log.d("Polygon picked:", topmostPolygon.toString());
    }
}

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

package com.here.routing;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.here.sdk.animation.Easing;
import com.here.sdk.animation.EasingFunction;
import com.here.sdk.core.Color;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoOrientationUpdate;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.Location;
import com.here.sdk.core.Point2D;
import com.here.sdk.core.Rectangle2D;
import com.here.sdk.core.Size2D;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.LineCap;
import com.here.sdk.mapview.MapCamera;
import com.here.sdk.mapview.MapCameraAnimation;
import com.here.sdk.mapview.MapCameraAnimationFactory;
import com.here.sdk.mapview.MapCameraUpdate;
import com.here.sdk.mapview.MapCameraUpdateFactory;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapMeasure;
import com.here.sdk.mapview.MapMeasureDependentRenderSize;
import com.here.sdk.mapview.MapPolyline;
import com.here.sdk.mapview.MapView;
import com.here.sdk.mapview.RenderSize;
import com.here.sdk.routing.CarOptions;
import com.here.sdk.routing.DynamicSpeedInfo;
import com.here.sdk.routing.Maneuver;
import com.here.sdk.routing.ManeuverAction;
import com.here.sdk.routing.PaymentMethod;
import com.here.sdk.routing.Route;
import com.here.sdk.routing.RouteRailwayCrossing;
import com.here.sdk.routing.RoutingEngine;
import com.here.sdk.routing.Section;
import com.here.sdk.routing.SectionNotice;
import com.here.sdk.routing.Span;
import com.here.sdk.routing.Toll;
import com.here.sdk.routing.TollFare;
import com.here.sdk.routing.Waypoint;
import com.here.time.Duration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoutingExample {

    private static final String TAG = RoutingExample.class.getName();

    private final Context context;
    private final MapView mapView;
    private final List<MapMarker> mapMarkerList = new ArrayList<>();
    private final List<MapPolyline> mapPolylines = new ArrayList<>();
    private final RoutingEngine routingEngine;
    private GeoCoordinates startGeoCoordinates;
    private GeoCoordinates destinationGeoCoordinates;
    private final TimeUtils timeUtils;
    List<Waypoint> waypoints = new ArrayList<>();
    List<Location> locations = Arrays.asList (
            new Location(new GeoCoordinates(29.65867663733661, 75.22938659414649)),
            new Location(new GeoCoordinates(29.671260000000004, 75.23416000000002)),
            new Location(new GeoCoordinates(29.67204, 75.23812000000002)),
            new Location(new GeoCoordinates(29.680099999999996, 75.23846000000003)),
            new Location(new GeoCoordinates(29.683749999999996, 75.23866000000005)),
            new Location(new GeoCoordinates(29.688419999999994, 75.23890000000009)),
            new Location(new GeoCoordinates(29.68706728087065, 75.23988076580954)),
            new Location(new GeoCoordinates(29.6857, 75.24337000000003)),
            new Location(new GeoCoordinates(29.672126691404188, 75.25604527290119)),
            new Location(new GeoCoordinates(29.66809, 75.25965)),
            new Location(new GeoCoordinates(29.66235, 75.26588)),
            new Location(new GeoCoordinates(29.65422, 75.27313999999998)),
            new Location(new GeoCoordinates(29.653709999999997, 75.27638999999999)),
            new Location(new GeoCoordinates(29.65476, 75.28183999999999)),
            new Location(new GeoCoordinates(29.65916, 75.29208000000001)),
            new Location(new GeoCoordinates(29.66451, 75.30461000000003)),
            new Location(new GeoCoordinates(29.665260000000004, 75.31141000000004)),
            new Location(new GeoCoordinates(29.661640000000002, 75.31742000000006)),
            new Location(new GeoCoordinates(29.658690000000007, 75.31909000000006)),
            new Location(new GeoCoordinates(29.653170000000006, 75.32084000000009)),
            new Location(new GeoCoordinates(29.65300000000001, 75.33028000000009)),
            new Location(new GeoCoordinates(29.65298000000001, 75.34115000000008)),
            new Location(new GeoCoordinates(29.65301000000001, 75.35640000000011)),
            new Location(new GeoCoordinates(29.653050000000007, 75.3689600000001)),
            new Location(new GeoCoordinates(29.64789000000001, 75.3734200000001)),
            new Location(new GeoCoordinates(29.645120000000016, 75.37438000000009)),
            new Location(new GeoCoordinates(29.642010000000017, 75.3752600000001)),
            new Location(new GeoCoordinates(29.642340000000015, 75.37865000000008)),
            new Location(new GeoCoordinates(29.642250000000015, 75.38230000000009)),
            new Location(new GeoCoordinates(29.639510000000016, 75.38747000000008)),
            new Location(new GeoCoordinates(29.636840000000014, 75.39247000000007)),
            new Location(new GeoCoordinates(29.632940000000012, 75.40259000000009)),
            new Location(new GeoCoordinates(29.62516154033156, 75.42635611423472)),
            new Location(new GeoCoordinates(29.62416, 75.42534000000002)),
            new Location(new GeoCoordinates(29.620729999999995, 75.42809000000003)),
            new Location(new GeoCoordinates(29.61485999999999, 75.42735)),
            new Location(new GeoCoordinates(29.602279999999993, 75.43108000000001)),
            new Location(new GeoCoordinates(29.59492999999999, 75.43131000000002)),
            new Location(new GeoCoordinates(29.59311999999999, 75.43162000000001)),
            new Location(new GeoCoordinates(29.589719999999982, 75.43226000000003)),
            new Location(new GeoCoordinates(29.579959999999982, 75.43241000000003)),
            new Location(new GeoCoordinates(29.573199999999993, 75.43248000000003)),
            new Location(new GeoCoordinates(29.55867999999999, 75.43235000000001)),
            new Location(new GeoCoordinates(29.547099999999986, 75.43251)),
            new Location(new GeoCoordinates(29.543949999999985, 75.43858)),
            new Location(new GeoCoordinates(29.541989999999984, 75.44004999999997)),
            new Location(new GeoCoordinates(29.53761999999998, 75.43958999999995)),
            new Location(new GeoCoordinates(29.533109999999983, 75.44113999999995)),
            new Location(new GeoCoordinates(29.529709999999984, 75.44286999999997)),
            new Location(new GeoCoordinates(29.526459999999982, 75.44491999999995)),
            new Location(new GeoCoordinates(29.523929999999986, 75.44761999999996)),
            new Location(new GeoCoordinates(29.522969999999987, 75.44696999999996)),
            new Location(new GeoCoordinates(29.51956999999998, 75.44417999999997)),
            new Location(new GeoCoordinates(29.515529999999977, 75.44349999999999)),
            new Location(new GeoCoordinates(29.514529999999976, 75.44826999999997)),
            new Location(new GeoCoordinates(29.5131814, 75.4509532)));

    public RoutingExample(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
        MapCamera camera = mapView.getCamera();
        double distanceInMeters = 1000 * 10;
        MapMeasure mapMeasureZoom = new MapMeasure(MapMeasure.Kind.DISTANCE_IN_METERS, distanceInMeters);
        camera.lookAt(new GeoCoordinates(29.543949999999985, 75.43858), mapMeasureZoom);
        timeUtils = new TimeUtils();
        try {
            routingEngine = new RoutingEngine();
        } catch (InstantiationErrorException e) {
            throw new RuntimeException("Initialization of RoutingEngine failed: " + e.error.name());
        }
    }

    public void addRoute() {
        startGeoCoordinates = new GeoCoordinates(29.65867663733661, 75.22938659414649);
        destinationGeoCoordinates = new GeoCoordinates(29.5131814, 75.4509532);
        calculateRoute(locations);
    }

    private void calculateRoute(List<Location> locations) {
        routingEngine.importRoute(
                locations,
                getCarOptions(),
                (routingError, routes) -> {
                    if (routingError == null) {
                        Route route = routes.get(0);
                        showRouteDetails(route);
                        showRouteOnMap(route);
                        logRouteRailwayCrossingDetails(route);
                        logRouteSectionDetails(route);
                        logRouteViolations(route);
                        logTollDetails(route);
                        animateToRoute(route);
                        showWaypointsOnMap(locations);
                    } else {
                        showDialog("Error while calculating a route:", routingError.toString());
                    }
                });
    }

    private void animateToRoute(Route route) {
        // We want to show the route fitting in the map view with an additional padding of 50 pixels
        Point2D origin = new Point2D(50, 50);
        Size2D sizeInPixels = new Size2D(mapView.getWidth() - 100, mapView.getHeight() - 100);
        Rectangle2D mapViewport = new Rectangle2D(origin, sizeInPixels);

        // Animate to the route within a duration of 3 seconds.
        MapCameraUpdate update = MapCameraUpdateFactory.lookAt(
                route.getBoundingBox(),
                new GeoOrientationUpdate(null, null),
                mapViewport);
        MapCameraAnimation animation =
                MapCameraAnimationFactory.createAnimation(update, Duration.ofMillis(3000), new Easing(EasingFunction.IN_CUBIC));
        mapView.getCamera().startAnimation(animation);
    }

    // A route may contain several warnings, for example, when a certain route option could not be fulfilled.
    // An implementation may decide to reject a route if one or more violations are detected.
    private void logRouteViolations(Route route) {
        for (Section section : route.getSections()) {
            for (Span span : section.getSpans()) {
                List<GeoCoordinates> spanGeometryVertices = span.getGeometry().vertices;
                // This route violation spreads across the whole span geometry.
                GeoCoordinates violationStartPoint = spanGeometryVertices.get(0);
                GeoCoordinates violationEndPoint = spanGeometryVertices.get(spanGeometryVertices.size() - 1);
                for (int index : span.getNoticeIndexes()) {
                    SectionNotice spanSectionNotice = section.getSectionNotices().get(index);
                    // The violation code such as "VIOLATED_VEHICLE_RESTRICTION".
                    String violationCode = spanSectionNotice.code.toString();
                    Log.d(TAG, "The violation " + violationCode + " starts at " + toString(violationStartPoint) + " and ends at " + toString(violationEndPoint) + " .");
                }
            }
        }
    }

    private String toString(GeoCoordinates geoCoordinates) {
        return geoCoordinates.latitude + ", " + geoCoordinates.longitude;
    }

    private void logRouteSectionDetails(Route route) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");

        for (int i = 0; i < route.getSections().size(); i++) {
            Section section = route.getSections().get(i);

            Log.d(TAG, "Route Section : " + (i + 1));
            Log.d(TAG, "Route Section Departure Time : "
                    + dateFormat.format(section.getDepartureLocationTime().localTime));
            Log.d(TAG, "Route Section Arrival Time : "
                    + dateFormat.format(section.getArrivalLocationTime().localTime));
            Log.d(TAG, "Route Section length : " + section.getLengthInMeters() + " m");
            Log.d(TAG, "Route Section duration : " + section.getDuration().getSeconds() + " s");
        }
    }

    private void logRouteRailwayCrossingDetails(Route route) {
        for (RouteRailwayCrossing routeRailwayCrossing : route.getRailwayCrossings()) {
            // Coordinates of the route offset
            GeoCoordinates routeOffsetCoordinates = routeRailwayCrossing.coordinates;
            // Index of the corresponding route section. The start of the section indicates the start of the offset.
            int routeOffsetSectionIndex = routeRailwayCrossing.routeOffset.sectionIndex;
            // Offset from the start of the specified section to the specified location along the route.
            double routeOffsetInMeters = routeRailwayCrossing.routeOffset.offsetInMeters;

            Log.d(TAG, "A railway crossing of type " + routeRailwayCrossing.type.name() +
                    "is situated " +
                    routeOffsetInMeters + " m away from start of section: " +
                    routeOffsetSectionIndex);
        }
    }

    private void logTollDetails(Route route) {
        for (Section section : route.getSections()) {
            // The spans that make up the polyline along which tolls are required or
            // where toll booths are located.
            List<Toll> tolls = section.getTolls();
            if (!tolls.isEmpty()) {
                Log.d(TAG, "Attention: This route may require tolls to be paid.");
            }
            for (Toll toll : tolls) {
                Log.d(TAG, "Toll information valid for this list of spans:");
                Log.d(TAG, "Toll systems: " + toll.tollSystems);
                Log.d(TAG, "Toll country code (ISO-3166-1 alpha-3): " + toll.countryCode);
                Log.d(TAG, "Toll fare information: ");
                for (TollFare tollFare : toll.fares) {
                    // A list of possible toll fares which may depend on time of day, payment method and
                    // vehicle characteristics. For further details please consult the local
                    // authorities.
                    Log.d(TAG, "Toll price: " + tollFare.price + " " + tollFare.currency);
                    for (PaymentMethod paymentMethod : tollFare.paymentMethods) {
                        Log.d(TAG, "Accepted payment methods for this price: " + paymentMethod.name());
                    }
                }
            }
        }
    }

    private void showRouteDetails(Route route) {
        // estimatedTravelTimeInSeconds includes traffic delay.
        long estimatedTravelTimeInSeconds = route.getDuration().getSeconds();
        long estimatedTrafficDelayInSeconds = route.getTrafficDelay().getSeconds();
        int lengthInMeters = route.getLengthInMeters();

        // Timezones can vary depending on the device's geographic location.
        // For instance, when calculating a route, the device's current timezone may differ from that of the destination.
        // Consider a scenario where a user calculates a route from Berlin to London — each city operates in a different timezone.
        // To address this, you can display the Estimated Time of Arrival (ETA) in multiple timezones: the device's current timezone (Berlin), the destination's timezone (London), and UTC (Coordinated Universal Time), which serves as a global reference.
        String routeDetails =
                "Travel Duration: " + timeUtils.formatTime(estimatedTravelTimeInSeconds) +
                        "\nTraffic delay: " + timeUtils.formatTime(estimatedTrafficDelayInSeconds)
                        + "\nRoute length (m): " + timeUtils.formatLength(lengthInMeters) +
                        "\nETA in device timezone: " + timeUtils.getETAinDeviceTimeZone(route) +
                        "\nETA in destination timezone: " + timeUtils.getETAinDestinationTimeZone(route) +
                        "\nETA in UTC: " + timeUtils.getEstimatedTimeOfArrivalInUTC(route);

        showDialog("Route Details", routeDetails);
    }

    private void showRouteOnMap(Route route) {
        // Optionally, clear any previous route.
        clearMap();

        // Show route as polyline.
        GeoPolyline routeGeoPolyline = route.getGeometry();
        float widthInPixels = 20;
        Color polylineColor = new Color(0, (float) 0.56, (float) 0.54, (float) 0.63);
        MapPolyline routeMapPolyline = null;

        try {
            routeMapPolyline = new MapPolyline(routeGeoPolyline, new MapPolyline.SolidRepresentation(
                    new MapMeasureDependentRenderSize(RenderSize.Unit.PIXELS, widthInPixels),
                    polylineColor,
                    LineCap.ROUND));
        } catch (MapPolyline.Representation.InstantiationException e) {
            Log.e("MapPolyline Representation Exception:", e.error.name());
        } catch (MapMeasureDependentRenderSize.InstantiationException e) {
            Log.e("MapMeasureDependentRenderSize Exception:", e.error.name());
        }

        mapView.getMapScene().addMapPolyline(routeMapPolyline);
        mapPolylines.add(routeMapPolyline);

        // Log maneuver instructions per route section.
        List<Section> sections = route.getSections();
        for (Section section : sections) {
            logManeuverInstructions(section);
        }
    }

    private void showWaypointsOnMap(List<Location> locations) {
        int n = locations.size();
        for (int i = 0; i < n; i++) {
            GeoCoordinates currentGeoCoordinates = locations.get(i).coordinates;
            if (i == 0 || i == n - 1) {
                addCircleMapMarker(currentGeoCoordinates, R.drawable.green_dot);
            } else {
                addCircleMapMarker(currentGeoCoordinates, R.drawable.red_dot);
            }
        }
    }

    private void hideWaypointsOnMap(List<MapMarker> mapMarkerList) {
        int n = mapMarkerList.size();
        for (int i = 1; i < n-1; i++) {
            mapView.getMapScene().removeMapMarker(mapMarkerList.get(i));
        }
    }

    private void logManeuverInstructions(Section section) {
        Log.d(TAG, "Log maneuver instructions per route section:");
        List<Maneuver> maneuverInstructions = section.getManeuvers();
        for (Maneuver maneuverInstruction : maneuverInstructions) {
            ManeuverAction maneuverAction = maneuverInstruction.getAction();
            GeoCoordinates maneuverLocation = maneuverInstruction.getCoordinates();
            String maneuverInfo = maneuverInstruction.getText()
                    + ", Action: " + maneuverAction.name()
                    + ", Location: " + maneuverLocation.toString();
            Log.d(TAG, maneuverInfo);
        }
    }

    public void removeWaypoints() {
        hideWaypointsOnMap(mapMarkerList);
    }

    private CarOptions getCarOptions() {
        CarOptions carOptions = new CarOptions();
        carOptions.routeOptions.enableTolls = true;
        return carOptions;
    }

    public void clearMap() {
        clearWaypointMapMarker();
        clearRoute();
    }

    private void clearWaypointMapMarker() {
        for (MapMarker mapMarker : mapMarkerList) {
            mapView.getMapScene().removeMapMarker(mapMarker);
        }
        mapMarkerList.clear();
    }

    private void clearRoute() {
        for (MapPolyline mapPolyline : mapPolylines) {
            mapView.getMapScene().removeMapPolyline(mapPolyline);
        }
        mapPolylines.clear();
    }


    private GeoCoordinates createRandomGeoCoordinatesAroundMapCenter() {
        GeoCoordinates centerGeoCoordinates = mapView.viewToGeoCoordinates(
                new Point2D(mapView.getWidth() / 2, mapView.getHeight() / 2));
        if (centerGeoCoordinates == null) {
            // Should never happen for center coordinates.
            throw new RuntimeException("CenterGeoCoordinates are null");
        }
        double lat = centerGeoCoordinates.latitude;
        double lon = centerGeoCoordinates.longitude;
        return new GeoCoordinates(getRandom(lat - 0.02, lat + 0.02),
                getRandom(lon - 0.02, lon + 0.02));
    }

    private double getRandom(double min, double max) {
        return min + Math.random() * (max - min);
    }

    private void addCircleMapMarker(GeoCoordinates geoCoordinates, int resourceId) {
        MapImage mapImage = MapImageFactory.fromResource(context.getResources(), resourceId);
        MapMarker mapMarker = new MapMarker(geoCoordinates, mapImage);
        mapView.getMapScene().addMapMarker(mapMarker);
        mapMarkerList.add(mapMarker);
    }

    private void showDialog(String title, String message) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }
}

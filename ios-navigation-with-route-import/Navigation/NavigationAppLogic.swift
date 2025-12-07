/*
 * Copyright (C) 2025 HERE Europe B.V.
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

import heresdk
import SwiftUI

enum ConstantsEnum {
    static let DEFAULT_MAP_CENTER = GeoCoordinates(latitude: 52.520798, longitude: 13.409408)
    static let DEFAULT_DISTANCE_IN_METERS: Double = 1000 * 2
}

// A protocol to send text messages from other classes to this class.
protocol MessageDelegate {
    func updateMessage(_ message: String)
}

// An app that allows to calculate a route and start navigation, using either platform positioning or
// simulated locations.
class NavigationAppLogic: ObservableObject, MessageDelegate, LongPressDelegate {
    
    private var mapView: MapView?
    private var routeCalculator: RouteCalculator?
    private var navigationExample: NavigationExample?
    private var mapMarkers = [MapMarker]()
    private var mapPolylineList = [MapPolyline]()
    private var startingWaypoint: Waypoint?
    private var destinationWaypoint: Waypoint?
    private var isLongpressDestination = false
    private var timeUtils = TimeUtils()
    
    @Published var messageText: String = "Initializing ..."

    func startExample(_ mapView: MapView) {
        self.mapView = mapView
        
        let distanceInMeters = MapMeasure(kind: .distanceInMeters, value: ConstantsEnum.DEFAULT_DISTANCE_IN_METERS)
        mapView.camera.lookAt(point: ConstantsEnum.DEFAULT_MAP_CENTER,
                              zoom: distanceInMeters)
        
        routeCalculator = RouteCalculator()
        if let routeCalculator = routeCalculator {
            navigationExample = NavigationExample(mapView: mapView, routeCalculator: routeCalculator)
            navigationExample?.startLocationProvider()
            navigationExample?.messageDelegate = self
        }
        
        // Load the map scene using a map scheme to render the map with.
        mapView.mapScene.loadScene(mapScheme: MapScheme.normalDay, completion: onLoadScene)
        
        setLongPressGestureHandler()
        updateMessage("Long press to set a destination or use a random one.")
    }
    
    // Completion handler for loadScene().
    private func onLoadScene(mapError: MapError?) {
        guard mapError == nil else {
            print("Error: Map scene not loaded, \(String(describing: mapError))")
            return
        }
        
        // Enable traffic flows, low speed zones and 3d landmarks, by default.
        mapView?.mapScene.enableFeatures([MapFeatures.trafficFlow : MapFeatureModes.trafficFlowWithFreeFlow])
        mapView?.mapScene.enableFeatures([MapFeatures.lowSpeedZones : MapFeatureModes.lowSpeedZonesAll])
        mapView?.mapScene.enableFeatures([MapFeatures.landmarks : MapFeatureModes.landmarksTextured])
    }
    
    // Calculate a route and start navigation using a location simulator.
    // Start is map center and destination location is set random within viewport,
    // unless a destination is set via long press.
    func addRouteSimulatedLocationButtonClicked() {
        calculateRoute(isSimulated: true)
    }
    
    // Calculate a route and start navigation using locations from device.
    // Start is current location and destination is set random within viewport,
    // unless a destination is set via long press.
    func addRouteDeviceLocationButtonClicked() {
        calculateRoute(isSimulated: false)
    }
    
    func clearMapButtonClicked() {
        clearMap()
        isLongpressDestination = false
    }
    
    func enableCameraTracking() {
        navigationExample?.startCameraTracking()
    }
    
    func disableCameraTracking() {
        navigationExample?.stopCameraTracking()
    }
    
    private func calculateRoute(isSimulated: Bool) {
        clearMap()
        
        if !determineRouteWaypoints(isSimulated: isSimulated) {
            return
        }
        
//        // Option 1: Calculates a car route with route calculator.
//        routeCalculator?.calculateRoute(start: startingWaypoint!,
//                                        destination: destinationWaypoint!) { (routingError, routes) in
//            if let error = routingError {
//                self.showDialog(title: "Error while calculating a route:", message: "\(error)")
//                return
//            }
//            
//            // When routingError is nil, routes is guaranteed to contain at least one route.
//            let route = routes!.first
//            self.showRouteOnMap(route: route!)
//            self.showRouteDetails(route: route!, isSimulated: isSimulated)
//        }
        
//        // Option 2: Calculates a car route with route import.
//        let routeLocations = [
//            Location(coordinates: GeoCoordinates(latitude: 52.524010, longitude: 13.382570)),
//            Location(coordinates: GeoCoordinates(latitude: 52.523970, longitude: 13.382350)),
//            Location(coordinates: GeoCoordinates(latitude: 52.523940, longitude: 13.382220)),
//            Location(coordinates: GeoCoordinates(latitude: 52.523910, longitude: 13.381990)),
//            Location(coordinates: GeoCoordinates(latitude: 52.523890, longitude: 13.381780)),
//            Location(coordinates: GeoCoordinates(latitude: 52.523510, longitude: 13.379480)),
//            Location(coordinates: GeoCoordinates(latitude: 52.522870, longitude: 13.379590)),
//            Location(coordinates: GeoCoordinates(latitude: 52.522590, longitude: 13.379630)),
//            Location(coordinates: GeoCoordinates(latitude: 52.522690, longitude: 13.380250)),
//            Location(coordinates: GeoCoordinates(latitude: 52.522770, longitude: 13.380790)),
//            Location(coordinates: GeoCoordinates(latitude: 52.522810, longitude: 13.381010)),
//            Location(coordinates: GeoCoordinates(latitude: 52.522920, longitude: 13.381710)),
//            Location(coordinates: GeoCoordinates(latitude: 52.522990, longitude: 13.382170)),
//            Location(coordinates: GeoCoordinates(latitude: 52.523080, longitude: 13.382730)),
//            Location(coordinates: GeoCoordinates(latitude: 52.523110, longitude: 13.382910)),
//            Location(coordinates: GeoCoordinates(latitude: 52.523200, longitude: 13.383470)),
//            Location(coordinates: GeoCoordinates(latitude: 52.523250, longitude: 13.383780)),
//            Location(coordinates: GeoCoordinates(latitude: 52.523370, longitude: 13.384490)),
//            Location(coordinates: GeoCoordinates(latitude: 52.523520, longitude: 13.385450)),
//            Location(coordinates: GeoCoordinates(latitude: 52.523770, longitude: 13.386980)),
//            Location(coordinates: GeoCoordinates(latitude: 52.523820, longitude: 13.387250)),
//            Location(coordinates: GeoCoordinates(latitude: 52.523880, longitude: 13.387590)),
//            Location(coordinates: GeoCoordinates(latitude: 52.523890, longitude: 13.387670)),
//            Location(coordinates: GeoCoordinates(latitude: 52.523910, longitude: 13.387780)),
//            Location(coordinates: GeoCoordinates(latitude: 52.524440, longitude: 13.387690)),
//            Location(coordinates: GeoCoordinates(latitude: 52.524910, longitude: 13.387600)),
//            Location(coordinates: GeoCoordinates(latitude: 52.525060, longitude: 13.387570)),
//            Location(coordinates: GeoCoordinates(latitude: 52.525900, longitude: 13.387440)),
//            Location(coordinates: GeoCoordinates(latitude: 52.526100, longitude: 13.387390)),
//            Location(coordinates: GeoCoordinates(latitude: 52.526260, longitude: 13.387340)),
//            Location(coordinates: GeoCoordinates(latitude: 52.526250, longitude: 13.387520)),
//            Location(coordinates: GeoCoordinates(latitude: 52.526210, longitude: 13.387730)),
//            Location(coordinates: GeoCoordinates(latitude: 52.526080, longitude: 13.388290)),
//            Location(coordinates: GeoCoordinates(latitude: 52.526030, longitude: 13.388470)),
//            Location(coordinates: GeoCoordinates(latitude: 52.526000, longitude: 13.388730)),
//            Location(coordinates: GeoCoordinates(latitude: 52.525950, longitude: 13.388970)),
//            Location(coordinates: GeoCoordinates(latitude: 52.525860, longitude: 13.389340)),
//            Location(coordinates: GeoCoordinates(latitude: 52.525700, longitude: 13.389960)),
//            Location(coordinates: GeoCoordinates(latitude: 52.525460, longitude: 13.390930)),
//            Location(coordinates: GeoCoordinates(latitude: 52.525390, longitude: 13.391230)),
//            Location(coordinates: GeoCoordinates(latitude: 52.525220, longitude: 13.391940)),
//            Location(coordinates: GeoCoordinates(latitude: 52.524980, longitude: 13.392860)),
//            Location(coordinates: GeoCoordinates(latitude: 52.524810, longitude: 13.393520)),
//            Location(coordinates: GeoCoordinates(latitude: 52.524540, longitude: 13.394590)),
//            Location(coordinates: GeoCoordinates(latitude: 52.524420, longitude: 13.395150)),
//            Location(coordinates: GeoCoordinates(latitude: 52.524380, longitude: 13.395290)),
//            Location(coordinates: GeoCoordinates(latitude: 52.524350, longitude: 13.395400)),
//            Location(coordinates: GeoCoordinates(latitude: 52.524970, longitude: 13.396070)),
//            Location(coordinates: GeoCoordinates(latitude: 52.525040, longitude: 13.396200)),
//            Location(coordinates: GeoCoordinates(latitude: 52.525060, longitude: 13.396270)),
//            Location(coordinates: GeoCoordinates(latitude: 52.525390, longitude: 13.398240)),
//            Location(coordinates: GeoCoordinates(latitude: 52.525480, longitude: 13.398850)),
//            Location(coordinates: GeoCoordinates(latitude: 52.525360, longitude: 13.398900))]
//
//        routeCalculator?.importRouteFromLocations(routeLocations: routeLocations) { (routingError, routes) in
//            if let error = routingError {
//                self.showDialog(title: "Error while calculating a route:", message: "\(error)")
//                return
//            }
//            
//            // When routingError is nil, routes is guaranteed to contain at least one route.
//            let route = routes!.first
//            self.showRouteOnMap(route: route!)
//            self.showRouteDetails(route: route!, isSimulated: isSimulated)
//        }
        
        // Option 3: Calcuate a car route with route handle.
        let routeHandle = "AHcA_v___wwAAQCfAAAAeNpjmM_AwMTAwMCekVqUapWcO6kihBvIZbjL9eMuBw8Dw3fdls6ak5GpcNqipdN9S2QqAxYAM8O1_5kMTIwJaMbf2PbOog-RqTD6v2F7p-Fn7GaAwMI8jzV6QFpBkfMQgwAAN68pCacvSDQ"
        
        routeCalculator?.importRouteFromRouteHandle(routeHandle: routeHandle) { (routingError, routes) in
            if let error = routingError {
                self.showDialog(title: "Error while calculating a route:", message: "\(error)")
                return
            }
            
            // When routingError is nil, routes is guaranteed to contain at least one route.
            let route = routes!.first
            self.showRouteOnMap(route: route!)
            self.showRouteDetails(route: route!, isSimulated: isSimulated)
        }
    }
    
    private func determineRouteWaypoints(isSimulated: Bool) -> Bool {
        // When using real GPS locations, we always start from the current location of user.
        if !isSimulated {
            guard let location = navigationExample?.getLastKnownLocation() else {
                showDialog(title: "Error", message: "No location found.")
                return false
            }
            
            startingWaypoint = Waypoint(coordinates: location.coordinates)
            
            // If a driver is moving, the bearing value can help to improve the route calculation.
            startingWaypoint?.headingInDegrees = location.bearingInDegrees
            
            mapView?.camera.lookAt(point: location.coordinates)
        }
        
        if (startingWaypoint == nil) {
            startingWaypoint = Waypoint(coordinates: createRandomGeoCoordinatesAroundMapCenter())
        }
        
        if (destinationWaypoint == nil) {
            destinationWaypoint = Waypoint(coordinates: createRandomGeoCoordinatesAroundMapCenter())
        }
        
        return true
    }
    
    private func showRouteDetails(route: Route, isSimulated: Bool) {
        let estimatedTravelTimeInSeconds = route.duration
        let lengthInMeters = route.lengthInMeters
        
        let routeDetails = "Travel Time: " + timeUtils.formatTime(sec: estimatedTravelTimeInSeconds)
        + ", Length: " + timeUtils.formatLength(meters: lengthInMeters)
        
        showStartNavigationDialog(title: "Route Details",
                                  message: routeDetails,
                                  route: route,
                                  isSimulated: isSimulated)
    }
    
    private func showRouteOnMap(route: Route) {
        // Show route as polyline.
        let routeGeoPolyline = route.geometry
        let widthInPixels = 20.0
        let polylineColor = UIColor(red: 0, green: 0.56, blue: 0.54, alpha: 0.63)
        do {
            let routeMapPolyline =  try MapPolyline(geometry: routeGeoPolyline,
                                                    representation: MapPolyline.SolidRepresentation(
                                                        lineWidth: MapMeasureDependentRenderSize(
                                                            sizeUnit: RenderSize.Unit.pixels,
                                                            size: widthInPixels),
                                                        color: polylineColor,
                                                        capShape: LineCap.round))
            mapView?.mapScene.addMapPolyline(routeMapPolyline)
            mapPolylineList.append(routeMapPolyline)
        } catch let error {
            fatalError("Failed to render MapPolyline. Cause: \(error)")
        }
    }
    
    func clearMap() {
        clearWaypointMapMarker()
        clearRoute()
        
        navigationExample?.stopNavigation()
    }
    
    private func clearWaypointMapMarker() {
        for mapMarker in mapMarkers {
            mapView?.mapScene.removeMapMarker(mapMarker)
        }
        mapMarkers.removeAll()
    }
    
    private func clearRoute() {
        for mapPolyline in mapPolylineList {
            mapView?.mapScene.removeMapPolyline(mapPolyline)
        }
        mapPolylineList.removeAll()
    }
    
    private func setLongPressGestureHandler() {
        mapView?.gestures.longPressDelegate = self
    }
    
    // Conform to LongPressDelegate protocol.
    func onLongPress(state: heresdk.GestureState, origin: Point2D) {
        guard let geoCoordinates = mapView?.viewToGeoCoordinates(viewCoordinates: origin) else {
            print("Warning: Long press coordinate is not on map view.")
            return
        }
        
        if state == GestureState.begin {
            if (isLongpressDestination) {
                destinationWaypoint = Waypoint(coordinates: geoCoordinates);
                addCircleMapMarker(geoCoordinates: destinationWaypoint!.coordinates, imageName: "green_dot.png")
                updateMessage("New long press destination set.")
            } else {
                startingWaypoint = Waypoint(coordinates: geoCoordinates)
                addCircleMapMarker(geoCoordinates: startingWaypoint!.coordinates, imageName: "green_dot.png")
                updateMessage("New long press starting point set.")
            }
            isLongpressDestination = !isLongpressDestination;
        }
    }
    
    private func createRandomGeoCoordinatesAroundMapCenter() -> GeoCoordinates {
        let centerGeoCoordinates = getMapViewCenter()
        let lat = centerGeoCoordinates.latitude
        let lon = centerGeoCoordinates.longitude
        return GeoCoordinates(latitude: getRandom(min: lat - 0.02,
                                                  max: lat + 0.02),
                              longitude: getRandom(min: lon - 0.02,
                                                   max: lon + 0.02))
    }
    
    private func getRandom(min: Double, max: Double) -> Double {
        return Double.random(in: min ... max)
    }
    
    private func getMapViewCenter() -> GeoCoordinates {
        return mapView?.camera.state.targetCoordinates ?? GeoCoordinates(latitude: 52, longitude: 13)
    }
    
    private func addCircleMapMarker(geoCoordinates: GeoCoordinates, imageName: String) {
        guard
            let image = UIImage(named: imageName),
            let imageData = image.pngData() else {
            return
        }
        
        let mapImage = MapImage(pixelData: imageData,
                                imageFormat: ImageFormat.png)
        let mapMarker = MapMarker(at: geoCoordinates,
                                  image: mapImage)
        mapView?.mapScene.addMapMarker(mapMarker)
        mapMarkers.append(mapMarker)
    }
    
    private func showStartNavigationDialog(title: String,
                                           message: String,
                                           route: Route,
                                           isSimulated: Bool) {
        
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootViewController = windowScene.windows.first(where: { $0.isKeyWindow })?.rootViewController {
            
            let alert = UIAlertController(
                title: title,
                message: message,
                preferredStyle: .alert
            )
            
            let buttonText = isSimulated ? "Start navigation (simulated)" : "Start navigation (device location)"
            
            alert.addAction(UIAlertAction(title: buttonText, style: .default, handler: { (alertAction) -> Void in
                // Handle OK button action.
                self.navigationExample?.startNavigation(route: route, isSimulated: isSimulated)
            }))
            alert.addAction(UIAlertAction(title: "Cancel", style: .default))
            
            rootViewController.present(alert, animated: true, completion: nil)
        }
    }
    
    private func showDialog(title: String, message: String) {
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootViewController = windowScene.windows.first(where: { $0.isKeyWindow })?.rootViewController {
            
            let alert = UIAlertController(
                title: title,
                message: message,
                preferredStyle: .alert
            )
            
            alert.addAction(UIAlertAction(title: "OK", style: .default, handler: { _ in
                // Handle OK button action.
                alert.dismiss(animated: true, completion: nil)
            }))
            
            rootViewController.present(alert, animated: true, completion: nil)
        }
    }
    
    // Conform to MessageDelegate protocol.
    func updateMessage(_ message: String) {
        // Publish the text in our ContentView.
        messageText = message
    }
}

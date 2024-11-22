import 'package:geolocator/geolocator.dart';
import 'package:amap_location/amap_location.dart';

class LocationService {
  Future<Position> getCurrentLocation() async {
    bool serviceEnabled;
    LocationPermission permission;

    // Check if location services are enabled.
    serviceEnabled = await Geolocator.isLocationServiceEnabled();
    if (!serviceEnabled) {
      return Future.error('Location services are disabled.');
    }

    // Check if location permissions are granted.
    permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
      if (permission == LocationPermission.denied) {
        return Future.error('Location permissions are denied.');
      }
    }

    if (permission == LocationPermission.deniedForever) {
      return Future.error(
          'Location permissions are permanently denied, we cannot request permissions.');
    }

    return await Geolocator.getCurrentPosition();
  }

  Future<double> calculateDistanceToStation(Position currentPosition,
      double stationLatitude, double stationLongitude) async {
    final amapLocation = AMapLocation();
    final result = await amapLocation.calculateDistance(
        currentPosition.latitude,
        currentPosition.longitude,
        stationLatitude,
        stationLongitude);
    return result.distance;
  }
}
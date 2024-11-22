import 'package:location_service.dart';

class JourneyPlanner {
  final LocationService locationService = LocationService();

  Future<double> calculateTravelTimeToStation(
      double stationLatitude, double stationLongitude) async {
    final currentPosition = await locationService.getCurrentLocation();
    final distance = await locationService.calculateDistanceToStation(
        currentPosition, stationLatitude, stationLongitude);
    // Assume an average walking speed of 5 km/h.
    final speed = 5;
    final travelTime = distance / speed;
    return travelTime;
  }
}
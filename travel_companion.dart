import 'package:journey_planner.dart';
import 'package:notification_service.dart';

class TravelCompanion {
  final JourneyPlanner journeyPlanner = JourneyPlanner();
  final NotificationService notificationService = NotificationService();

  Future<void> calculateOptimalDepartureTime(
      double stationLatitude, double stationLongitude) async {
    final travelTime = await journeyPlanner.calculateTravelTimeToStation(
        stationLatitude, stationLongitude);
    final bufferTime = 45;
    final departureTime =
        DateTime.now().add(Duration(minutes: travelTime + bufferTime));
    await notificationService.scheduleDepartureAlarm(departureTime);
  }

  Future<void> verifyDepartureLocation(double stationLatitude,
      double stationLongitude, double distanceThreshold) async {
    final currentPosition = await journeyPlanner.locationService.getCurrentLocation();
    final distance = await journeyPlanner.locationService.calculateDistanceToStation(
        currentPosition, stationLatitude, stationLongitude);
    if (distance <= distanceThreshold) {
      await notificationService.showLocationBasedAlert();
    }
  }

  Future<void> setNotificationPreferences(
      bool enableVoiceNotifications, bool enableTextNotifications) async {
    // Implement notification preference settings.
  }
}
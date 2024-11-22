import 'package:flutter/material.dart';
import 'package:travel_companion_app/travel_companion.dart';
import 'package:google_fonts/google_fonts.dart';

void main() {
  runApp(const TravelCompanionApp());
}

class TravelCompanionApp extends StatefulWidget {
  const TravelCompanionApp({Key? key}) : super(key: key);

  @override
  _TravelCompanionAppState createState() => _TravelCompanionAppState();
}

class _TravelCompanionAppState extends State<TravelCompanionApp> {
  final TravelCompanion travelCompanion = TravelCompanion();

  @override
  void initState() {
    super.initState();
    travelCompanion.notificationService.initNotifications();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Travel Companion',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
        useMaterial3: true,
        textTheme: GoogleFonts.poppinsTextTheme(),
      ),
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Travel Companion'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ElevatedButton(
                onPressed: () async {
                  // Implement ticket scanning and journey planning.
                  await travelCompanion.calculateOptimalDepartureTime(
                      37.7749, -122.4194); // Replace with actual station coordinates.
                },
                child: const Text('Plan Journey'),
              ),
              ElevatedButton(
                onPressed: () async {
                  // Implement location-based departure verification.
                  await travelCompanion.verifyDepartureLocation(
                      37.7749, -122.4194, 1000); // Replace with actual station coordinates and distance threshold.
                },
                child: const Text('Verify Departure Location'),
              ),
              ElevatedButton(
                onPressed: () async {
                  // Implement notification preference settings.
                  await travelCompanion.setNotificationPreferences(
                      true, true);
                },
                child: const Text('Set Notification Preferences'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
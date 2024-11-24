# Travel Time Planner

A NativeScript mobile application for managing train and car travel schedules.

## Features

- Ticket information entry
- Real-time location tracking
- Maps integration
- Smart alarms for departure and arrival
- Location-based alerts
- Customizable settings

## Development

1. Install dependencies:
```bash
npm install
```

2. Run the application:
```bash
ns preview
```

## Testing

Run the test suite:
```bash
npm run test
```

## Linting

Check code style:
```bash
npm run lint
```

Fix code style issues:
```bash
npm run lint:fix
```

## Building

Build for Android:
```bash
npm run build:android
```

## CI/CD

This project uses GitHub Actions for continuous integration and deployment. The pipeline:

- Runs tests and linting on every push and PR
- Builds Android APK
- Creates releases on version tags
import { Observable } from '@nativescript/core';
import { Geolocation } from '@nativescript/geolocation';
import { LocalNotifications } from '@nativescript/local-notifications';

export class HomeViewModel extends Observable {
    private _fromLocation: string = '';
    private _toLocation: string = '';
    private _departureHour: number = 9;
    private _departureMinute: number = 0;
    private _arrivalHour: number = 10;
    private _arrivalMinute: number = 0;
    private _selectedTransportMode: number = 0;
    private _statusMessage: string = 'Ready to plan your journey';
    private _statusClass: string = 'text-gray-600';

    constructor() {
        super();
        this.startLocationTracking();
    }

    get fromLocation(): string {
        return this._fromLocation;
    }

    set fromLocation(value: string) {
        if (this._fromLocation !== value) {
            this._fromLocation = value;
            this.notifyPropertyChange('fromLocation', value);
        }
    }

    // Similar getters/setters for other properties...

    async calculateRoute() {
        try {
            // Implement route calculation logic here
            this.statusMessage = 'Calculating route...';
            // Mock calculation for demo
            setTimeout(() => {
                this.statusMessage = 'Route calculated! Journey time: 1h 30m';
            }, 1500);
        } catch (error) {
            console.error('Error calculating route:', error);
            this.statusMessage = 'Error calculating route';
        }
    }

    async setAlarms() {
        try {
            const departureTime = new Date();
            departureTime.setHours(this._departureHour, this._departureMinute);

            await LocalNotifications.schedule([
                {
                    id: 1,
                    title: 'Departure Alert',
                    body: 'Time to leave! Your journey begins in 45 minutes',
                    at: new Date(departureTime.getTime() - 45 * 60000)
                },
                {
                    id: 2,
                    title: 'Boarding Alert',
                    body: '30 minutes until departure',
                    at: new Date(departureTime.getTime() - 30 * 60000)
                }
            ]);

            this.statusMessage = 'Alarms set successfully';
        } catch (error) {
            console.error('Error setting alarms:', error);
            this.statusMessage = 'Error setting alarms';
        }
    }

    private async startLocationTracking() {
        try {
            const watchId = await Geolocation.watchLocation(
                (location) => {
                    this.checkProximityToStation(location);
                },
                (error) => {
                    console.error('Error watching location:', error);
                },
                {
                    desiredAccuracy: 3,
                    updateDistance: 10,
                    minimumUpdateTime: 1000 * 60
                }
            );
        } catch (error) {
            console.error('Error starting location tracking:', error);
        }
    }

    private checkProximityToStation(location: any) {
        // Mock station location for demo
        const stationLat = 35.6812;
        const stationLon = 139.7671;
        
        // Calculate distance (simplified)
        const distance = this.calculateDistance(
            location.latitude,
            location.longitude,
            stationLat,
            stationLon
        );

        if (distance < 1) {
            this.statusClass = 'text-red-500';
            this.statusMessage = 'Hurry up! You\'re near the station';
        } else {
            this.statusClass = 'text-green-500';
            this.statusMessage = 'Mind the time';
        }
    }

    private calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
        // Simplified distance calculation (km)
        const R = 6371;
        const dLat = (lat2 - lat1) * Math.PI / 180;
        const dLon = (lon2 - lon1) * Math.PI / 180;
        const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                Math.sin(dLon/2) * Math.sin(dLon/2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}
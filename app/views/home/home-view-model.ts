import { Observable } from '@nativescript/core';
import { Geolocation } from '@nativescript/geolocation';
import { LocalNotifications } from '@nativescript/local-notifications';

interface Location {
    latitude: number;
    longitude: number;
    speed: number;
    direction: number;
    timestamp: number;
}

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
    private _locationWatchId: number | null = null;

    constructor() {
        super();
        this.startLocationTracking().catch(error => {
            console.error('Failed to start location tracking:', error);
        });
    }

    // Getters and setters with proper typing
    get fromLocation(): string {
        return this._fromLocation;
    }

    set fromLocation(value: string) {
        if (this._fromLocation !== value) {
            this._fromLocation = value;
            this.notifyPropertyChange('fromLocation', value);
        }
    }

    get toLocation(): string {
        return this._toLocation;
    }

    set toLocation(value: string) {
        if (this._toLocation !== value) {
            this._toLocation = value;
            this.notifyPropertyChange('toLocation', value);
        }
    }

    get statusMessage(): string {
        return this._statusMessage;
    }

    set statusMessage(value: string) {
        if (this._statusMessage !== value) {
            this._statusMessage = value;
            this.notifyPropertyChange('statusMessage', value);
        }
    }

    get statusClass(): string {
        return this._statusClass;
    }

    set statusClass(value: string) {
        if (this._statusClass !== value) {
            this._statusClass = value;
            this.notifyPropertyChange('statusClass', value);
        }
    }

    async calculateRoute(): Promise<void> {
        try {
            this.statusMessage = 'Calculating route...';
            
            // Validate inputs
            if (!this._fromLocation || !this._toLocation) {
                throw new Error('Please enter both departure and destination locations');
            }

            // Mock calculation for demo
            await new Promise(resolve => setTimeout(resolve, 1500));
            this.statusMessage = 'Route calculated! Journey time: 1h 30m';
        } catch (error) {
            console.error('Error calculating route:', error);
            this.statusMessage = `Error: ${error instanceof Error ? error.message : 'Failed to calculate route'}`;
        }
    }

    async setAlarms(): Promise<void> {
        try {
            const departureTime = new Date();
            departureTime.setHours(this._departureHour, this._departureMinute);

            // Validate departure time is in the future
            if (departureTime.getTime() <= Date.now()) {
                throw new Error('Departure time must be in the future');
            }

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
            this.statusMessage = `Error: ${error instanceof Error ? error.message : 'Failed to set alarms'}`;
        }
    }

    private async startLocationTracking(): Promise<void> {
        try {
            // Clear existing watch if any
            if (this._locationWatchId !== null) {
                Geolocation.clearWatch(this._locationWatchId);
            }

            this._locationWatchId = await Geolocation.watchLocation(
                (location: Location) => {
                    this.checkProximityToStation(location);
                },
                (error) => {
                    console.error('Error watching location:', error);
                    this.statusMessage = 'Error tracking location';
                },
                {
                    desiredAccuracy: 3,
                    updateDistance: 10,
                    minimumUpdateTime: 1000 * 60
                }
            );
        } catch (error) {
            console.error('Error starting location tracking:', error);
            throw error;
        }
    }

    private checkProximityToStation(location: Location): void {
        // Mock station location for demo
        const stationLat = 35.6812;
        const stationLon = 139.7671;
        
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
        const R = 6371; // Earth's radius in km
        const dLat = (lat2 - lat1) * Math.PI / 180;
        const dLon = (lon2 - lon1) * Math.PI / 180;
        const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                Math.sin(dLon/2) * Math.sin(dLon/2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    // Cleanup method to prevent memory leaks
    public cleanup(): void {
        if (this._locationWatchId !== null) {
            Geolocation.clearWatch(this._locationWatchId);
            this._locationWatchId = null;
        }
    }
}
import { Permissions } from 'nativescript-permissions';
import { Geolocation } from '@nativescript/geolocation';

export async function requestPermissions() {
    try {
        await Permissions.requestPermission([
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ]);
        
        const hasLocationPermission = await Geolocation.enableLocationRequest();
        if (!hasLocationPermission) {
            console.log('Location permission denied');
        }
    } catch (error) {
        console.error('Error requesting permissions:', error);
    }
}
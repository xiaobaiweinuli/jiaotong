import { PERMISSIONS } from 'nativescript-permissions';
import { Location } from '@nativescript/geolocation';

// 使用 PERMISSIONS 替代 Permissions
export async function requestPermissions() {
    try {
        await PERMISSIONS.requestPermission([
            // 正确引用 android.Manifest.permission
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
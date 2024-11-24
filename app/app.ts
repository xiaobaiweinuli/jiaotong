import { Application } from '@nativescript/core';
import { requestPermissions } from './utils/permissions';

Application.on(Application.launchEvent, () => {
    requestPermissions();
});

Application.run({ moduleName: 'app-root' });
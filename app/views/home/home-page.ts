import { NavigatedData, Page } from '@nativescript/core';
import { HomeViewModel } from './home-view-model';

let viewModel: HomeViewModel | null = null;

export function onNavigatingTo(args: NavigatedData) {
    const page = <Page>args.object;
    viewModel = new HomeViewModel();
    page.bindingContext = viewModel;
}

export function onUnloaded() {
    if (viewModel) {
        viewModel.cleanup();
        viewModel = null;
    }
}
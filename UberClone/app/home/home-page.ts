import {
  EventData,
  ItemEventData,
  ListView,
  Page,
  View
} from '@nativescript/core';

import {
  MapReadyEvent,
  MapType
} from '@nativescript/google-maps';

import { HomeViewModel } from './home-view-model';

let viewModel: HomeViewModel;

export function onNavigatingTo(args: EventData): void {
  const page = args.object as Page;

  if (!viewModel) {
    viewModel = new HomeViewModel();
  }

  page.bindingContext = viewModel;
}

export function onPageLoaded(args: EventData): void {
  const page = args.object as Page;
  const contentArea = page.getViewById('contentArea') as View;
  const bottomNav = page.getViewById('bottomNav') as View;

  if (contentArea) {
    contentArea.opacity = 0;
    contentArea.translateY = 18;

    contentArea.animate({
      opacity: 1,
      translate: { x: 0, y: 0 },
      duration: 350,
      curve: 'easeOut'
    });
  }

  if (bottomNav) {
    bottomNav.opacity = 0;
    bottomNav.translateY = 35;

    bottomNav.animate({
      opacity: 1,
      translate: { x: 0, y: 0 },
      duration: 420,
      curve: 'easeOut'
    });
  }
}

function getViewModel(args: EventData): HomeViewModel {
  const view = args.object as View;
  return view.page.bindingContext as HomeViewModel;
}

function animatePress(view: View): void {
  view.animate({
    scale: { x: 0.94, y: 0.94 },
    duration: 70
  }).then(() => {
    return view.animate({
      scale: { x: 1, y: 1 },
      duration: 70
    });
  });
}

export function onLocationTap(args: EventData): void {
  const view = args.object as View;
  const vm = getViewModel(args);

  animatePress(view);
  vm.showLocationWarning();
}

export function onSearchTap(args: EventData): void {
  const view = args.object as View;
  const vm = getViewModel(args);

  animatePress(view);
  vm.openSearch();
}

export function onNavHomeTap(args: EventData): void {
  const view = args.object as View;
  const vm = getViewModel(args);

  animatePress(view);
  vm.showHome();
}

export function onNavOptionsTap(args: EventData): void {
  const view = args.object as View;
  const vm = getViewModel(args);

  animatePress(view);
  vm.showOptions();
}

export function onNavActivityTap(args: EventData): void {
  const view = args.object as View;
  const vm = getViewModel(args);

  animatePress(view);
  vm.showActivity();
}

export function onNavAccountTap(args: EventData): void {
  const view = args.object as View;
  const vm = getViewModel(args);

  animatePress(view);
  vm.showAccount();
}

export function onStartRideTap(args: EventData): void {
  const view = args.object as View;
  const vm = getViewModel(args);

  animatePress(view);
  vm.showHome();
}

export function onReserveTap(args: EventData): void {
  const view = args.object as View;
  const vm = getViewModel(args);

  animatePress(view);
  vm.openFeature('Reserva');
}

export function onOlderPeopleTap(args: EventData): void {
  const view = args.object as View;
  const vm = getViewModel(args);

  animatePress(view);
  vm.openFeature('Personas mayores');
}

export function onTeensTap(args: EventData): void {
  const view = args.object as View;
  const vm = getViewModel(args);

  animatePress(view);
  vm.openFeature('Uber Teens');
}

export function onGenericFeatureTap(args: EventData): void {
  const view = args.object as View;
  const vm = getViewModel(args);

  animatePress(view);
  vm.openFeature('Función de Uber');
}

export function onDeliveryRowTap(args: ItemEventData): void {
  const list = args.object as ListView;
  const vm = list.page.bindingContext as HomeViewModel;

  vm.selectDeliveryRow(args.index);
}

export function onActivityTripTap(args: ItemEventData): void {
  const list = args.object as ListView;
  const vm = list.page.bindingContext as HomeViewModel;

  vm.reorderTrip(args.index);
}

export function onAccountCardTap(args: ItemEventData): void {
  const list = args.object as ListView;
  const vm = list.page.bindingContext as HomeViewModel;

  vm.openAccountCard(args.index);
}

export function onMapReady(args: MapReadyEvent): void {
  const map = args.map;

  map.mapType = MapType.Normal;
  map.trafficEnabled = false;
  map.buildingsEnabled = true;

  map.uiSettings.zoomControlsEnabled = false;
  map.uiSettings.myLocationButtonEnabled = false;
  map.uiSettings.mapToolbarEnabled = false;
  map.uiSettings.compassEnabled = false;

  map.mapStyle = [
    {
      featureType: 'all',
      elementType: 'geometry',
      stylers: [{ color: '#242f3e' }]
    },
    {
      featureType: 'all',
      elementType: 'labels.text.fill',
      stylers: [{ color: '#ffffff' }]
    },
    {
      featureType: 'road',
      elementType: 'geometry',
      stylers: [{ color: '#38414e' }]
    },
    {
      featureType: 'road',
      elementType: 'geometry.stroke',
      stylers: [{ color: '#212a37' }]
    },
    {
      featureType: 'water',
      elementType: 'geometry',
      stylers: [{ color: '#17263c' }]
    },
    {
      featureType: 'poi',
      elementType: 'geometry',
      stylers: [{ color: '#283d35' }]
    }
  ];

  map.addMarker({
    position: {
      lat: -0.1807,
      lng: -78.4678
    },
    title: 'Centro Comercial Iñaquito (CCI)',
    snippet: 'Destino del viaje'
  });

  map.addMarker({
    position: {
      lat: -0.1862,
      lng: -78.4801
    },
    title: 'Punto de partida',
    snippet: 'Inicio del viaje'
  });

  map.addPolyline({
    points: [
      { lat: -0.1862, lng: -78.4801 },
      { lat: -0.1847, lng: -78.4774 },
      { lat: -0.1830, lng: -78.4740 },
      { lat: -0.1815, lng: -78.4708 },
      { lat: -0.1807, lng: -78.4678 }
    ],
    color: '#ffffff',
    width: 8,
    visible: true,
    geodesic: true
  });
}

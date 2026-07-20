import { Dialogs, Observable, ObservableArray } from '@nativescript/core';

export interface DeliveryRow {
  leftTitle: string;
  leftImage: string;
  rightTitle: string;
  rightImage: string;
}

export interface TripHistory {
  destination: string;
  date: string;
  price: string;
  image: string;
}

export interface AccountCard {
  title: string;
  subtitle: string;
  image: string;
  value: string;
}

export class HomeViewModel extends Observable {
  public currentTab = 'home';

  public pickupText = '¿A dónde vas?';
  public userName = 'Saul Tualombo';
  public rating = '5.0';

  public profileImage = 'https://img.icons8.com/fluency/240/user-male-circle.png';

  public navHomeColor = '#ffffff';
  public navOptionsColor = '#9a9a9a';
  public navActivityColor = '#9a9a9a';
  public navAccountColor = '#9a9a9a';

  public navHomeBg = '#2f2f2f';
  public navOptionsBg = '#151515';
  public navActivityBg = '#151515';
  public navAccountBg = '#151515';

  public deliveryRows: ObservableArray<DeliveryRow>;
  public tripHistory: ObservableArray<TripHistory>;
  public accountCards: ObservableArray<AccountCard>;

  constructor() {
    super();

    this.deliveryRows = new ObservableArray<DeliveryRow>([
      {
        leftTitle: 'Alimentos',
        leftImage: 'https://img.icons8.com/fluency/160/shopping-basket.png',
        rightTitle: 'Súper',
        rightImage: 'https://img.icons8.com/fluency/160/shopping-cart.png'
      },
      {
        leftTitle: 'Alcohol',
        leftImage: 'https://img.icons8.com/fluency/160/wine-bottle.png',
        rightTitle: 'Farmacia',
        rightImage: 'https://img.icons8.com/fluency/160/pill.png'
      },
      {
        leftTitle: 'Electrónica',
        leftImage: 'https://img.icons8.com/fluency/160/electronics.png',
        rightTitle: 'Express',
        rightImage: 'https://img.icons8.com/fluency/160/delivery.png'
      },
      {
        leftTitle: 'Retail',
        leftImage: 'https://img.icons8.com/fluency/160/shop.png',
        rightTitle: 'Mascotas',
        rightImage: 'https://img.icons8.com/fluency/160/dog-bowl.png'
      },
      {
        leftTitle: 'Cuidado personal',
        leftImage: 'https://img.icons8.com/fluency/160/cosmetic-brush.png',
        rightTitle: 'Bebé',
        rightImage: 'https://img.icons8.com/fluency/160/baby-bottle.png'
      }
    ]);

    this.tripHistory = new ObservableArray<TripHistory>([
      {
        destination: 'Primavera 1',
        date: '27 nov · 2:03 p.m.',
        price: '$3.00',
        image: 'https://img.icons8.com/fluency/160/car.png'
      },
      {
        destination: 'Primavera 1',
        date: '2 nov · 9:22 a.m.',
        price: '$3.32',
        image: 'https://img.icons8.com/fluency/160/car.png'
      },
      {
        destination: 'Monumento al General Rumiñahui',
        date: '31 oct · 4:49 p.m.',
        price: '$2.80',
        image: 'https://img.icons8.com/fluency/160/car.png'
      },
      {
        destination: 'Facultad de Ingeniería Eléctrica',
        date: '18 oct · 8:10 a.m.',
        price: '$2.45',
        image: 'https://img.icons8.com/fluency/160/car.png'
      }
    ]);

    this.accountCards = new ObservableArray<AccountCard>([
      {
        title: 'Prueba Uber One sin costo',
        subtitle: 'Desbloquea un 10% en créditos Uber One para viajes y más',
        image: 'https://img.icons8.com/fluency/200/taxi.png',
        value: ''
      },
      {
        title: 'Ahorro de CO₂ estimado',
        subtitle: 'Impacto aproximado de tus viajes compartidos',
        image: 'https://img.icons8.com/fluency/200/leaf.png',
        value: '0 g'
      },
      {
        title: 'Refiere a tus amistades',
        subtitle: 'Recompensas para ti y tus amigos',
        image: 'https://img.icons8.com/fluency/200/gift.png',
        value: ''
      },
      {
        title: 'Uber Teens',
        subtitle: 'Invita a tu adolescente a crear su propia cuenta',
        image: 'https://img.icons8.com/fluency/200/confetti.png',
        value: ''
      },
      {
        title: 'Elige tu equipo',
        subtitle: 'Viste tu viaje con la bandera de tu equipo favorito',
        image: 'https://img.icons8.com/fluency/200/sports-car.png',
        value: ''
      }
    ]);
  }

  get isHomeVisible(): boolean {
    return this.currentTab === 'home';
  }

  get isOptionsVisible(): boolean {
    return this.currentTab === 'options';
  }

  get isActivityVisible(): boolean {
    return this.currentTab === 'activity';
  }

  get isAccountVisible(): boolean {
    return this.currentTab === 'account';
  }

  public showHome(): void {
    this.setTab('home');
  }

  public showOptions(): void {
    this.setTab('options');
  }

  public showActivity(): void {
    this.setTab('activity');
  }

  public showAccount(): void {
    this.setTab('account');
  }

  private setTab(tab: string): void {
    this.currentTab = tab;

    this.navHomeColor = tab === 'home' ? '#ffffff' : '#9a9a9a';
    this.navOptionsColor = tab === 'options' ? '#ffffff' : '#9a9a9a';
    this.navActivityColor = tab === 'activity' ? '#ffffff' : '#9a9a9a';
    this.navAccountColor = tab === 'account' ? '#ffffff' : '#9a9a9a';

    this.navHomeBg = tab === 'home' ? '#2f2f2f' : '#151515';
    this.navOptionsBg = tab === 'options' ? '#2f2f2f' : '#151515';
    this.navActivityBg = tab === 'activity' ? '#2f2f2f' : '#151515';
    this.navAccountBg = tab === 'account' ? '#2f2f2f' : '#151515';

    this.notifyPropertyChange('currentTab', this.currentTab);

    this.notifyPropertyChange('isHomeVisible', this.isHomeVisible);
    this.notifyPropertyChange('isOptionsVisible', this.isOptionsVisible);
    this.notifyPropertyChange('isActivityVisible', this.isActivityVisible);
    this.notifyPropertyChange('isAccountVisible', this.isAccountVisible);

    this.notifyPropertyChange('navHomeColor', this.navHomeColor);
    this.notifyPropertyChange('navOptionsColor', this.navOptionsColor);
    this.notifyPropertyChange('navActivityColor', this.navActivityColor);
    this.notifyPropertyChange('navAccountColor', this.navAccountColor);

    this.notifyPropertyChange('navHomeBg', this.navHomeBg);
    this.notifyPropertyChange('navOptionsBg', this.navOptionsBg);
    this.notifyPropertyChange('navActivityBg', this.navActivityBg);
    this.notifyPropertyChange('navAccountBg', this.navAccountBg);
  }

  public async openSearch(): Promise<void> {
    const result = await Dialogs.prompt({
      title: 'Destino',
      message: 'Ingresa a dónde quieres ir',
      okButtonText: 'Buscar',
      cancelButtonText: 'Cancelar',
      defaultText: ''
    });

    if (result.result && result.text.trim().length > 0) {
      this.pickupText = result.text.trim();
      this.notifyPropertyChange('pickupText', this.pickupText);
    }
  }

  public async showLocationWarning(): Promise<void> {
    await Dialogs.alert({
      title: 'Ubicación desactivada',
      message: 'Activa la ubicación para mostrar conductores cercanos, calcular rutas y mejorar la precisión del viaje.',
      okButtonText: 'Entendido'
    });
  }

  public async openFeature(name: string): Promise<void> {
    await Dialogs.alert({
      title: name,
      message: `Abriste la sección ${name}. Esta navegación está simulada para el taller.`,
      okButtonText: 'Aceptar'
    });
  }

  public async selectDeliveryRow(index: number): Promise<void> {
    const item = this.deliveryRows.getItem(index);
    if (!item) return;

    await Dialogs.alert({
      title: 'Servicio seleccionado',
      message: `Seleccionaste ${item.leftTitle} o ${item.rightTitle}.`,
      okButtonText: 'Aceptar'
    });
  }

  public async reorderTrip(index: number): Promise<void> {
    const item = this.tripHistory.getItem(index);
    if (!item) return;

    this.pickupText = item.destination;
    this.notifyPropertyChange('pickupText', this.pickupText);
    this.showHome();

    await Dialogs.alert({
      title: 'Viaje reagendado',
      message: `Se cargó el destino: ${item.destination}`,
      okButtonText: 'Aceptar'
    });
  }

  public async openAccountCard(index: number): Promise<void> {
    const item = this.accountCards.getItem(index);
    if (!item) return;

    await Dialogs.alert({
      title: item.title,
      message: item.subtitle || 'Información de tu cuenta.',
      okButtonText: 'Aceptar'
    });
  }
}

import {ErrorHandler, NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {IonicApp, IonicErrorHandler, IonicModule} from 'ionic-angular';
import {MyApp} from './app.component';

import {SplashScreen} from '@ionic-native/splash-screen';
import {StatusBar} from '@ionic-native/status-bar';
import {Firebase} from "@ionic-native/firebase";

import {NotificationsPage} from './pages/notifications/notifications';
import {TabsPage} from './pages/tabs/tabs';
import {HistoryPage} from "./pages/history/history";
import {AngularFireDatabaseModule} from "angularfire2/database";
import {AngularFireModule} from "angularfire2";
import {ReversePipe} from "./pipes/reverse.pipe";
import {NotificationService} from "./services/notification.service";
import {LocalNotifications} from "@ionic-native/local-notifications";
import {TimeAgoPipe} from "./pipes/time-ago.pipe";
import {NotificationPage} from "./pages/notifications/notification/notification";

const firebaseConfig = {
  apiKey: "AIzaSyC0_u2JxUEPINa2A2KWfbuE-NXzqXNBiGM",
  authDomain: "augur-bia.firebaseapp.com",
  databaseURL: "https://augur-bia.firebaseio.com",
  projectId: "augur-bia",
  storageBucket: "augur-bia.appspot.com",
  messagingSenderId: "1011740616784"
};

@NgModule({
  declarations: [
    MyApp,
    HistoryPage,
    NotificationsPage,
    TabsPage,
    NotificationPage,
    ReversePipe,
    TimeAgoPipe
  ],
  imports: [
    BrowserModule,
    IonicModule.forRoot(MyApp),
    AngularFireModule.initializeApp(firebaseConfig),
    AngularFireDatabaseModule
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    MyApp,
    HistoryPage,
    NotificationsPage,
    NotificationPage,
    TabsPage
  ],
  providers: [
    StatusBar,
    SplashScreen,
    LocalNotifications,
    Firebase,
    NotificationService,
    {provide: ErrorHandler, useClass: IonicErrorHandler}
  ]
})
export class AppModule {
}

import {Component, OnInit} from '@angular/core';
import {App, Platform, ToastController} from 'ionic-angular';
import {Observable} from "rxjs/Observable";
import {NotificationService} from "../../services/notification.service";
import {LocalNotifications} from "@ionic-native/local-notifications";
import {IntervalObservable} from "rxjs/observable/IntervalObservable";
import {NotificationPage} from "./notification/notification";
import {Notification} from "../../models/notification";

@Component({
  selector: 'page-home',
  templateUrl: 'notifications.html'
})
export class NotificationsPage implements OnInit {

  notifications: Observable<Notification[]>;
  currentTime: number;

  constructor(private _appCtrl: App,
              private toastCtrl: ToastController,
              private _platform: Platform,
              private _notificationService: NotificationService,
              private _localNotifications: LocalNotifications) {

  }

  ngOnInit() {
    this.notifications = this._notificationService.getNotificationsObservable();
    this.notifications.subscribe(val => {

      // todo Check local notifications support
      // Schedule a single notification
      this._localNotifications.schedule({
        id: 1,
        text: 'Single ILocalNotification',
        sound: this._platform.is("android") ? 'file://sound.mp3' : 'file://beep.caf',
        data: {secret: "val"}
      });

      this.currentTime = Date.now();
      IntervalObservable.create(30000)
        .subscribe(value => {
          this.currentTime = Date.now();
        });
    });
  }

  public removeNotification(notification: Notification): void {
    console.log("Removing notification", notification);
    this._notificationService.removeNotification(notification)
      .then(() => {
        let toast = this.toastCtrl.create({
          message: 'Notification marked as read',
          duration: 3000
        });
        toast.present();
      });
  }

  public viewNotification(notification: Notification): void {
    this._appCtrl.getRootNav().push(NotificationPage, {notification: notification});
  }
}

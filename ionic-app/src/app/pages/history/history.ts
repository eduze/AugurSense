import {App, NavController} from "ionic-angular";
import {Component, OnInit} from "@angular/core";
import {NotificationService} from "../../services/notification.service";
import {NotificationPage} from "../notifications/notification/notification";
import {Notification} from "../../models/notification";
import {IntervalObservable} from "rxjs/observable/IntervalObservable";

@Component({
  selector: 'page-history',
  templateUrl: 'history.html'
})
export class HistoryPage implements OnInit {

  oldNotifications: Notification[];
  currentTime: number;

  constructor(private _navCtrl: NavController,
              private _appCtrl: App,
              private _notificationService: NotificationService) {
  }

  public ngOnInit() {
    this._notificationService.getOldNotifications()
      .subscribe(value => {
        this.oldNotifications = [];
        for (const n of value) {
          this.oldNotifications.push(Notification.fromJSON(null, n));
        }
      });

    IntervalObservable.create(30000)
      .subscribe(value => {
        this.currentTime = Date.now();
      });
  }

  public viewNotification(notification: Notification): void {
    this._appCtrl.getRootNav().push(NotificationPage, {notification: notification});
  }
}

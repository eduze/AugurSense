import {Injectable} from "@angular/core";
import {AngularFireDatabase} from "angularfire2/database";
import {Observable} from "rxjs/Observable";
import {Notification} from "../models/notification";

@Injectable()
export class NotificationService {

  private static readonly collection: string = '/notifications';
  private static readonly timestamp: string = 'timestamp';
  private static readonly seen: string = 'seen';

  notifications: Notification[] = [];

  constructor(private _database: AngularFireDatabase) {
    this._database
      .list(NotificationService.collection,
        ref => ref.orderByChild(NotificationService.seen)
          .equalTo(null)
          .limitToLast(50))
      .snapshotChanges()
      .subscribe(data => {
        console.log("Updating notifications");

        this.notifications.length = 0;
        for (let item of data) {
          let stats = Notification.fromJSON(item.key, item.payload.val());
          this.notifications.push(stats);
        }
      });
  }

  public getNotificationsObservable(): Observable<Notification[]> {
    return Observable.of(this.notifications);
  }

  public removeNotification(stats: Notification): Promise<void> {
    let data = stats.toJSON();
    data[NotificationService.seen] = true;
    console.log("Removing stat", data);
    return this._database
      .list(NotificationService.collection)
      .update(stats.id, data);
  }

  public getOldNotifications(): Observable<any[]> {
    return this._database
      .list(NotificationService.collection,
        ref => ref.orderByChild(NotificationService.seen).equalTo(true))
      .valueChanges();
  }
}

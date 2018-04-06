import {NavController, NavParams} from "ionic-angular";
import {Component, OnInit} from "@angular/core";
import {Notification} from "../../../models/notification";

@Component({
  selector: 'page-notification',
  templateUrl: 'notification.html'
})
export class NotificationPage implements OnInit {

  private _notification: Notification;

  constructor(private _navCtrl: NavController, private _navParams: NavParams) {
    this.notification = _navParams.get("notification");
  }

  ngOnInit() {
  }

  get notification(): Notification {
    return this._notification;
  }

  set notification(value: Notification) {
    this._notification = value;
  }
}

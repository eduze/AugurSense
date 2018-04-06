import {Component} from '@angular/core';

import {NotificationsPage} from '../notifications/notifications';
import {HistoryPage} from "../history/history";

@Component({
  templateUrl: 'tabs.html'
})
export class TabsPage {

  tab1Root = NotificationsPage;
  tab2Root = HistoryPage;

  constructor() {

  }
}

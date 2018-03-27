import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {AnalyticsService} from '../services/analytics.service';
import {ConfigService} from '../services/config.service';
import {PersonSnapshot} from '../resources/person-snapshot';
import {Observable} from 'rxjs/Rx';
import {PersonImage} from '../resources/person-image';
import {CameraGroup} from '../resources/camera-group';
import {Subscription} from "rxjs/Subscription";

@Component({
  selector: 'app-realtime-map',
  templateUrl: './realtime-map.component.html',
  styleUrls: ['./realtime-map.component.css']
})
export class RealtimeMapComponent implements OnInit, OnDestroy {

  @Input() cameraGroup: CameraGroup;
  showAllInInfo = true;
  personSnapshots: PersonSnapshot[][] = [];
  selectedTrackIndex = -1;

  private timer;
  private subscription: Subscription;

  constructor(private analyticsService: AnalyticsService, private configService: ConfigService) {
    this.timer = Observable.timer(2000, 5000);
  }

  private static getColour(index: number): string {
    return 'rgb(' + Math.round(((index / 256 / 256) * 40) % 256).toString() +
      ',' + Math.round(((index / 256) * 40) % 256).toString() + ',' + Math.round((index * 40) % 256).toString() + ')';
  }

  private static getStandSitColour(person: PersonSnapshot): string {
    const standCol = Math.round(person.standProbability * 255);
    const sitCol = Math.round(person.sitProbability * 255);

    return 'rgb(' + standCol.toString() + ', ' + sitCol.toString() + ',255 )';
  }

  ngOnInit() {
    this.subscription = this.timer.subscribe(x => this.getPersonSnapshots());
  }

  personClicked(person: PersonImage): void {
    if (person.ids.length > 0) {
      this.selectedTrackIndex = person.ids[0];
      this.showAllInInfo = false;
    }
    console.log('Person clicked' + person.ids[0].toString());
  }

  private backgroundClicked(): void {
    this.selectedTrackIndex = -1;
    this.showAllInInfo = true;
  }

  private trackClicked(track: PersonSnapshot[]): void {
    if (track[0].ids.length > 0) {
      this.selectedTrackIndex = track[0].ids[0];
      this.showAllInInfo = false;
    }
    console.log('Track clicked' + track[0].ids[0].toString());
    console.log(track);
  }

  private getPersonSnapshots(): void {
    console.log('Sending real time map request');

    this.analyticsService.getRealTimeMap(this.cameraGroup.id)
      .then(ps => {
        ps.forEach((item) => {
          item[0]['colour'] = RealtimeMapComponent.getColour(item[0].ids[0]);
          item[0]['standSitColour'] = RealtimeMapComponent.getStandSitColour(item[0]);
        });

        this.personSnapshots = ps;
      })
      .catch(reason => console.log(reason));
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}

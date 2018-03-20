import {Component, OnInit} from '@angular/core';
import {AnalyticsService} from '../services/analytics.service';
import {ConfigService} from '../services/config.service';
import {PersonSnapshot} from '../resources/person-snapshot';
import {Observable} from 'rxjs/Rx';
import {PersonImage} from '../resources/person-image';
import {CameraGroup} from '../resources/camera-group';

@Component({
  selector: 'app-realtime-map',
  templateUrl: './realtime-map.component.html',
  styleUrls: ['./realtime-map.component.css']
})
export class RealtimeMapComponent implements OnInit {

  showAllInInfo = true;

  data: { cameraGroup: CameraGroup, personSnapshots: PersonSnapshot[][] }[] = [];
  selectedTrackIndex = -1;

  constructor(private analyticsService: AnalyticsService, private configService: ConfigService) {
  }

  private getColour(index: number): string {
    return 'rgb(' + Math.round(((index / 256 / 256) * 40) % 256).toString() + ',' + Math.round(((index / 256) * 40) % 256).toString() + ',' + Math.round((index * 40) % 256).toString() + ')';
  }

  private getStandSitColour(person: PersonSnapshot): string {
    const standCol = Math.round(person.standProbability * 255);
    const sitCol = Math.round(person.sitProbability * 255);

    return 'rgb(' + standCol.toString() + ', ' + sitCol.toString() + ',255 )';
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

  private startMap(): void {
    Observable.interval(5000).subscribe(x => {
      console.log('Sending real time map request');

      this.data.forEach(d => {
        this.analyticsService.getRealTimeMap(d.cameraGroup.id)
          .then(ps => {
            ps.forEach((item) => {
              item[0]['colour'] = this.getColour(item[0].ids[0]);
              item[0]['standSitColour'] = this.getStandSitColour(item[0]);
            });

            d.personSnapshots = ps;
          })
          .catch(reason => console.log(reason));
      });
    });
  }


  ngOnInit() {
    this.configService.getCameraGroups().then(groups => {
      this.data = [];
      groups.forEach(group => {
        this.data.push({
          cameraGroup: group,
          personSnapshots: []
        });
      });
    });

    this.startMap();
  }

}

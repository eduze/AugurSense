import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {ConfigService} from '../../services/config.service';
import {Observable} from 'rxjs/Observable';
import {Zone} from '../../resources/zone';
import {CameraGroup} from '../../resources/camera-group';
import {ZoneStatistic} from '../../resources/zone-statistic';
import {AnalyticsService} from '../../services/analytics.service';
import {Subscription} from "rxjs/Subscription";

@Component({
  selector: 'app-zone',
  templateUrl: './zone.component.html'
})
export class ZoneComponent implements OnInit, OnDestroy {

  zones: Zone[] = [];
  @Input() cameraGroup: CameraGroup;

  polygons: any[] = [];

  zoneStatistics: ZoneStatistic[] = [];
  selectedZoneIndex = -1;
  totalPeople: number;

  private _fromDate: Date = new Date(0);
  private _toDate: Date = new Date();

  private timer;
  private subscription: Subscription;

  constructor(private analyticsService: AnalyticsService, private configService: ConfigService) {
    this.timer = Observable.timer(2000, 60000);
  }

  ngOnInit() {
    this.configService.getZonesOf(this.cameraGroup)
      .then((zones) => {
        this.zones = zones;
        for (const x in this.zones) {
          const poly: { zone: Zone, zoneStatistic: ZoneStatistic, outgoingMap: any, incomingMap: any } = {
            zone: this.zones[x],
            zoneStatistic: null,
            outgoingMap: [],
            incomingMap: []
          };

          this.polygons.push(poly);

          this.zones.forEach((v) => {
            if (v.id === 0) {
              // Rest of the world zone found
              this.selectedZoneIndex = this.zones.indexOf(v);
            }
          });
        }
      });

    this.subscription = this.timer.subscribe(x => {
      this.fetchResults();
    });
  }

  private zoneClicked(index: number): void {
    this.selectedZoneIndex = index;
  }

  private backgroundClicked() {
    this.selectedZoneIndex = -1;
    console.log('Unselected');

    this.zones.forEach((v) => {
      if (v.id === 0) {
        // Rest of the world zone found
        this.selectedZoneIndex = this.zones.indexOf(v);
      }
    });

  }

  private fetchResults(): void {
    if (this.fromDate == null) {
      return;
    }
    if (this.toDate == null) {
      return;
    }

    console.log("Fetching zone stats");

    this.analyticsService.getZoneStatistics(this.fromDate.getTime(), this.toDate.getTime()).then((zs) => {
      this.zoneStatistics = zs;

      if (this.zoneStatistics.length > 0) {
        this.totalPeople = this.zoneStatistics.map((item) => item.averagePersonCount)
          .reduce((r1, r2) => r1 + r2);
      } else {
        this.totalPeople = 0;
      }

      this.polygons.map((item) => {
        item.outgoingMap = [];
        item.incomingMap = [];

        const matches: ZoneStatistic[] = zs.filter((match) => {
          return item.zone.id == match.zoneId;
        });

        if (matches.length > 0) {
          item.zoneStatistic = matches[0];

          for (const key in item.zoneStatistic.outgoingMap) {
            if (item.zoneStatistic.outgoingMap.hasOwnProperty(key)) {
              // find matching polygon
              const matching_polys = this.polygons.filter((mat_poly) => {
                return key == mat_poly.zone.id;
              });

              if (matching_polys.length > 0) {
                const matching_poly = matching_polys[0];
                const transmission = {
                  midX: matching_poly.zone.midX,
                  midY: matching_poly.zone.midY,
                  count: item.zoneStatistic.outgoingMap[key] / item.zoneStatistic.totalOutgoing
                };
                item.outgoingMap.push(transmission);
              }
            }
          }

          for (const key in item.zoneStatistic.incomingMap) {
            if (item.zoneStatistic.incomingMap.hasOwnProperty(key)) {
              // find matching polygon
              const matching_polys = this.polygons.filter((mat_poly) => {
                return key == mat_poly.zone.id;
              });
              if (matching_polys.length > 0) {
                const matching_poly = matching_polys[0];
                const transmission = {
                  midX: matching_poly.zone.midX,
                  midY: matching_poly.zone.midY,
                  count: item.zoneStatistic.incomingMap[key] / item.zoneStatistic.totalIncoming
                };
                item.incomingMap.push(transmission);
              }
            }
          }
        }
      });
      // this.selectedZoneStatistic = zs[0];
    });
  }

  get fromDate(): Date {
    return this._fromDate;
  }

  set fromDate(value: Date) {
    this._fromDate = value;
    this.fetchResults();
  }

  get toDate(): Date {
    return this._toDate;
  }

  set toDate(value: Date) {
    this._toDate = value;
    this.fetchResults();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}

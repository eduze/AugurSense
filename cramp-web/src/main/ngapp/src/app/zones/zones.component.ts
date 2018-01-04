import {Component, OnInit} from '@angular/core';
import {AnalyticsService} from "../services/analytics.service";
import {Zone} from "../resources/zone";
import {ConfigService} from "../services/config.service";
import {GlobalMap} from "../resources/global-map";
import {ZoneStatistic} from "../resources/zone-statistic";
import {Observable} from "rxjs/Observable";

@Component({
  selector: 'app-zones',
  templateUrl: './zones.component.html',
  styleUrls: ['./zones.component.css']
})

export class ZonesComponent implements OnInit {

  zones: Zone[] = [];
  globalMap: GlobalMap;

  polygons: any[] = [];

  zoneStatistics: ZoneStatistic[] = [];
  selectedZoneIndex: number = -1;
  totalPeople: number;

  private _fromDate: Date = new Date(0);
  private _toDate: Date = new Date();

  constructor(private analyticsService: AnalyticsService, private configService: ConfigService) {
  }

  private zoneClicked(index: number): void {
    this.selectedZoneIndex = index;
  }

  private backgroundClicked() {
    this.selectedZoneIndex = -1;
    console.log("Unselected");

    this.zones.forEach((v) => {
      if (v.id == 0) {
        //Rest of the world zone found
        this.selectedZoneIndex = this.zones.indexOf(v);
      }
    });

  }

  private fetchResults(): void {
    if (this.fromDate == null)
      return;
    if (this.toDate == null)
      return;

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

        let matches: ZoneStatistic[] = zs.filter((match) => {
          return item.zone.id == match.zoneId
        });

        if (matches.length > 0) {
          item.zoneStatistic = matches[0];

          for (let key in item.zoneStatistic.outgoingMap) {
            if (item.zoneStatistic.outgoingMap.hasOwnProperty(key)) {
              // find matching polygon
              let matching_polys = this.polygons.filter((mat_poly) => {
                return key == mat_poly.zone.id;
              });

              if (matching_polys.length > 0) {
                let matching_poly = matching_polys[0];
                let transmission = {
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
              let matching_polys = this.polygons.filter((mat_poly) => {
                return key == mat_poly.zone.id;
              });
              if (matching_polys.length > 0) {
                let matching_poly = matching_polys[0];
                let transmission = {
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
      //this.selectedZoneStatistic = zs[0];
    });
  }

  ngOnInit() {
    this.configService.getZones()
      .then((zones) => {
        this.zones = zones;
        for (let x in this.zones) {
          let poly: { zone: Zone, zoneStatistic: ZoneStatistic, outgoingMap: any, incomingMap: any } = {
            zone: this.zones[x],
            zoneStatistic: null,
            outgoingMap: [],
            incomingMap: []
          };

          this.polygons.push(poly);

          this.zones.forEach((v) => {
            if (v.id == 0) {
              //Rest of the world zone found
              this.selectedZoneIndex = this.zones.indexOf(v);
            }
          });
        }
      });

    this.configService.getMap()
      .then((globalMap) => {
        this.globalMap = globalMap;
      });

    Observable.interval(2000).subscribe(x => {
      this.fetchResults();
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
}

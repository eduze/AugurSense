import {Component, OnInit} from '@angular/core';
import {AnalyticsService} from "../services/analytics.service";
import {Zone} from "../resources/zone";
import {ConfigService} from "../services/config.service";
import {GlobalMap} from "../resources/global-map";

@Component({
  selector: 'app-zones',
  templateUrl: './zones.component.html',
  styleUrls: ['./zones.component.css']
})

export class ZonesComponent implements OnInit {

  zones: Zone[] = [];
  globalMap: GlobalMap;
  polygons: string[] = [];

  constructor(private analyticsService: AnalyticsService, private configService: ConfigService) {
  }

  ngOnInit() {
    this.configService.getZones().then((zones) => {
      this.zones = zones;
      for (let zone of zones) {
        let p = "";
        for (let i in zone.xCoordinates) {
          p += zone.xCoordinates[i] + "," + zone.yCoordinates[i] + " ";
        }
        this.polygons.push(p);
      }
    });

    this.configService.getMap().then((globalMap) => {
      this.globalMap = globalMap;
      console.log(this.globalMap);
    });
  }
}

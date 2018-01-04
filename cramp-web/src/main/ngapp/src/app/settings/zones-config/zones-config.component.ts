import {Component, OnInit} from '@angular/core';
import {ConfigService} from "../../services/config.service";
import {Zone} from "../../resources/zone";
import {GlobalMap} from "../../resources/global-map";

@Component({
  selector: 'app-zones-config',
  templateUrl: './zones-config.component.html',
  styleUrls: ['./zones-config.component.css']
})
export class ZonesConfigComponent implements OnInit {

  private _zones: Zone[] = [];
  private _zone: Zone;
  private _globalMap: GlobalMap;

  constructor(private configService: ConfigService) {
  }

  ngOnInit() {
    this.configService.getMap()
      .then(map => {
        this._globalMap = map;
      });

    this.configService.getZones()
      .then(zones => {
        this._zones = zones;
      });
  }

  public zoneClicked(zone: Zone): void {
    this.zone = zone;
  }

  get zones(): Zone[] {
    return this._zones;
  }

  get zone(): Zone {
    return this._zone;
  }

  set zone(value: Zone) {
    this._zone = value;
  }

  get globalMap(): GlobalMap {
    return this._globalMap;
  }
}

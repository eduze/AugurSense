import {Component, Input, OnInit} from '@angular/core';
import {Zone} from "../../../resources/zone";
import {GlobalMap} from "../../../resources/global-map";

@Component({
  selector: 'app-zone-ui',
  templateUrl: './zone-ui.component.html',
  styleUrls: ['./zone-ui.component.css']
})
export class ZoneUiComponent implements OnInit {

  private _zones: Zone[] = [];
  private _globalMap: GlobalMap;
  zone: Zone;

  constructor() {
    this.zone = new Zone(0, null, [], []);
  }

  ngOnInit() {
  }

  public clicked(event: MouseEvent) {
    this.zone.xCoordinates.push(event.offsetX);
    this.zone.xCoordinates = this.zone.xCoordinates;
    this.zone.yCoordinates.push(event.offsetY);
    this.zone.yCoordinates = this.zone.yCoordinates;
  }

  public clear() {
    this.zone.xCoordinates = [];
    this.zone.yCoordinates = [];
  }

  get zones(): Zone[] {
    return this._zones;
  }

  @Input() set zones(value: Zone[]) {
    this._zones = value;
  }

  get globalMap(): GlobalMap {
    return this._globalMap;
  }

  @Input() set globalMap(value: GlobalMap) {
    this._globalMap = value;
  }
}

import {Component, Input, OnInit} from '@angular/core';
import {ConfigService} from '../../services/config.service';
import {Zone} from '../../resources/zone';
import {CameraGroup} from '../../resources/camera-group';

@Component({
  selector: 'app-zones-config',
  templateUrl: './zones-config.component.html',
  styleUrls: ['./zones-config.component.css']
})
export class ZonesConfigComponent implements OnInit {

  private _zones: Zone[] = [];
  private _zone: Zone;
  @Input() cameraGroup: CameraGroup;
  modalId;

  constructor(private configService: ConfigService) {
  }

  ngOnInit() {
    this.configService.getZonesOf(this.cameraGroup)
      .then(zones => {
        this._zones = zones;
      });

    this.modalId = `modal-${this.cameraGroup.id}`;
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
}

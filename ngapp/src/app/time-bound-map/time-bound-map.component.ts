import {Component, OnInit} from '@angular/core';
import {ConfigService} from '../services/config.service';
import {CameraGroup} from "../resources/camera-group";

@Component({
  selector: 'app-time-bound-map',
  templateUrl: './time-bound-map.component.html'
})
export class TimeBoundMapComponent implements OnInit {

  cameraGroups: CameraGroup[];
  active: CameraGroup;

  constructor(private configService: ConfigService) {
  }

  ngOnInit() {
    this.configService.getCameraGroups()
      .then(groups => {
        this.cameraGroups = groups;
        this.active = groups[0];
      });
  }
}

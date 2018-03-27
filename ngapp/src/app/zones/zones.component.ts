import {Component, OnInit} from '@angular/core';
import {AnalyticsService} from '../services/analytics.service';
import {ConfigService} from '../services/config.service';
import {CameraGroup} from "../resources/camera-group";

@Component({
  selector: 'app-zones',
  templateUrl: './zones.component.html',
  styleUrls: ['./zones.component.css']
})

export class ZonesComponent implements OnInit {

  cameraGroups: CameraGroup[] = [];
  active: CameraGroup;

  constructor(private analyticsService: AnalyticsService, private configService: ConfigService) {
  }

  ngOnInit() {
    this.configService.getCameraGroups()
      .then(groups => {
        this.cameraGroups = groups;
        this.active = groups[0];
      });
  }
}

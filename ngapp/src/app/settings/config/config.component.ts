import {Component, OnInit} from '@angular/core';
import {ConfigService} from "../../services/config.service";
import {CameraGroup} from "../../resources/camera-group";

@Component({
  selector: 'app-config',
  templateUrl: './config.component.html',
  styleUrls: ['./config.component.css']
})
export class ConfigComponent implements OnInit {

  cameraGroups: CameraGroup[] = [];

  constructor(private configService: ConfigService) {
  }

  ngOnInit() {
    this.configService.getCameraGroups().then(groups => {
      this.cameraGroups = groups;
    });
  }

}

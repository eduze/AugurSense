/*
 * Copyright 2017 Eduze
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

import {Component, OnInit} from '@angular/core';
import {ConfigService} from "../../services/config.service";
import {CameraConfig} from "../../resources/camera-config";
import {GlobalMap} from "../../resources/global-map";
import {Point} from "../../resources/point";
import {Message} from "../../lib/message";

@Component({
  selector: 'app-point-mapping',
  templateUrl: './point-mapping.component.html',
  styleUrls: ['./point-mapping.component.css']
})
export class PointMappingComponent implements OnInit {

  cameraConfigs: CameraConfig[];
  message: Message;
  globalMap: GlobalMap;

  constructor(private configService: ConfigService) {
  }

  ngOnInit(): void {
    this.configService.getCameraConfigs()
      .then(configs => {
        this.cameraConfigs = configs;
        console.log(configs);
      })
      .catch(reason => console.error(reason));

    this.configService.getMap()
      .then(map => {
        this.globalMap = map;
      });
  }

  public mapClicked(event: MouseEvent, config: CameraConfig) {
    if (config.pointMapping.worldSpacePoints.length < 4) {
      config.pointMapping.worldSpacePoints.push(new Point(event.offsetX, event.offsetY));
    }
  }

  public viewClicked(event: MouseEvent, config: CameraConfig) {
    if (config.pointMapping.screenSpacePoints.length < 4) {
      config.pointMapping.screenSpacePoints.push(new Point(event.offsetX, event.offsetY));
    }
  }

  public save(config: CameraConfig) {
    if (config.pointMapping.worldSpacePoints.length != 4 || config.pointMapping.screenSpacePoints.length != 4) {
      this.message = new Message("You should select 4 points from each map", Message.ERROR);
      return;
    }

    this.configService.addCameraConfig(config)
      .then(success => {
        this.message = new Message("Point mapping added successfully", Message.SUCCESS);
      }).catch(reason => {
      this.message = new Message("Point mapping couldn't be added", Message.ERROR);
    })
  }

  public clear(config: CameraConfig) {
    config.pointMapping.worldSpacePoints = [];
    config.pointMapping.screenSpacePoints = [];
  }
}

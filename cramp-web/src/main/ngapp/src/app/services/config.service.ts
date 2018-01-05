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

import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http'
import 'rxjs/add/operator/toPromise';
import {Zone} from "../resources/zone";
import {GlobalMap} from "../resources/global-map";
import {CameraConfig} from "../resources/camera-config";
import {PointMapping} from "../resources/point-mapping";
import {Point} from "../resources/point";
import {CameraView} from "../resources/camera-view";

@Injectable()
export class ConfigService {

  private baseUrl: string = "http://localhost:8000/api/v1/config/";

  constructor(private http: HttpClient) {
  }

  getCameraConfigs(): Promise<CameraConfig[]> {
    return this.http.get(this.baseUrl + "cameraConfigs")
      .toPromise()
      .then(response => {
        console.log(response);
        let configs: Map<number, CameraConfig> = response as Map<number, CameraConfig>;
        let cameraConfigs: CameraConfig[] = [];
        for (let key in configs) {
          let cameraConfig: CameraConfig = ConfigService.toCameraConfig(configs[key]);
          cameraConfigs.push(cameraConfig);
        }
        return cameraConfigs;
      })
      .catch(ConfigService.handleError);
  }

  addCameraConfig(cameraConfig: CameraConfig): Promise<boolean> {
    return this.http.post(this.baseUrl + "cameraConfig", cameraConfig)
      .toPromise()
      .then(response => {
        return true;
      }).catch(ConfigService.handleError)
  }

  getMap(): Promise<GlobalMap> {
    return this.http.get(this.baseUrl + "getMap")
      .toPromise()
      .then(response => {
        console.log(response);
        let views: Map<string, string> = response as Map<string, string>;
        let base64: string = "data:image/JPEG;base64," + views["mapImage"];

        return new GlobalMap(base64);
      })
      .catch(ConfigService.handleError);
  }

  addZone(zone: Zone): Promise<Zone> {
    return this.http.post(this.baseUrl + "zone", zone)
      .toPromise()
      .then(response => {
        let z = response as Zone;
        return new Zone(z.id, z.zoneName, z.xCoordinates, z.yCoordinates, z.zoneLimit);
      }).catch(ConfigService.handleError)
  }

  updateZone(zone: Zone): Promise<boolean> {
    return this.http.put(this.baseUrl + "zone", zone)
      .toPromise()
      .then(response => {
        return true;
      }).catch(ConfigService.handleError)
  }

  deleteZone(zoneId: number): Promise<boolean> {
    return this.http.delete(this.baseUrl + "zone/" + zoneId)
      .toPromise()
      .then(response => {
        return true;
      }).catch(ConfigService.handleError)
  }

  getZones(): Promise<Zone[]> {
    return this.http.get<Zone[]>(this.baseUrl + "zones")
      .toPromise()
      .then(response => {
        let arr = response as Zone[];
        let zones: Zone[] = [];
        for (let z of arr) {
          zones.push(new Zone(z.id, z.zoneName, z.xCoordinates, z.yCoordinates, z.zoneLimit));
        }

        return zones;
      })
      .catch(ConfigService.handleError);
  }

  private static toCameraConfig(config: any): CameraConfig {
    let cameraId = config.cameraId;
    let view = "data:image/JPEG;base64," + config.view;
    let ipPort = config.ipAndPort;
    let cameraConfig = new CameraConfig(cameraId, ipPort, new PointMapping(), new CameraView(view));

    for (let i in config.pointMapping.screenSpacePoints) {
      let screenSpacePoint = new Point(config.pointMapping.screenSpacePoints[i].x, config.pointMapping.screenSpacePoints[i].y);
      cameraConfig.pointMapping.screenSpacePoints.push(screenSpacePoint);
      let worldSpacePoint = new Point(config.pointMapping.worldSpacePoints[i].x, config.pointMapping.worldSpacePoints[i].y);
      cameraConfig.pointMapping.worldSpacePoints.push(worldSpacePoint);
    }
    return cameraConfig;
  }

  private static stringToNumberArray(str: string): number[] {
    let arr: number[] = [];
    for (let s of str.split(",")) {
      arr.push(parseInt(s));
    }
    return arr;
  }

  private static handleError(error: any): Promise<any> {
    console.error('An error occurred', error);
    return Promise.reject(error.message || error);
  }

}

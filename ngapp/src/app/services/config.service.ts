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
import {HttpClient} from '@angular/common/http';
import 'rxjs/add/operator/toPromise';
import {Zone} from '../resources/zone';
import {GlobalMap} from '../resources/global-map';
import {CameraConfig} from '../resources/camera-config';
import {CameraGroup} from "../resources/camera-group";

@Injectable()
export class ConfigService {

  private baseUrl = 'http://localhost:8000/api/v1/config/';

  constructor(private http: HttpClient) {
  }

  private static stringToNumberArray(str: string): number[] {
    const arr: number[] = [];
    for (const s of str.split(',')) {
      arr.push(parseInt(s));
    }
    return arr;
  }

  private static handleError(error: any): Promise<any> {
    console.error('An error occurred', error);
    return Promise.reject(error.message || error);
  }

  getCameraConfigs(): Promise<CameraConfig[]> {
    return this.http.get(this.baseUrl + 'cameraConfigs')
      .toPromise()
      .then(response => {
        const configs: Map<number, CameraConfig> = response as Map<number, CameraConfig>;
        const cameraConfigs: CameraConfig[] = [];
        for (const key in configs) {
          const cameraConfig: CameraConfig = CameraConfig.fromJSON(configs[key]);
          cameraConfigs.push(cameraConfig);
        }
        return cameraConfigs;
      })
      .catch(ConfigService.handleError);
  }

  addCameraConfig(cameraConfig: CameraConfig): Promise<boolean> {
    return this.http.post(this.baseUrl + 'cameraConfig', cameraConfig)
      .toPromise()
      .then(response => {
        return true;
      }).catch(ConfigService.handleError);
  }

  getCameraGroups(): Promise<CameraGroup[]> {
    return this.http.get(this.baseUrl + 'cameraGroups')
      .toPromise()
      .then(response => {
        const groups = response as CameraGroup[];
        console.log(groups);
        const cameraGroups = [];
        for (let g of groups) {
          cameraGroups.push(CameraGroup.fromJSON(g));
        }
        return cameraGroups;
      })
      .catch(ConfigService.handleError);
  }

  addCameraGroup(cameraGroup: CameraGroup): Promise<boolean> {
    return this.http.post(this.baseUrl + 'cameraGroups', cameraGroup)
      .toPromise()
      .then(response => {
        console.log(response);
        return true;
      })
      .catch(ConfigService.handleError);
  }

  getMap(): Promise<GlobalMap> {
    return this.http.get(this.baseUrl + 'getMap')
      .toPromise()
      .then(response => {
        console.log(response);
        const views: Map<string, string> = response as Map<string, string>;
        return GlobalMap.fromJSON(views['mapImage']);
      })
      .catch(ConfigService.handleError);
  }

  addZone(zone: Zone): Promise<Zone> {
    return this.http.post(this.baseUrl + 'zone', zone)
      .toPromise()
      .then(response => {
        const z = response as Zone;
        return new Zone(z.id, z.zoneName, z.xCoordinates, z.yCoordinates, z.zoneLimit);
      }).catch(ConfigService.handleError);
  }

  updateZone(zone: Zone): Promise<boolean> {
    return this.http.put(this.baseUrl + 'zone', zone)
      .toPromise()
      .then(response => {
        return true;
      }).catch(ConfigService.handleError);
  }

  deleteZone(zoneId: number): Promise<boolean> {
    return this.http.delete(this.baseUrl + 'zone/' + zoneId)
      .toPromise()
      .then(response => {
        return true;
      }).catch(ConfigService.handleError);
  }

  getZones(): Promise<Zone[]> {
    return this.http.get<Zone[]>(this.baseUrl + 'zones')
      .toPromise()
      .then(response => {
        const arr = response as Zone[];
        const zones: Zone[] = [];
        for (const z of arr) {
          zones.push(new Zone(z.id, z.zoneName, z.xCoordinates, z.yCoordinates, z.zoneLimit));
        }

        return zones;
      })
      .catch(ConfigService.handleError);
  }
}

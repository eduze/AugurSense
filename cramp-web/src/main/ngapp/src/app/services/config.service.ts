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
import {Http} from '@angular/http'
import 'rxjs/add/operator/toPromise';
import {Zone} from "../resources/zone";
import {GlobalMap} from "../resources/global-map";

@Injectable()
export class ConfigService {

  private baseUrl: string = "http://localhost:8085/api/v1/config/";

  constructor(private http: Http) {
  }

  getCameraViews(): Promise<Map<string, string>> {
    return this.http.get(this.baseUrl + "views")
      .toPromise()
      .then(response => {
        let views: Map<string, string> = response.json() as Map<string, string>;
        for (let key in views) {
          views[key] = "data:image/JPEG;base64," + views[key];
        }
        return views;
      })
      .catch(ConfigService.handleError);
  }

  getMap(): Promise<GlobalMap> {
    return this.http.get(this.baseUrl + "getMap")
      .toPromise()
      .then(response => {
        console.log(response.json());
        let views: Map<string, string> = response.json() as Map<string, string>;
        let base64: string = "data:image/JPEG;base64," + views["mapImage"];

        return new GlobalMap(base64);
      })
      .catch(ConfigService.handleError);
  }

  getZones(): Promise<Zone[]> {
    return this.http.get(this.baseUrl + "zones")
      .toPromise()
      .then(response => {
        let arr = response.json();
        let zones: Zone[] = [];
        for (let i of arr) {
          zones.push(new Zone(i.id, i.zoneName, i.xcoordinates, i.ycoordinates));
        }
        return zones;
      })
      .catch(ConfigService.handleError);
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

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

import {PersonSnapshot} from '../resources/person-snapshot';
import {ZoneStatistic} from '../resources/zone-statistic';
import {PersonImage} from '../resources/person-image';
import {ReIdStatus} from '../resources/re-id-status';
import {TimelineZone} from '../resources/timeline-zone';
import {TimelineTrack} from '../resources/timeline-track';
import {PointDirections} from '../resources/point-directions';

@Injectable()
export class AnalyticsService {

  private baseUrl = 'http://localhost:8000/api/v1/analytics/';

  constructor(private http: HttpClient) {
  }

  getProfile(uuid: String): Promise<PersonImage> {
    return this.http.get(this.baseUrl + 'profile/' + uuid)
      .toPromise()
      .then(response => {

        const personImage = response;

        const base64: string = 'data:image/JPEG;base64,' + personImage['image'];

        const obj = personImage;
        const img = new Image();
        img.onload = function () {
          obj['height'] = img.height;
          obj['width'] = img.width;
          console.log(obj);
        };
        img.src = base64;
        obj['image'] = base64;

        return personImage;
      })
      .catch(AnalyticsService.handleError);
  }

  getRealTimeMap(cameraGroupId: number): Promise<PersonSnapshot[][]> {
    return this.http.get(`${this.baseUrl}realTimeMap/${cameraGroupId}`)
      .toPromise()
      .then(response => {
        return response as PersonSnapshot[][];
      })
      .catch(AnalyticsService.handleError);
  }

  invokeReId(uuid: String, from: number, to: number, useTrackSegments: boolean): Promise<boolean> {
    let url = this.baseUrl + 're_id/invoke/' + from + '/' + to + '/' + uuid;
    if (useTrackSegments) {
      url = url + '/segmented';
    }
    return this.http.get(url)
      .toPromise()
      .then(response => {
        console.log(response);
        return response;
      })
      .catch(AnalyticsService.handleError);
  }

  getReIdResults(uuid: String, from: number, to: number, useTrackSegments: boolean): Promise<ReIdStatus> {
    let url = this.baseUrl + 're_id/results/' + from + '/' + to + '/' + uuid;
    if (useTrackSegments) {
      url = url + '/segmented';
    }
    return this.http.get(url)
      .toPromise()
      .then(response => {
        console.log(response);

        const status = response as ReIdStatus;

        if (status.completed) {
          const results = status.results;

          results.forEach((personImage) => {
            const base64: string = 'data:image/JPEG;base64,' + personImage['image'];

            const obj = personImage;
            const img = new Image();
            img.onload = function () {
              obj['height'] = img.height;
              obj['width'] = img.width;
              console.log(obj);
            };
            img.src = base64;
            obj['image'] = base64;

          });

        }
        return status;
      })
      .catch(AnalyticsService.handleError);
  }

  getTimeboundMap(from: number, to: number, useTrackSegments: boolean): Promise<PersonSnapshot[][]> {
    let url = this.baseUrl + 'timeBoundMap/' + from + '/' + to;
    if (useTrackSegments) {
      url += '/trackSegmented';
    }
    return this.http.get(url)
      .toPromise()
      .then(response => {
        return response as PersonSnapshot[][];
      })
      .catch(AnalyticsService.handleError);
  }

  getZoneStatistics(cameraGroupId: number, from: number, to: number): Promise<ZoneStatistic[]> {
    return this.http.get(`${this.baseUrl}zoneStatistics/${cameraGroupId}/${from}/${to}`)
      .toPromise()
      .then(response => {
        console.debug(response);
        return response as ZoneStatistic[];
      })
      .catch(AnalyticsService.handleError);
  }


  private static getZoneColour(index: number): string {
    //return "rgb(" + Math.round(((index / 256 / 256) * 80) % 256).toString() + "," + Math.round(((index / 256) * 80) % 256).toString() + "," + Math.round((index * 80) % 256).toString() + ")";
    const colours = ['#c0392b', '#f1c40f', '#16a085', '#2980b9', '#34495e', '#9b59b5', '#2cee91', '#171796', '#fec3fc', '#8e44ad'];
    return colours[index % colours.length];
  }

  getTimelineFromTrack(trackId: number, segmentId: number, segmented: boolean): Promise<TimelineTrack> {
    let url = this.baseUrl + 'zoneTimeline/' + trackId;
    if (segmented) {
      url = url + '/segmented/' + segmentId;
    }

    console.log(url);

    return this.http.get(url)
      .toPromise()
      .then(response => {
        console.debug(response);
        const results = response as TimelineZone[];
        const result: TimelineTrack = new TimelineTrack();
        results.forEach((r) => {
          r.colour = AnalyticsService.getZoneColour(r.zone.id);
        });
        result.timelineZones = results;
        if (results.length > 0) {
          result.person = results[0].person;
          result.label = results[0].person.ids[0].toString();
        }


        return result;
      })
      .catch(AnalyticsService.handleError);
  }


  getZonedTimeVelocityDistribution(from: number, to: number, zoneId: number, interval: number, segmented: boolean): Promise<{ [id: number]: number[]; }> {
    let url = this.baseUrl + 'zonedTimeVelocity/' + from + '/' + to + '/' + zoneId + '/' + interval;
    if (segmented) {
      url = url + '/segmented';
    }

    console.log(url);

    return this.http.get(url)
      .toPromise()
      .then(response => {
        console.debug(response);

        return response as { [id: number]: number[]; };
      })
      .catch(AnalyticsService.handleError);
  }

  getZonedVelocityFrequencyDistribution(from: number, to: number, zoneId: number, interval: number, segmented: boolean): Promise<{ [id: number]: number; }> {
    let url = this.baseUrl + 'zonedVelocityFrequency/' + from + '/' + to + '/' + zoneId + '/' + interval;
    if (segmented) {
      url = url + '/segmented';
    }

    console.log(url);

    return this.http.get(url)
      .toPromise()
      .then(response => {
        console.debug(response);

        return response as { [id: number]: number; };
      })
      .catch(AnalyticsService.handleError);
  }

  getHeatMap(cameraGroupId: number, from: number, to: number): Promise<number[][]> {
    return this.http.get(`${this.baseUrl}heatMap/${cameraGroupId}/${from}/${to}`)
      .toPromise()
      .then(response => {
        console.debug(response);
        return response as number[][];
      })
      .catch(AnalyticsService.handleError);
  }


  getDirectionMap(from: number, to: number, cellSize: number, directionCount: number): Promise<PointDirections[]> {
    return this.http.get(this.baseUrl + 'directionMap/' + from + '/' + to + '/' + cellSize + '/' + directionCount)
      .toPromise()
      .then(response => {
        console.debug(response);
        return response as PointDirections[];
      })
      .catch(AnalyticsService.handleError);
  }

  getRealtimeAllInfo(): Promise<PersonImage[]> {
    return this.http.get(this.baseUrl + 'realTimeMap/all')
      .toPromise()
      .then(response => {
        const results = response as PersonImage[];

        results.forEach((personImage) => {
          const base64: string = 'data:image/JPEG;base64,' + personImage['image'];

          const obj = personImage;
          const img = new Image();
          img.onload = function () {
            obj['height'] = img.height;
            obj['width'] = img.width;
            console.log(obj);
          };
          img.src = base64;
          obj['image'] = base64;

        });

        return results;
      })
      .catch(AnalyticsService.handleError);
  }

  getZoneInflowPhotos(from: number, to: number, zoneId: number, useSegments: boolean): Promise<PersonImage[]> {
    let url = this.baseUrl + 'zoneStatistics/' + from + '/' + to + '/' + zoneId + '/inflow';
    if (useSegments) {
      url = url + '/segmented';
    }
    return this.http.get(url)
      .toPromise()
      .then(response => {
        const results = response as PersonImage[];

        results.forEach((personImage) => {
          const base64: string = 'data:image/JPEG;base64,' + personImage['image'];

          const obj = personImage;
          const img = new Image();
          img.onload = function () {
            obj['height'] = img.height;
            obj['width'] = img.width;
            console.log(obj);
          };
          img.src = base64;
          obj['image'] = base64;

        });

        return results;
      })
      .catch(AnalyticsService.handleError);
  }

  getZoneOutflowPhotos(from: number, to: number, zoneId: number, useSegments: boolean): Promise<PersonImage[]> {
    let url = this.baseUrl + 'zoneStatistics/' + from + '/' + to + '/' + zoneId + '/outflow';
    if (useSegments) {
      url = url + '/segmented';
    }
    return this.http.get(url)
      .toPromise()
      .then(response => {
        const results = response as PersonImage[];

        results.forEach((personImage) => {
          const base64: string = 'data:image/JPEG;base64,' + personImage['image'];

          const obj = personImage;
          const img = new Image();
          img.onload = function () {
            obj['height'] = img.height;
            obj['width'] = img.width;
            console.log(obj);
          };
          img.src = base64;
          obj['image'] = base64;

        });

        return results;
      })
      .catch(AnalyticsService.handleError);
  }

  getTimeboundAllPhotos(from: number, to: number): Promise<PersonImage[]> {
    return this.http.get(this.baseUrl + 'timeBoundMap/' + from + '/' + to + '/photos')
      .toPromise()
      .then(response => {
        const results = response as PersonImage[];

        results.forEach((personImage) => {
          const base64: string = 'data:image/JPEG;base64,' + personImage['image'];

          const obj = personImage;
          const img = new Image();
          img.onload = function () {
            obj['height'] = img.height;
            obj['width'] = img.width;
            console.log(obj);
          };
          img.src = base64;
          obj['image'] = base64;

        });

        return results;
      })
      .catch(AnalyticsService.handleError);
  }

  getRealtimeInfo(trackingId: number): Promise<PersonImage[]> {
    return this.http.get(this.baseUrl + 'realTimePhotos/' + trackingId)
      .toPromise()
      .then(response => {
        const results = response as PersonImage[];

        results.forEach((personImage) => {
          const base64: string = 'data:image/JPEG;base64,' + personImage['image'];

          const obj = personImage;
          const img = new Image();
          img.onload = function () {
            obj['height'] = img.height;
            obj['width'] = img.width;
            console.log(obj);
          };
          img.src = base64;
          obj['image'] = base64;

        });

        return results;
      })
      .catch(AnalyticsService.handleError);
  }

  getMap(): Promise<string> {
    return this.http.get(this.baseUrl + 'getMap')
      .toPromise()
      .then(response => {
        console.debug(response);
        const res = response as Map<string, string>;
        return res['image'] as string;
      })
      .catch(AnalyticsService.handleError);
  }

  getCount(from: number, to: number): Promise<number> {
    return this.http.get(this.baseUrl + 'count/' + from + '/' + to)
      .toPromise().then(
        response => {
          console.log(response);
          return response as number;
        }
      ).catch(AnalyticsService.handleError);
  }

  getStopPoints(from: number, to: number, radius: number, time: number, height: number, width: number): Promise<number[][]> {
    return this.http.get(this.baseUrl + 'stoppoints/' + from + '/' + to + '/' + radius + '/' + time + '/'
      + height + '/' + width)
      .toPromise().then(
        response => {
          console.log(response);
          return response as number[][];
        }
      ).catch(AnalyticsService.handleError);
  }

  private static handleError(error: any): Promise<any> {
    console.error('An error occurred', error);
    return Promise.reject(error.message || error);
  }

  getPastInfo(trackingId: number, segmentIndex: number, useSegmentIndex: boolean): Promise<PersonImage[]> {
    let url = this.baseUrl + 'trackingSnaps/' + trackingId;
    if (useSegmentIndex) {
      url += '/' + segmentIndex;
    }
    return this.http.get(url)
      .toPromise()
      .then(response => {
        const results = response as PersonImage[];

        results.forEach((personImage) => {
          const base64: string = 'data:image/JPEG;base64,' + personImage['image'];

          const obj = personImage;
          const img = new Image();
          img.onload = function () {
            obj['height'] = img.height;
            obj['width'] = img.width;
            console.log(obj);
          };
          img.src = base64;
          obj['image'] = base64;

        });

        return results;
      })
      .catch(AnalyticsService.handleError);
  }

  getTrackFromUUID(startTime: number, endTime: number, uuid: string, useSegments: boolean) {
    let url = this.baseUrl + 'route/' + startTime + '/' + endTime + '/' + uuid;
    if (useSegments) {
      url = url + '/segmented';
    }
    return this.http.get(url)
      .toPromise()
      .then(response => {
        return response as PersonSnapshot[];
      })
      .catch(AnalyticsService.handleError);
  }


}

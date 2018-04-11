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

import {Component, ElementRef, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {AnalyticsService} from '../../services/analytics.service';
import {SimpleHeatMap} from '../../lib/simple-heat-map';
import {CanvasUtils} from '../../lib/utils/canvas-utils';
import {CameraGroup} from '../../resources/camera-group';
import {Constants} from "../../constants";

@Component({
  selector: 'app-heatmap',
  templateUrl: './heatmap.component.html'
})

export class HeatmapComponent implements OnInit, OnDestroy {

  @Input() cameraGroup: CameraGroup;
  canvasConfigured = false;

  private _startTime: Date;
  private _endTime: Date;
  private _secondRange: number[];
  @ViewChild('canvas') private canvas: ElementRef;
  private cx: CanvasRenderingContext2D;

  constructor(private analyticsService: AnalyticsService) {
    this._endTime = new Date();
    this._startTime = new Date(this.endTime.getTime() - 7 * Constants.DAY);
    this._secondRange = [this.startTime.getTime(), this.endTime.getTime()];
  }

  ngOnInit(): void {
    const canvasEl: HTMLCanvasElement = this.canvas.nativeElement;
    this.cx = canvasEl.getContext('2d');

    CanvasUtils.setBackgroundImage(this.canvas.nativeElement, this.cameraGroup.map.image, this.configureCanvas, this);

    if (this.canvasConfigured) {
      this.generateHeatMap();
    } else {
      setTimeout(() => this.generateHeatMap(), Constants.SECOND * 5);
    }
  }

  /**
   * Generates the heat map corresponding to time between {@link from} and {@link to}
   */
  generateHeatMap(): void {
    if (this.from == null || this.to == null) {
      console.log('Dates are not set');
      return;
    }

    if (!this.canvasConfigured) {
      console.log('Canvas is not configured yet');
      return;
    }

    this.analyticsService.getHeatMap(this.cameraGroup.id, this.from, this.to)
      .then(points => {
        const p = [];
        let max = 0;
        for (let y = 0; y < points.length; y++) {
          for (let x = 0; x < points[0].length; x++) {
            if (points[y][x] !== 0) {
              p.push([x, y, points[y][x]]);
              if (points[y][x] > max) {
                max = points[y][x];
              }
            }
          }
        }

        const heatmap: SimpleHeatMap = new SimpleHeatMap(this.canvas);
        heatmap.data(p);
        heatmap.max(max);
        heatmap.resize();
        heatmap.draw(0.05);
      });
  }

  configureCanvas(canvasEl: HTMLCanvasElement): void {
    this.cx = canvasEl.getContext('2d');

    // set some default properties about the line
    this.cx.lineWidth = 3;
    this.cx.lineCap = 'round';
    this.cx.strokeStyle = '#000';

    this.canvasConfigured = true;
  }

  get secondRange(): number[] {
    return this._secondRange;
  }

  set secondRange(value: number[]) {
    this._secondRange = value;
    this.generateHeatMap();
  }

  get endTime(): Date {
    return this._endTime;
  }

  set endTime(value: Date) {
    this._endTime = value;
    this.generateHeatMap();
  }

  get startTime(): Date {
    return this._startTime;
  }

  set startTime(value: Date) {
    this._startTime = value;
    this.generateHeatMap();
  }

  get from(): number {
    return this.secondRange[0];
  }

  get to(): number {
    return this.secondRange[1];
  }

  ngOnDestroy(): void {

  }
}

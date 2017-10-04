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

import {AfterViewInit, Component, ElementRef, ViewChild} from '@angular/core'

import {AnalyticsService} from "../services/analytics.service";
import {SimpleHeatMap} from "../lib/simple-heat-map";
import {CanvasUtils} from "../lib/utils/canvas-utils";

@Component({
  selector: 'heatmap',
  templateUrl: './heatmap.component.html',
  styleUrls: ['./heatmap.component.css']
})

export class HeatmapComponent implements AfterViewInit {

  mapImage: string;
  canvasConfigured: boolean = false;
  // Date picker for heat map range
  from: Date;
  to: Date;

  @ViewChild('canvas') private canvas: ElementRef;
  private cx: CanvasRenderingContext2D;

  constructor(private analyticsService: AnalyticsService) {
  }

  ngAfterViewInit(): void {
    const canvasEl: HTMLCanvasElement = this.canvas.nativeElement;
    this.cx = canvasEl.getContext('2d');
    this.getMapImage();
  }

  /**
   * Generates the heat map corresponding to time between {@link from} and {@link to}
   */
  generateHeatMap(): void {
    if (!this.from || !this.to) {
      console.error("Dates are not set");
      return;
    }

    if (!this.canvasConfigured) {
      console.error("Canvas is not configured yet");
      return;
    }

    this.analyticsService.getHeatMap(this.from.getTime(), this.to.getTime())
      .then(points => {
        let p = [];
        let max: number = 0;
        for (let x = 0; x < points.length; x++) {
          for (let y = 0; y < points[0].length; y++) {
            if (points[x][y] != 0) {
              p.push([x, y, points[x][y]]);
              if (points[x][y] > max) {
                max = points[x][y];
              }
            }
          }
        }

        let heatmap: SimpleHeatMap = new SimpleHeatMap(this.canvas);
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

  /**
   * Downloads the image of the floor plan from server
   */
  private getMapImage(): void {
    this.analyticsService.getMap()
      .then(image => {
        this.mapImage = "data:image/JPEG;base64," + image;
        CanvasUtils.setBackgroundImage(this.canvas.nativeElement, this.mapImage, this.configureCanvas, this);
      })
      .catch(reason => console.error(reason));
  }
}

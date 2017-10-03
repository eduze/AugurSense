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
import data from '../lib/data';

@Component({
  selector: 'heatmap',
  templateUrl: './heatmap.component.html'
})

export class HeatmapComponent implements AfterViewInit {

  @ViewChild('canvas') private canvas: ElementRef;
  private cx: CanvasRenderingContext2D;

  constructor(private analyticsService: AnalyticsService) {
  }

  ngAfterViewInit(): void {
    const canvasEl: HTMLCanvasElement = this.canvas.nativeElement;
    this.cx = canvasEl.getContext('2d');
  }

  generateHeatMap(): void {
    const canvasEl: HTMLCanvasElement = this.canvas.nativeElement;
    // set the width and height
    canvasEl.width = 800;
    canvasEl.height = 800;

    let heatmap: SimpleHeatMap = new SimpleHeatMap(this.canvas);
    heatmap.data(data);
    heatmap.draw(0.15);
  }
}

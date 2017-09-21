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

import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core'
import {Observable} from 'rxjs/Rx';

import {AnalyticsService} from "../services/analytics.service";
import {PersonSnapshot} from "../resources/person-snapshot";

@Component({
  selector: 'dashboard',
  templateUrl: './dashboard.component.html',
  styles: ['canvas { border: 1px solid #000; }']
})

export class DashboardComponent implements OnInit, AfterViewInit {

  @ViewChild('canvas') public canvas: ElementRef;
  private cx: CanvasRenderingContext2D;

  personSnapshots: PersonSnapshot[][] = [[]];

  constructor(private analyticsService: AnalyticsService) {
  }

  ngOnInit(): void {
    Observable.interval(5000).subscribe(x => {
      console.log("Sending request");
      this.analyticsService.getRealTimeMap()
        .then(personSnapshots => {
          console.log(personSnapshots);
          this.personSnapshots = personSnapshots;
          this.drawOnCanvas();
        })
        .catch(reason => console.log(reason));
    });
  }

  private drawOnCanvas(): void {
    if (!this.cx) {
      console.log("cx is not set");
      return;
    }

    const canvasEl: HTMLCanvasElement = this.canvas.nativeElement;
    this.cx.clearRect(0, 0, canvasEl.width, canvasEl.height);

    this.personSnapshots.forEach(snapshots => {
      var prev: PersonSnapshot;
      for (let i in snapshots) {
        if (prev) {
          this.cx.beginPath();
          this.cx.moveTo(prev.x, prev.y);
          this.cx.lineTo(snapshots[i].x, snapshots[i].y);
          this.cx.stroke();
        }

        prev = snapshots[i];
      }
    });
  }

  ngAfterViewInit(): void {
    const canvasEl: HTMLCanvasElement = this.canvas.nativeElement;
    this.cx = canvasEl.getContext('2d');

    // set the width and height
    canvasEl.width = 800;
    canvasEl.height = 800;

    // set some default properties about the line
    this.cx.lineWidth = 3;
    this.cx.lineCap = 'round';
    this.cx.strokeStyle = '#000';
  }
}

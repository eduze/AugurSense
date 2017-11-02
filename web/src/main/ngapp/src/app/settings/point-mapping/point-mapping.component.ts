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

import {AfterViewInit, Component, OnInit} from '@angular/core';
import {ConfigService} from "../../services/config.service";
import {CameraView} from "../../resources/camera-view";

@Component({
  selector: 'point-mapping',
  templateUrl: './point-mapping.component.html',
  styleUrls: ['./point-mapping.component.css']
})

export class PointMappingComponent implements OnInit, AfterViewInit {

  cameraViews: CameraView[];
  mapImage: string;
  imagesLoaded: boolean = false;

  constructor(private configService: ConfigService) {
  }

  ngOnInit(): void {
    this.configService.getCameraViews()
      .then(views => {
        console.log(views);
        this.cameraViews = [];
        for (let key in views) {
          if (key === "mapImage") {
            this.mapImage = views[key];
          } else {
            this.cameraViews.push(new CameraView(key, views[key]));
          }
        }

        this.imagesLoaded = true;
      })
      .catch(reason => console.error(reason));
  }

  ngAfterViewInit(): void {
    if (this.imagesLoaded) {
      this.drawImages();
    } else {
      console.log("Images are not loaded");
      setTimeout(() => this.drawImages(), 1000);
    }
  }

  private drawImages(): void {
    console.log("Drawing images");
    for (let index in this.cameraViews) {
      let canvas = <HTMLCanvasElement> document.getElementById("camera" + index);
      console.log("Index is: %s", ("camera" + index));
      if (!canvas) {
        console.log("Canvas is null");
        continue;
      }
      let ctx = canvas.getContext('2d');
      const img: HTMLImageElement = new Image();
      img.onload = function () {
        canvas.width = img.width;
        canvas.height = img.height;

        ctx.lineWidth = 3;
        ctx.lineCap = 'round';
        ctx.strokeStyle = '#000';
        ctx.drawImage(img, 0, 0);
      };
      img.src = this.cameraViews[index].view;
    }
  }

  cameraViewClicked(event: MouseEvent, i: number): void {
    const canvasElement = <HTMLCanvasElement>event.srcElement;
    console.log("Offset (%d, %d) clicked", event.offsetX, event.offsetY);
// this.cameraCanvasList.find((element, index, elements) => {
//   return element.id === ("camera" + i);
// });
  }

  mapImageClicked(event: MouseEvent): void {
    console.log("%d, %d clicked", event.x, event.y);
  }

}

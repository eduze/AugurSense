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
  mapImage: CameraView;
  imagesLoaded: boolean = false;

  constructor(private configService: ConfigService) {
  }

  ngOnInit(): void {
    this.configService.getCameraViews()
      .then(views => {
        this.cameraViews = [];
        for (let key in views) {
          if (key === "mapImage") {
            this.mapImage = new CameraView("0", views[key]);
          } else {
            this.cameraViews.push(new CameraView(key, views[key]));
          }
        }

        this.imagesLoaded = true;
      })
      .catch(reason => console.error(reason));
  }

  ngAfterViewInit(): void {
  }

  public cameraViewClicked(event: MouseEvent, i: number): void {
    let cameraView = this.cameraViews[i];
    let h = parseInt(document.getElementById("camera" + i).getAttribute("height"));
    let w = (cameraView.width / cameraView.height) * h;
    console.log("%d x %d", w, h);

    document.getElementById("camera" + i)
  }

  public mapImageClicked(event: MouseEvent, i: number): void {
    const svgElement = <SVGImageElement>event.srcElement;
    console.log("Offset (%d, %d) clicked", event.offsetX, event.offsetY);
    console.log("Offset (%d, %d) clicked", event.x, event.y);
    console.log("Offset (%d, %d) clicked", event.clientX, event.clientY);
    console.log(svgElement);
  }
}

import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {AnalyticsService} from "../services/analytics.service";
import {ConfigService} from "../services/config.service";
import {GlobalMap} from "../resources/global-map";
import {CanvasUtils} from "../lib/utils/canvas-utils";

@Component({
  selector: 'app-person-stop-points',
  templateUrl: './person-stop-points.component.html',
  styleUrls: ['./person-stop-points.component.css']
})
export class PersonStopPointsComponent implements OnInit, AfterViewInit {

  @ViewChild('canvas') private canvas: ElementRef;
  private cx: CanvasRenderingContext2D;

  // Date picker for heat map range
  from: Date = new Date(0);
  to: Date = new Date();
  radius: number = 10;
  time: number = 1; // Time duration min
  stopPoints: number[][];

  globalMap : GlobalMap;

  constructor(private analyticsService: AnalyticsService, private configService : ConfigService) {
  }

  ngOnInit() {
  }

  getStopPoints(): void {
    var width = 500;
    var height = 600;

    if (!this.from || !this.to) {
      console.log("Dates are not set");
      return;
    }
    const canvasEl: HTMLCanvasElement = this.canvas.nativeElement;
    const rectWidth = 5;

    // this.analyticsService.getStopPoints(this.from.getTime(), this.to.getTime(), this.radius, this.time, height,width)
    //   .then(stopPoints => {
    //     console.log(stopPoints);
    //     this.stopPoints = stopPoints;
    //   })
    //   .catch(reason => console.log(reason));

    this.analyticsService.getStopPoints(this.from.getTime(), this.to.getTime(), this.radius, this.time,height,width )
      .then(stopPoints => {
        // set the width and height
        canvasEl.width = height;
        canvasEl.height = width;

        this.cx.fillStyle = 'white';
        this.cx.globalAlpha = 0.8;
        this.cx.fillRect(0,0,1000,1000);
        this.cx.globalAlpha = 1;
        this.cx.fillStyle="black";

        this.cx.strokeRect(0, 0, canvasEl.width, canvasEl.height);

        this.cx.strokeStyle = '#ff0000';
        for (let x = 0; x < stopPoints.length; x++) {
          for (let y = 0; y < stopPoints[x].length; y++) {
            this.cx.globalAlpha = stopPoints[x][y] / 100;
            this.cx.fillRect(x * rectWidth, y * rectWidth, rectWidth, rectWidth);
          }
        }
      })
      .catch(reason => console.log(reason));
  }

  configureCanvas(canvasEl: HTMLCanvasElement): void {
    this.cx = canvasEl.getContext('2d');

    // set some default properties about the line
    this.cx.lineWidth = 3;
    this.cx.lineCap = 'round';

    this.cx.strokeStyle = '#000';
  }

  ngAfterViewInit(): void {
    const canvasEl: HTMLCanvasElement = this.canvas.nativeElement;
    this.cx = canvasEl.getContext('2d');

    // set some default properties about the line
    this.cx.lineWidth = 3;
    this.cx.lineCap = 'round';
    this.cx.strokeStyle = '#000000';

    this.configService.getMap().then((globalMap) => {
      this.globalMap = globalMap;
      CanvasUtils.setBackgroundImage(this.canvas.nativeElement, this.globalMap.image, this.configureCanvas, this);

      console.log(this.globalMap);
    });
  }

  // generateHeatMap(): void {
  //   const canvasEl: HTMLCanvasElement = this.canvas.nativeElement;
  //   const rectWidth = 5;
  //
  //   this.analyticsService.getHeatMap(this.from.getTime(), this.to.getTime())
  //     .then(data => {
  //       // set the width and height
  //       canvasEl.width = data[0].length;
  //       canvasEl.height = data.length;
  //       this.cx.strokeRect(0, 0, canvasEl.width, canvasEl.height);
  //
  //       this.cx.strokeStyle = '#ff0000';
  //       for (let x = 0; x < data.length; x++) {
  //         for (let y = 0; y < data[x].length; y++) {
  //           this.cx.globalAlpha = data[x][y] / 100;
  //           this.cx.fillRect(x * rectWidth, y * rectWidth, rectWidth, rectWidth);
  //         }
  //       }
  //     })
  //     .catch(reason => console.log(reason));
  // }
}

// import {AfterViewInit, Component, ElementRef, ViewChild} from '@angular/core'
// import {AnalyticsService} from "../services/analytics.service";
//
// @Component({
//   selector: 'heatmap',
//   templateUrl: './heatmap.component.html'
// })

// export class HeatmapComponent implements AfterViewInit {







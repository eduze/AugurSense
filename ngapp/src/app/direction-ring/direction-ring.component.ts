import {AfterViewInit, Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {PointDirections} from "../resources/point-directions";
import {AnalyticsService} from "../services/analytics.service";
import {ConfigService} from "../services/config.service";
import {GlobalMap} from "../resources/global-map";
import {CanvasUtils} from "../lib/utils/canvas-utils";


@Component({
  selector: 'app-direction-ring',
  templateUrl: './direction-ring.component.html',
  styleUrls: ['./direction-ring.component.css']
})
export class DirectionRingComponent implements OnInit, AfterViewInit {
  get selectedRingIndex(): number {
    return this._selectedRingIndex;
  }

  @Input()
  set selectedRingIndex(value: number) {
    this._selectedRingIndex = value;
    this.refreshView();
    this.selectionChanged.emit(value);
  }

  get drawHeadDirection(): boolean {
    return this._drawHeadDirection;
  }

  @Input()
  set drawHeadDirection(value: boolean) {
    this._drawHeadDirection = value;
    this.refreshView();
  }

  get drawPersonVelocity(): boolean {
    return this._drawPersonVelocity;
  }

  @Input()
  set drawPersonVelocity(value: boolean) {
    this._drawPersonVelocity = value;
    this.refreshView();
  }

  get drawPersonCount(): boolean {
    return this._drawPersonCount;
  }

  @Input()
  set drawPersonCount(value: boolean) {
    this._drawPersonCount = value;
    this.refreshView();
  }

  private globalMap: GlobalMap;

  get width(): number {
    return this._width;
  }

  @Input()
  set width(value: number) {
    this._width = value;
    this.refreshView();
  }

  get height(): number {
    return this._height;
  }

  @Input()
  set height(value: number) {
    this._height = value;
    this.refreshView();
  }

  get lineWidth(): number {
    return this._lineWidth;
  }

  @Input()
  set lineWidth(value: number) {
    this._lineWidth = value;
    this.refreshView();
  }

  get pointRadi(): number {
    return this._pointRadi;
  }

  private _lineWidth: number = 4;

  private _width: number = 500;
  private _height: number = 500;


  @Input()
  set pointRadi(value: number) {
    this._pointRadi = value;
    this.refreshView();
  }

  get dataPoints(): PointDirections[] {
    return this._dataPoints;
  }

  @Input()
  set dataPoints(value: PointDirections[]) {
    this._dataPoints = value;
    this.selectedRingIndex = -1;
    this.refreshView();
  }

  @ViewChild('canvas') private canvas: ElementRef;
  private cx: CanvasRenderingContext2D;


  constructor(private analyticsService: AnalyticsService, private configService: ConfigService) {
  }

  private _dataPoints: PointDirections[];

  private _pointRadi: number = 8;

  ngOnInit() {

  }

  private static drawMultiRadiantCircle(ctx, xc, yc, r, radientColors, lineWidth): void {
    //External Code: https://stackoverflow.com/questions/22223950/angle-gradient-in-canvas

    let partLength = (2 * Math.PI) / radientColors.length;
    let start = 0;
    let gradient = null;
    let startColor = null,
      endColor = null;

    for (let i = 0; i < radientColors.length; i++) {
      startColor = radientColors[i];
      endColor = radientColors[(i + 1) % radientColors.length];

      // x start / end of the next arc to draw
      let xStart = xc + Math.cos(start) * r;
      let xEnd = xc + Math.cos(start + partLength) * r;
      // y start / end of the next arc to draw
      let yStart = yc + Math.sin(start) * r;
      let yEnd = yc + Math.sin(start + partLength) * r;

      ctx.beginPath();

      if (startColor.includes("NaN")) {
        console.log("nan " + xc + " " + yc);
        continue;
      }

      if (endColor.includes("NaN")) {
        console.log("nan " + xc + " " + yc);
        continue;
      }

      gradient = ctx.createLinearGradient(xStart, yStart, xEnd, yEnd);


      gradient.addColorStop(0, startColor);
      gradient.addColorStop(1.0, endColor);

      ctx.strokeStyle = gradient;
      ctx.arc(xc, yc, r, start, start + partLength);
      ctx.lineWidth = lineWidth;
      ctx.stroke();
      ctx.closePath();

      start += partLength;
    }
  }

  private static getStrengthColour(strength: number) {
    let defaultGradient = {
      0.0: "#99ffffff",
      0.025: "#FFefff39",
      0.05: '#FFffde03',
      0.1: '#FFFFaf07',
      0.25: '#FFff7900',
      0.5: '#FFff5000',
      1.0: '#FFFF0000'
    };

    let lessKey: number = Number.MAX_VALUE;
    let moreKey: number = Number.MIN_VALUE;
    for (let key in defaultGradient) {
      //console.log(key);
      let k: number = parseFloat(key);
      if (k > moreKey)
        moreKey = k;
      if (k < lessKey)
        lessKey = k;

    }

    //console.log(lessKey + " " + moreKey);
    for (let key in defaultGradient) {
      let k: number = parseFloat(key);
      let value = defaultGradient[key];
      if (strength > k && k >= lessKey) {
        lessKey = k;
      }
      if (strength < k && k <= moreKey) {
        moreKey = k;
      }
    }


    let blendFactor = (strength - lessKey) / (moreKey - lessKey) * 100;

    //console.log(defaultGradient[lessKey] + " " + defaultGradient[moreKey] + " " + blendFactor);

    return DirectionRingComponent.mix(defaultGradient[moreKey], defaultGradient[lessKey], blendFactor);
  }

  private static mix(color_1: string, color_2: string, weight: number): any {
    //https://gist.github.com/jedfoster/7939513
    color_1 = color_1.substring(1);
    color_2 = color_2.substring(1);

    function d2h(d) {
      return d.toString(16);
    }  // convert a decimal value to hex
    function h2d(h) {
      return parseInt(h, 16);
    } // convert a hex value to decimal

    weight = (typeof(weight) !== 'undefined') ? weight : 50; // set the weight to 50%, if that argument is omitted

    let color = "#";

    for (let i = 0; i <= 7; i += 2) { // loop through each of the 3 hex pairs—red, green, and blue
      let v1 = h2d(color_1.substr(i, 2)); // extract the current pairs
      let v2 = h2d(color_2.substr(i, 2));
      // combine the current pairs from each source color, according to the specified weight
      //console.log("i: " + i + " v1: " +v1 + " v2: " + v2 + " c1: " + color_1.substr(i,2) + " c2: " + color_2.substr(i,2));

      let val = d2h(Math.floor(v2 + (v1 - v2) * (weight / 100.0)));

      while (val.length < 2) {
        val = '0' + val;
      } // prepend a '0' if val results in a single digit

      color += val; // concatenate val to our new color string
    }

    return color; // PROFIT!
  };

  private _drawPersonCount: boolean = false;
  private _drawPersonVelocity: boolean = false;
  private _drawHeadDirection: boolean = false;

  refreshView(): void {
    if (this.width == null || this.height == null || this.dataPoints == null || this.lineWidth == null || this.pointRadi == null)
      return;

    const canvasEl: HTMLCanvasElement = this.canvas.nativeElement;

    // This is not required since size is set through the image
    // canvasEl.width = this.width;
    // canvasEl.height = this.height;

    // this.container.nativeElement.setAttribute("width",this.width);
    // this.container.nativeElement.setAttribute("height",this.height);

    this.cx = canvasEl.getContext('2d');

    this.cx.clearRect(0, 0, canvasEl.width, canvasEl.height);
    if (this.dataPoints != null) {
      this.dataPoints.forEach((pd) => {
        if (this.dataPoints.indexOf(pd) == this.selectedRingIndex) {
          this.cx.fillStyle = "rgba(0,125,255,0.5)";
          this.cx.beginPath();
          this.cx.ellipse(pd.x, pd.y, this.pointRadi, this.pointRadi, 0, 0, 360);
          this.cx.fill();
        }
        let someColors = [];

        if (this.drawPersonCount) {
          pd.normalizedDirectionCountList.forEach(value => {
            let colour = DirectionRingComponent.getStrengthColour(value);
            let alpha = parseInt(colour.substr(1, 2), 16) / 256;
            let red = parseInt(colour.substr(3, 2), 16);
            let green = parseInt(colour.substr(5, 2), 16);
            let blue = parseInt(colour.substr(7, 2), 16);
            let rgb = "#" + colour.substr(3);
            someColors.push("rgba(" + red + "," + green + "," + blue + ", " + alpha + ")");
          });
          DirectionRingComponent.drawMultiRadiantCircle(this.cx, pd.x, pd.y, this.pointRadi, someColors, this.lineWidth);
        }
        if (this.drawPersonVelocity) {
          pd.normalizedDirectionVelocityList.forEach(value => {
            let colour = DirectionRingComponent.getStrengthColour(value);
            let alpha = parseInt(colour.substr(1, 2), 16) / 256;
            let red = parseInt(colour.substr(3, 2), 16);
            let green = parseInt(colour.substr(5, 2), 16);
            let blue = parseInt(colour.substr(7, 2), 16);
            let rgb = "#" + colour.substr(3);
            someColors.push("rgba(" + red + "," + green + "," + blue + ", " + alpha + ")");
          });
          DirectionRingComponent.drawMultiRadiantCircle(this.cx, pd.x, pd.y, this.pointRadi, someColors, this.lineWidth);
        }

        if (this.drawHeadDirection) {
          pd.headDirectionList.forEach(value => {
            let colour = DirectionRingComponent.getStrengthColour(value);
            let alpha = parseInt(colour.substr(1, 2), 16) / 256;
            let red = parseInt(colour.substr(3, 2), 16);
            let green = parseInt(colour.substr(5, 2), 16);
            let blue = parseInt(colour.substr(7, 2), 16);
            let rgb = "#" + colour.substr(3);
            someColors.push("rgba(" + red + "," + green + "," + blue + ", " + alpha + ")");
          });
          DirectionRingComponent.drawMultiRadiantCircle(this.cx, pd.x, pd.y, this.pointRadi, someColors, this.lineWidth);
        }


      });

      console.log("Directions drawn");
    }

  }

  @Output()
  selectionChanged: EventEmitter<number> = new EventEmitter<number>();

  canvasConfigured: boolean = false;

  configureCanvas(canvasEl: HTMLCanvasElement): void {
    this.cx = canvasEl.getContext('2d');

    // set some default properties about the line
    this.cx.lineWidth = 3;
    this.cx.lineCap = 'round';
    this.cx.strokeStyle = '#000';

    this.canvasConfigured = true;
    console.log("configure called");
    this.refreshView();
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
      console.log(this.globalMap);
      CanvasUtils.setBackgroundImage(this.canvas.nativeElement, this.globalMap.image, this.configureCanvas, this);
      this.refreshView();
    });

    this.refreshView();
  }

  private _selectedRingIndex: number = -1;

  canvasClicked($event: MouseEvent) {
    console.log($event);
    let hasSelection: boolean = false;
    this.dataPoints.forEach((p) => {
      let dx = $event.layerX - p.x;
      let dy = $event.layerY - p.y;
      let d = Math.sqrt(dx * dx + dy * dy);
      //console.log(d);
      if (d < this.pointRadi) {
        console.log("Selected ");
        console.log(p);
        hasSelection = true;
        this.selectedRingIndex = this.dataPoints.indexOf(p);
      }
    });
    if (!hasSelection)
      this.selectedRingIndex = -1;
  }
}

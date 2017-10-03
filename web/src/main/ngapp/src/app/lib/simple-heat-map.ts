import {ElementRef, OnInit} from '@angular/core';

export class SimpleHeatMap {
  private _canvas: HTMLCanvasElement;
  private _ctx: CanvasRenderingContext2D;
  private _width: number;
  private _height: number;
  private _max: number;
  private _data: number[][];

  private defaultRadius: number = 25;
  private defaultGradient = {
    0.4: 'blue',
    0.6: 'cyan',
    0.7: 'lime',
    0.8: 'yellow',
    1.0: 'red'
  };
  private _grad: any;
  private _circle: any;
  private _r: number;

  constructor(element: ElementRef) {
    this._canvas = element.nativeElement;
    this._ctx = this._canvas.getContext('2d');
    this._width = this._canvas.width;
    this._height = this._canvas.height;

    this._max = 1;
    this._data = [];
  }

  data(data): SimpleHeatMap {
    this._data = data;
    return this;
  }

  max(max: number): SimpleHeatMap {
    this._max = max;
    return this;
  }

  add(point): SimpleHeatMap {
    this._data.push(point);
    return this;
  }

  clear(): SimpleHeatMap {
    this._data = [];
    return this;
  }

  radius(r, blur): SimpleHeatMap {
    // create a grayscale blurred circle image that we'll use for drawing points
    var circle = this._circle = this._createCanvas(),
      ctx = circle.getContext('2d'),
      r2 = this._r = r + blur;

    circle.width = circle.height = r2 * 2;

    ctx.shadowOffsetX = ctx.shadowOffsetY = r2 * 2;
    ctx.shadowBlur = blur;
    ctx.shadowColor = 'black';

    ctx.beginPath();
    ctx.arc(-r2, -r2, r, 0, Math.PI * 2, true);
    ctx.closePath();
    ctx.fill();

    return this;
  }

  resize(): void {
    this._width = this._canvas.width;
    this._height = this._canvas.height;
  }

  gradient(grad): SimpleHeatMap {
    // create a 256x1 gradient that we'll use to turn a grayscale heatmap into a colored one
    var canvas = this._createCanvas();
    var ctx = canvas.getContext('2d');
    var gradient = ctx.createLinearGradient(0, 0, 0, 256);

    canvas.width = 1;
    canvas.height = 256;

    for (var i in grad) {
      gradient.addColorStop(+i, grad[i]);
    }

    ctx.fillStyle = gradient;
    ctx.fillRect(0, 0, 1, 256);

    this._grad = ctx.getImageData(0, 0, 1, 256).data;

    return this;
  }

  draw(minOpacity): SimpleHeatMap {
    if (!this._circle) this.radius(this.defaultRadius, 15);
    if (!this._grad) this.gradient(this.defaultGradient);

    var ctx = this._ctx;

    ctx.clearRect(0, 0, this._width, this._height);

    // draw a grayscale heatmap by putting a blurred circle at each data point
    for (var i = 0, len = this._data.length, p; i < len; i++) {
      p = this._data[i];
      ctx.globalAlpha = Math.max(p[2] / this._max, minOpacity === undefined ? 0.05 : minOpacity);
      ctx.drawImage(this._circle, p[0] - this._r, p[1] - this._r);
    }

    // colorize the heatmap, using opacity value of each pixel to get the right color from our gradient
    var colored = ctx.getImageData(0, 0, this._width, this._height);
    this._colorize(colored.data, this._grad);
    ctx.putImageData(colored, 0, 0);

    return this;
  }

  _colorize(pixels, gradient): void {
    for (var i = 0, len = pixels.length, j; i < len; i += 4) {
      j = pixels[i + 3] * 4; // get gradient color from opacity value

      if (j) {
        pixels[i] = gradient[j];
        pixels[i + 1] = gradient[j + 1];
        pixels[i + 2] = gradient[j + 2];
      }
    }
  }

  _createCanvas(): any {
    return document.createElement('canvas');
  }
}

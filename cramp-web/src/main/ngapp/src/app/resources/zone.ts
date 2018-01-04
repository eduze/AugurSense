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

export class Zone {
  private _id: number;
  private _zoneName: string;
  private _xCoordinates: number[];
  private _yCoordinates: number[];
  private _polygon: string;
  private _midX: number;
  private _midY: number;

  constructor(id: number, name: string, xCoordinates: number[], yCoordinates: number[]) {
    this._id = id;
    this._zoneName = name;
    this._xCoordinates = xCoordinates;
    this._yCoordinates = yCoordinates;

    this.updatePolygon();
  }

  private updatePolygon() {
    this._midX = 0;
    this._midY = 0;

    this._polygon = "";
    for (let i in this._xCoordinates) {
      this._polygon += this._xCoordinates[i] + "," + this._yCoordinates[i] + " ";
      this._midX += this._xCoordinates[i];
      this._midY += this._yCoordinates[i];
    }

    this._midX = this._midX / this.xCoordinates.length;
    this._midY = this._midY / this.yCoordinates.length;
  }

  get id(): number {
    return this._id;
  }

  get zoneName(): string {
    return this._zoneName;
  }

  get xCoordinates(): number[] {
    return this._xCoordinates;
  }

  get yCoordinates(): number[] {
    return this._yCoordinates;
  }

  get polygon(): string {
    return this._polygon;
  }

  get midX(): number {
    return this._midX;
  }

  get midY(): number {
    return this._midY;
  }

  set zoneName(value: string) {
    this._zoneName = value;
  }

  set xCoordinates(value: number[]) {
    this._xCoordinates = value;
    this.updatePolygon();
  }

  set yCoordinates(value: number[]) {
    this._yCoordinates = value;
    this.updatePolygon();
  }

  public toJSON(key: string): Object {
    return {
      id: this.id,
      zoneName: this.zoneName,
      xCoordinates: this.xCoordinates,
      yCoordinates: this.yCoordinates
    }
  }
}

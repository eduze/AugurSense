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
import {Component} from '@angular/core';
import {NavigationEnd, Router} from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})

export class AppComponent {

  options: any = [
    {
      route: "/realtime",
      name: "Real Time",
      icon: "tachometer"
    },
    {
      route: "/time-bound-map",
      name: "Time Shift",
      icon: "tachometer"
    },
    {
      route: "/zones",
      name: "Zones",
      icon: "tachometer"
    },
    {
      route: "/heatmap",
      name: "Heat Map",
      icon: "tachometer"
    },
    {
      route: "/statistics",
      name: "Statistics",
      icon: "line-chart"
    },
    {
      route: "/stoppoints",
      name: "Stop Points",
      icon: "users"
    },
    {
      route: "/movement-direction",
      name: "Movement Directions",
      icon: "compass"
    }
  ];

  current: any = {};

  constructor(private router: Router) {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        for (let option of this.options) {
          console.log(this.router.url);
          if (event.url.endsWith(option.route)) {
            this.current = option;
            break;
          }
        }
      }
    });
  }
}

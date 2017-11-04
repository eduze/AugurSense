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

import {NgModule} from '@angular/core'
import {RouterModule, Routes} from '@angular/router'
import {DashboardComponent} from "./dashboard/dashboard.component";
import {HeatmapComponent} from "./heatmap/heatmap.component";
import {PeopleCountComponent} from "./people-count/people-count.component";
import {PointMappingComponent} from "./settings/point-mapping/point-mapping.component";
import {ZonesComponent} from "./zones/zones.component";
import {ZoneInfoComponent} from "./zone-info/zone-info.component";
import {RealtimeMapComponent} from "./realtime-map/realtime-map.component";
import {RealtimeInfoComponent} from "./realtime-info/realtime-info.component";
import {TimeBoundMapComponent} from "./time-bound-map/time-bound-map.component";

const routes: Routes = [
  {
    path: 'dashboard',
    component: DashboardComponent
  },
  {
    path: 'zones',
    component: ZonesComponent
  },
  {
    path: 'heatmap',
    component: HeatmapComponent
  },
  {
    path: 'statistics',
    component: PeopleCountComponent
  },
  {
    path: 'settings',
    component: PointMappingComponent
  },
  {
    path: '',
    redirectTo: '/realtime',
    pathMatch: 'full'
  },
  {
    path :'realtime',
    component: RealtimeMapComponent
  },
  {
    path :'time-bound-map',
    component: TimeBoundMapComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})

export class AppRoutingModule {
}

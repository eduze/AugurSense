import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {DashboardComponent} from './dashboard/dashboard.component';
import {ZonesComponent} from './zones/zones.component';
import {ConfigComponent} from './settings/config/config.component';
import {HeatmapsComponent} from './heatmaps/heatmaps.component';
import {TimeBoundMapComponent} from './time-bound-map/time-bound-map.component';

const routes: Routes = [
  {path: 'dashboard', component: DashboardComponent},
  {path: 'time-line', component: TimeBoundMapComponent},
  {path: 'zones', component: ZonesComponent},
  {path: 'heat-map', component: HeatmapsComponent},
  {path: 'settings', component: ConfigComponent},
  {path: '**', redirectTo: '/dashboard', pathMatch: 'full'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})

export class AppRoutingModule {
}

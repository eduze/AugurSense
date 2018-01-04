import {NgModule} from '@angular/core'
import {RouterModule, Routes} from '@angular/router'
import {DashboardComponent} from "./dashboard/dashboard.component";
import {ZonesComponent} from "./zones/zones.component";
import {TimeBoundMapComponent} from "./time-bound-map/time-bound-map.component";
import {MovementDirectionComponent} from "./movement-direction/movement-direction.component";
import {ConfigComponent} from "./settings/config/config.component";

const routes: Routes = [
  {path: '', redirectTo: '/dashboard', pathMatch: 'full'},
  {path: 'dashboard', component: DashboardComponent},
  {path: 'zones', component: ZonesComponent},
  {path: 'time-bound-map', component: TimeBoundMapComponent},
  {path: 'movement-direction', component: MovementDirectionComponent},
  {path: 'settings', component: ConfigComponent}
  // {path: ':id', component: ReIdComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})

export class AppRoutingModule {
}

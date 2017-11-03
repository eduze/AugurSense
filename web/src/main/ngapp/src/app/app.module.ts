import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpModule} from '@angular/http';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {BrowserModule} from '@angular/platform-browser';
import {CalendarModule} from 'primeng/primeng';

import {AppComponent} from "./app.component";
import {AppRoutingModule} from "./app-routing.module";
import {DashboardComponent} from "./dashboard/dashboard.component";
import {AnalyticsService} from "./services/analytics.service";
import {HeatmapComponent} from "./heatmap/heatmap.component";
import { PeopleCountComponent } from './people-count/people-count.component';
import { PointMappingComponent } from './settings/point-mapping/point-mapping.component';
import {ConfigService} from "./services/config.service";
import { ZonesComponent } from './zones/zones.component';
import { ZoneInfoComponent } from './zone-info/zone-info.component';
import { ChartModule } from 'primeng/primeng';

@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent,
    HeatmapComponent,
    PeopleCountComponent,
    PointMappingComponent,
    ZonesComponent,
    ZoneInfoComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    AppRoutingModule,
    HttpModule,
    CalendarModule,
    ChartModule
  ],
  providers: [AnalyticsService, ConfigService],
  bootstrap: [AppComponent]
})

export class AppModule {
}

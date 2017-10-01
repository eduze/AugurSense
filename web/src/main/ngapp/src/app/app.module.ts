import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpModule} from '@angular/http';
import {BrowserModule} from '@angular/platform-browser';
import {ChartModule} from 'angular-highcharts';

import {AppComponent} from "./app.component";
import {AppRoutingModule} from "./app-routing.module";
import {NavBarComponent} from "./nav-bar/nav-bar.component";
import {DashboardComponent} from "./dashboard/dashboard.component";
import {AnalyticsService} from "./services/analytics.service";
import {HeatmapComponent} from "./heatmap/heatmap.component";

@NgModule({
  declarations: [
    AppComponent,
    NavBarComponent,
    DashboardComponent,
    HeatmapComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    AppRoutingModule,
    HttpModule,
    ChartModule
  ],
  providers: [AnalyticsService],
  bootstrap: [AppComponent]
})

export class AppModule {
}

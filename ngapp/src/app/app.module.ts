import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {CalendarModule, ChartModule, CheckboxModule, RadioButtonModule, SliderModule} from 'primeng/primeng';

import {AppComponent} from './app.component';
import {AppRoutingModule} from './app-routing.module';
import {DashboardComponent} from './dashboard/dashboard.component';
import {HeatmapsComponent} from './heatmaps/heatmaps.component';
import {PeopleCountComponent} from './people-count/people-count.component';
import {PointMappingComponent} from './settings/point-mapping/point-mapping.component';
import {ZonesComponent} from './zones/zones.component';
import {ZoneInfoComponent} from './zones/zone-info/zone-info.component';
import {RealtimeMapComponent} from './realtime-map/realtime-map.component';
import {RealtimeInfoComponent} from './realtime-info/realtime-info.component';
import {TimeBoundMapComponent} from './time-bound-map/time-bound-map.component';
import {PersonStopPointsComponent} from './person-stop-points/person-stop-points.component';
import {TimelineComponent} from './timeline/timeline.component';
import {TimeVelocityDistributionComponent} from './time-velocity-distribution/time-velocity-distribution.component';
import {DirectionRingComponent} from './direction-ring/direction-ring.component';
import {MovementDirectionComponent} from './movement-direction/movement-direction.component';
import {AnalyticsService} from './services/analytics.service';
import {ConfigService} from './services/config.service';

import {
  MatButtonModule,
  MatCardModule,
  MatCheckboxModule,
  MatChipsModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatListModule,
  MatMenuModule,
  MatSidenavModule,
  MatSliderModule,
  MatToolbarModule
} from '@angular/material';
import {ZonesConfigComponent} from './settings/zones-config/zones-config.component';
import {ConfigComponent} from './settings/config/config.component';
import {ZonesConfigInfoComponent} from './settings/zones-config/zones-config-info/zones-config-info.component';
import {SuccessMessageComponent} from './helpers/success-message/success-message.component';
import {ZoneUiComponent} from './settings/zones-config/zone-ui/zone-ui.component';
import {ZoneComponent} from './zones/zone/zone.component';
import {HeatmapComponent} from './heatmaps/heatmap/heatmap.component';
import {TimeLineComponent} from './time-bound-map/time-line/time-line.component';
import {HashLocationStrategy, LocationStrategy} from '@angular/common';


@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent,
    /* Heat Map */
    HeatmapsComponent,
    HeatmapComponent,
    PeopleCountComponent,
    PointMappingComponent,
    /* Zones */
    ZonesComponent,
    ZoneComponent,
    ZoneInfoComponent,
    /* Timelines */
    RealtimeMapComponent,
    RealtimeInfoComponent,
    TimeBoundMapComponent,
    TimeLineComponent,
    PersonStopPointsComponent,
    TimelineComponent,
    TimeVelocityDistributionComponent,
    DirectionRingComponent,
    MovementDirectionComponent,
    ZonesConfigComponent,
    ConfigComponent,
    ZonesConfigInfoComponent,
    SuccessMessageComponent,
    ZoneUiComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    AppRoutingModule,
    HttpClientModule,
    CalendarModule,
    CheckboxModule,
    ChartModule,
    SliderModule,
    MatMenuModule,
    RadioButtonModule,
    MatSidenavModule,
    MatButtonModule,
    MatToolbarModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    MatListModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatSliderModule
  ],
  providers: [
    AnalyticsService,
    ConfigService,
    {provide: LocationStrategy, useClass: HashLocationStrategy}
  ],
  bootstrap: [AppComponent]
})

export class AppModule {
}

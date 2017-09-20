import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpModule} from '@angular/http';
import {BrowserModule} from '@angular/platform-browser';

import {AppComponent} from "./app.component";
import {AppRoutingModule} from "./app-routing.module";
import {NavBarComponent} from "./nav-bar/nav-bar.component";
import {DashboardComponent} from "./dashboard/dashboard.component";
import {AnalyticsService} from "./services/analytics.service";

@NgModule({
  declarations: [
    AppComponent,
    NavBarComponent,
    DashboardComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    AppRoutingModule,
    HttpModule
  ],
  providers: [AnalyticsService],
  bootstrap: [AppComponent]
})

export class AppModule {
}

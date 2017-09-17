import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {AppComponent} from "./app.component";
import {AppRoutingModule} from "./app-routing.module";
import {NavBarComponent} from "./nav-bar/nav-bar.component";
import {DashboardComponent} from "./dashboard/dashboard.component";

@NgModule({
  declarations: [
    AppComponent,
    NavBarComponent,
    DashboardComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})

export class AppModule {
}

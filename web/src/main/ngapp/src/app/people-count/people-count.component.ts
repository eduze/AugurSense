import {Component, OnInit, AfterViewInit} from '@angular/core';
import {PersonSnapshot} from "../resources/person-snapshot";
import {Observable} from "rxjs/Observable";
import {AnalyticsService} from "../services/analytics.service";


@Component({
  selector: 'app-people-count',
  templateUrl: './people-count.component.html',
  styleUrls: ['./people-count.component.css']
})
export class PeopleCountComponent implements OnInit , AfterViewInit {

  // Date picker for heat map range
  from: Date;
  to: Date;

  personCount: number;

  constructor(private analyticsService: AnalyticsService) {
  }

  ngOnInit() {
  }

  ngAfterViewInit(): void {
  }
  getCount():void{
    this.analyticsService.getCount(this.from.getTime(),this.to.getTime())
      .then(personCount => {
        console.log(personCount);
        this.personCount = personCount;
      })
      .catch(reason => console.log(reason));

  }

}

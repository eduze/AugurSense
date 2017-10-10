import {AfterViewInit, Component} from '@angular/core';
import {AnalyticsService} from "../services/analytics.service";


@Component({
  selector: 'people-count',
  templateUrl: './people-count.component.html',
  styleUrls: ['./people-count.component.css']
})
export class PeopleCountComponent implements AfterViewInit {

  // Date picker for heat map range
  from: Date;
  to: Date;

  personCount: number;

  constructor(private analyticsService: AnalyticsService) {
  }

  ngAfterViewInit(): void {
  }

  getCount(): void {
    this.analyticsService.getCount(this.from.getTime(), this.to.getTime())
      .then(personCount => {
        console.log(personCount);
        this.personCount = personCount;
      })
      .catch(reason => console.log(reason));
  }

}

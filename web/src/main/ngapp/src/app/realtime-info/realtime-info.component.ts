import {Component, Input, OnInit} from '@angular/core';
import {PersonImage} from "../resources/person-image";
import {AnalyticsService} from "../services/analytics.service";
import {ConfigService} from "../services/config.service";

@Component({
  selector: 'app-realtime-info',
  templateUrl: './realtime-info.component.html',
  styleUrls: ['./realtime-info.component.css']
})
export class RealtimeInfoComponent implements OnInit {

  private _id : number;

  @Input()
  private useRealtimeEndpoint: boolean = true;

  private personImages : PersonImage[] = null;

  get id(): number{
    return this._id;
  }

  @Input() set id(value : number){
    this._id = value;
    this.refresh();
  }

  refresh() : void{
    if(this.id < 0){
      this.personImages = null;
      return;
    }
    if(this.useRealtimeEndpoint)
    {
      this.analyticsService.getRealtimeInfo(this.id).then((pi) => {
        this.personImages = pi;
        console.log(pi);
      });
    }
    else{
      this.analyticsService.getPastInfo(this.id).then((pi) => {
        this.personImages = pi;
        console.log(pi);
      });
    }


  }

  constructor(private analyticsService: AnalyticsService, private configService: ConfigService) { }

  ngOnInit() {
  }

  getDateString(timestamp: number) {
    return new Date(timestamp).toLocaleString();
  }
}

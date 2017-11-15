import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {PersonImage} from "../resources/person-image";
import {AnalyticsService} from "../services/analytics.service";
import {ConfigService} from "../services/config.service";

@Component({
  selector: 'app-realtime-info',
  templateUrl: './realtime-info.component.html',
  styleUrls: ['./realtime-info.component.css']
})
export class RealtimeInfoComponent implements OnInit {
  get refreshToggle(): boolean {
    return this._refreshToggle;
  }

  @Input() set refreshToggle(value: boolean) {
    this._refreshToggle = value;
    this.refresh();
  }

  get showAll(): boolean {
    return this._showAll;
  }

  @Input() set showAll(value: boolean) {
    this._showAll = value;
    this.personImages = null;
    this.refresh();
  }

  @Input()
  reIDOnClick : boolean = false;


  @Output()
  personClicked: EventEmitter<PersonImage> = new EventEmitter<PersonImage>();

  imageClicked(person: PersonImage) : void {
    this.personClicked.emit(person);
  }

  private _id : number = -1;

  private _showAll : boolean = false;

  private _refreshToggle: boolean = false;


  @Input()
  private useRealtimeEndpoint: boolean = true;

  private personImages : PersonImage[] = null;

  get id(): number{
    return this._id;
  }

  @Input() set id(value : number){
    this._id = value;
    this.personImages = null;
    this.refresh();
  }

  refresh() : void{

    if(this.useRealtimeEndpoint)
    {
      if(!this.showAll)
      {
        if(this.id < 0){
          this.personImages = null;
          return;
        }
        this.analyticsService.getRealtimeInfo(this.id).then((pi) => {
          this.personImages = pi;
          console.log(pi);
        });
      }
      else{
        this.analyticsService.getRealtimeAllInfo().then((pi) => {
          this.personImages = pi;
          console.log(pi);
        });
      }

    }
    else{
      if(this.id < 0){
        this.personImages = null;
        return;
      }
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

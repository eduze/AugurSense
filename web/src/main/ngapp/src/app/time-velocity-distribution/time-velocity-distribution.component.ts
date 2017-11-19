import {Component, Input, OnInit} from '@angular/core';
import {AnalyticsService} from "../services/analytics.service";
import {TimelineComponent} from "../timeline/timeline.component";

@Component({
  selector: 'app-time-velocity-distribution',
  templateUrl: './time-velocity-distribution.component.html',
  styleUrls: ['./time-velocity-distribution.component.css']
})
export class TimeVelocityDistributionComponent implements OnInit {
  get velocityFrequencyView(): boolean {
    return this._velocityFrequencyView;
  }

  @Input()
  set velocityFrequencyView(value: boolean) {
    this._velocityFrequencyView = value;
    this.refresh();
  }
  get timeVelocityView(): boolean {
    return this._timeVelocityView;
  }

  @Input()
  set timeVelocityView(value: boolean) {
    this._timeVelocityView = value;
    this.refresh();
  }
  get timeInterval(): number {
    return this._timeInterval;
  }

  @Input()
  set timeInterval(value: number) {
    this._timeInterval = value;
    this.refresh();
  }
  get segmented(): boolean {
    return this._segmented;
  }

  @Input()
  set segmented(value: boolean) {
    this._segmented = value;
  }
  get to(): Date {
    return this._to;
  }

  @Input()
  set to(value: Date) {
    this._to = value;
    this.refresh();
  }
  get from(): Date {
    return this._from;
  }

  @Input()
  set from(value: Date) {
    this._from = value;
    this.refresh();
  }
  get zoneId(): number {
    return this._zoneId;
  }

  @Input()
  set zoneId(value: number) {
    this._zoneId = value;
    this.refresh();
  }

  constructor(private analyticsService : AnalyticsService) {

  }

  refreshTimeVelocity():void{
    if(this.zoneId == null || this.segmented == null || this.from == null || this.to == null || this.timeInterval == null)
      return;
    if(this.timeVelocityView != true)
      return;

    this.analyticsService.getZonedTimeVelocityDistribution(this.from.getTime(),this.to.getTime(),this.zoneId,this.timeInterval,this.segmented)
      .then(tv => {
        console.log("Loaded zoned time velocity distribution..");
        console.log(tv);

        let points : any = [];

        Object.keys(tv).forEach(key => {
          if (tv.hasOwnProperty(key)) {
            let value : any[] = tv[key];
            value.forEach(v=>{
              points.push({x:key,y:v});
            });

          }
        });

        console.log(points);
        let dataset = {
          label: "Time-Velocity Distribution",
          data: points,
          fill: false,
          borderColor: '#7CB342',
          backgroundColor: '#9CCC65'
        };
        this.dataTV = {
          datasets:[dataset]
        };

      })
      .catch(reason => console.log(reason));
  }

  refreshVelocityFrequency(): void{

    if(this.zoneId == null || this.segmented == null || this.from == null || this.to == null || this.timeInterval == null)
      return;

    if(this.velocityFrequencyView != true)
      return;

    this.analyticsService.getZonedVelocityFrequencyDistribution(this.from.getTime(),this.to.getTime(),this.zoneId,this.timeInterval,this.segmented)
      .then(tv => {
        console.log("Loaded zoned velocity frequency distribution..");
        console.log(tv);

        let values : any = [];
        let labels : any = [];

        Object.keys(tv).forEach(key => {
          if (tv.hasOwnProperty(key)) {
            let value = tv[key];
            labels.push(key);
            values.push(value);
          }
        });
        let dataset = {
          label: "Velocity-Frequency Distribution",
          data: values,
          fill: false,
          borderColor:"#1E88E5",
          backgroundColor: '#42A5F5'
        };

        this.dataVF = {
          labels: labels,
          datasets:[dataset]
        };

      })
      .catch(reason => console.log(reason));

  }

  velocityFrequencyOptions : any = {
      scales : {
        xAxes : [{
          barPercentage : 1,
          categoryPercentage : 1,
          scaleLabel: {
            display: true,
            labelString: 'Velocity (Units/Seconds)'
          }
        }],
        yAxes: [{
          scaleLabel: {
            display: true,
            labelString: 'Frequency'
          }
        }]
      },
      legend: {
        display: false
      },
      animation:{ duration: 0 }
  };

  timeVelocityOptions:any={
    animation:{ duration: 0 },
    legend: {
      display: false
    },
    scales:{
      yAxes: [{
        scaleLabel: {
          display: true,
          labelString: 'Velocity (Units/Second)'
        }
      }],
      xAxes : [{
        barPercentage : 1,
        categoryPercentage : 1,
        scaleLabel: {
          display: true,
          labelString: 'Time'
        },
        ticks:{
          callback:(v)=> TimelineComponent.epoch_to_hh_mm_ss(v),
        }
      }]
    },
    tooltips: {
      callbacks: {
        label: function(tooltipItem, data) {
          return tooltipItem.yLabel + ': ' + TimelineComponent.epoch_to_hh_mm_ss(tooltipItem.xLabel)
        }
      }
    }

  };

  refresh(): void{
    this.refreshTimeVelocity();
    this.refreshVelocityFrequency();
  }

  private _zoneId: number;
  private _from: Date;
  private _to: Date;
  private _segmented: boolean;
  private _timeInterval : number;

  private _timeVelocityView: boolean;
  private _velocityFrequencyView: boolean;

  dataTV: any;
  dataVF: any;

  ngOnInit() {
    // this.zoneId = 3;
    // this.segmented = true;
    // this.from = new Date(0);
    // this.to = new Date();
    // this.timeInterval = 1000;
    // this.timeVelocityView = true;
    // this.velocityFrequencyView = true;
  }

}

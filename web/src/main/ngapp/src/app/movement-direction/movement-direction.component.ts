import {Component, Input, OnInit} from '@angular/core';
import {PointDirections} from "../resources/point-directions";
import {AnalyticsService} from "../services/analytics.service";

@Component({
  selector: 'app-movement-direction',
  templateUrl: './movement-direction.component.html',
  styleUrls: ['./movement-direction.component.css']
})
export class MovementDirectionComponent implements OnInit {
  get selectedDataPoint(): PointDirections {
    return this._selectedDataPoint;
  }

  set selectedDataPoint(value: PointDirections) {
    this._selectedDataPoint = value;
    this.refreshDataPointInfo();
  }

  get selectedView(): string {
    return this._selectedView;
  }

  set selectedView(value: string) {
    this._selectedView = value;
  }

  get directionCount(): number {
    return this._directionCount;
  }

  set directionCount(value: number) {
    this._directionCount = value;
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

  get to(): Date {
    return this._to;
  }

  @Input()
  set to(value: Date) {
    this._to = value;
    this.refresh();
  }

  get cellSize(): number {
    return this._cellSize;
  }

  @Input()
  set cellSize(value: number) {
    this._cellSize = value;
    this.refresh();
  }


  constructor(private analyticsService: AnalyticsService) {
  }

  private _directionCount = 16;

  private _from: Date = new Date(0);
  private _to: Date = new Date();

  private _cellSize: number = 15;

  datePoints: PointDirections[] = [];
  private _selectedView: string = "count";


  refresh(): void {
    let requestedCellSize = this.cellSize;
    let requestedDirectionCount = this.directionCount;
    this.analyticsService.getDirectionMap(this.from.getTime(), this.to.getTime(), this.cellSize, this.directionCount)
      .then(pds => {
        if (this.cellSize != requestedCellSize) {
          return;
        }
        if (this.directionCount != requestedDirectionCount)
          return;
        console.log("Direction map fetched:");
        console.log(pds);
        this.datePoints = pds;
      })
      .catch(reason => console.error(reason));
  }

  ngOnInit() {
    this.refresh();
  }

  private _selectedDataPoint: PointDirections = null;
  dataRadarCount: any;
  dataRadarHeadDirectionAndVelocity: any;

  selectedRingChanged(event: number) {
    if (event < 0) {
      this.selectedDataPoint = null;
      return;
    }
    this.selectedDataPoint = this.datePoints[event];
  }

  chartNoLegendOptions: any = {
    legend: {
      display: false
    }
  };


  chartRightLegendOptions: any = {
    legend: {
      position: 'right',
      display: true
    }
  };

  private static rotate(input: any[]): any[] {
    let d = input.slice(0);
    let size = input.length;
    for (let i = 0; i < size / 4; i++) {
      let l = d.pop();
      d = [l].concat(d);
    }
    return d;
  }

  private refreshDataPointInfo() {
    if (this.selectedDataPoint == null)
      return;

    let labels: string[] = [];
    for (let i = 0; i < this.selectedDataPoint.directionCountList.length; i++) {
      let angle = i * 360 / this.selectedDataPoint.directionCountList.length;
      angle -= 90;
      angle = angle % 360;
      labels.push(angle.toString())
    }


    this.dataRadarHeadDirectionAndVelocity = {
      labels: labels,
      datasets: [
        {
          label: 'Head Direction',
          backgroundColor: 'rgba(156,204,101,0.2)',
          borderColor: 'rgba(156,204,101,1)',
          pointBackgroundColor: 'rgba(156,204,101,1)',
          pointBorderColor: '#fff',
          pointHoverBackgroundColor: '#fff',
          pointHoverBorderColor: 'rgba(156,204,101,1)',
          data: MovementDirectionComponent.rotate(this.selectedDataPoint.headDirectionList)
        }, {
          label: 'Average Velocity',
          backgroundColor: 'rgba(255,99,132,0.2)',
          borderColor: 'rgba(255,99,132,1)',
          pointBackgroundColor: 'rgba(255,99,132,1)',
          pointBorderColor: '#fff',
          pointHoverBackgroundColor: '#fff',
          pointHoverBorderColor: 'rgba(255,99,132,1)',
          data: MovementDirectionComponent.rotate(this.selectedDataPoint.directionVelocityList)
        }]
    };

    this.dataRadarCount = {
      labels: labels,
      datasets: [
        {
          label: 'Count',
          backgroundColor: 'rgba(0,125,255,0.2)',
          borderColor: 'rgba(0,125,255,1)',
          pointBackgroundColor: 'rgba(0,125,255,1)',
          pointBorderColor: '#fff',
          pointHoverBackgroundColor: '#fff',
          pointHoverBorderColor: 'rgba(0,125,255,1)',
          data: MovementDirectionComponent.rotate(this.selectedDataPoint.directionCountList)
        }
      ]
    };
  }
}
